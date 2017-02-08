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
package io.wcm.tooling.commons.packmgr.install;

import static io.wcm.tooling.commons.packmgr.PackageManagerHelper.CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import io.wcm.tooling.commons.packmgr.PackageManagerProperties;

/**
 * Installs a list of AEM content packages via Package Manager.
 */
public final class PackageInstaller {

  private final PackageManagerProperties props;
  private final PackageManagerHelper pkgmgr;
  private final Logger log;

  /**
   * @param props Package manager configuration properties.
   * @param log Logger
   */
  public PackageInstaller(PackageManagerProperties props, Logger log) {
    this.props = props;
    this.pkgmgr = new PackageManagerHelper(props, log);
    this.log = log;
  }

  /**
   * Deploy files via package manager.
   * @param packageFiles AEM content packages
   */
  public void installFiles(Collection<PackageFile> packageFiles) {
    for (PackageFile packageFile : packageFiles) {
      installFile(packageFile);
    }
  }

  /**
   * Deploy file via package manager.
   * @param packageFile AEM content package
   */
  public void installFile(PackageFile packageFile) {
    File file = packageFile.getFile();
    if (!file.exists()) {
      throw new PackageManagerException("File does not exist: " + file.getAbsolutePath());
    }

    try (CloseableHttpClient httpClient = pkgmgr.getHttpClient()) {

      // before install: if bundles are still stopping/starting, wait for completion
      pkgmgr.waitForBundlesActivation(httpClient);

      if (packageFile.isInstall()) {
        log.info("Upload and install " + file.getName() + " to " + props.getPackageManagerUrl());
      }
      else {
        log.info("Upload " + file.getName() + " to " + props.getPackageManagerUrl());
      }

      // prepare post method
      HttpPost post = new HttpPost(props.getPackageManagerUrl() + "/.json?cmd=upload");
      MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
          .addBinaryBody("package", file);
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
            post = new HttpPost(props.getPackageManagerUrl() + "/console.html"
                + new URIBuilder().setPath(path).build().getRawPath()
                + "?cmd=install" + (packageFile.isRecursive() ? "&recursive=true" : ""));
          }
          catch (URISyntaxException ex) {
            throw new PackageManagerException("Invalid path: " + path, ex);
          }

          // execute post
          pkgmgr.executePackageManagerMethodHtml(httpClient, post, 0);

          // delay further processing after install (if activated)
          delay(packageFile.getDelayAfterInstallSec());

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
    catch (IOException ex) {
      throw new PackageManagerException("Install operation failed.", ex);
    }
  }

  private void delay(int seconds) {
    if (seconds > 0) {
      log.info("Wait for " + seconds + " seconds after package install...");
      try {
        Thread.sleep(seconds * 1000);
      }
      catch (InterruptedException ex) {
        // ignore
      }
    }
  }

}
