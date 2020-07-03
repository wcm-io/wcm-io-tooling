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
import static io.wcm.tooling.commons.packmgr.util.XmlUnitUtil.assertXpathEvaluatesTo;
import static io.wcm.tooling.commons.packmgr.util.XmlUnitUtil.assertXpathExists;
import static io.wcm.tooling.commons.packmgr.util.XmlUnitUtil.assertXpathNotExists;
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

    assertXpathExists("/jcr:root",
        new File(outputDirectory, "jcr_root/content/adaptto/sample/en/.content.xml"));
  }

  @Test
  void testUnpack_MarkReplicationActivated() throws Exception {
    File contentPackage = new File("src/test/resources/content-package-test.zip");
    File outputDirectory = new File("target/unpacktest-MarkReplicationActivated");
    outputDirectory.mkdirs();

    props.setMarkReplicationActivated(true);

    underTest = new ContentUnpacker(props);
    underTest.unpack(contentPackage, outputDirectory);

    assertNoReplicationAction(outputDirectory, "jcr_root/.content.xml");
    assertNoReplicationAction(outputDirectory, "jcr_root/content/.content.xml");
    assertReplicationActionActivate(outputDirectory, "jcr_root/content/adaptto/.content.xml");
    assertReplicationActionActivate(outputDirectory, "jcr_root/content/adaptto/sample/.content.xml");
    assertReplicationActionActivate(outputDirectory, "jcr_root/content/adaptto/sample/en/.content.xml");
    assertReplicationActionActivate(outputDirectory, "jcr_root/content/adaptto/sample/en/schedule/.content.xml");
    assertReplicationActionActivate(outputDirectory, "jcr_root/content/adaptto/sample/en/schedule/oak--an-introduction-for-users/.content.xml");
  }

  @Test
  void testUnpack_MarkReplicationActivated_IncludeNodes() throws Exception {
    File contentPackage = new File("src/test/resources/content-package-test.zip");
    File outputDirectory = new File("target/unpacktest-MarkReplicationActivated_IncludeNodes");
    outputDirectory.mkdirs();

    props.setMarkReplicationActivated(true);
    props.setMarkReplicationActivatedIncludeNodes(new String[] {
        "^/content/adaptto/sample/en/jcr:content$",
        "^/content/adaptto/sample/en/schedule/[^/]+/jcr:content$",
    });

    underTest = new ContentUnpacker(props);
    underTest.unpack(contentPackage, outputDirectory);

    assertNoReplicationAction(outputDirectory, "jcr_root/.content.xml");
    assertNoReplicationAction(outputDirectory, "jcr_root/content/.content.xml");
    assertNoReplicationAction(outputDirectory, "jcr_root/content/adaptto/.content.xml");
    assertNoReplicationAction(outputDirectory, "jcr_root/content/adaptto/sample/.content.xml");
    assertReplicationActionActivate(outputDirectory, "jcr_root/content/adaptto/sample/en/.content.xml");
    assertNoReplicationAction(outputDirectory, "jcr_root/content/adaptto/sample/en/schedule/.content.xml");
    assertReplicationActionActivate(outputDirectory, "jcr_root/content/adaptto/sample/en/schedule/oak--an-introduction-for-users/.content.xml");
  }

  @Test
  public void testGetNamespacePrefix() {
    assertNull(getNamespacePrefix("aaa"));
    assertNull(getNamespacePrefix("aaa/bbb"));
    assertNull(getNamespacePrefix("aaa/_cq_bbb"));
    assertEquals("cq", getNamespacePrefix("aaa/_cq_bbb/.content.xml"));
  }

  private void assertReplicationActionActivate(File outputDirectory, String filePath) {
    assertXpathEvaluatesTo("Activate", "/jcr:root/jcr:content/@cq:lastReplicationAction",
        new File(outputDirectory, filePath));
  }

  private void assertNoReplicationAction(File outputDirectory, String filePath) {
    assertXpathNotExists("/jcr:root/jcr:content/@cq:lastReplicationAction",
        new File(outputDirectory, filePath));
  }

}
