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
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.wcm.tooling.commons.packmgr.util.ContentPackageProperties;

/**
 * References a content package file for uploading.
 */
public final class PackageFile {

  private File file;
  private int delayAfterInstallSec;
  private boolean install = true;
  private Boolean force;
  private boolean recursive = true;

  private static final int DEFAULT_DELAY_AFTER_CONTAINER_PACKAGE_SEC = 3;

  /**
   * Content package file.
   * @return File
   */
  public File getFile() {
    return this.file;
  }

  public void setFile(File file) {
    this.file = file;
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
   * If not delay was configured try to detect a sensible default value:
   * A few secs for container/mixed packages, 0 sec for others.
   */
  public void setDelayAfterInstallSecAutoDetect() {
    try {
      Map<String, Object> props = ContentPackageProperties.get(file);
      String packageType = StringUtils.defaultString((String)props.get("packageType"), "content");
      if (StringUtils.equals(packageType, "container") || StringUtils.equals(packageType, "mixed")) {
        this.delayAfterInstallSec = DEFAULT_DELAY_AFTER_CONTAINER_PACKAGE_SEC;
      }
    }
    catch (IOException ex) {
      // ignore
    }
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
    // if no force parameter was set auto-detect best-matching force mode from file name
    if (this.force == null) {
      return StringUtils.contains(file.getName(), "-SNAPSHOT");
    }
    return this.force;
  }

  public void setForce(Boolean force) {
    this.force = force;
  }

  // keep signature for backwards compatibility
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
