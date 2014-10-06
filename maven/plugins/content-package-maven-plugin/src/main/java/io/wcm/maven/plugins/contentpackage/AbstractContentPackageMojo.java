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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

/**
 * Common functionality for all mojors
 */
abstract class AbstractContentPackageMojo extends AbstractMojo {

  /**
   * Name of the generated ZIP.
   * @parameter alias="zipName" expression="${zip.finalName}" default-value="${project.build.finalName}"
   * @required
   */
  private String finalName;

  /**
   * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
   * @parameter
   */
  private String classifier;

  /**
   * Directory containing the generated ZIP.
   * @parameter expression="${project.build.directory}"
   * @required
   */
  private File outputDirectory;

  /**
   * The Maven project.
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  protected MavenProject getProject() {
    return project;
  }

  /**
   * Overload this to produce a jar with another classifier, for example a test-jar.
   */
  protected String getClassifier() {
    return classifier;
  }

  protected File getZipFile() {
    String classifierSuffix = getClassifier();
    if (classifierSuffix == null) {
      classifierSuffix = "";
    }
    else if (classifierSuffix.trim().length() > 0 && !classifierSuffix.startsWith("-")) {
      classifierSuffix = "-" + classifierSuffix;
    }

    return new File(outputDirectory, finalName + classifierSuffix + ".zip");
  }

}
