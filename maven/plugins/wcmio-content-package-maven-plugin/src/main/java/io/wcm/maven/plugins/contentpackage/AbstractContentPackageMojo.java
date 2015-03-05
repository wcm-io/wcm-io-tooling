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
package io.wcm.maven.plugins.contentpackage;

import io.wcm.maven.plugins.contentpackage.httpaction.BundleStatus;
import io.wcm.maven.plugins.contentpackage.httpaction.BundleStatusCall;
import io.wcm.maven.plugins.contentpackage.httpaction.HttpCall;
import io.wcm.maven.plugins.contentpackage.httpaction.PackageManagerHtmlMessageCall;
import io.wcm.maven.plugins.contentpackage.httpaction.PackageManagerJsonCall;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.json.JSONObject;

/**
 * Common functionality for all mojos.
 */
abstract class AbstractContentPackageMojo extends AbstractMojo {

  /**
   * Prefix or error message from CRX HTTP interfaces when uploading a package that already exists.
   */
  protected static final String CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX = "Package already exists: ";

  /**
   * The name of the content package file to install on the target system.
   * If not set, the primary artifact of the project is considered the content package to be installed.
   */
  @Parameter(property = "vault.file", defaultValue = "${project.build.directory}/${project.build.finalName}.zip")
  private File packageFile;

  /**
   * The URL of the HTTP service API of the CRX package manager.
   * See <a href=
   * "http://dev.day.com/docs/en/crx/current/how_to/package_manager.html#Managing%20Packages%20on%20the%20Command%20Line"
   * >CRX HTTP service Interface</a> for details on this interface.
   */
  @Parameter(property = "vault.serviceURL", required = true, defaultValue = "http://localhost:4502/crx/packmgr/service")
  private String serviceURL;

  /**
   * The user name to authenticate as against the remote CRX system.
   */
  @Parameter(property = "vault.userId", required = true, defaultValue = "admin")
  private String userId;

  /**
   * The password to authenticate against the remote CRX system.
   */
  @Parameter(property = "vault.password", required = true, defaultValue = "admin")
  private String password;

  /**
   * Set this to "true" to skip installing packages to CRX although configured in the POM.
   */
  @Parameter(property = "vault.skip", defaultValue = "false")
  private boolean skip;

  /**
   * Number of times to retry upload and install via CRX HTTP interface if it fails.
   */
  @Parameter(property = "vault.retryCount", defaultValue = "0")
  private int retryCount;

  /**
   * Number of seconds between retry attempts.
   */
  @Parameter(property = "vault.retryDelay", defaultValue = "0")
  private int retryDelay;

  /**
   * Bundle status JSON URL. If an URL is configured the activation status of all bundles in the system is checked
   * before it is tried to upload and install a new package and after each upload.
   * If not all packages are installed the upload is delayed up to 10 minutes, every 5 seconds the
   * activation status is checked anew.
   * Expected is an URL like: http://localhost:4502/system/console/bundles/.json
   */
  @Parameter(property = "vault.bundleStatusURL", required = false)
  private String bundleStatusURL;

  /**
   * Number of seconds to wait as maximum for a positive bundle status check.
   * If this limit is reached and the bundle status is still not positive the install of the package proceeds anyway.
   */
  @Parameter(property = "vault.bundleStatusWaitLimit", defaultValue = "360")
  private int bundleStatusWaitLimit;

  @Parameter(property = "project", required = true, readonly = true)
  private MavenProject project;

  protected final MavenProject getProject() {
    return this.project;
  }

  protected final File getPackageFile() {
    return this.packageFile;
  }

  protected final String getCrxPackageManagerUrl() {
    String serviceUrl = this.serviceURL;
    // convert "legacy interface URL" with service.jsp to new CRX interface (since CRX 2.1)
    serviceUrl = StringUtils.replace(serviceUrl, "/crx/packmgr/service.jsp", "/crx/packmgr/service");
    // remove /.json suffix if present
    serviceUrl = StringUtils.removeEnd(serviceUrl, "/.json");
    return serviceUrl;
  }

  protected final boolean isSkip() {
    return this.skip;
  }

  /**
   * Set up http client with credentials
   * @return Http client
   * @throws MojoExecutionException
   */
  protected final CloseableHttpClient getHttpClient() throws MojoExecutionException {
    try {
      URI crxUri = new URI(getCrxPackageManagerUrl());

      final AuthScope authScope = new AuthScope(crxUri.getHost(), crxUri.getPort());
      final Credentials credentials = new UsernamePasswordCredentials(this.userId, this.password);
      final CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(authScope, credentials);

      return HttpClients.custom()
          .setDefaultCredentialsProvider(credsProvider)
          .addInterceptorFirst(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
              // enable preemptive authentication
              AuthState authState = (AuthState)context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);
              authState.update(new BasicScheme(), credentials);
            }
          })
          .build();
    }
    catch (URISyntaxException ex) {
      throw new MojoExecutionException("Invalid url: " + getCrxPackageManagerUrl(), ex);
    }
  }

  /**
   * Execute HTTP call with automatic retry as configured for the MOJO.
   * @param call HTTP call
   * @param runCount Number of runs this call was already executed
   * @throws MojoExecutionException
   */
  private <T> T executeHttpCallWithRetry(HttpCall<T> call, int runCount) throws MojoExecutionException {
    try {
      return call.execute();
    }
    catch (MojoExecutionException ex) {
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

  /**
   * Execute CRX HTTP Package manager method and parse/output xml response.
   * @param httpClient Http client
   * @param method Get or Post method
   * @throws MojoExecutionException
   */
  protected final JSONObject executePackageManagerMethodJson(CloseableHttpClient httpClient, HttpRequestBase method)
      throws MojoExecutionException {
    PackageManagerJsonCall call = new PackageManagerJsonCall(httpClient, method, getLog());
    return executeHttpCallWithRetry(call, 0);
  }

  /**
   * Execute CRX HTTP Package manager method and parse/output xml response.
   * @param httpClient Http client
   * @param method Get or Post method
   * @throws MojoExecutionException
   */
  protected final void executePackageManagerMethodHtml(CloseableHttpClient httpClient, HttpRequestBase method,
      int runCount) throws MojoExecutionException {
    PackageManagerHtmlMessageCall call = new PackageManagerHtmlMessageCall(httpClient, method, getLog());
    String message = executeHttpCallWithRetry(call, 0);
    getLog().info(message);
  }

  /**
   * Wait up to 10 min for bundles to become active.
   * @throws MojoExecutionException
   */
  protected void waitForBundlesActivation(CloseableHttpClient httpClient) throws MojoExecutionException {
    if (StringUtils.isBlank(bundleStatusURL)) {
      getLog().debug("Skipping check for bundle activation state because no bundleStatusURL is defined.");
      return;
    }

    final int WAIT_INTERVAL_SEC = 3;
    final long CHECK_RETRY_COUNT = bundleStatusWaitLimit / WAIT_INTERVAL_SEC;

    getLog().info("Check bundle activation states...");
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

}
