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
package io.wcm.tooling.commons.packmgr.install;

import java.io.File;

/**
 * References a content package file for uploading.
 */
public final class PackageFile {

  private File packageFile;
  private int delayAfterInstallSec;
  private boolean install = true;
  private boolean force;
  private boolean recursive = true;

  /**
   * Content package file.
   * @return File
   */
  public File getPackageFile() {
    return this.packageFile;
  }

  public void setPackageFile(File packageFile) {
    this.packageFile = packageFile;
  }

  /**
   * Delay further steps after package installation by this amount of seconds
   * @return Delay time
   */
  public int getDelayAfterInstallSec() {
    return this.delayAfterInstallSec;
  }


  public void setDelayAfterInstallSec(int delayAfterInstallSec) {
    this.delayAfterInstallSec = delayAfterInstallSec;
  }

  /**
   * Whether to install (unpack) the uploaded package automatically or not.
   * @return Install/unpack
   */
  public boolean isInstall() {
    return this.install;
  }

  public void setInstall(boolean install) {
    this.install = install;
  }

  /**
   * Force upload and install of content package. If set to false a package is not uploaded or installed
   * if it was already uploaded before.
   * @return Force
   */
  public boolean isForce() {
    return this.force;
  }

  public void setForce(boolean force) {
    this.force = force;
  }

  /**
   * If set to true nested packages get installed as well.
   * @return Recursive
   */
  public boolean isRecursive() {
    return this.recursive;
  }

  public void setRecursive(boolean recursive) {
    this.recursive = recursive;
  }

}
