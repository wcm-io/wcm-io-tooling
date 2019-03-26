/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PackageInstalledCheckerTest {

  private PackageInstalledChecker underTest;

  @BeforeEach
  void setUp() throws Exception {
    String testResult;
    try (InputStream is = getClass().getResourceAsStream("/packmgr/listResponse.json")) {
      testResult = IOUtils.toString(is);
    }
    JSONObject result = new JSONObject(testResult);
    underTest = new PackageInstalledChecker(result);
  }

  @Test
  void testGetStatus() throws Exception {
    assertEquals(PackageInstalledStatus.NOT_FOUND, underTest.getStatus("invalidgroup", "invalidpackage", "1.0.0"));
    assertEquals(PackageInstalledStatus.INSTALLED, underTest.getStatus("day/cq60/product", "cq-wcm-content", "6.3.214"));
    assertEquals(PackageInstalledStatus.INSTALLED_OTHER_VERSION, underTest.getStatus("Netcentric", "accesscontroltool-package", "2.0.6"));
    assertEquals(PackageInstalledStatus.INSTALLED, underTest.getStatus("Netcentric", "accesscontroltool-package", "2.0.8"));
    assertEquals(PackageInstalledStatus.UPLOADED, underTest.getStatus("Netcentric", "accesscontroltool-package", "2.0.9"));
    assertEquals(PackageInstalledStatus.NOT_FOUND, underTest.getStatus("Netcentric", "accesscontroltool-package", "2.0.99"));
  }

}
