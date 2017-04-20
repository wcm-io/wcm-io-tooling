package io.wcm.tooling.commons.packmgr.install;

import io.wcm.tooling.commons.packmgr.install.composum.ComposumPackageInstaller;
import io.wcm.tooling.commons.packmgr.install.crx.CrxPackageInstaller;

/**
 * Created by schaefa on 4/19/17.
 */
public class VendorInstallerFactory {

    public static VendorPackageInstaller getPackageInstaller(String url) {
        if(url != null) {
            if(url.indexOf("/bin/cpm/package.service") >= 0) {
                return new ComposumPackageInstaller(url);
            } else {
                return new CrxPackageInstaller(url);
            }
        } else {
            //AS TODO log error or throw exception
            return null;
        }
    }
}
