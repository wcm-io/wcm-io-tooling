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
package io.wcm.tooling.commons.packmgr.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.junit.Test;


public class ContentPackagePropertiesTest {

  @Test
  public void testContentPackage() throws Exception {
    File packageFile = new File("src/test/resources/package/example.zip");
    Map<String, Object> props = ContentPackageProperties.get(packageFile);

    assertEquals("mapping-sample", props.get("name"));
    assertEquals(false, props.get("requiresRoot"));
    assertEquals(2, props.get("packageFormatVersion"));
  }

  @Test
  public void testNoneContentPackage() throws Exception {
    File packageFile = new File("src/test/resources/package/no-content-package.zip");
    Map<String, Object> props = ContentPackageProperties.get(packageFile);
    assertTrue(props.isEmpty());
  }

}
