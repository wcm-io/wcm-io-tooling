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
import java.util.ArrayList;
import java.util.List;

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

import io.wcm.tooling.commons.packmgr.install.PackageInstaller;

/**
 * Install a Content Package on a remote CRX or AEM system.
 */
@Mojo(name = "install", defaultPhase = LifecyclePhase.INSTALL, requiresProject = false, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public final class InstallMojo extends AbstractContentPackageMojo {

  /**
   * Whether to install (unpack) the uploaded package automatically or not.
   */
  @Parameter(property = "vault.install", defaultValue = "true")
  private boolean install;

  /**
   * Force upload and install of content package. If set to:
   * <ul>
   * <li><code>true</code>: Package is always installed, even if it was already uploaded before.</li>
   * <li><code>false</code>: Package is only installed if it was not already uploade before.</li>
   * <li>nothing (default): Force is applied to packages with the string "-SNAPSHOT" in it's filename.</li>
   * </ul>
   */
  @Parameter(property = "vault.force")
  private Boolean force;

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
   * The classifier of the artifact to install.
   */
  @Parameter(property = "vault.classifier")
  private String classifier;

  /**
   * A string of the form <code>groupId:artifactId[:packaging][:classifier]:version</code>.
   */
  @Parameter(property = "vault.artifact")
  private String artifact;

  /**
   * <p>
   * The names of the content package files to install on the target system, separated by ",".
   * </p>
   * <p>
   * This has lower precedence than the 'packageFiles' parameter, but higher precedence than other options to specify
   * files.
   * </p>
   */
  @Parameter(property = "vault.fileList")
  private String packageFileList;

  /**
   * Delay further steps after package installation by this amount of seconds
   */
  @Parameter(property = "vault.delayAfterInstallSec")
  private Integer delayAfterInstallSec;

  /**
   * Fail build when no file was found for installing.
   */
  @Parameter(property = "vault.failOnNoFile", defaultValue = "true")
  private boolean failOnNoFile;

  /**
   * <p>
   * Allows to specify multiple package files at once, either referencing local file systems or maven artifacts.
   * This has higher precedence than all other options to specify files.
   * </p>
   * <p>
   * You can set the following properties for each package item:
   * </p>
   * <ul>
   * <li><code>packageFile</code>: Content package file.</li>
   * <li><code>groupId</code>: The groupId of the artifact to install.</li>
   * <li><code>artifactId</code>: The artifactId of the artifact to install.</li>
   * <li><code>type</code>: The packaging of the artifact to install. (default: zip)</li>
   * <li><code>version</code>: The version of the artifact to install.</li>
   * <li><code>classifier</code>: The classifier of the artifact to install.</li>
   * <li><code>artifact</code>: A string of the form
   * <code>groupId:artifactId[:packaging][:classifier]:version</code>.</li>
   * <li><code>install</code>: Whether to install (unpack) the uploaded package automatically or not.</li>
   * <li><code>force</code>: Force upload and install of content package. If set to false a package is not uploaded or
   * installed if it was already uploaded before.</li>
   * <li><code>recursive</code>: If set to true nested packages get installed as well.</li>
   * <li><code>delayAfterInstallSec</code>: Delay further steps after package installation by this amount of
   * seconds.</li>
   * <li><code>httpSocketTimeoutSec</code>: HTTP socket timeout (in seconds) for this package.</li>
   * </ul>
   */
  @Parameter
  private PackageFile[] packageFiles;

  @Component
  private RepositorySystem repository;

  @Parameter(property = "localRepository", required = true, readonly = true)
  private ArtifactRepository localRepository;

  @Parameter(property = "project.remoteArtifactRepositories", required = true, readonly = true)
  private List<ArtifactRepository> remoteRepositories;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (isSkip()) {
      return;
    }

    List<io.wcm.tooling.commons.packmgr.install.PackageFile> items = new ArrayList<>();

    ArtifactHelper helper = new ArtifactHelper(repository, localRepository, remoteRepositories);
    if (packageFiles != null && packageFiles.length > 0) {
      for (PackageFile ref : packageFiles) {
        io.wcm.tooling.commons.packmgr.install.PackageFile item = toPackageFile(ref, helper);
        if (item.getFile() != null) {
          items.add(item);
        }
      }
    }
    else if (StringUtils.isNotBlank(packageFileList)) {
      String[] fileNames = StringUtils.split(packageFileList, ",");
      for (String fileName : fileNames) {
        File file = new File(StringUtils.trimToEmpty(fileName));
        items.add(toPackageFile(file));
      }
    }
    else {
      File file = helper.getArtifactFile(artifactId, groupId, version, type, classifier, artifact);
      if (file == null) {
        file = getPackageFile();
        if (file != null && !file.exists() && !failOnNoFile) {
          file = null;
        }
      }
      if (file != null) {
        items.add(toPackageFile(file));
      }
    }
    if (items.isEmpty()) {
      if (failOnNoFile) {
        throw new MojoExecutionException("No file found for installing.");
      }
      else {
        getLog().warn("No file found for installing.");
      }
    }
    else {
      PackageInstaller installer = new PackageInstaller(getPackageManagerProperties());
      installer.installFiles(items);
    }
  }

  private io.wcm.tooling.commons.packmgr.install.PackageFile toPackageFile(PackageFile ref, ArtifactHelper helper)
      throws MojoFailureException, MojoExecutionException {
    io.wcm.tooling.commons.packmgr.install.PackageFile output = new io.wcm.tooling.commons.packmgr.install.PackageFile();

    File file = helper.getArtifactFile(ref.getArtifactId(), ref.getGroupId(), ref.getVersion(), ref.getType(), ref.getClassifier(), ref.getArtifact());
    if (file == null) {
      file = ref.getPackageFile();
    }
    output.setFile(file);

    if (ref.getInstall() != null) {
      output.setInstall(ref.getInstall());
    }
    else {
      output.setInstall(this.install);
    }
    if (ref.getForce() != null) {
      output.setForce(ref.getForce());
    }
    else {
      output.setForce(this.force);
    }
    if (ref.getRecursive() != null) {
      output.setRecursive(ref.getRecursive());
    }
    else {
      output.setRecursive(this.recursive);
    }
    if (ref.getDelayAfterInstallSec() != null) {
      output.setDelayAfterInstallSec(ref.getDelayAfterInstallSec());
    }
    else if (this.delayAfterInstallSec != null) {
      output.setDelayAfterInstallSec(this.delayAfterInstallSec);
    }
    else {
      output.setDelayAfterInstallSecAutoDetect();
    }
    output.setHttpSocketTimeoutSec(ref.getHttpSocketTimeoutSec());

    return output;
  }

  private io.wcm.tooling.commons.packmgr.install.PackageFile toPackageFile(File file) {
    io.wcm.tooling.commons.packmgr.install.PackageFile output = new io.wcm.tooling.commons.packmgr.install.PackageFile();

    output.setFile(file);
    output.setInstall(this.install);
    output.setForce(this.force);
    output.setRecursive(this.recursive);
    if (this.delayAfterInstallSec != null) {
      output.setDelayAfterInstallSec(this.delayAfterInstallSec);
    }

    return output;
  }

}
