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

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerProperties;

/**
 * Common functionality for all mojos.
 */
abstract class AbstractContentPackageMojo extends AbstractMojo {

  /**
   * The name of the content package file to install on the target system.
   * If not set, the primary artifact of the project is considered the content package to be installed.
   */
  @Parameter(property = "vault.file", defaultValue = "${project.build.directory}/${project.build.finalName}.zip")
  private File packageFile;

  /**
   * <p>
   * The URL of the HTTP service API of the CRX package manager.
   * </p>
   * <p>
   * See <a href=
   * "http://dev.day.com/docs/en/crx/current/how_to/package_manager.html#Managing%20Packages%20on%20the%20Command%20Line"
   * >CRX HTTP service Interface</a> for details on this interface.
   * </p>
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
  @Parameter(property = "vault.retryCount", defaultValue = "24")
  private int retryCount;

  /**
   * Number of seconds between retry attempts.
   */
  @Parameter(property = "vault.retryDelay", defaultValue = "5")
  private int retryDelay;

  /**
   * <p>
   * Bundle status JSON URL. If an URL is configured the activation status of all bundles in the system is checked
   * before it is tried to upload and install a new package and after each upload.
   * </p>
   * <p>
   * If not all packages are installed the upload is delayed up to 10 minutes, every 5 seconds the
   * activation status is checked anew.
   * </P>
   * <p>
   * If the URL is not set it is derived from serviceURL.
   * Expected is an URL like: http://localhost:4502/system/console/bundles/.json
   * </p>
   */
  @Parameter(property = "vault.bundleStatusURL", required = false)
  private String bundleStatusURL;

  /**
   * Number of seconds to wait as maximum for a positive bundle status check.
   * If this limit is reached and the bundle status is still not positive the install of the package proceeds anyway.
   */
  @Parameter(property = "vault.bundleStatusWaitLimit", defaultValue = "360")
  private int bundleStatusWaitLimit;

  /**
   * If set to true also self-signed certificates are accepted.
   */
  @Parameter(property = "vault.relaxedSSLCheck", defaultValue = "false")
  private boolean relaxedSSLCheck;

  /**
   * HTTP connection timeout (in seconds).
   */
  @Parameter(property = "vault.httpConnectTimeoutSec", defaultValue = "10")
  private int httpConnectTimeoutSec;

  /**
   * HTTP socket timeout (in seconds).
   */
  @Parameter(property = "vault.httpSocketTimeoutSec", defaultValue = "60")
  private int httpSocketTimeout;

  protected final File getPackageFile() {
    return this.packageFile;
  }

  protected final boolean isSkip() {
    return this.skip;
  }

  protected PackageManagerProperties getPackageManagerProperties() {
    PackageManagerProperties props = new PackageManagerProperties();

    props.setPackageManagerUrl(buildPackageManagerUrl());
    props.setUserId(this.userId);
    props.setPassword(this.password);
    props.setRetryCount(this.retryCount);
    props.setRetryDelaySec(this.retryDelay);
    props.setBundleStatusUrl(buildBundleStatusUrl());
    props.setBundleStatusWaitLimitSec(this.bundleStatusWaitLimit);
    props.setRelaxedSSLCheck(this.relaxedSSLCheck);
    props.setHttpConnectTimeoutSec(this.httpConnectTimeoutSec);
    props.setHttpSocketTimeoutSec(this.httpSocketTimeout);

    return props;
  }

  private String buildPackageManagerUrl() {
    String serviceUrl = this.serviceURL;
    // convert "legacy interface URL" with service.jsp to new CRX interface (since CRX 2.1)
    serviceUrl = StringUtils.replace(serviceUrl, "/crx/packmgr/service.jsp", "/crx/packmgr/service");
    // remove /.json suffix if present
    serviceUrl = StringUtils.removeEnd(serviceUrl, "/.json");
    return serviceUrl;
  }

  private String buildBundleStatusUrl() {
    if (this.bundleStatusURL != null) {
      return this.bundleStatusURL;
    }
    // if not set use hostname from serviceURL and add default path to bundle status
    String crxUrl = StringUtils.removeEnd(buildPackageManagerUrl(), "/crx/packmgr/service");
    return crxUrl + "/system/console/bundles/.json";
  }

  protected Logger getLoggerWrapper() {
    return new Logger() {
      @Override
      public void warn(CharSequence message, Throwable t) {
        getLog().warn(message, t);
      }
      @Override
      public void warn(CharSequence message) {
        getLog().warn(message);
      }
      @Override
      public boolean isWarnEnabled() {
        return getLog().isWarnEnabled();
      }
      @Override
      public boolean isInfoEnabled() {
        return getLog().isInfoEnabled();
      }
      @Override
      public boolean isErrorEnabled() {
        return getLog().isErrorEnabled();
      }
      @Override
      public boolean isDebugEnabled() {
        return getLog().isDebugEnabled();
      }
      @Override
      public void info(CharSequence message, Throwable t) {
        getLog().info(message, t);
      }
      @Override
      public void info(CharSequence message) {
        getLog().info(message);
      }
      @Override
      public void error(CharSequence message, Throwable t) {
        getLog().error(message, t);
      }
      @Override
      public void error(CharSequence message) {
        getLog().error(message);
      }
      @Override
      public void debug(CharSequence message, Throwable t) {
        getLog().debug(message, t);
      }
      @Override
      public void debug(CharSequence message) {
        getLog().debug(message);
      }
    };
  }

}
