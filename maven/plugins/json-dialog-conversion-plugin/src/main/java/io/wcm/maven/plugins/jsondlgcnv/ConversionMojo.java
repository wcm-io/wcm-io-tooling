/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.maven.plugins.jsondlgcnv;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Converts dialog definitions
 */
@Mojo(name = "convert", threadSafe = true)
public class ConversionMojo extends AbstractMojo {

  /**
   * Source path containing Sling-Initial-Content JSON files.
   */
  @Parameter(defaultValue = "${basedir}/src/main/webapp/app-root")
  private String source;

  /**
   * Ruleset for transformation
   */
  @Parameter(defaultValue = "/libs/cq/dialogconversion/rules/coral2")
  private String rules;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    // TODO: implement
  }

}
