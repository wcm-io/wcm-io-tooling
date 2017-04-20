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
package io.wcm.tooling.commons.packmgr.install;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.http.impl.client.CloseableHttpClient;

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import io.wcm.tooling.commons.packmgr.PackageManagerProperties;

/**
 * Installs a list of AEM content packages via Package Manager.
 */
public final class PackageInstaller {

  private final PackageManagerProperties props;
  private final PackageManagerHelper pkgmgr;
  private final Logger log;

  /**
   * @param props Package manager configuration properties.
   * @param log Logger
   */
  public PackageInstaller(PackageManagerProperties props, Logger log) {
    this.props = props;
    this.pkgmgr = new PackageManagerHelper(props, log);
    this.log = log;
  }

  /**
   * Deploy files via package manager.
   * @param packageFiles AEM content packages
   */
  public void installFiles(Collection<PackageFile> packageFiles) {
    for (PackageFile packageFile : packageFiles) {
      installFile(packageFile);
    }
  }

  /**
   * Deploy file via package manager.
   * @param packageFile AEM content package
   */
  public void installFile(PackageFile packageFile) {
    File file = packageFile.getFile();
    if (!file.exists()) {
      throw new PackageManagerException("File does not exist: " + file.getAbsolutePath());
    }

    try (CloseableHttpClient httpClient = pkgmgr.getHttpClient()) {

      // before install: if bundles are still stopping/starting, wait for completion
      pkgmgr.waitForBundlesActivation(httpClient);

      if (packageFile.isInstall()) {
        log.info("Upload and install " + file.getName() + " to " + props.getPackageManagerUrl());
      }
      else {
        log.info("Upload " + file.getName() + " to " + props.getPackageManagerUrl());
      }

      VendorPackageInstaller installer = VendorInstallerFactory.getPackageInstaller(props.getPackageManagerUrl());
      installer.installPackage(packageFile, pkgmgr, httpClient, log);
    }
    catch (IOException ex) {
      throw new PackageManagerException("Install operation failed.", ex);
    }
  }

  private void delay(int seconds) {
    if (seconds > 0) {
      log.info("Wait for " + seconds + " seconds after package install...");
      try {
        Thread.sleep(seconds * 1000);
      }
      catch (InterruptedException ex) {
        // ignore
      }
    }
  }

}
