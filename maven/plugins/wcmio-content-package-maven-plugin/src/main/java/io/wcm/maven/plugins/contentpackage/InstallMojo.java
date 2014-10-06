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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
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
  @Parameter(property = "vault.packaging", defaultValue = "zip")
  private String packaging;

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
    File file = getArtifactFile();
    if (file == null) {
      file = getPackageFile();
    }
    installFile(file);
  }

  private File getArtifactFile() throws MojoFailureException, MojoExecutionException {
    // check if artifact was specified
    if ((StringUtils.isEmpty(this.artifactId) || StringUtils.isEmpty(this.groupId) || StringUtils.isEmpty(this.version))
        && StringUtils.isEmpty(this.artifact)) {
      return null;
    }

    // split up artifact string
    if (StringUtils.isEmpty(this.artifactId)) {
      String[] parts = StringUtils.split(this.artifact, ":");
      if (parts.length < 3 && parts.length > 4) {
        throw new MojoFailureException("Invalid artifact: " + artifact);
      }
      this.groupId = parts[0];
      this.artifactId = parts[1];
      this.version = parts[2];
      if (parts.length > 3) {
        this.packaging = parts[3];
      }
    }
    Artifact artifactObject = repository.createArtifact(groupId, artifactId, version, packaging);
    ArtifactResolutionRequest request = new ArtifactResolutionRequest();
    request.setArtifact(artifactObject);
    request.setLocalRepository(localRepository);
    request.setRemoteRepositories(remoteRepositories);
    ArtifactResolutionResult result = repository.resolve(request);
    if (result.isSuccess()) {
      return artifactObject.getFile();
    }
    else {
      throw new MojoExecutionException("Unable to download artifact: " + artifactObject.toString());
    }
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
