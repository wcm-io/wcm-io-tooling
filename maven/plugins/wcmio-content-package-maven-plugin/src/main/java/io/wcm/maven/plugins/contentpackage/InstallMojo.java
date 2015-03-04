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
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
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
        if (file == null) {
          file = ref.getPackageFile();
        }
        if (file != null) {
          installFile(file);
          foundAny = true;
        }
      }
    }
    else {
      File file = helper.getArtifactFile(artifactId, groupId, version, type, artifact);
      if (file == null) {
        file = getPackageFile();
      }
      if (file != null) {
        installFile(file);
        foundAny = true;
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
    try (CloseableHttpClient httpClient = getHttpClient()) {

      // if bundles are still stopping/starting, wait for completion
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
            throw new MojoExecutionException("Invalid path: " + path, ex);
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
    catch (IOException ex) {
      throw new MojoExecutionException("Install operation failed.", ex);
    }
  }

}
