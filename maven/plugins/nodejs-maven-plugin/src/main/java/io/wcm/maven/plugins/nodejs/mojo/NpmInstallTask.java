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

import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * wrapper around the npm install command
 */
public class NpmInstallTask extends Task {

  @Parameter
  private String[] arguments;

  @Override
  public Commandline getCommandline(NodeInstallationInformation information) {
    Commandline commandLine = new Commandline();
    commandLine.getShell().setQuotedExecutableEnabled(false);
    commandLine.getShell().setQuotedArgumentsEnabled(false);

    setCommandlineWorkingDirectory(commandLine);

    commandLine.setExecutable(information.getNodeExecutable().getAbsolutePath());
    Arg npm = commandLine.createArg();
    npm.setValue(information.getNpmExecutable().getAbsolutePath());

    Arg install = commandLine.createArg();
    install.setValue("install");

    if (arguments != null) {
      commandLine.addArguments(arguments);
    }

    return commandLine;
  }

  public String[] getArguments() {
    return arguments;
  }

  public void setArguments(String[] arguments) {
    this.arguments = arguments;
  }

}
