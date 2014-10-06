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
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * General task implementation.
 */
public class Task {

  private static final Pattern ERROR_LOG_PATTERN = Pattern.compile(".*(ERROR|FAILED|ERR|npm error).*");
  private static final Pattern WARNING_LOG_PATTERN = Pattern.compile(".*(warn).*", Pattern.CASE_INSENSITIVE);

  /**
   * Directory in which the should be executed.
   */
  @Parameter
  protected File workingDirectory;

  private Log log;

  /**
   * Executes the command line returned by {@link Task#getCommandline(NodeInstallationInformation)}.
   * @param information
   * @throws MojoExecutionException
   */
  public void execute(NodeInstallationInformation information) throws MojoExecutionException {
    StreamConsumer customNodejsLogger = new CustomNodeJsLogStreamConsumer(getLog());
    Commandline commandLine = getCommandline(information);
    getLog().info("Executing command: " + commandLine.toString());
    int exitCode;
    try {
      exitCode = CommandLineUtils.executeCommandLine(commandLine, customNodejsLogger, customNodejsLogger);
    }
    catch (CommandLineException ex) {
      getLog().error("Command Line Exception", ex);
      throw new MojoExecutionException("Command execution failed.", ex);
    }
    if (exitCode != 0) {
      throw new MojoExecutionException("Result of " + commandLine + " execution is: '" + exitCode + "'.");
    }

  }

  protected void setCommandlineWorkingDirectory(Commandline commandLine) {
    if (workingDirectory != null) {
      if (!workingDirectory.exists()) {
        workingDirectory.mkdirs();
      }
      commandLine.setWorkingDirectory(workingDirectory.getAbsolutePath());
    }
  }

  /**
   * @param information about the node installation
   * @return {@link Commandline} which will be executed by the task
   * @throws MojoExecutionException
   */
  protected Commandline getCommandline(NodeInstallationInformation information) throws MojoExecutionException {
    return null;
  }

  public Log getLog() {
    return log;
  }

  public void setLog(Log log) {
    this.log = log;
  }

  private static class CustomNodeJsLogStreamConsumer implements StreamConsumer {

    protected Log logger;

    public CustomNodeJsLogStreamConsumer(Log logger) {
      this.logger = logger;
    }

    @Override
    public void consumeLine(String pLine) {
      if (ERROR_LOG_PATTERN.matcher(pLine).matches()) {
        logger.error(pLine);
      }
      else if (WARNING_LOG_PATTERN.matcher(pLine).matches()) {
        logger.warn(pLine);
      }
      else {
        logger.info(pLine);
      }
    }
  }
}
