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
package io.wcm.maven.plugins.nodejs.mojo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import io.wcm.maven.plugins.nodejs.installation.NodeInstallationInformation;
import io.wcm.maven.plugins.nodejs.installation.NodeUnarchiveTask;

/**
 * Common Node.js Mojo functionality.
 */
public abstract class AbstractNodeJsMojo extends AbstractMojo {

  /**
   * Node.js version (minimum version: 6.3.0).
   */
  @Parameter(property = "nodejs.version", defaultValue = "10.15.3", required = true)
  protected String nodeJsVersion;

  /**
   * NPM version. If not set the NPM version that is bundled with Node.js is used.
   */
  @Parameter(property = "nodejs.npm.version")
  protected String npmVersion;

  /**
   * Default location where Node.js will be extracted to and run from
   */
  @Parameter(property = "nodejs.directory", defaultValue = "${java.io.tmpdir}/nodejs")
  protected File nodeJsDirectory;

  /**
   * Tasks that should be run on Node.js execution.
   * <p>
   * You can define different types of tasks: <code>npmInstallTask</code> or <code>nodeJsTask</code> items.
   * </p>
   * <p>
   * Example 1:
   * </p>
   *
   * <pre>
   * &lt;tasks&gt;
   *   &lt;npmInstallTask&gt;
   *     &lt;workingDirectory&gt;${frontend.dir}&lt;/workingDirectory&gt;
   *   &lt;/npmInstallTask&gt;
   *   &lt;nodeJsTask&gt;
   *     &lt;workingDirectory&gt;${frontend.dir}&lt;/workingDirectory&gt;
   *     &lt;moduleName&gt;grunt-cli&lt;/moduleName&gt;
   *     &lt;executableName&gt;grunt&lt;/executableName&gt;
   *     &lt;arguments&gt;
   *       &lt;argument&gt;build&lt;/argument&gt;
   *     &lt;/arguments&gt;
   *   &lt;/nodeJsTask&gt;
   * &lt;/tasks&gt;
   * </pre>
   * <p>
   * Example 2:
   * </p>
   *
   * <pre>
   * &lt;tasks&gt;
   *   &lt;npmInstallTask&gt;
   *     &lt;workingDirectory&gt;${frontend.dir}&lt;/workingDirectory&gt;
   *   &lt;/npmInstallTask&gt;
   *   &lt;nodeJsTask&gt;
   *     &lt;workingDirectory&gt;${frontend.dir}&lt;/workingDirectory&gt;
   *     &lt;moduleName&gt;npm&lt;/moduleName&gt;
   *     &lt;executableName&gt;npm-cli&lt;/executableName&gt;
   *     &lt;arguments&gt;
   *       &lt;argument&gt;run&lt;/argument&gt;
   *       &lt;argument&gt;test&lt;/argument&gt;
   *     &lt;/arguments&gt;
   *   &lt;/nodeJsTask&gt;
   * &lt;/tasks&gt;
   * </pre>
   */
  @Parameter
  protected List<? extends Task> tasks;

  /**
   * Stop maven build if error occurs.
   */
  @Parameter(defaultValue = "true")
  protected boolean stopOnError;

  /**
   * If set to true all Node.js plugin operations are skipped.
   */
  @Parameter(property = "nodejs.skip")
  protected boolean skip;

  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;
  @Parameter(defaultValue = "${session}", readonly = true)
  private MavenSession session;
  @Component
  private ArtifactHandlerManager artifactHandlerManager;
  @Component
  private ArtifactResolver resolver;

  private static final ComparableVersion NODEJS_MIN_VERSION = new ComparableVersion("6.3.0");

  /**
   * Installs node js if necessary and performs defined tasks
   * @throws MojoExecutionException Mojo execution exception
   */
  public void run() throws MojoExecutionException {
    if (skip) {
      return;
    }

    if (tasks == null || tasks.isEmpty()) {
      getLog().warn("No Node.js tasks have been defined. Nothing to do.");
    }

    // validate nodejs version
    ComparableVersion nodeJsVersionComparable = new ComparableVersion(nodeJsVersion);
    if (nodeJsVersionComparable.compareTo(NODEJS_MIN_VERSION) < 0) {
      throw new MojoExecutionException("This plugin supports Node.js " + NODEJS_MIN_VERSION + " and up.");
    }

    NodeInstallationInformation information = getOrInstallNodeJS();

    if (tasks != null) {
      for (Task task : tasks) {
        task.setLog(getLog());
        task.execute(information);
      }
    }
  }

  private NodeInstallationInformation getOrInstallNodeJS() throws MojoExecutionException {
    NodeInstallationInformation information = NodeInstallationInformation.forVersion(nodeJsVersion, npmVersion, nodeJsDirectory);
    try {
      if (!information.getNodeExecutable().exists() || !information.getNpmExecutableBundledWithNodeJs().exists()) {
        getLog().info("Install Node.js to " + information.getNodeJsInstallPath());
        if (!cleanNodeJsInstallPath(information)) {
          throw new MojoExecutionException("Could not delete node js directory: " + information.getNodeJsInstallPath());
        }
        File nodeJsBinary = resolveArtifact(information.getNodeJsDependency());
        FileUtils.copyFile(nodeJsBinary, information.getArchive());
        Task installationTask = new NodeUnarchiveTask(nodeJsDirectory.getAbsolutePath());
        installationTask.setLog(getLog());
        installationTask.execute(information);
      }

      if (StringUtils.isNotEmpty(npmVersion) && !information.getNpmExecutable().exists()) {
        updateNPMExecutable(information);
      }
    }
    catch (java.net.MalformedURLException ex) {
      throw new MojoExecutionException("Malformed provided node URL", ex);
    }
    catch (IOException ex) {
      getLog().error("Failed to get nodeJs from " + information.getNodeJsDependency(), ex);
      throw new MojoExecutionException("Failed to downloading nodeJs from " + information.getNodeJsDependency(), ex);
    }
    catch (MojoExecutionException ex) {
      getLog().error("Execution Exception", ex);
      if (stopOnError) {
        throw new MojoExecutionException("Execution Exception", ex);
      }
    }
    return information;
  }

  private boolean cleanNodeJsInstallPath(NodeInstallationInformation information) {
    File directory = new File(information.getNodeJsInstallPath());
    if (directory.exists()) {
      try {
        FileUtils.deleteDirectory(directory);
      }
      catch (IOException ex) {
        getLog().error(ex);
        return false;
      }
    }

    if (information.getArchive().exists()) {
      if (!information.getArchive().delete()) {
        return false;
      }
    }

    return true;
  }

  /**
   * Makes sure the specified npm version is installed in the base directory, regardless in which environment.
   * @param information
   * @throws MojoExecutionException
   */
  private void updateNPMExecutable(NodeInstallationInformation information) throws MojoExecutionException {
    getLog().info("Installing specified npm version " + npmVersion);
    NpmInstallTask npmInstallTask = new NpmInstallTask();
    npmInstallTask.setLog(getLog());
    npmInstallTask.setNpmBundledWithNodeJs(true);
    npmInstallTask.setArguments(new String[] {
        "--prefix", information.getNodeModulesRootPath(), "--global", "npm@" + npmVersion
    });
    npmInstallTask.execute(information);
  }

  @SuppressWarnings("deprecation")
  private File resolveArtifact(Dependency dependency) throws MojoExecutionException {
    Artifact artifact = new DefaultArtifact(dependency.getGroupId(),
        dependency.getArtifactId(),
        VersionRange.createFromVersion(dependency.getVersion()),
        Artifact.SCOPE_PROVIDED,
        dependency.getType(),
        dependency.getClassifier(),
        artifactHandlerManager.getArtifactHandler(dependency.getType()));
    try {
      resolver.resolve(artifact, project.getRemoteArtifactRepositories(), session.getLocalRepository());
    }
    catch (ArtifactResolutionException ex) {
      throw new MojoExecutionException("Unable to get artifact for " + dependency, ex);
    }
    catch (ArtifactNotFoundException ex) {
      throw new MojoExecutionException("Unable to get artifact for " + dependency, ex);
    }
    return artifact.getFile();
  }

}
