package io.wcm.tooling.commons.packmgr.install;

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

/**
 * Created by schaefa on 4/19/17.
 */
public interface VendorPackageInstaller {

    public void installPackage(PackageFile packageFile, PackageManagerHelper pkgmgr, CloseableHttpClient httpClient, Logger log)
        throws IOException, PackageManagerException;
}
