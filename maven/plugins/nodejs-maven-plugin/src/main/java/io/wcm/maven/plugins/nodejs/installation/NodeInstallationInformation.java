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
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.Os;

/**
 * Holds the general information about the node installation. Provides node and npm executables
 */
public class NodeInstallationInformation {

  private URL url;
  private URL npmUrl;
  private File archive;
  private File npmArchive;
  private File nodeExecutable;
  private File npmExecutable;
  private String basePath;

  private static final String LATEST_WINDOWS_NPM_VERSION = "1.4.9";

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
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
    String baseURL = "http://nodejs.org/dist/v" + version + "/";
    String basePath = directory.getAbsolutePath() + File.separator;
    String arch;
    if (Os.isArch("x86") || Os.isArch("i386")) {
      arch = "x86";
    }
    else if (Os.isArch("x86_64") || Os.isArch("amd64")) {
      arch = "x64";
    }
    else {
      throw new MojoExecutionException("Unsupported OS arch: " + Os.OS_ARCH);
    }

    NodeInstallationInformation result = new NodeInstallationInformation();
    result.setBasePath(basePath);
    try {
      if (Os.isFamily(Os.FAMILY_WINDOWS) || Os.isFamily(Os.FAMILY_WIN9X)) {
        npmVersion = getWindowsNpmVersion(npmVersion);
        basePath = basePath + "v-" + version + File.separator;
        result.setUrl(new URL(baseURL + "node.exe"));
        result.setNpmUrl(new URL("http://nodejs.org/dist/npm/npm-" + npmVersion + ".tgz"));
        result.setArchive(new File(basePath + "node.exe"));
        result.setNpmArchive(new File(basePath + "npm-" + npmVersion + ".tgz"));
        result.setNodeExecutable(new File(basePath + "node.exe"));
        result.setNpmExecutable(new File(basePath + "npm/bin/npm-cli.js"));
      }
      else if (Os.isFamily(Os.FAMILY_MAC)) {
        result.setUrl(new URL(baseURL + "node-v" + version + "-darwin-" + arch + ".tar.gz"));
        result.setArchive(new File(basePath + "node-v" + version + "-darwin-" + arch + ".tar.gz"));
        result.setNodeExecutable(new File(basePath + "node-v" + version + "-darwin-" + arch + File.separator + "bin" + File.separator + "node"));
        result.setNpmExecutable(new File(basePath + "node-v" + version + "-darwin-" + arch + File.separator + "lib" + File.separator
            + "node_modules/npm/bin/npm-cli.js"));
      }
      else if (Os.isFamily(Os.FAMILY_UNIX)) {
        result.setUrl(new URL(baseURL + "node-v" + version + "-linux-" + arch + ".tar.gz"));
        result.setArchive(new File(basePath + "node-v" + version + "-linux-" + arch + ".tar.gz"));
        result.setNodeExecutable(new File(basePath + "node-v" + version + "-linux-" + arch + File.separator + "bin" + File.separator + "node"));
        result.setNpmExecutable(new File(basePath + "node-v" + version + "-linux-" + arch + File.separator + "lib" + File.separator
            + "node_modules/npm/bin/npm-cli.js"));
      }
      else {
        throw new MojoExecutionException("Unsupported OS: " + Os.OS_FAMILY);
      }
    }
    catch (java.net.MalformedURLException ex) {
      throw new MojoExecutionException("Malformed node URL", ex);
    }
    return result;

  }

  /**
   * Sets the executable of the npm to specified version, previosly installed in the base directory
   * @param information
   * @param directory
   * @throws MojoExecutionException
   */
  public static void updateNpmExecutable(NodeInstallationInformation information, File directory) throws MojoExecutionException {
    String basePath = directory.getAbsolutePath() + File.separator;
    if (Os.isFamily(Os.FAMILY_WINDOWS) || Os.isFamily(Os.FAMILY_WIN9X)) {
      //basePath = basePath + "v-" + version + File.separator;
      information.setNpmExecutable(new File(basePath + "node_modules/npm/bin/npm-cli.js"));
    }
    else if (Os.isFamily(Os.FAMILY_MAC)) {
      information.setNpmExecutable(new File(basePath + "node_modules/npm/bin/npm-cli.js"));
    }
    else if (Os.isFamily(Os.FAMILY_UNIX)) {
      information.setNpmExecutable(new File(basePath + File.separator + "node_modules/npm/bin/npm-cli.js"));
    }
    else {
      throw new MojoExecutionException("Unsupported OS: " + Os.OS_FAMILY);
    }

  }

  private static String getWindowsNpmVersion(String npmVersion) {
    if (StringUtils.startsWith(npmVersion, "2.")) {
      return LATEST_WINDOWS_NPM_VERSION;
    }
    return npmVersion;
  }

  public File getNpmArchive() {
    return npmArchive;
  }

  public void setNpmArchive(File npmArchive) {
    this.npmArchive = npmArchive;
  }

  public URL getNpmUrl() {
    return npmUrl;
  }

  public void setNpmUrl(URL npmUrl) {
    this.npmUrl = npmUrl;
  }

}
