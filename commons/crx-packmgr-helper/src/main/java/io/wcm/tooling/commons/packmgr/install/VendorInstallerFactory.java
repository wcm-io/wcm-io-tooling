package io.wcm.tooling.commons.packmgr.install;

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.install.composum.ComposumPackageInstaller;
import io.wcm.tooling.commons.packmgr.install.crx.CrxPackageInstaller;

/**
 * This factory provides Package Manager specific handling
 * provided by different vendors like CRX Package Manager
 * and Composum
 */
public class VendorInstallerFactory {

    public static final String COMPOSUM_URL = "/bin/cpm/";
    public static final String CRX_URL = "/crx/packmgr/service";

    public enum Service { crx, composum, unsupported };

    /**
     * Identifies the Service Vendor based on the given URL
     * @param url Base URL to check
     * @return Service Enum found or unsupported
     */
    public static Service identify(String url) {
        Service answer = Service.unsupported;
        int index = url.indexOf(COMPOSUM_URL);
        if(index > 0) {
            answer = Service.composum;
        }
        else {
            index = url.indexOf(CRX_URL);
            if (index > 0) {
                answer = Service.crx;
            }
        }
        return answer;
    }

    /**
     * Returns the Base Url of a given URL with
     * based on its Vendors from the URL
     * @param url Service URL
     * @return Base URL if service vendor was found otherwise the given URL
     */
    public static String getBaseUrl(String url, Logger logger) {
        String answer = url;
        switch(identify(url)) {
            case composum:
                answer = url.substring(0, url.indexOf(COMPOSUM_URL));
                break;
            case crx:
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
    public static VendorPackageInstaller getPackageInstaller(String url)
        throws PackageManagerException
    {
        VendorPackageInstaller answer;
        switch(identify(url)) {
            case composum:
                answer = new ComposumPackageInstaller(url);
                break;
            case crx:
                answer = new CrxPackageInstaller(url);
                break;
            default:
                throw new PackageManagerException("Given URL is not supported: " + url);
        }
        return answer;
    }
}
