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

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import io.wcm.tooling.commons.packmgr.PackageManagerProperties;

/**
 * Installs a list of AEM content packages via Package Manager.
 */
public final class PackageInstaller {

  private final PackageManagerProperties props;
  private final PackageManagerHelper pkgmgr;

  private boolean replicate;

  private static final Logger log = LoggerFactory.getLogger(PackageInstaller.class);

  /**
   * @param props Package manager configuration properties.
   */
  public PackageInstaller(PackageManagerProperties props) {
    this.props = props;
    this.pkgmgr = new PackageManagerHelper(props);
  }

  /**
   * @param replicate Whether to replicate the package after upload.
   */
  public void setReplicate(boolean replicate) {
    this.replicate = replicate;
  }

  /**
   * Deploy files via package manager.
   * @param packageFiles AEM content packages
   */
  public void installFiles(Collection<PackageFile> packageFiles) {
    try (CloseableHttpClient httpClient = pkgmgr.getHttpClient()) {
      HttpClientContext packageManagerHttpClientContext = pkgmgr.getPackageManagerHttpClientContext();
      HttpClientContext consoleHttpClientContext = pkgmgr.getConsoleHttpClientContext();

      for (PackageFile packageFile : packageFiles) {
        installFile(packageFile, httpClient, packageManagerHttpClientContext, consoleHttpClientContext);
      }
    }
    catch (IOException ex) {
      throw new PackageManagerException("Install operation failed.", ex);
    }
  }

  /**
   * Deploy file via package manager.
   * @param packageFile AEM content package
   */
  public void installFile(PackageFile packageFile) {
    try (CloseableHttpClient httpClient = pkgmgr.getHttpClient()) {
      HttpClientContext packageManagerHttpClientContext = pkgmgr.getPackageManagerHttpClientContext();
      HttpClientContext consoleHttpClientContext = pkgmgr.getConsoleHttpClientContext();

      installFile(packageFile, httpClient, packageManagerHttpClientContext, consoleHttpClientContext);
    }
    catch (IOException ex) {
      throw new PackageManagerException("Install operation failed.", ex);
    }
  }

  private void installFile(PackageFile packageFile, CloseableHttpClient httpClient,
      HttpClientContext packageManagerHttpClientContext, HttpClientContext consoleHttpClientContext) throws IOException {
    File file = packageFile.getFile();
    if (!file.exists()) {
      throw new PackageManagerException("File does not exist: " + file.getAbsolutePath());
    }

    // before install: if bundles are still stopping/starting, wait for completion
    pkgmgr.waitForBundlesActivation(httpClient, consoleHttpClientContext);

    if (packageFile.isInstall()) {
      log.info("Upload and install {}{} to {}", packageFile.isForce() ? "(force) " : "", file.getName(), props.getPackageManagerUrl());
    }
    else {
      log.info("Upload {} to {}", file.getName(), props.getPackageManagerUrl());
    }

    VendorPackageInstaller installer = VendorInstallerFactory.getPackageInstaller(props.getPackageManagerUrl());
    if (installer != null) {
      installer.installPackage(packageFile, replicate,
          pkgmgr, httpClient, packageManagerHttpClientContext, consoleHttpClientContext, props);
    }
  }

}
