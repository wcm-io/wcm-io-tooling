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

import static io.wcm.maven.plugins.nodejs.installation.NodeInstallationInformation.TYPE_ZIP;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;

import io.wcm.maven.plugins.nodejs.mojo.Task;

/**
 * Task to for extracting gzipped tar archives
 */
public class NodeUnarchiveTask extends Task {

  protected String nodeJsDirectory;

  /**
   * @param nodeJsDirectory nodejs directory
   */
  public NodeUnarchiveTask(String nodeJsDirectory) {
    this.nodeJsDirectory = nodeJsDirectory;
  }


  @Override
  public void execute(NodeInstallationInformation information) throws MojoExecutionException {
    File archive = information.getArchive();
    if (StringUtils.endsWith(archive.getName(), "." + TYPE_ZIP)) {
      ZipUnArchiver unArchiver = new ZipUnArchiver(archive);
      unArchiver.unarchive(nodeJsDirectory);
    }
    else {
      TarUnArchiver unArchiver = new TarUnArchiver(archive);
      unArchiver.unarchive(nodeJsDirectory);
    }
  }

}
