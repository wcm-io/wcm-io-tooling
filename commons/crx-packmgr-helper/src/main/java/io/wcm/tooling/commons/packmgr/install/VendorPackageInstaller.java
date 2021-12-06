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

import java.io.IOException;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import io.wcm.tooling.commons.packmgr.PackageManagerProperties;

/**
 * Interface any Vendor Package Installer must provide
 */
public interface VendorPackageInstaller {

  /**
   * Install a Package
   * @param packageFile Package to be installed
   * @param pkgmgr Package Manager
   * @param httpClient Http Client
   * @param packageManagerHttpClientContext Http Client context used to call the package manager
   * @param consoleHttpClientContext Http Client context used to call the Felix console
   * @param props Package manager properties
   * @param log Logger to report issues
   * @throws IOException If calls to the Web Service fail
   * @throws PackageManagerException If the package installation failed
   */
  void installPackage(PackageFile packageFile, PackageManagerHelper pkgmgr,
      CloseableHttpClient httpClient, HttpClientContext packageManagerHttpClientContext, HttpClientContext consoleHttpClientContext,
      PackageManagerProperties props, Logger log) throws IOException, PackageManagerException;

}
