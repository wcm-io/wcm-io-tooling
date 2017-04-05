package io.wcm.tooling.commons.contentpackagebuilder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ContentPackageTest {
  @Test
  public void buildJcrPathForZip() {
    assertEquals("jcr_root/_oak_index", ContentPackage.buildJcrPathForZip("oak:index"));
    assertEquals("jcr_root/etc/content", ContentPackage.buildJcrPathForZip("etc/content"));
    assertEquals("jcr_root/etc/content", ContentPackage.buildJcrPathForZip("/etc/content"));
  }
}
