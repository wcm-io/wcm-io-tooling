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

/**
 * Installs a list of AEM content packages via Package Manager.
 */
public final class PackageInstaller {

  /**
   * Deploy file via package manager.
   */
  /*
  private void installFile(File file, int fileDelayAfterInstallSec) {
    if (!file.exists()) {
      throw new PackageManagerException("File does not exist: " + file.getAbsolutePath());
    }

    try (CloseableHttpClient httpClient = getHttpClient()) {

      // before install: if bundles are still stopping/starting, wait for completion
      waitForBundlesActivation(httpClient);

      if (this.install) {
        getLog().info("Upload and install " + file.getName() + " to " + getCrxPackageManagerUrl());
      }
      else {
        getLog().info("Upload " + file.getName() + " to " + getCrxPackageManagerUrl());
      }

      // prepare post method
      HttpPost post = new HttpPost(getCrxPackageManagerUrl() + "/.json?cmd=upload");
      MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
          .addBinaryBody("package", file);
      if (this.force) {
        entityBuilder.addTextBody("force", "true");
      }
      post.setEntity(entityBuilder.build());

      // execute post
      JSONObject jsonResponse = executePackageManagerMethodJson(httpClient, post);
      boolean success = jsonResponse.optBoolean("success", false);
      String msg = jsonResponse.optString("msg", null);
      String path = jsonResponse.optString("path", null);

      if (success) {

        if (this.install) {
          getLog().info("Package uploaded, now installing...");

          try {
            post = new HttpPost(getCrxPackageManagerUrl() + "/console.html"
                + new URIBuilder().setPath(path).build().getRawPath()
                + "?cmd=install" + (this.recursive ? "&recursive=true" : ""));
          }
          catch (URISyntaxException ex) {
            throw new PackageManagerException("Invalid path: " + path, ex);
          }

          // execute post
          executePackageManagerMethodHtml(httpClient, post, 0);

          // delay further processing after install (if activated)
          delay(fileDelayAfterInstallSec);

          // after install: if bundles are still stopping/starting, wait for completion
          waitForBundlesActivation(httpClient);
        }
        else {
          getLog().info("Package uploaded successfully (without installing).");
        }

      }
      else if (StringUtils.startsWith(msg, CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX) && !this.force) {
        getLog().info("Package skipped because it was already uploaded.");
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
      getLog().info("Wait for " + seconds + " seconds after package install...");
      try {
        Thread.sleep(seconds * 1000);
      }
      catch (InterruptedException ex) {
        // ignore
      }
    }
  }
  */

}
