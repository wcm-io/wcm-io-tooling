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
package io.wcm.tooling.commons.packmgr.install.crx;

import static io.wcm.tooling.commons.packmgr.PackageManagerHelper.CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jackrabbit.vault.packaging.PackageProperties;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import io.wcm.tooling.commons.packmgr.PackageManagerProperties;
import io.wcm.tooling.commons.packmgr.install.PackageFile;
import io.wcm.tooling.commons.packmgr.install.VendorInstallerFactory;
import io.wcm.tooling.commons.packmgr.install.VendorPackageInstaller;
import io.wcm.tooling.commons.packmgr.util.ContentPackageProperties;
import io.wcm.tooling.commons.packmgr.util.HttpClientUtil;

/**
 * Package Installer for AEM's CRX Package Manager
 */
public class CrxPackageInstaller implements VendorPackageInstaller {

  private final String url;

  private static final Logger log = LoggerFactory.getLogger(CrxPackageInstaller.class);

  /**
   * @param url URL
   */
  public CrxPackageInstaller(String url) {
    this.url = url;
  }

  @Override
  public void installPackage(PackageFile packageFile, PackageManagerHelper pkgmgr,
      CloseableHttpClient httpClient, HttpClientContext packageManagerHttpClientContext, HttpClientContext consoleHttpClientContext,
      PackageManagerProperties props) throws IOException, PackageManagerException {

    boolean force = packageFile.isForce();

    if (force) {
      // in force mode, just check that package manager is available and then start uploading
      ensurePackageManagerAvailability(pkgmgr, httpClient, packageManagerHttpClientContext);
    }
    else {
      // otherwise check if package is already installed first, and skip further processing if it is
      // this implicitly also checks the availability of the package manager
      PackageInstalledStatus status = getPackageInstalledStatus(packageFile, pkgmgr, httpClient, packageManagerHttpClientContext);
      switch (status) {
        case NOT_FOUND:
          log.debug("Package is not found in package list: proceed with install.");
          break;
        case INSTALLED:
          log.info("Package skipped because it was already uploaded.");
          return;
        case UPLOADED:
          log.info("Package was already uploaded but not installed: proceed with install and switch to force mode.");
          force = true;
          break;
        case INSTALLED_OTHER_VERSION:
          log.info("Package was already uploaded, but another version was installed more recently: proceed with install and switch to force mode.");
          force = true;
          break;
        default:
          throw new PackageManagerException("Unexpected status: " + status);
      }
    }

    // prepare post method
    HttpPost post = new HttpPost(url + "/.json?cmd=upload");
    HttpClientUtil.applyRequestConfig(post, packageFile, props);
    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
        .addBinaryBody("package", packageFile.getFile());
    if (force) {
      entityBuilder.addTextBody("force", "true");
    }
    post.setEntity(entityBuilder.build());

    // execute post
    JSONObject jsonResponse = pkgmgr.executePackageManagerMethodJson(httpClient, packageManagerHttpClientContext, post);
    boolean success = jsonResponse.optBoolean("success", false);
    String msg = jsonResponse.optString("msg", null);
    String path = jsonResponse.optString("path", null);
    if (success) {
      if (packageFile.isInstall()) {
        log.info("Package uploaded, now installing...");

        try {
          post = new HttpPost(url + "/console.html" + new URIBuilder().setPath(path).build().getRawPath() + "?cmd=install"
              + (packageFile.isRecursive() ? "&recursive=true" : ""));
          HttpClientUtil.applyRequestConfig(post, packageFile, props);
        }
        catch (URISyntaxException ex) {
          throw new PackageManagerException("Invalid path: " + path, ex);
        }

        // execute post
        pkgmgr.executePackageManagerMethodHtmlOutputResponse(httpClient, packageManagerHttpClientContext, post);

        // delay further processing after install (if activated)
        delay(packageFile.getDelayAfterInstallSec());

        // after install: if bundles are still stopping/starting, wait for completion
        pkgmgr.waitForBundlesActivation(httpClient, consoleHttpClientContext);
      }
      else {
        log.info("Package uploaded successfully (without installing).");
      }
    }
    else if (StringUtils.startsWith(msg, CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX) && !force) {
      log.info("Package skipped because it was already uploaded.");
    }
    else {
      throw new PackageManagerException("Package upload failed: " + msg);
    }

  }

  @SuppressWarnings("PMD.GuardLogStatement")
  private void delay(int seconds) {
    if (seconds > 0) {
      log.info("Wait {} seconds after package install...", seconds);
      try {
        Thread.sleep(seconds * 1000);
      }
      catch (InterruptedException ex) {
        // ignore
      }
    }
  }

  private void ensurePackageManagerAvailability(PackageManagerHelper pkgmgr, CloseableHttpClient httpClient, HttpClientContext context) {
    // do a help GET call before upload to ensure package manager is running
    HttpGet get = new HttpGet(url + ".jsp?cmd=help");
    pkgmgr.executePackageManagerMethodStatus(httpClient, context, get);
  }

  private PackageInstalledStatus getPackageInstalledStatus(PackageFile packageFile, PackageManagerHelper pkgmgr,
      CloseableHttpClient httpClient, HttpClientContext context) throws IOException {
    // list packages in AEM instances and check for exact match
    String baseUrl = VendorInstallerFactory.getBaseUrl(url);
    String packageListUrl = baseUrl + PackageInstalledChecker.PACKMGR_LIST_URL;
    HttpGet get = new HttpGet(packageListUrl);
    JSONObject result = pkgmgr.executePackageManagerMethodJson(httpClient, context, get);

    Map<String, Object> props = ContentPackageProperties.get(packageFile.getFile());
    String group = (String)props.get(PackageProperties.NAME_GROUP);
    String name = (String)props.get(PackageProperties.NAME_NAME);
    String version = (String)props.get(PackageProperties.NAME_VERSION);

    PackageInstalledChecker checker = new PackageInstalledChecker(result);
    return checker.getStatus(group, name, version);
  }

}
