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
import java.util.List;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
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
import org.jdom2.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import io.wcm.tooling.commons.packmgr.httpaction.BundleStatus;
import io.wcm.tooling.commons.packmgr.httpaction.BundleStatusCall;
import io.wcm.tooling.commons.packmgr.httpaction.HttpCall;
import io.wcm.tooling.commons.packmgr.httpaction.PackageManagerHtmlCall;
import io.wcm.tooling.commons.packmgr.httpaction.PackageManagerHtmlMessageCall;
import io.wcm.tooling.commons.packmgr.httpaction.PackageManagerJsonCall;
import io.wcm.tooling.commons.packmgr.httpaction.PackageManagerStatusCall;
import io.wcm.tooling.commons.packmgr.httpaction.PackageManagerXmlCall;
import io.wcm.tooling.commons.packmgr.util.HttpClientUtil;

/**
 * Common functionality for all mojos.
 */
public final class PackageManagerHelper {

  /**
   * Prefix or error message from CRX HTTP interfaces when uploading a package that already exists.
   */
  public static final String CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX = "Package already exists: ";

  private static final String HTTP_CONTEXT_ATTRIBUTE_PREEMPTIVE_AUTHENTICATION_CREDS = PackageManagerHelper.class.getName() + "_PreemptiveAuthenticationCreds";
  private static final String HTTP_CONTEXT_ATTRIBUTE_OAUTH2_ACCESS_TOKEN = PackageManagerHelper.class.getName() + "_oauth2AccessToken";

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
   * Get HTTP client to be used for all communications (package manager and Felix console).
   * @return HTTP client
   */
  public @NotNull CloseableHttpClient getHttpClient() {
    HttpClientBuilder httpClientBuilder = HttpClients.custom()
        .setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
          @Override
          public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            // keep reusing connections to a minimum - may conflict when instance is restarting and responds in unexpected manner
            return 1;
          }
        })
        .addInterceptorFirst(new HttpRequestInterceptor() {
          @Override
          public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            Credentials credentials = (Credentials)context.getAttribute(HTTP_CONTEXT_ATTRIBUTE_PREEMPTIVE_AUTHENTICATION_CREDS);
            if (credentials != null) {
              // enable preemptive authentication
              AuthState authState = (AuthState)context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);
              authState.update(new BasicScheme(), credentials);
            }
            String oauth2AccessToken = (String)context.getAttribute(HTTP_CONTEXT_ATTRIBUTE_OAUTH2_ACCESS_TOKEN);
            if (oauth2AccessToken != null) {
              // send OAuth 2 bearer token
              request.setHeader("Authorization", "Bearer " + oauth2AccessToken);
            }
          }
        });

    // relaxed SSL check
    if (props.isRelaxedSSLCheck()) {
      try {
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        httpClientBuilder.setSSLSocketFactory(sslsf);
      }
      catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException ex) {
        throw new PackageManagerException("Could not set relaxedSSLCheck", ex);
      }
    }

    // proxy support
    Proxy proxy = getProxyForUrl(props.getPackageManagerUrl());
    if (proxy != null) {
      httpClientBuilder.setProxy(new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol()));
    }

    return httpClientBuilder.build();
  }

  /**
   * Set up http client context with credentials for CRX package manager.
   * @return Http client context
   */
  public @NotNull HttpClientContext getPackageManagerHttpClientContext() {
    return getHttpClientContext(props.getPackageManagerUrl(),
        props.getUserId(), props.getPassword(), props.getOAuth2AccessToken());
  }

  /**
   * Set up http client context with credentials for Felix console.
   * @return Http client context. May be null of bundle status URL is set to "-".
   */
  public @Nullable HttpClientContext getConsoleHttpClientContext() {
    String bundleStatusUrl = props.getBundleStatusUrl();
    if (bundleStatusUrl == null) {
      return null;
    }
    return getHttpClientContext(bundleStatusUrl,
        props.getConsoleUserId(), props.getConsolePassword(), props.getConsoleOAuth2AccessToken());
  }

  private @NotNull HttpClientContext getHttpClientContext(String url, String userId, String password, String oauth2AccessToken) {
    URI uri;
    try {
      uri = new URI(url);
    }
    catch (URISyntaxException ex) {
      throw new PackageManagerException("Invalid url: " + url, ex);
    }

    final CredentialsProvider credsProvider = new BasicCredentialsProvider();
    HttpClientContext context = new HttpClientContext();
    context.setCredentialsProvider(credsProvider);

    if (StringUtils.isNotBlank(oauth2AccessToken)) {
      context.setAttribute(HTTP_CONTEXT_ATTRIBUTE_OAUTH2_ACCESS_TOKEN, oauth2AccessToken);
    }
    else {
      // use basic (preemptive) authentication with username/password
      final AuthScope authScope = new AuthScope(uri.getHost(), uri.getPort());
      final Credentials credentials = new UsernamePasswordCredentials(userId, password);
      credsProvider.setCredentials(authScope, credentials);
      context.setAttribute(HTTP_CONTEXT_ATTRIBUTE_PREEMPTIVE_AUTHENTICATION_CREDS, credentials);
    }

    // timeout settings
    context.setRequestConfig(HttpClientUtil.buildRequestConfig(props));

    // proxy support
    Proxy proxy = getProxyForUrl(url);
    if (proxy != null && proxy.useAuthentication()) {
      AuthScope proxyAuthScope = new AuthScope(proxy.getHost(), proxy.getPort());
      Credentials proxyCredentials = new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword());
      credsProvider.setCredentials(proxyAuthScope, proxyCredentials);
    }

    return context;
  }

  /**
   * Get proxy for given URL
   * @param requestUrl Request URL
   * @return Proxy or null if none matching found
   */
  private Proxy getProxyForUrl(String requestUrl) {
    List<Proxy> proxies = props.getProxies();
    if (proxies == null || proxies.isEmpty()) {
      return null;
    }
    final URI uri = URI.create(requestUrl);
    for (Proxy proxy : proxies) {
      if (!proxy.isNonProxyHost(uri.getHost())) {
        return proxy;
      }
    }
    return null;
  }


  /**
   * Execute HTTP call with automatic retry as configured for the MOJO.
   * @param call HTTP call
   * @param runCount Number of runs this call was already executed
   */
  @SuppressWarnings("PMD.GuardLogStatement")
  private <T> T executeHttpCallWithRetry(HttpCall<T> call, int runCount) {
    try {
      return call.execute();
    }
    catch (PackageManagerHttpActionException ex) {
      // retry again if configured so...
      if (runCount < props.getRetryCount()) {
        log.info("ERROR: " + ex.getMessage());
        log.debug("HTTP call failed.", ex);
        log.info("---------------");

        StringBuilder msg = new StringBuilder();
        msg.append("HTTP call failed, try again (" + (runCount + 1) + "/" + props.getRetryCount() + ")");
        if (props.getRetryDelaySec() > 0) {
          msg.append(" after " + props.getRetryDelaySec() + " second(s)");
        }
        msg.append("...");
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
   * Execute CRX HTTP Package manager method and parse JSON response.
   * @param httpClient HTTP client
   * @param context HTTP client context
   * @param method Get or Post method
   * @return JSON object
   */
  public JSONObject executePackageManagerMethodJson(CloseableHttpClient httpClient, HttpClientContext context, HttpRequestBase method) {
    PackageManagerJsonCall call = new PackageManagerJsonCall(httpClient, context, method, log);
    return executeHttpCallWithRetry(call, 0);
  }

  /**
   * Execute CRX HTTP Package manager method and parse XML response.
   * @param httpClient HTTP client
   * @param context HTTP client context
   * @param method Get or Post method
   * @return XML document
   */
  public Document executePackageManagerMethodXml(CloseableHttpClient httpClient, HttpClientContext context, HttpRequestBase method) {
    PackageManagerXmlCall call = new PackageManagerXmlCall(httpClient, context, method, log);
    return executeHttpCallWithRetry(call, 0);
  }

  /**
   * Execute CRX HTTP Package manager method and get HTML response.
   * @param httpClient HTTP client
   * @param context HTTP client context
   * @param method Get or Post method
   * @return Response from HTML server
   */
  public String executePackageManagerMethodHtml(CloseableHttpClient httpClient, HttpClientContext context, HttpRequestBase method) {
    PackageManagerHtmlCall call = new PackageManagerHtmlCall(httpClient, context, method, log);
    String message = executeHttpCallWithRetry(call, 0);
    return message;
  }

  /**
   * Execute CRX HTTP Package manager method and output HTML response.
   * @param httpClient HTTP client
   * @param context HTTP client context
   * @param method Get or Post method
   */
  public void executePackageManagerMethodHtmlOutputResponse(CloseableHttpClient httpClient, HttpClientContext context, HttpRequestBase method) {
    PackageManagerHtmlMessageCall call = new PackageManagerHtmlMessageCall(httpClient, context, method, log);
    executeHttpCallWithRetry(call, 0);
  }

  /**
   * Execute CRX HTTP Package manager method and checks response status. If the response status is not 200 the call
   * fails (after retrying).
   * @param httpClient HTTP client
   * @param context HTTP client context
   * @param method Get or Post method
   */
  public void executePackageManagerMethodStatus(CloseableHttpClient httpClient, HttpClientContext context, HttpRequestBase method) {
    PackageManagerStatusCall call = new PackageManagerStatusCall(httpClient, context, method, log);
    executeHttpCallWithRetry(call, 0);
  }

  /**
   * Wait for bundles to become active.
   * @param httpClient HTTP client
   * @param context HTTP client context
   */
  @SuppressWarnings("PMD.GuardLogStatement")
  public void waitForBundlesActivation(CloseableHttpClient httpClient, HttpClientContext context) {
    if (StringUtils.isBlank(props.getBundleStatusUrl())) {
      log.debug("Skipping check for bundle activation state because no bundleStatusURL is defined.");
      return;
    }

    final int WAIT_INTERVAL_SEC = 3;
    final long CHECK_RETRY_COUNT = props.getBundleStatusWaitLimitSec() / WAIT_INTERVAL_SEC;

    log.info("Check bundle activation status...");
    for (int i = 1; i <= CHECK_RETRY_COUNT; i++) {
      BundleStatusCall call = new BundleStatusCall(httpClient, context, props.getBundleStatusUrl(),
          props.getBundleStatusWhitelistBundleNames(), log);
      BundleStatus bundleStatus = executeHttpCallWithRetry(call, 0);

      boolean instanceReady = true;

      // check if bundles are still stopping/staring
      if (!bundleStatus.isAllBundlesRunning()) {
        log.info("Bundles starting/stopping: " + bundleStatus.getStatusLineCompact()
            + " - wait " + WAIT_INTERVAL_SEC + " sec "
            + "(max. " + props.getBundleStatusWaitLimitSec() + " sec) ...");
        sleep(WAIT_INTERVAL_SEC);
        instanceReady = false;
      }

      // check if any of the blacklisted bundles is still present
      if (instanceReady) {
        for (Pattern blacklistBundleNamePattern : props.getBundleStatusBlacklistBundleNames()) {
          String bundleSymbolicName = bundleStatus.getMatchingBundle(blacklistBundleNamePattern);
          if (bundleSymbolicName != null) {
            log.info("Bundle '" + bundleSymbolicName + "' is still deployed "
                + " - wait " + WAIT_INTERVAL_SEC + " sec "
                + "(max. " + props.getBundleStatusWaitLimitSec() + " sec) ...");
            sleep(WAIT_INTERVAL_SEC);
            instanceReady = false;
            break;
          }
        }
      }

      // instance is ready
      if (instanceReady) {
        break;
      }
    }
  }

  private void sleep(int sec) {
    try {
      Thread.sleep(sec * DateUtils.MILLIS_PER_SECOND);
    }
    catch (InterruptedException e) {
      // ignore
    }
  }

}
