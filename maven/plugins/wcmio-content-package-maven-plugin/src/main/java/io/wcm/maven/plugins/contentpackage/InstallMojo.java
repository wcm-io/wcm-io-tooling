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
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.repository.RepositorySystem;
import org.json.JSONObject;

/**
 * Install a Content Package on a remote CRX or AEM system.
 */
@Mojo(name = "install", defaultPhase = LifecyclePhase.INSTALL, requiresProject = true,
requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public final class InstallMojo extends AbstractContentPackageMojo {

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
   * If set to true nested packages get installed as well.
   */
  @Parameter(property = "vault.recursive", defaultValue = "true")
  private boolean recursive;

  /**
   * The groupId of the artifact to install.
   */
  @Parameter(property = "vault.groupId")
  private String groupId;

  /**
   * The artifactId of the artifact to install.
   */
  @Parameter(property = "vault.artifactId")
  private String artifactId;

  /**
   * The packaging of the artifact to install.
   */
  @Parameter(alias = "packaging", property = "vault.packaging", defaultValue = "zip")
  private String type;

  /**
   * The version of the artifact to install.
   */
  @Parameter(property = "vault.version")
  private String version;

  /**
   * A string of the form <code>groupId:artifactId:version[:packaging]</code>.
   */
  @Parameter(property = "vault.artifact")
  private String artifact;

  /**
   * Allows to specify multiple package files at once, either referencing local file systems or maven artifacts.
   */
  @Parameter
  private PackageFile[] packageFiles;

  @Component
  private RepositorySystem repository;

  @Parameter(property = "localRepository", required = true, readonly = true)
  private ArtifactRepository localRepository;

  @Parameter(property = "project.remoteArtifactRepositories", required = true, readonly = true)
  private java.util.List<ArtifactRepository> remoteRepositories;

  /**
   * Generates the ZIP.
   * @throws MojoFailureException
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (isSkip()) {
      return;
    }

    boolean foundAny = false;
    ArtifactHelper helper = new ArtifactHelper(repository, localRepository, remoteRepositories);
    if (packageFiles != null && packageFiles.length > 0) {
      for (PackageFile ref : packageFiles) {
        File file = helper.getArtifactFile(ref.getArtifactId(), ref.getGroupId(), ref.getVersion(), ref.getType(), ref.getArtifact());
        if (file != null) {
          installFile(file);
          foundAny = true;
        }
        else {
          file = ref.getPackageFile();
          if (file != null) {
            installFile(file);
            foundAny = true;
          }
        }
      }
    }
    else {
      File file = helper.getArtifactFile(this.artifactId, this.groupId, this.version, this.type, this.artifact);
      if (file != null) {
        installFile(file);
        foundAny = true;
      }
      else {
        file = getPackageFile();
        if (file != null) {
          installFile(file);
          foundAny = true;
        }
      }
    }
    if (!foundAny) {
      throw new MojoExecutionException("No file found for installing.");
    }
  }

  /**
   * Deploy file via package manager
   */
  private void installFile(File file) throws MojoExecutionException {
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

          try {
            post = new PostMethod(getCrxPackageManagerUrl() + "/console.html" + URIUtil.encodePath(path)
                + "?cmd=install" + (this.recursive ? "&recursive=true" : ""));
          }
          catch (URIException ex) {
            throw new MojoExecutionException("Invalid URI: " + path, ex);
          }

          // execute post
          executePackageManagerMethodHtml(httpClient, post, 0);
        }
        else {
          getLog().info("Package uploaded successfully (without installing).");
        }

      }
      else if (StringUtils.startsWith(msg, CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX) && !this.force) {
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
