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

import static io.wcm.tooling.commons.packmgr.PackageManagerHelper.CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import io.wcm.tooling.commons.packmgr.PackageManagerProperties;
import io.wcm.tooling.commons.packmgr.install.VendorInstallerFactory;

/**
 * Downloads a single AEM content package.
 */
public final class PackageDownloader {

  private final PackageManagerProperties props;
  private final PackageManagerHelper pkgmgr;

  private static final Logger log = LoggerFactory.getLogger(PackageDownloader.class);

  /**
   * @param props Package manager configuration properties.
   */
  public PackageDownloader(PackageManagerProperties props) {
    this.props = props;
    this.pkgmgr = new PackageManagerHelper(props);
  }

  /**
   * Download content package from CRX instance.
   * @param file Local version of package that should be downloaded.
   * @param ouputFilePath Path to download package from AEM instance to.
   * @return Downloaded file
   */
  @SuppressWarnings("PMD.GuardLogStatement")
  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  public File downloadFile(File file, String ouputFilePath) {
    try (CloseableHttpClient httpClient = pkgmgr.getHttpClient()) {
      HttpClientContext httpClientContext = pkgmgr.getPackageManagerHttpClientContext();
      log.info("Download {} from {}", file.getName(), props.getPackageManagerUrl());

      // 1st: try upload to get path of package - or otherwise make sure package def exists (no install!)
      HttpPost post = new HttpPost(props.getPackageManagerUrl() + "/.json?cmd=upload");
      MultipartEntityBuilder entity = MultipartEntityBuilder.create()
          .addBinaryBody("package", file)
          .addTextBody("force", "true");
      post.setEntity(entity.build());
      JSONObject jsonResponse = pkgmgr.executePackageManagerMethodJson(httpClient, httpClientContext, post);
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

      log.info("Package path is: {} - now rebuilding package...", path);

      // 2nd: build package
      HttpPost buildMethod = new HttpPost(props.getPackageManagerUrl() + "/console.html" + path + "?cmd=build");
      pkgmgr.executePackageManagerMethodHtmlOutputResponse(httpClient, httpClientContext, buildMethod);

      // 3rd: download package
      String baseUrl = VendorInstallerFactory.getBaseUrl(props.getPackageManagerUrl());
      HttpGet downloadMethod = new HttpGet(baseUrl + path);

      // execute download
      CloseableHttpResponse response = httpClient.execute(downloadMethod, httpClientContext);
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
          IOUtils.copy(responseStream, fos);
          fos.flush();
          responseStream.close();
          fos.close();

          log.info("Package downloaded to {}", outputFileObject.getAbsolutePath());

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

}
