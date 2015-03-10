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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.Os;

/**
 * General task implementation.
 */
public class Task {

  private static final String PATH_VARIABLE_NAME = "PATH";

  /**
   * Directory in which the should be executed.
   */
  @Parameter
  protected File workingDirectory;

  private Log log;

  /**
   * Executes the {@link Process} with commands returned by {@link #getCommand(NodeInstallationInformation)}.
   * @param information
   * @throws MojoExecutionException
   */
  public void execute(NodeInstallationInformation information) throws MojoExecutionException {
    ProcessBuilder processBuilder = new ProcessBuilder(getCommand(information));
    if (workingDirectory != null) {
      if (!workingDirectory.exists()) {
        workingDirectory.mkdir();
      }
      processBuilder.directory(workingDirectory);
    }
    setNodePath(processBuilder, information);
    startProcess(processBuilder);
  }

  private void startProcess(ProcessBuilder processBuilder) throws MojoExecutionException {
    try {
      final Process process = processBuilder.start();
      getLog().info("Running process: " + StringUtils.join(processBuilder.command(), " "));
      initLogging(process);
      int result = process.waitFor();
      if (result != 0) {
        throw new MojoExecutionException("Process: " + StringUtils.join(processBuilder.command(), " ") + " terminated with " + result);
      }
    }
    catch (IOException ex) {
      throw new MojoExecutionException("Error executing process: " + StringUtils.join(processBuilder.command(), " "), ex);
    }
    catch (InterruptedException ex) {
      throw new MojoExecutionException("Error executing process: " + StringUtils.join(processBuilder.command(), " "), ex);
    }
  }

  private void initLogging(final Process process) throws InterruptedException {
    final Thread infoLogThread = new NodejsOutputStreamHandler(process.getInputStream(), getLog());
    final Thread errorLogThread = new NodejsOutputStreamHandler(process.getErrorStream(), getLog());

    infoLogThread.start();
    errorLogThread.start();
    infoLogThread.join();
    errorLogThread.join();
  }

  private void setNodePath(ProcessBuilder pbuilder, NodeInstallationInformation information) {
    final Map<String, String> environment = pbuilder.environment();
    String pathVariableName = PATH_VARIABLE_NAME;
    String pathValue = environment.get(pathVariableName);
    if (Os.isFamily(Os.FAMILY_WINDOWS) || Os.isFamily(Os.FAMILY_WIN9X)) {
      for (String key : environment.keySet()) {
        if (PATH_VARIABLE_NAME.equalsIgnoreCase(key)) {
          pathVariableName = key;
          pathValue = environment.get(key);
        }
      }
    }
    if (pathValue == null) {
      environment.put(pathVariableName, information.getNodeExecutable().getParent());
    }
    else {
      environment.put(pathVariableName, information.getNodeExecutable().getParent() + File.pathSeparator + pathValue);
    }
  }

  /**
   * @param information about the node installation
   * @return {@link List} of commands which will be executed by the task
   * @throws MojoExecutionException
   */
  protected List<String> getCommand(NodeInstallationInformation information) throws MojoExecutionException {
    return null;
  }

  public Log getLog() {
    return log;
  }

  public void setLog(Log log) {
    this.log = log;
  }
}
