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
   * The name of the content package file to install on the target system.
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
   * A string of the form <code>groupId:artifactId:version[:packaging]</code>.
   */
  private String artifact;

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

}
