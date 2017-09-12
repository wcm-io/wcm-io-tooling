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
package io.wcm.tooling.commons.packmgr.install.crx;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;

import io.wcm.tooling.commons.packmgr.install.PackageFile;


public class CrxPackageInstallerTest {

  @Test
  public void testIsPackageInstalledDocumentPackageFile() throws Exception {
    PackageFile packageFile = new PackageFile();
    packageFile.setFile(new File("src/test/resources/package/example.zip"));

    PackageFile anotherPackageFile = new PackageFile();
    anotherPackageFile.setFile(new File("src/test/resources/content-package-test.zip"));

    PackageFile noPackageFile = new PackageFile();
    noPackageFile.setFile(new File("src/test/resources/package/no-content-package.zip"));

    Document doc = new SAXBuilder().build(new File("src/test/resources/packmgr/listResponse.xml"));

    assertTrue(CrxPackageInstaller.isPackageInstalled(doc, packageFile));
    assertFalse(CrxPackageInstaller.isPackageInstalled(doc, anotherPackageFile));
    assertFalse(CrxPackageInstaller.isPackageInstalled(doc, noPackageFile));
  }

}
