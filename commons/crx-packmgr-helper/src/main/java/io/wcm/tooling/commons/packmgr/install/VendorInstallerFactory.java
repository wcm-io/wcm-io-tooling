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

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.install.composum.ComposumPackageInstaller;
import io.wcm.tooling.commons.packmgr.install.crx.CrxPackageInstaller;

/**
 * This factory provides Package Manager specific handling
 * provided by different vendors like CRX Package Manager and Composum.
 */
public final class VendorInstallerFactory {

  /**
   * Base URL for CRX package manager.
   */
  public static final String CRX_URL = "/crx/packmgr/service";

  /**
   * Base URL for componsum package manager.
   */
  public static final String COMPOSUM_URL = "/bin/cpm/";

  /**
   * Pagckage manager services
   */
  public enum Service {

    /**
     * CRX
     */
    CRX,

    /**
     * Composum
     */
    COMPOSUM,

    /**
     * Unsupported
     */
    UNSUPPORTED
  }

  private VendorInstallerFactory() {
    // static methods only
  }

  /**
   * Identifies the Service Vendor based on the given URL
   * @param url Base URL to check
   * @return Service Enum found or unsupported
   */
  public static Service identify(String url) {
    Service answer = Service.UNSUPPORTED;
    int index = url.indexOf(COMPOSUM_URL);
    if (index > 0) {
      answer = Service.COMPOSUM;
    }
    else {
      index = url.indexOf(CRX_URL);
      if (index > 0) {
        answer = Service.CRX;
      }
    }
    return answer;
  }

  /**
   * Returns the Base Url of a given URL with
   * based on its Vendors from the URL
   * @param url Service URL
   * @param logger Logger
   * @return Base URL if service vendor was found otherwise the given URL
   */
  public static String getBaseUrl(String url, Logger logger) {
    String answer = url;
    switch (identify(url)) {
      case COMPOSUM:
        answer = url.substring(0, url.indexOf(COMPOSUM_URL));
        break;
      case CRX:
        answer = url.substring(0, url.indexOf(CRX_URL));
        break;
      default:
        logger.error("Given URL is not supported: " + url);
    }
    return answer;
  }

  /**
   * Provides the Installer of the Service Vendor
   * @param url Base URL of the service
   * @return Installer if URL is supported otherwise null
   */
  public static VendorPackageInstaller getPackageInstaller(String url) throws PackageManagerException {
    VendorPackageInstaller answer;
    switch (identify(url)) {
      case COMPOSUM:
        answer = new ComposumPackageInstaller(url);
        break;
      case CRX:
        answer = new CrxPackageInstaller(url);
        break;
      default:
        throw new PackageManagerException("Given URL is not supported: " + url);
    }
    return answer;
  }

}
