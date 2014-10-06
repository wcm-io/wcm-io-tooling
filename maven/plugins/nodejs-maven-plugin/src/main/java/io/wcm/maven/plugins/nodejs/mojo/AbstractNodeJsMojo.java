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

import io.wcm.maven.plugins.nodejs.installation.NodeInstallationInformation;
import io.wcm.maven.plugins.nodejs.installation.NodeUnarchiveTask;
import io.wcm.maven.plugins.nodejs.installation.NpmUnarchiveTask;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;

/**
 *
 */
public abstract class AbstractNodeJsMojo extends AbstractMojo {

  @Parameter
  protected String nodeJsURL;

  @Parameter(defaultValue = "0.10.32")
  protected String nodeJsVersion;

  @Parameter(defaultValue = "1.4.9")
  protected String npmVersion;

  @Parameter
  protected List<? extends Task> tasks;

  @Parameter
  protected boolean stopOnError;

  /**
   * Default location where nodejs will be extracted to and run from
   */
  @Parameter(defaultValue = "${java.io.tmpdir}/nodejs")
  protected File nodeJsDirectory;

  /**
   * Installs node js if necessary and performs defined tasks
   * @throws MojoExecutionException
   */
  public void run() throws MojoExecutionException {
    if (tasks == null || tasks.isEmpty()) {
      getLog().warn("No NodeJSTasks have been defined. Nothing to do");
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
      if (nodeJsURL != null) {
        information.setUrl(new URL(nodeJsURL));
      }

      if (!information.getNodeExecutable().exists()) {
        getLog().info("Downloading Node JS from " + information.getUrl());
        FileUtils.copyURLToFile(information.getUrl(), information.getArchive());
        if (information.getArchive().getName().endsWith(".tar.gz")) {
          Task installationTask = new NodeUnarchiveTask(nodeJsDirectory.getAbsolutePath());
          installationTask.setLog(getLog());
          installationTask.execute(information);
        }
        if (Os.isFamily(Os.FAMILY_WINDOWS) || Os.isFamily(Os.FAMILY_WIN9X)) {
          installNPM(information);
        }
      }
    }
    catch (java.net.MalformedURLException ex) {
      throw new MojoExecutionException("Malformed provided node URL", ex);
    }
    catch (IOException ex) {
      getLog().error("Failed to downloading nodeJs from " + nodeJsURL, ex);
      throw new MojoExecutionException("Failed to downloading nodeJs from " + nodeJsURL, ex);
    }
    catch (MojoExecutionException ex) {
      getLog().error("Execution Exception", ex);
      if (stopOnError) {
        throw new MojoExecutionException("Execution Exception", ex);
      }
    }
    return information;
  }

  private void installNPM(NodeInstallationInformation information) throws IOException, MojoExecutionException {
    getLog().info("Downloading npm from " + information.getNpmUrl() + "to " + information.getNpmArchive().getAbsolutePath());
    FileUtils.copyURLToFile(information.getNpmUrl(), information.getNpmArchive());
    Task installationTask = new NpmUnarchiveTask(nodeJsDirectory.getAbsolutePath() + File.separator + "v-" + nodeJsVersion);
    installationTask.setLog(getLog());
    installationTask.execute(information);
  }

}
