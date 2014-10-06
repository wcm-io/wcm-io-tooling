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
package io.wcm.maven.plugins.contentpackage;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Deploy CQ5/CRX package specified via parameter to CRX instance via CRX Package Manager HTTP interface
 * @goal deploy-file
 * @phase install
 * @requiresProject
 * @requiresDependencyResolution runtime
 * @threadSafe
 */
public class DeployFileMojo extends DeployPackageMojo {

  /**
   * File reference to deploy to CRX instance
   * @parameter
   */
  protected File[] deployFiles;

  /**
   * Generates the ZIP.
   */
  @Override
  public void execute() throws MojoExecutionException {
    if (this.deployFiles != null) {
      for (File deployFile : this.deployFiles) {
        deployFile(deployFile);
      }
    }
  }

}
