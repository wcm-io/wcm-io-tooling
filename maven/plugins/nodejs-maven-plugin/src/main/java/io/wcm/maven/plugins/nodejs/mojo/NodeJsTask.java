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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Wrapper around the execution of a nodejs module.
 */
public class NodeJsTask extends Task {

  /**
   * Name of the nodejs module
   */
  @Parameter
  private String moduleName;

  /**
   * Name of the module executable
   */
  @Parameter
  private String executableName;

  /**
   * Version of the module
   */
  @Parameter
  private String moduleVersion;

  /**
   * Task arguments
   */
  @Parameter
  private String[] arguments;

  @Override
  protected List<String> getCommand(NodeInstallationInformation information) throws MojoExecutionException {
    List<String> commands = new ArrayList<>();
    commands.add(information.getNodeExecutable().getAbsolutePath());
    setNodeModule(commands, information);
    if (arguments != null) {
      commands.addAll(Arrays.asList(arguments));
    }

    return commands;
  }

  private void setNodeModule(List<String> commands, NodeInstallationInformation information) throws MojoExecutionException {
    String modulePath = installModule(information);
    String moduleExecutable = getModuleExecutable(modulePath);
    commands.add(moduleExecutable);
  }

  private String installModule(NodeInstallationInformation information) throws MojoExecutionException {
    String modulePath = "";
    String localInstallationPath = workingDirectory.getAbsolutePath() + File.separator + "node_modules" + File.separator + moduleName;
    File localInstallation = new File(localInstallationPath);

    if (!localInstallation.exists()) {
      String globalInstallationPath = information.getBasePath() + "node_modules" + File.separator + moduleName;
      File moduleInstallation = new File(globalInstallationPath);
      if (!moduleInstallation.exists()) {
        NpmInstallTask installTask = new NpmInstallTask();
        installTask.setLog(getLog());
        installTask.setArguments(new String[] {
            "--prefix", information.getBasePath(), moduleName
        });
        installTask.execute(information);
      }

      modulePath = globalInstallationPath;
    }
    else {
      modulePath = localInstallationPath;
    }

    return modulePath;
  }

  private String getModuleExecutable(String modulePath) {
    String executable = executableName == null ? moduleName : executableName;
    return modulePath + File.separator + "bin" + File.separator + executable;
  }

}
