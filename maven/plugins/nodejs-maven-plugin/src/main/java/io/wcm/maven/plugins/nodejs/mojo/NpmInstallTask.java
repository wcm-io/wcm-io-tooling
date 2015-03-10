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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * wrapper around the npm install command
 */
public class NpmInstallTask extends Task {

  @Parameter
  private String[] arguments;

  @Override
  protected List<String> getCommand(NodeInstallationInformation information) {
    List<String> commands = new ArrayList<>();
    String nodeExecutable = information.getNodeExecutable().getAbsolutePath();
    String npmExecutable = information.getNpmExecutable().getAbsolutePath();
    commands.add(nodeExecutable);
    commands.add(npmExecutable);
    commands.add("install");
    if (arguments != null && arguments.length > 0) {
      commands.addAll(Arrays.asList(arguments));
    }
    return commands;
  }

  public String[] getArguments() {
    return arguments;
  }

  public void setArguments(String[] arguments) {
    this.arguments = arguments;
  }

}
