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

/**
 * References a content package file for uploading.
 */
public final class PackageFile {

  /**
   * Content package file.
   */
  private File packageFile;

  /**
   * The groupId of the artifact to install.
   */
  private String groupId;

  /**
   * The artifactId of the artifact to install.
   */
  private String artifactId;

  /**
   * The packaging of the artifact to install.
   */
  private String type = "zip";

  /**
   * The version of the artifact to install.
   */
  private String version;

  /**
   * The classifier of the artifact to install.
   */
  private String classifier;

  /**
   * A string of the form <code>groupId:artifactId[:packaging][:classifier]:version</code>.
   */
  private String artifact;

  /**
   * Delay further steps after package installation by this amount of seconds
   */
  private int delayAfterInstallSec;

  /**
   * Force upload and install of content package. If set to false a package is not uploaded or installed
   * if it was already uploaded before.
   */
  private boolean force;

  File getPackageFile() {
    return this.packageFile;
  }

  String getGroupId() {
    return this.groupId;
  }

  String getArtifactId() {
    return this.artifactId;
  }

  String getType() {
    return this.type;
  }

  String getVersion() {
    return this.version;
  }

  String getArtifact() {
    return this.artifact;
  }

  public String getClassifier() {
    return classifier;
  }

  public int getDelayAfterInstallSec() {
    return this.delayAfterInstallSec;
  }

  public boolean isForce() {
    return this.force;
  }

}
