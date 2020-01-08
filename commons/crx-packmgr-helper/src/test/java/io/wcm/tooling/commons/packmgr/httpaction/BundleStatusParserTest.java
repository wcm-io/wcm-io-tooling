/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.tooling.commons.packmgr.httpaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class BundleStatusParserTest {

  private static final Pattern[] DEFAULT_WHITELIST = new Pattern[] {
      Pattern.compile("^com\\.day\\.crx\\.crxde-support$"),
      Pattern.compile("^com\\.adobe\\.granite\\.crx-explorer$"),
      Pattern.compile("^com\\.adobe\\.granite\\.crxde-lite$")
  };

  @Test
  void testSamplesResolved() throws IOException {
    BundleStatus status = getStatus("/bundlestatus/bundlelist_samples_resolved.json");

    assertEquals("597 total, 590 active, 4 fragment, 3 resolved", status.getStatusLineCompact());
    assertEquals(597, status.getTotal());
    assertEquals(590, status.getActive());
    assertEquals(4, status.getActiveFragment());
    assertEquals(3, status.getResolved());
    assertEquals(0, status.getInstalled());
    assertEquals(0, status.getIgnored());
    assertTrue(status.containsBundle("io.wcm.samples.core"));
    assertTrue(status.containsBundle("com.adobe.granite.crxde-lite"));
    assertEquals("io.wcm.samples.core", status.getMatchingBundle(Pattern.compile("^io\\.wcm\\.samples\\.c.*$")));
    assertFalse(status.isAllBundlesRunning());
  }

  @Test
  void testSamplesResolved_Whitelist() throws IOException {
    BundleStatus status = getStatus("/bundlestatus/bundlelist_samples_resolved.json", DEFAULT_WHITELIST);

    assertEquals("597 total, 590 active, 4 fragment, 1 resolved, 2 ignored", status.getStatusLineCompact());
    assertEquals(597, status.getTotal());
    assertEquals(590, status.getActive());
    assertEquals(4, status.getActiveFragment());
    assertEquals(1, status.getResolved());
    assertEquals(0, status.getInstalled());
    assertEquals(2, status.getIgnored());
    assertTrue(status.containsBundle("io.wcm.samples.core"));
    assertFalse(status.containsBundle("com.adobe.granite.crxde-lite"));
    assertEquals("io.wcm.samples.core", status.getMatchingBundle(Pattern.compile("^io\\.wcm\\.samples\\.c.*$")));
    assertFalse(status.isAllBundlesRunning());
  }

  @Test
  void testSamplesActive_Whitelist() throws IOException {
    BundleStatus status = getStatus("/bundlestatus/bundlelist_samples_active.json", DEFAULT_WHITELIST);

    assertEquals("597 total, 591 active, 4 fragment, 2 ignored", status.getStatusLineCompact());
    assertEquals(597, status.getTotal());
    assertEquals(591, status.getActive());
    assertEquals(4, status.getActiveFragment());
    assertEquals(0, status.getResolved());
    assertEquals(0, status.getInstalled());
    assertEquals(2, status.getIgnored());
    assertTrue(status.containsBundle("io.wcm.samples.core"));
    assertFalse(status.containsBundle("com.adobe.granite.crxde-lite"));
    assertEquals("io.wcm.samples.core", status.getMatchingBundle(Pattern.compile("^io\\.wcm\\.samples\\.c.*$")));
    assertTrue(status.isAllBundlesRunning());
  }

  private BundleStatus getStatus(String jsonFile, Pattern... bundleStatusWhitelistBundleNames) throws IOException {
    try (InputStream is = getClass().getResourceAsStream(jsonFile)) {
      String bundleListJson = IOUtils.toString(is, StandardCharsets.UTF_8);
      BundleStatusParser underTest = new BundleStatusParser(Arrays.asList(bundleStatusWhitelistBundleNames));
      return underTest.parse(bundleListJson);
    }
  }

}
