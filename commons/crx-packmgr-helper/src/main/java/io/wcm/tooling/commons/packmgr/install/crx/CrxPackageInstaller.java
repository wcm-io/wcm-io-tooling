package io.wcm.tooling.commons.packmgr.install.crx;

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import io.wcm.tooling.commons.packmgr.install.PackageFile;
import io.wcm.tooling.commons.packmgr.install.VendorPackageInstaller;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static io.wcm.tooling.commons.packmgr.PackageManagerHelper.CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX;

/**
 * Package Installer for AEM's CRX Package Manager
 */
public class CrxPackageInstaller
    implements VendorPackageInstaller
{
    private String url;

    public CrxPackageInstaller(String url) {
        this.url = url;
    }

    @Override
    public void installPackage(PackageFile packageFile, PackageManagerHelper pkgmgr, CloseableHttpClient httpClient, Logger log)
        throws IOException, PackageManagerException
    {
        // prepare post method
        HttpPost post = new HttpPost(url + "/.json?cmd=upload");
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
            .addBinaryBody("package", packageFile.getFile());
        if (packageFile.isForce()) {
            entityBuilder.addTextBody("force", "true");
        }
        post.setEntity(entityBuilder.build());

        // execute post
        JSONObject jsonResponse = pkgmgr.executePackageManagerMethodJson(httpClient, post);
        boolean success = jsonResponse.optBoolean("success", false);
        String msg = jsonResponse.optString("msg", null);
        String path = jsonResponse.optString("path", null);
        if (success) {
            if(packageFile.isInstall()) {
                log.info("Package uploaded, now installing...");

                try {
                    post = new HttpPost(url + "/console.html" + new URIBuilder().setPath(path).build().getRawPath() + "?cmd=install" + (packageFile.isRecursive() ? "&recursive=true" : ""));
                } catch(URISyntaxException ex) {
                    throw new PackageManagerException("Invalid path: " + path, ex);
                }

                // execute post
                pkgmgr.executePackageManagerMethodHtml(httpClient, post, 0);

                // delay further processing after install (if activated)
                delay(packageFile.getDelayAfterInstallSec(), log);

                // after install: if bundles are still stopping/starting, wait for completion
                pkgmgr.waitForBundlesActivation(httpClient);
            } else {
                log.info("Package uploaded successfully (without installing).");
            }
        }
        else if (StringUtils.startsWith(msg, CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX) && !packageFile.isForce()) {
            log.info("Package skipped because it was already uploaded.");
        }
        else {
            //        throw new PackageManagerException("Package upload failed: " + msg);
            throw new PackageManagerException("Package upload failed: " + msg);
        }

    }

    private void delay(int seconds, Logger log) {
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
