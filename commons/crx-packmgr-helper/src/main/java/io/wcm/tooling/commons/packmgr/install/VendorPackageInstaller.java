package io.wcm.tooling.commons.packmgr.install;

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

/**
 * Interface any Vendor Package Installer must provide
 */
public interface VendorPackageInstaller {

    /**
     * Install a Package
     * @param packageFile Package to be installed
     * @param pkgmgr Package Manager
     * @param httpClient Http Client used to call the service
     * @param log Logger to report issues
     * @throws IOException If calls to the Web Service fail
     * @throws PackageManagerException If the package installation failed
     */
    public void installPackage(PackageFile packageFile, PackageManagerHelper pkgmgr, CloseableHttpClient httpClient, Logger log)
        throws IOException, PackageManagerException;
}
