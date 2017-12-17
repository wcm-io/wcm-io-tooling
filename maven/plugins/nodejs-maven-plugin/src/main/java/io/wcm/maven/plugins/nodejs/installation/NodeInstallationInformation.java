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
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
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
  private static final String NPM_BINARIES_ARTIFACTID = "npm-binaries";

  private static final String LATEST_WINDOWS_NPM_VERSION = "1.4.9";

  private Dependency nodeJsDependency;
  private Dependency npmDependency;
  private File archive;
  private File npmArchive;
  private File nodeExecutable;
  private File npmExecutable;
  private String basePath;

  public Dependency getNodeJsDependency() {
    return this.nodeJsDependency;
  }

  public void setNodeJsDependency(Dependency nodeJsDependency) {
    this.nodeJsDependency = nodeJsDependency;
  }

  public Dependency getNpmDependency() {
    return this.npmDependency;
  }

  public void setNpmDependency(Dependency npmDependency) {
    this.npmDependency = npmDependency;
  }

  public File getArchive() {
    return archive;
  }

  public void setArchive(File archive) {
    this.archive = archive;
  }

  public File getNodeExecutable() {
    return nodeExecutable;
  }

  public void setNodeExecutable(File nodeExecutable) {
    this.nodeExecutable = nodeExecutable;
  }

  public File getNpmExecutable() {
    return npmExecutable;
  }

  public void setNpmExecutable(File npmExecutable) {
    this.npmExecutable = npmExecutable;
  }

  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  /**
   * Creates a {@link NodeInstallationInformation} for a specific Node.js and npm version and directory
   * @param version
   * @param npmVersion
   * @param directory
   * @return {@link NodeInstallationInformation}
   * @throws MojoExecutionException
   */
  public static NodeInstallationInformation forVersion(String version, String npmVersion, File directory) throws MojoExecutionException {
    String basePath = directory.getAbsolutePath() + File.separator;
    String arch;
    if (Os.isArch("x86") || Os.isArch("i386")) {
      arch = "x86";
    }
    else if (Os.isArch("x86_64") || Os.isArch("amd64")) {
      arch = "x64";
    } else if (Os.isArch("arm")) {
        try {
			arch = getArchitecture("arm");
		} catch (IOException e) {
			throw new MojoExecutionException(e.toString(), e);
		}
    }
    else {
      throw new MojoExecutionException("Unsupported OS arch: " + Os.OS_ARCH);
    }

    NodeInstallationInformation result = new NodeInstallationInformation();
    result.setBasePath(basePath);
    if (Os.isFamily(Os.FAMILY_WINDOWS) || Os.isFamily(Os.FAMILY_WIN9X)) {
      String windowsNpmVersion = getWindowsNpmVersion(npmVersion);
      basePath = basePath + "v-" + version + File.separator;
      result.setNodeJsDependency(buildDependency(NODEJS_BINARIES_GROUPID, NODEJS_BINARIES_ARTIFACTID, version, "windows", arch, "exe"));
      result.setNpmDependency(buildDependency(NODEJS_BINARIES_GROUPID, NPM_BINARIES_ARTIFACTID, windowsNpmVersion, null, null, "tgz"));
      result.setArchive(new File(basePath + "node.exe"));
      result.setNpmArchive(new File(basePath + "npm-" + windowsNpmVersion + ".tgz"));
      result.setNodeExecutable(new File(basePath + "node.exe"));
      result.setNpmExecutable(new File(basePath + "npm/bin/npm-cli.js"));
    }
    else if (Os.isFamily(Os.FAMILY_MAC)) {
      result.setNodeJsDependency(buildDependency(NODEJS_BINARIES_GROUPID, NODEJS_BINARIES_ARTIFACTID, version, "darwin", arch, "tar.gz"));
      result.setArchive(new File(basePath + "node-v" + version + "-darwin-" + arch + ".tar.gz"));
      result.setNodeExecutable(new File(basePath + "node-v" + version + "-darwin-" + arch + File.separator + "bin" + File.separator + "node"));
      result.setNpmExecutable(new File(basePath + "node-v" + version + "-darwin-" + arch + File.separator + "lib" + File.separator
          + "node_modules/npm/bin/npm-cli.js"));
    }
    else if (Os.isFamily(Os.FAMILY_UNIX)) {
      result.setNodeJsDependency(buildDependency(NODEJS_BINARIES_GROUPID, NODEJS_BINARIES_ARTIFACTID, version, "linux", arch, "tar.gz"));
      result.setArchive(new File(basePath + "node-v" + version + "-linux-" + arch + ".tar.gz"));
      result.setNodeExecutable(new File(basePath + "node-v" + version + "-linux-" + arch + File.separator + "bin" + File.separator + "node"));
      result.setNpmExecutable(new File(basePath + "node-v" + version + "-linux-" + arch + File.separator + "lib" + File.separator
          + "node_modules/npm/bin/npm-cli.js"));
    }
    else {
      throw new MojoExecutionException("Unsupported OS: " + Os.OS_FAMILY);
    }
    return result;

  }

  public static String getArchitecture(String search) throws IOException {
    return Arrays.stream(IOUtils.toString(new ProcessBuilder("uname", "-a").start().getInputStream()).split(" ")).filter(w -> w.startsWith(search)).findFirst().get();
  }

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

  /**
   * Sets the executable of the npm to specified version, previosly installed in the base directory
   * @param information
   * @param directory
   * @throws MojoExecutionException
   */
  public static void setSpecifiedNpmExecutable(NodeInstallationInformation information, File directory) throws MojoExecutionException {
    String basePath = directory.getAbsolutePath() + File.separator;
    if (Os.isFamily(Os.FAMILY_WINDOWS) || Os.isFamily(Os.FAMILY_WIN9X)) {
      information.setNpmExecutable(new File(basePath + "node_modules/npm/bin/npm-cli.js"));
    }
    else if (Os.isFamily(Os.FAMILY_MAC)) {
      information.setNpmExecutable(new File(basePath + "node_modules/npm/bin/npm-cli.js"));
    }
    else if (Os.isFamily(Os.FAMILY_UNIX)) {
      information.setNpmExecutable(new File(basePath + "node_modules/npm/bin/npm-cli.js"));
    }
    else {
      throw new MojoExecutionException("Unsupported OS: " + Os.OS_FAMILY);
    }

  }

  private static String getWindowsNpmVersion(String npmVersion) {
    if (StringUtils.startsWith(npmVersion, "1.")) {
      return npmVersion;
    }
    // NPM 2.x and up is no longer published and nodejs.org/dist - use latest 1.x version and update to 2.x via NPM
    return LATEST_WINDOWS_NPM_VERSION;
  }

  public File getNpmArchive() {
    return npmArchive;
  }

  public void setNpmArchive(File npmArchive) {
    this.npmArchive = npmArchive;
  }

}
