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
package io.wcm.maven.plugins.nodejs.installation;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.Os;

/**
 * Holds the general information about the node installation. Provides node and npm executables
 */
public class NodeInstallationInformation {

  private static final String NODEJS_BINARIES_GROUPID = "org.nodejs.dist";
  private static final String NODEJS_BINARIES_ARTIFACTID = "nodejs-binaries";

  private static final String TYPE_TAR_GZ = "tar.gz";
  static final String TYPE_ZIP = "zip";

  private static final String OS_WINDOWS = "win";
  private static final String OS_MACOS = "darwin";
  private static final String OS_LINUX = "linux";

  private Dependency nodeJsDependency;
  private Dependency npmDependency;
  private File archive;
  private String nodeExecutableRelativePath;
  private String npmExecutableRelativePath;
  private String nodeJsInstallPath;
  private String nodeModulesRootPath;

  public Dependency getNodeJsDependency() {
    return this.nodeJsDependency;
  }

  void setNodeJsDependency(Dependency nodeJsDependency) {
    this.nodeJsDependency = nodeJsDependency;
  }

  public Dependency getNpmDependency() {
    return this.npmDependency;
  }

  void setNpmDependency(Dependency npmDependency) {
    this.npmDependency = npmDependency;
  }

  public File getArchive() {
    return archive;
  }

  void setArchive(File archive) {
    this.archive = archive;
  }

  public File getNodeExecutable() {
    return new File(this.nodeJsInstallPath + File.separator + nodeExecutableRelativePath);
  }

  void setNodeExecutableRelativePath(String nodeExecutableRelativePath) {
    this.nodeExecutableRelativePath = nodeExecutableRelativePath;
  }

  public File getNpmExecutable() {
    return new File(this.nodeModulesRootPath + File.separator + npmExecutableRelativePath);
  }

  public File getNpmExecutableBundledWithNodeJs() {
    return new File(this.nodeJsInstallPath + File.separator + npmExecutableRelativePath);
  }

  void setNpmExecutableRelativePath(String npmExecutableRelativePath) {
    this.npmExecutableRelativePath = npmExecutableRelativePath;
  }

  public String getNodeJsInstallPath() {
    return nodeJsInstallPath;
  }

  void setNodeJsInstallPath(String nodeJsInstallPath) {
    this.nodeJsInstallPath = nodeJsInstallPath;
  }

  public String getNodeModulesRootPath() {
    return this.nodeModulesRootPath;
  }

  void setNodeModulesRootPath(String nodeModulesRootPath) {
    this.nodeModulesRootPath = nodeModulesRootPath;
  }

  /**
   * Creates a {@link NodeInstallationInformation} for a specific Node.js and npm version and directory
   * @param version Version
   * @param npmVersion NPM version
   * @param directory directory
   * @return {@link NodeInstallationInformation}
   * @throws MojoExecutionException Mojo execution exception
   */
  public static NodeInstallationInformation forVersion(String version, String npmVersion, File directory) throws MojoExecutionException {
    String arch;
    if (Os.isArch("x86") || Os.isArch("i386")) {
      arch = "x86";
    }
    else if (Os.isArch("x86_64") || Os.isArch("amd64")) {
      arch = "x64";
    }
    else if (Os.isArch("aarch64")) {
      arch = "arm64";
    }
    else {
      throw new MojoExecutionException("Unsupported OS arch: " + Os.OS_ARCH);
    }

    NodeInstallationInformation result = new NodeInstallationInformation();

    String basePath = directory.getAbsolutePath() + File.separator;

    if (Os.isFamily(Os.FAMILY_WINDOWS) || Os.isFamily(Os.FAMILY_WIN9X)) {
      String nodeJsInstallPath = basePath + "node-v" + version + "-" + OS_WINDOWS + "-" + arch;
      result.setNodeJsInstallPath(nodeJsInstallPath);
      result.setNodeJsDependency(buildDependency(NODEJS_BINARIES_GROUPID, NODEJS_BINARIES_ARTIFACTID, version, OS_WINDOWS, arch, TYPE_ZIP));
      result.setArchive(new File(nodeJsInstallPath + "." + TYPE_ZIP));
      result.setNodeExecutableRelativePath("node.exe");
      result.setNpmExecutableRelativePath("node_modules" + File.separator
          + "npm" + File.separator + "bin" + File.separator + "npm-cli.js");
    }
    else if (Os.isFamily(Os.FAMILY_MAC)) {
      String nodeJsInstallPath = basePath + "node-v" + version + "-" + OS_MACOS + "-" + arch;
      result.setNodeJsInstallPath(nodeJsInstallPath);
      result.setNodeJsDependency(buildDependency(NODEJS_BINARIES_GROUPID, NODEJS_BINARIES_ARTIFACTID, version, OS_MACOS, arch, TYPE_TAR_GZ));
      result.setArchive(new File(nodeJsInstallPath + "." + TYPE_TAR_GZ));
      result.setNodeExecutableRelativePath("bin" + File.separator + "node");
      result.setNpmExecutableRelativePath("lib" + File.separator + "node_modules" + File.separator
          + "npm" + File.separator + "bin" + File.separator + "npm-cli.js");
    }
    else if (Os.isFamily(Os.FAMILY_UNIX)) {
      String nodeJsInstallPath = basePath + "node-v" + version + "-" + OS_LINUX + "-" + arch;
      result.setNodeJsInstallPath(nodeJsInstallPath);
      result.setNodeJsDependency(buildDependency(NODEJS_BINARIES_GROUPID, NODEJS_BINARIES_ARTIFACTID, version, OS_LINUX, arch, TYPE_TAR_GZ));
      result.setArchive(new File(nodeJsInstallPath + "." + TYPE_TAR_GZ));
      result.setNodeExecutableRelativePath("bin" + File.separator + "node");
      result.setNpmExecutableRelativePath("lib" + File.separator + "node_modules" + File.separator
          + "npm" + File.separator + "bin" + File.separator + "npm-cli.js");
    }
    else {
      throw new MojoExecutionException("Unsupported OS: " + Os.OS_FAMILY);
    }

    if (StringUtils.isNotEmpty(npmVersion)) {
      result.setNodeModulesRootPath(result.getNodeJsInstallPath() + File.separator + "npm-v" + npmVersion);
    }
    else {
      result.setNodeModulesRootPath(result.getNodeJsInstallPath());
    }

    return result;
  }

  @SuppressWarnings("PMD.UseStringBufferForStringAppends")
  private static Dependency buildDependency(String groupId, String artifactId, String version, String os, String arch, String type) {
    String classifier = null;
    if (StringUtils.isNotEmpty(os)) {
      classifier = os;
    }
    if (StringUtils.isNotEmpty(arch)) {
      classifier += "-" + arch;
    }

    Dependency dependency = new Dependency();
    dependency.setGroupId(groupId);
    dependency.setArtifactId(artifactId);
    dependency.setVersion(version);
    dependency.setType(type);
    dependency.setClassifier(classifier);
    return dependency;
  }

}
