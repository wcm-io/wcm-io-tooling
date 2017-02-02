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

/**
 * Common functionality for all mojos.
 */
public final class PackageManagerHelper {

  /**
   * Prefix or error message from CRX HTTP interfaces when uploading a package that already exists.
   */
  private static final String CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX = "Package already exists: ";

  /**
   * Set up http client with credentials
   * @return Http client
   */
  /*
  public CloseableHttpClient getHttpClient() {
    try {
      URI crxUri = new URI(getCrxPackageManagerUrl());

      final AuthScope authScope = new AuthScope(crxUri.getHost(), crxUri.getPort());
      final Credentials credentials = new UsernamePasswordCredentials(this.userId, this.password);
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
          .setConnectTimeout(httpConnectTimeoutSec * (int)DateUtils.MILLIS_PER_SECOND)
          .setSocketTimeout(httpSocketTimeout * (int)DateUtils.MILLIS_PER_SECOND)
          .build());


      if (this.relaxedSSLCheck) {
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        httpClientBuilder.setSSLSocketFactory(sslsf);
      }

      return httpClientBuilder.build();
    }
    catch (URISyntaxException ex) {
      throw new PackageManagerException("Invalid url: " + getCrxPackageManagerUrl(), ex);
    }
    catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException ex) {
      throw new PackageManagerException("Could not set relaxedSSLCheck", ex);
    }
  }
  */

  /**
   * Execute HTTP call with automatic retry as configured for the MOJO.
   * @param call HTTP call
   * @param runCount Number of runs this call was already executed
   */
  /*
  private <T> T executeHttpCallWithRetry(HttpCall<T> call, int runCount) {
    try {
      return call.execute();
    }
    catch (PackageManagerException ex) {
      // retry again if configured so...
      if (runCount < this.retryCount) {
        getLog().info("ERROR: " + ex.getMessage());
        getLog().debug("HTTP call failed.", ex);
        getLog().info("---------------");

        String msg = "HTTP call failed, try again (" + (runCount + 1) + "/" + this.retryCount + ")";
        if (this.retryDelay > 0) {
          msg += " after " + this.retryDelay + " second(s)";
        }
        msg += "...";
        getLog().info(msg);
        if (this.retryDelay > 0) {
          try {
            Thread.sleep(this.retryDelay * DateUtils.MILLIS_PER_SECOND);
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
  */

  /**
   * Execute CRX HTTP Package manager method and parse/output xml response.
   * @param httpClient Http client
   * @param method Get or Post method
   * @return JSON object
   */
  /*
  public JSONObject executePackageManagerMethodJson(CloseableHttpClient httpClient, HttpRequestBase method) {
    PackageManagerJsonCall call = new PackageManagerJsonCall(httpClient, method, getLog());
    return executeHttpCallWithRetry(call, 0);
  }
  */

  /**
   * Execute CRX HTTP Package manager method and parse/output xml response.
   * @param httpClient Http client
   * @param method Get or Post method
   * @param runCount Execution run count
   */
  /*
  public void executePackageManagerMethodHtml(CloseableHttpClient httpClient, HttpRequestBase method, int runCount) {
    PackageManagerHtmlMessageCall call = new PackageManagerHtmlMessageCall(httpClient, method, getLog());
    String message = executeHttpCallWithRetry(call, 0);
    getLog().info(message);
  }
  */

  /**
   * Wait up to 10 min for bundles to become active.
   * @param httpClient Http client
   */
  /*
  public void waitForBundlesActivation(CloseableHttpClient httpClient) {
    if (StringUtils.isBlank(bundleStatusURL)) {
      getLog().debug("Skipping check for bundle activation state because no bundleStatusURL is defined.");
      return;
    }

    final int WAIT_INTERVAL_SEC = 3;
    final long CHECK_RETRY_COUNT = bundleStatusWaitLimit / WAIT_INTERVAL_SEC;

    getLog().info("Check bundle activation status...");
    for (int i = 1; i <= CHECK_RETRY_COUNT; i++) {
      BundleStatusCall call = new BundleStatusCall(httpClient, bundleStatusURL, getLog());
      BundleStatus bundleStatus = executeHttpCallWithRetry(call, 0);
      if (bundleStatus.isAllBundlesRunning()) {
        return;
      }
      getLog().info(bundleStatus.getStatusLine());
      getLog().info("Bundles are currently starting/stopping - wait " + WAIT_INTERVAL_SEC + " seconds "
          + "(max. " + bundleStatusWaitLimit + " seconds) ...");
      try {
        Thread.sleep(WAIT_INTERVAL_SEC * DateUtils.MILLIS_PER_SECOND);
      }
      catch (InterruptedException e) {
        // ignore
      }
    }
  }
  */

}
