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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.IOUtil;
import org.json.JSONObject;

/**
 * Builds and downloads a content package defined on a remote CRX or AEM system.
 */
@Mojo(name = "download", defaultPhase = LifecyclePhase.INSTALL, requiresProject = true,
requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class DownloadMojo extends AbstractContentPackageMojo {

  /**
   * The output file to save.
   */
  @Parameter(property = "vault.outputFile", required = true, defaultValue = "${project.build.directory}/${project.build.finalName}.zip")
  private String outputFile;

  /**
   * Downloads the files
   */
  @Override
  public void execute() throws MojoExecutionException {
    downloadFile(getPackageFile(), this.outputFile);
  }

  /**
   * Download file via package manager
   */
  protected void downloadFile(File file, String ouputFilePath) throws MojoExecutionException {
    if (isSkip()) {
      return;
    }

    try {
      getLog().info("Download " + file.getName() + " from " + getCrxPackageManagerUrl());

      // setup http client with credentials
      HttpClient httpClient = getCrxPackageManagerHttpClient();

      // 1st: try upload to get path of package - or otherwise make sure package def exists (no install!)
      PostMethod post = new PostMethod(getCrxPackageManagerUrl() + "/.json?cmd=upload");
      List<Part> parts = new ArrayList<Part>();
      parts.add(new FilePart("package", file));
      parts.add(new StringPart("force", "false"));
      post.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[parts.size()]), post.getParams()));
      JSONObject response = executePackageManagerMethodJson(httpClient, post, 0);
      boolean success = response.optBoolean("success", false);
      String msg = response.optString("msg", null);
      String path = response.optString("path", null);

      // package already exists - get path from error message and continue
      if (!success && StringUtils.startsWith(msg, CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX) && StringUtils.isEmpty(path)) {
        path = StringUtils.substringAfter(msg, CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX);
        success = true;
      }
      if (!success) {
        throw new MojoExecutionException("Package path detection failed: " + msg);
      }

      getLog().info("Package path is: " + path + " - now rebuilding package...");

      // 2nd: build package
      PostMethod buildMethod = new PostMethod(getCrxPackageManagerUrl() + "/console.html" + path + "?cmd=build");
      executePackageManagerMethodHtml(httpClient, buildMethod, 0);

      // 3rd: download package
      String crxUrl = StringUtils.removeEnd(getCrxPackageManagerUrl(), "/crx/packmgr/service");
      GetMethod downloadMethod = new GetMethod(crxUrl + path);

      // execute download
      int httpStatus = httpClient.executeMethod(downloadMethod);
      if (httpStatus == HttpStatus.SC_OK) {

        // get response stream
        InputStream responseStream = downloadMethod.getResponseBodyAsStream();

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

        getLog().info("Package downloaded succesfully to " + outputFileObject.getAbsolutePath());
      }
      else {
        throw new MojoExecutionException("Package download failed:\n"
            + downloadMethod.getResponseBodyAsString());
      }

      // cleanup
      downloadMethod.releaseConnection();

    }
    catch (FileNotFoundException ex) {
      throw new MojoExecutionException("File not found: " + file.getAbsolutePath(), ex);
    }
    catch (HttpException ex) {
      throw new MojoExecutionException("Post method failed.", ex);
    }
    catch (IOException ex) {
      throw new MojoExecutionException("Post method failed.", ex);
    }
  }

}
