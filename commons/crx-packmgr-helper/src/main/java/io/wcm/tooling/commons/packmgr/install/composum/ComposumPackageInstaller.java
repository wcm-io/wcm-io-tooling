package io.wcm.tooling.commons.packmgr.install.composum;

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import io.wcm.tooling.commons.packmgr.install.PackageFile;
import io.wcm.tooling.commons.packmgr.install.VendorPackageInstaller;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

import static io.wcm.tooling.commons.packmgr.PackageManagerHelper.CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX;

/**
 * Created by schaefa on 4/19/17.
 */
public class ComposumPackageInstaller
    implements VendorPackageInstaller
{
    private String url;

    public ComposumPackageInstaller(String url) {
        this.url = url;
    }

    @Override
    public void installPackage(PackageFile packageFile, PackageManagerHelper pkgmgr, CloseableHttpClient httpClient, Logger log)
        throws IOException, PackageManagerException
    {
        // prepare post method
        HttpPost post = new HttpPost(url);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
            .addBinaryBody("file", packageFile.getFile());
        if (packageFile.isForce()) {
            entityBuilder.addTextBody("force", "true");
        }
        post.setEntity(entityBuilder.build());

        // execute post
        String response = pkgmgr.executePackageManagerMethodHtml(httpClient, post);
        boolean success = response.indexOf("<status code=\"200\">ok</status>") > 0;

        if (success) {
            //AS TODO: As of now we do the upload and installation in one step so no futher action needed
        }
        else if (StringUtils.startsWith(response, CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX) && !packageFile.isForce()) {
            log.info("Package skipped because it was already uploaded.");
        }
        else {
            throw new PackageManagerException("Package upload failed: " + response);
        }
    }
}
