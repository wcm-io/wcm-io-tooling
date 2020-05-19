/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.tooling.commons.packmgr.unpack;

import static io.wcm.tooling.commons.packmgr.unpack.ContentUnpacker.getNamespacePrefix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContentUnpackerTest {

  private static final String[] EXCLUDE_FILES = new String[] {
    ".*/sling-ide-tooling/.*",
    "^META-INF/.*"
  };
  private static final String[] EXCLUDE_NODES = new String[] {
    "^.*/scheduleday_0$"
  };
  private static final String[] EXCLUDE_PROPERTIES = new String[] {
    "jcr\\:created",
    "jcr\\:createdBy",
    "jcr\\:lastModified",
    "jcr\\:lastModifiedBy",
    "cq\\:lastModified",
    "cq\\:lastModifiedBy"
  };

  private ContentUnpackerProperties props;
  private ContentUnpacker underTest;

  @BeforeEach
  void setUp() throws Exception {
    props = new ContentUnpackerProperties();
    props.setExcludeFiles(EXCLUDE_FILES);
    props.setExcludeNodes(EXCLUDE_NODES);
    props.setExcludeProperties(EXCLUDE_PROPERTIES);
  }

  @Test
  void testUnpack() throws Exception {
    File contentPackage = new File("src/test/resources/content-package-test.zip");
    File outputDirectory = new File("target/unpacktest");
    outputDirectory.mkdirs();

    underTest = new ContentUnpacker(props);
    underTest.unpack(contentPackage, outputDirectory);
  }

  @Test
  void testUnpack_MarkReplicationActivated() throws Exception {
    File contentPackage = new File("src/test/resources/content-package-test.zip");
    File outputDirectory = new File("target/unpacktest-MarkReplicationActivated");
    outputDirectory.mkdirs();

    props.setMarkReplicationActivated(true);
    underTest = new ContentUnpacker(props);
    underTest.unpack(contentPackage, outputDirectory);
  }

  @Test
  public void testGetNamespacePrefix() {
    assertNull(getNamespacePrefix("aaa"));
    assertNull(getNamespacePrefix("aaa/bbb"));
    assertNull(getNamespacePrefix("aaa/_cq_bbb"));
    assertEquals("cq", getNamespacePrefix("aaa/_cq_bbb/.content.xml"));
  }

}
