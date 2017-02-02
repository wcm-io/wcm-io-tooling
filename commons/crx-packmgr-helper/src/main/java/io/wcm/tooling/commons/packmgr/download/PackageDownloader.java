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
package io.wcm.tooling.commons.packmgr.download;

/**
 * Downloads a single AEM content package.
 */
public final class PackageDownloader {

  /**
   * Download content package from CRX instance
   */
  /*
  private File downloadFile(File file, String ouputFilePath) {
    try (CloseableHttpClient httpClient = getHttpClient()) {
      getLog().info("Download " + file.getName() + " from " + getCrxPackageManagerUrl());

      // 1st: try upload to get path of package - or otherwise make sure package def exists (no install!)
      HttpPost post = new HttpPost(getCrxPackageManagerUrl() + "/.json?cmd=upload");
      MultipartEntityBuilder entity = MultipartEntityBuilder.create()
          .addBinaryBody("package", file)
          .addTextBody("force", "true");
      post.setEntity(entity.build());
      JSONObject jsonResponse = executePackageManagerMethodJson(httpClient, post);
      boolean success = jsonResponse.optBoolean("success", false);
      String msg = jsonResponse.optString("msg", null);
      String path = jsonResponse.optString("path", null);

      // package already exists - get path from error message and continue
      if (!success && StringUtils.startsWith(msg, CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX) && StringUtils.isEmpty(path)) {
        path = StringUtils.substringAfter(msg, CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX);
        success = true;
      }
      if (!success) {
        throw new PackageManagerException("Package path detection failed: " + msg);
      }

      getLog().info("Package path is: " + path + " - now rebuilding package...");

      // 2nd: build package
      HttpPost buildMethod = new HttpPost(getCrxPackageManagerUrl() + "/console.html" + path + "?cmd=build");
      executePackageManagerMethodHtml(httpClient, buildMethod, 0);

      // 3rd: download package
      String crxUrl = StringUtils.removeEnd(getCrxPackageManagerUrl(), "/crx/packmgr/service");
      HttpGet downloadMethod = new HttpGet(crxUrl + path);

      // execute download
      CloseableHttpResponse response = httpClient.execute(downloadMethod);
      try {
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

          // get response stream
          InputStream responseStream = response.getEntity().getContent();

          // delete existing file
          File outputFileObject = new File(ouputFilePath);
          if (outputFileObject.exists()) {
            outputFileObject.delete();
          }

          // write response file
          FileOutputStream fos = new FileOutputStream(outputFileObject);
          IOUtil.copy(responseStream, fos);
          fos.flush();
          responseStream.close();
          fos.close();

          getLog().info("Package downloaded to " + outputFileObject.getAbsolutePath());

          return outputFileObject;
        }
        else {
          throw new PackageManagerException("Package download failed:\n"
              + EntityUtils.toString(response.getEntity()));
        }
      }
      finally {
        if (response != null) {
          EntityUtils.consumeQuietly(response.getEntity());
          try {
            response.close();
          }
          catch (IOException ex) {
            // ignore
          }
        }
      }
    }
    catch (FileNotFoundException ex) {
      throw new PackageManagerException("File not found: " + file.getAbsolutePath(), ex);
    }
    catch (IOException ex) {
      throw new PackageManagerException("Download operation failed.", ex);
    }
  }
  */

}
