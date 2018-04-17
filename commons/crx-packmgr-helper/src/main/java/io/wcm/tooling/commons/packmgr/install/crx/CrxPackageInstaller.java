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
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jackrabbit.vault.packaging.PackageProperties;
import org.jdom2.Document;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.json.JSONObject;

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import io.wcm.tooling.commons.packmgr.install.PackageFile;
import io.wcm.tooling.commons.packmgr.install.VendorPackageInstaller;
import io.wcm.tooling.commons.packmgr.util.ContentPackageProperties;

/**
 * Package Installer for AEM's CRX Package Manager
 */
public class CrxPackageInstaller implements VendorPackageInstaller {

  private String url;

  /**
   * @param url URL
   */
  public CrxPackageInstaller(String url) {
    this.url = url;
  }

  @Override
  public void installPackage(PackageFile packageFile, PackageManagerHelper pkgmgr, CloseableHttpClient httpClient, Logger log)
      throws IOException, PackageManagerException {

    if (packageFile.isForce()) {
      // in force mode, just check that package manager is available and then start uploading
      ensurePackageManagerAvailability(pkgmgr, httpClient);
    }
    else {
      // otherwise check if package is already installed first, and skip further processing if it is
      // this implicitly also checks the availability of the package manager
      if (isPackageInstalled(packageFile, pkgmgr, httpClient)) {
        log.info("Package skipped because it was already uploaded.");
        return;
      }
    }

    // prepare post method
    HttpPost post = new HttpPost(url + "/.json?cmd=upload");
    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
        .addBinaryBody("package", packageFile.getFile());
    if (packageFile.isForce()) {
      entityBuilder.addTextBody("force", "true");
    }
    post.setEntity(entityBuilder.build());

    // execute post
    JSONObject jsonResponse = pkgmgr.executePackageManagerMethodJson(httpClient, post);
    boolean success = jsonResponse.optBoolean("success", false);
    String msg = jsonResponse.optString("msg", null);
    String path = jsonResponse.optString("path", null);
    if (success) {
      if (packageFile.isInstall()) {
        log.info("Package uploaded, now installing...");

        try {
          post = new HttpPost(url + "/console.html" + new URIBuilder().setPath(path).build().getRawPath() + "?cmd=install"
              + (packageFile.isRecursive() ? "&recursive=true" : ""));
        }
        catch (URISyntaxException ex) {
          throw new PackageManagerException("Invalid path: " + path, ex);
        }

        // execute post
        pkgmgr.executePackageManagerMethodHtmlOutputResponse(httpClient, post);

        // delay further processing after install (if activated)
        delay(packageFile.getDelayAfterInstallSec(), log);

        // after install: if bundles are still stopping/starting, wait for completion
        pkgmgr.waitForBundlesActivation(httpClient);
      }
      else {
        log.info("Package uploaded successfully (without installing).");
      }
    }
    else if (StringUtils.startsWith(msg, CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX) && !packageFile.isForce()) {
      log.info("Package skipped because it was already uploaded.");
    }
    else {
      throw new PackageManagerException("Package upload failed: " + msg);
    }

  }

  private void delay(int seconds, Logger log) {
    if (seconds > 0) {
      log.info("Wait " + seconds + " seconds after package install...");
      try {
        Thread.sleep(seconds * 1000);
      }
      catch (InterruptedException ex) {
        // ignore
      }
    }
  }

  private void ensurePackageManagerAvailability(PackageManagerHelper pkgmgr, CloseableHttpClient httpClient) {
    // do a help GET call before upload to ensure package manager is running
    HttpGet get = new HttpGet(url + ".jsp?cmd=help");
    pkgmgr.executePackageManagerMethodStatus(httpClient, get);
  }

  private boolean isPackageInstalled(PackageFile packageFile, PackageManagerHelper pkgmgr, CloseableHttpClient httpClient) throws IOException {
    // list packages in AEM instances and check for exact match
    HttpGet get = new HttpGet(url + ".jsp?cmd=ls");
    Document doc = pkgmgr.executePackageManagerMethodXml(httpClient, get);
    return isPackageInstalled(doc, packageFile);
  }

  static boolean isPackageInstalled(Document listResponse, PackageFile packageFile) throws IOException {
    // get properties from package file
    Map<String, Object> props = ContentPackageProperties.get(packageFile.getFile());
    String group = (String)props.get(PackageProperties.NAME_GROUP);
    String name = (String)props.get(PackageProperties.NAME_NAME);
    String version = (String)props.get(PackageProperties.NAME_VERSION);
    if (StringUtils.isBlank(group) || StringUtils.isBlank(name) || StringUtils.isBlank(version)) {
      return false;
    }

    // check if package is contained in package list XML
    XPathExpression xpath = XPathFactory.instance().compile("/crx/response/data/packages/package"
        + "[group=$group and name=$name and version=$version]",
        Filters.element(),
        props);
    return xpath.evaluateFirst(listResponse) != null;
  }

}
