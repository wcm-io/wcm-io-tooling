/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.tooling.commons.packmgr;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONObject;

import io.wcm.tooling.commons.packmgr.httpaction.BundleStatus;
import io.wcm.tooling.commons.packmgr.httpaction.BundleStatusCall;
import io.wcm.tooling.commons.packmgr.httpaction.HttpCall;
import io.wcm.tooling.commons.packmgr.httpaction.PackageManagerHtmlMessageCall;
import io.wcm.tooling.commons.packmgr.httpaction.PackageManagerJsonCall;

/**
 * Common functionality for all mojos.
 */
public final class PackageManagerHelper {

  /**
   * Prefix or error message from CRX HTTP interfaces when uploading a package that already exists.
   */
  public static final String CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX = "Package already exists: ";

  private final PackageManagerProperties props;
  private final Logger log;

  /**
   * @param props Package manager properties
   * @param log Logger
   */
  public PackageManagerHelper(PackageManagerProperties props, Logger log) {
    this.props = props;
    this.log = log;
  }

  /**
   * Set up http client with credentials
   * @return Http client
   */
  public CloseableHttpClient getHttpClient() {
    try {
      URI crxUri = new URI(props.getPackageManagerUrl());

      final AuthScope authScope = new AuthScope(crxUri.getHost(), crxUri.getPort());
      final Credentials credentials = new UsernamePasswordCredentials(props.getUserId(), props.getPassword());
      final CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(authScope, credentials);

      HttpClientBuilder httpClientBuilder = HttpClients.custom()
          .setDefaultCredentialsProvider(credsProvider)
          .addInterceptorFirst(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
              // enable preemptive authentication
              AuthState authState = (AuthState)context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);
              authState.update(new BasicScheme(), credentials);
            }
          })
          .setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
              // keep reusing connections to a minimum - may conflict when instance is restarting and responds in unexpected manner
              return 1;
            }
          });

      // timeout settings
      httpClientBuilder.setDefaultRequestConfig(RequestConfig.custom()
          .setConnectTimeout(props.getHttpConnectTimeoutSec() * (int)DateUtils.MILLIS_PER_SECOND)
          .setSocketTimeout(props.getHttpSocketTimeoutSec() * (int)DateUtils.MILLIS_PER_SECOND)
          .build());


      if (props.isRelaxedSSLCheck()) {
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        httpClientBuilder.setSSLSocketFactory(sslsf);
      }

      return httpClientBuilder.build();
    }
    catch (URISyntaxException ex) {
      throw new PackageManagerException("Invalid url: " + props.getPackageManagerUrl(), ex);
    }
    catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException ex) {
      throw new PackageManagerException("Could not set relaxedSSLCheck", ex);
    }
  }

  /**
   * Execute HTTP call with automatic retry as configured for the MOJO.
   * @param call HTTP call
   * @param runCount Number of runs this call was already executed
   */
  private <T> T executeHttpCallWithRetry(HttpCall<T> call, int runCount) {
    try {
      return call.execute();
    }
    catch (PackageManagerException ex) {
      // retry again if configured so...
      if (runCount < props.getRetryCount()) {
        log.info("ERROR: " + ex.getMessage());
        log.debug("HTTP call failed.", ex);
        log.info("---------------");

        String msg = "HTTP call failed, try again (" + (runCount + 1) + "/" + props.getRetryCount() + ")";
        if (props.getRetryDelaySec() > 0) {
          msg += " after " + props.getRetryDelaySec() + " second(s)";
        }
        msg += "...";
        log.info(msg);
        if (props.getRetryDelaySec() > 0) {
          try {
            Thread.sleep(props.getRetryDelaySec() * DateUtils.MILLIS_PER_SECOND);
          }
          catch (InterruptedException ex1) {
            // ignore
          }
        }
        return executeHttpCallWithRetry(call, runCount + 1);
      }
      else {
        throw ex;
      }
    }
  }

  /**
   * Execute CRX HTTP Package manager method and parse/output xml response.
   * @param httpClient Http client
   * @param method Get or Post method
   * @return JSON object
   */
  public JSONObject executePackageManagerMethodJson(CloseableHttpClient httpClient, HttpRequestBase method) {
    PackageManagerJsonCall call = new PackageManagerJsonCall(httpClient, method, log);
    return executeHttpCallWithRetry(call, 0);
  }

  /**
   * Execute CRX HTTP Package manager method and parse/output xml response.
   * @param httpClient Http client
   * @param method Get or Post method
   * @param runCount Execution run count
   */
  public void executePackageManagerMethodHtml(CloseableHttpClient httpClient, HttpRequestBase method, int runCount) {
    PackageManagerHtmlMessageCall call = new PackageManagerHtmlMessageCall(httpClient, method, log);
    String message = executeHttpCallWithRetry(call, 0);
    log.info(message);
  }

  /**
   * Wait for bundles to become active.
   * @param httpClient Http client
   */
  public void waitForBundlesActivation(CloseableHttpClient httpClient) {
    if (StringUtils.isBlank(props.getBundleStatusUrl())) {
      log.debug("Skipping check for bundle activation state because no bundleStatusURL is defined.");
      return;
    }

    final int WAIT_INTERVAL_SEC = 3;
    final long CHECK_RETRY_COUNT = props.getBundleStatusWaitLimitSec() / WAIT_INTERVAL_SEC;

    log.info("Check bundle activation status...");
    for (int i = 1; i <= CHECK_RETRY_COUNT; i++) {
      BundleStatusCall call = new BundleStatusCall(httpClient, props.getBundleStatusUrl(), log);
      BundleStatus bundleStatus = executeHttpCallWithRetry(call, 0);
      if (bundleStatus.isAllBundlesRunning()) {
        return;
      }
      log.info(bundleStatus.getStatusLine());
      log.info("Bundles are currently starting/stopping - wait " + WAIT_INTERVAL_SEC + " seconds "
          + "(max. " + props.getBundleStatusWaitLimitSec() + " seconds) ...");
      try {
        Thread.sleep(WAIT_INTERVAL_SEC * DateUtils.MILLIS_PER_SECOND);
      }
      catch (InterruptedException e) {
        // ignore
      }
    }
  }

}
