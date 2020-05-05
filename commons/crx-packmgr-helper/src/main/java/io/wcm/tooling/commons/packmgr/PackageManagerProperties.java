/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Configuration properties for {@link PackageManagerHelper}.
 */
public final class PackageManagerProperties {

  private String packageManagerUrl;
  private String userId;
  private String password;
  private String consoleUserId;
  private String consolePassword;
  private int retryCount = 24;
  private int retryDelaySec = 5;
  private String bundleStatusUrl;
  private int bundleStatusWaitLimitSec = 360;
  private List<Pattern> bundleStatusBlacklistBundleNames = Collections.emptyList();
  private List<Pattern> bundleStatusWhitelistBundleNames = Collections.emptyList();
  private boolean relaxedSSLCheck;
  private int httpConnectTimeoutSec = 10;
  private int httpSocketTimeoutSec = 60;
  private List<Proxy> proxies;

  /**
   * The URL of the HTTP service API of the CRX package manager.
   * See <a href=
   * "https://docs.adobe.com/content/docs/en/crx/2-3/how_to/package_manager.html#Managing%20Packages%20on%20the%20Command%20Line">CRX
   * HTTP service Interface</a> for details on this interface.
   * @return URL
   */
  public String getPackageManagerUrl() {
    return this.packageManagerUrl;
  }

  public void setPackageManagerUrl(String packageManagerUrl) {
    this.packageManagerUrl = packageManagerUrl;
  }

  /**
   * The user name to authenticate as against the remote CRX system (package manager).
   * @return User ID
   */
  public String getUserId() {
    return this.userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * The password to authenticate against the remote CRX system (package manager).
   * @return Password
   */
  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * The user name to authenticate as against the remote CRX system (Felix console).
   * Fallback to {@link #getUserId()} if not set.
   * @return User ID
   */
  public String getConsoleUserId() {
    if (this.consoleUserId == null) {
      return this.userId;
    }
    return this.consoleUserId;
  }

  public void setConsoleUserId(String consoleUserId) {
    this.consoleUserId = consoleUserId;
  }

  /**
   * The password to authenticate against the remote CRX system (Felix console).
   * Fallback to {@link #getPassword()} if not set.
   * @return Password
   */
  public String getConsolePassword() {
    if (this.consolePassword == null) {
      return this.password;
    }
    return this.consolePassword;
  }

  public void setConsolePassword(String consolePassword) {
    this.consolePassword = consolePassword;
  }

  /**
   * Number of times to retry upload and install via CRX HTTP interface if it fails.
   * @return Retry count
   */
  public int getRetryCount() {
    return this.retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  /**
   * Number of seconds between retry attempts.
   * @return Retry delay
   */
  public int getRetryDelaySec() {
    return this.retryDelaySec;
  }

  public void setRetryDelaySec(int retryDelaySec) {
    this.retryDelaySec = retryDelaySec;
  }

  /**
   * Bundle status JSON URL. If an URL is configured the activation status of all bundles in the system is checked
   * before it is tried to upload and install a new package and after each upload.
   * If not all packages are installed the upload is delayed up to 10 minutes, every 5 seconds the
   * activation status is checked anew.
   * Expected is an URL like: http://localhost:4502/system/console/bundles/.json
   * @return URL
   */
  public String getBundleStatusUrl() {
    return this.bundleStatusUrl;
  }

  public void setBundleStatusUrl(String bundleStatusUrl) {
    this.bundleStatusUrl = bundleStatusUrl;
  }

  /**
   * Number of seconds to wait as maximum for a positive bundle status check.
   * If this limit is reached and the bundle status is still not positive the install of the package proceeds anyway.
   * @return Limit
   */
  public int getBundleStatusWaitLimitSec() {
    return this.bundleStatusWaitLimitSec;
  }

  public void setBundleStatusWaitLimitSec(int bundleStatusWaitLimitSec) {
    this.bundleStatusWaitLimitSec = bundleStatusWaitLimitSec;
  }

  /**
   * Patterns for symbolic names of bundles that are expected to be not present in bundle list.
   * If any of these bundles are found in the bundle list, this system is assumed as not ready for installing further
   * packages because a previous installation (e.g. of AEM service pack) is still in progress.
   * @return List of regular expressions for symbolic bundle names.
   */
  public List<Pattern> getBundleStatusBlacklistBundleNames() {
    return this.bundleStatusBlacklistBundleNames;
  }

  public void setBundleStatusBlacklistBundleNames(List<String> bundleStatusBlacklistBundleNames) {
    this.bundleStatusBlacklistBundleNames = bundleStatusBlacklistBundleNames.stream()
        .map(Pattern::compile)
        .collect(Collectors.toList());
  }

  /**
   * Patterns for symbolic names of bundles that are ignored in bundle list.
   * The state of these bundles has no effect on the bundle status check.
   * @return List of regular expressions for symbolic bundle names.
   */
  public List<Pattern> getBundleStatusWhitelistBundleNames() {
    return this.bundleStatusWhitelistBundleNames;
  }

  public void setBundleStatusWhitelistBundleNames(List<String> bundleStatusWhitelistBundleNames) {
    this.bundleStatusWhitelistBundleNames = bundleStatusWhitelistBundleNames.stream()
        .map(Pattern::compile)
        .collect(Collectors.toList());
  }

  /**
   * If set to true also self-signed certificates are accepted.
   * @return Relaced SSL check
   */
  public boolean isRelaxedSSLCheck() {
    return this.relaxedSSLCheck;
  }

  public void setRelaxedSSLCheck(boolean relaxedSSLCheck) {
    this.relaxedSSLCheck = relaxedSSLCheck;
  }

  /**
   * HTTP connection timeout (in seconds).
   * @return Timeout
   */
  public int getHttpConnectTimeoutSec() {
    return this.httpConnectTimeoutSec;
  }

  public void setHttpConnectTimeoutSec(int httpConnectTimeoutSec) {
    this.httpConnectTimeoutSec = httpConnectTimeoutSec;
  }

  /**
   * HTTP socket timeout (in seconds).
   * @return Timeout
   */
  public int getHttpSocketTimeoutSec() {
    return this.httpSocketTimeoutSec;
  }

  public void setHttpSocketTimeoutSec(int httpSocketTimeoutSec) {
    this.httpSocketTimeoutSec = httpSocketTimeoutSec;
  }

  /**
   * HTTP proxies from maven settings
   * @return List of proxies
   */
  public List<Proxy> getProxies() {
    return this.proxies;
  }

  public void setProxies(List<Proxy> proxies) {
    this.proxies = proxies;
  }

}
