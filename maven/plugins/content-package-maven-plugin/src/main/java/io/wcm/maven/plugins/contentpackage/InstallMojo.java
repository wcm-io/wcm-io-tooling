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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
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
import org.json.JSONObject;

/**
 * Install a Content Package on a remote CRX or AEM system.
 */
@Mojo(name = "install", defaultPhase = LifecyclePhase.INSTALL, requiresProject = true,
requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class InstallMojo extends AbstractContentPackageMojo {

  /**
   * Whether to install (unpack) the uploaded package automatically or not.
   */
  @Parameter(property = "vault.install", defaultValue = "true")
  private boolean install;

  /**
   * Force upload and install of content package. If set to false a package is not uploaded or installed
   * if it was already uploaded before.
   */
  @Parameter(property = "vault.force", defaultValue = "true")
  private boolean force;

  /**
   * Generates the ZIP.
   */
  @Override
  public void execute() throws MojoExecutionException {
    installFile(getPackageFile());
  }

  /**
   * Deploy file via package manager
   */
  private void installFile(File file) throws MojoExecutionException {
    if (isSkip()) {
      return;
    }

    try {
      if (this.install) {
        getLog().info("Upload and install " + file.getName() + " to " + getCrxPackageManagerUrl());
      }
      else {
        getLog().info("Upload " + file.getName() + " to " + getCrxPackageManagerUrl());
      }

      // setup http client with credentials
      HttpClient httpClient = getCrxPackageManagerHttpClient();

      // prepare post method
      PostMethod post = new PostMethod(getCrxPackageManagerUrl() + "/.json?cmd=upload");
      List<Part> parts = new ArrayList<Part>();
      parts.add(new FilePart("package", file));
      if (this.force) {
        parts.add(new StringPart("force", "true"));
      }
      post.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[parts.size()]), post.getParams()));

      // execute post
      JSONObject response = executePackageManagerMethodJson(httpClient, post, 0);
      boolean success = response.optBoolean("success", false);
      String msg = response.optString("msg", null);
      String path = response.optString("path", null);

      if (success) {

        if (this.install) {
          getLog().info("Package uploaded, now installing...");

          post = new PostMethod(getCrxPackageManagerUrl() + "/console.html" + path + "?cmd=install");

          // execute post
          executePackageManagerMethodHtml(httpClient, post, 0);
        }
        else {
          getLog().info("Package uploaded successfully (without installing).");
        }

      }
      else if (StringUtils.startsWith(msg, "Package already exists") && !this.force) {
        getLog().info("Package skipped because it was already uploaded.");
      }
      else {
        throw new MojoExecutionException("Package upload failed: " + msg);
      }

    }
    catch (FileNotFoundException ex) {
      throw new MojoExecutionException("File not found: " + file.getAbsolutePath(), ex);
    }
  }

}
