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
package io.wcm.tooling.commons.packmgr.install.composum;

import java.io.IOException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import io.wcm.tooling.commons.packmgr.PackageManagerProperties;
import io.wcm.tooling.commons.packmgr.install.PackageFile;
import io.wcm.tooling.commons.packmgr.install.VendorPackageInstaller;
import io.wcm.tooling.commons.packmgr.util.HttpClientUtil;

/**
 * Vendor Installer for Composum.
 */
public class ComposumPackageInstaller implements VendorPackageInstaller {

  private final String url;

  private static final Logger log = LoggerFactory.getLogger(ComposumPackageInstaller.class);

  /**
   * @param url URL
   */
  public ComposumPackageInstaller(String url) {
    this.url = url;
  }

  @Override
  public void installPackage(PackageFile packageFile, boolean replicate, PackageManagerHelper pkgmgr,
      CloseableHttpClient httpClient, HttpClientContext packageManagerHttpClientContext, HttpClientContext consoleHttpClientContext,
      PackageManagerProperties props) throws IOException, PackageManagerException {

    if (replicate) {
      throw new IllegalArgumentException("Replicating packages not supported for Composum package installer.");
    }

    // prepare post method
    int index = url.indexOf("/bin/cpm/");
    String baseUrl = url.substring(0, index) + "/bin/cpm/package.";
    String uploadUrl = baseUrl + "upload.json";
    HttpPost post = new HttpPost(uploadUrl);
    HttpClientUtil.applyRequestConfig(post, packageFile, props);
    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
        .addBinaryBody("file", packageFile.getFile());
    if (packageFile.isForce()) {
      entityBuilder.addTextBody("force", "true");
    }
    post.setEntity(entityBuilder.build());

    // execute post
    JSONObject jsonResponse = pkgmgr.executePackageManagerMethodJson(httpClient, packageManagerHttpClientContext, post);
    String status = jsonResponse.optString("status", "not-found");
    boolean success = "successful".equals(status);
    String path = jsonResponse.optString("path", null);
    if (success) {
      if (packageFile.isInstall()) {
        log.info("Package uploaded, now installing...");

        String installUrl = baseUrl + "install.json" + path;
        post = new HttpPost(installUrl);
        HttpClientUtil.applyRequestConfig(post, packageFile, props);

        // execute post
        JSONObject jsonResponseInstallation = pkgmgr.executePackageManagerMethodJson(httpClient, packageManagerHttpClientContext, post);
        String installationStatus = jsonResponseInstallation.optString("status", "not-found");
        if (!"done".equals(installationStatus)) {
          throw new PackageManagerException("Package installation failed: " + status);
        }

        // delay further processing after install (if activated)
        delay(packageFile.getDelayAfterInstallSec());

        // after install: if bundles are still stopping/starting, wait for completion
        pkgmgr.waitForBundlesActivation(httpClient, consoleHttpClientContext);
      }
      else {
        log.info("Package uploaded successfully (without installing).");
      }
    }
    // As of now the force flag is ignored by Composum and it fill upload not matter what (ticket pending: https://github.com/ist-dresden/composum/issues/73)
    //else if (StringUtils.startsWith(response, CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX) && !packageFile.isForce()) {
    //  log.info("Package skipped because it was already uploaded.");
    //}
    else {
      throw new PackageManagerException("Package upload failed: " + status);
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

}
