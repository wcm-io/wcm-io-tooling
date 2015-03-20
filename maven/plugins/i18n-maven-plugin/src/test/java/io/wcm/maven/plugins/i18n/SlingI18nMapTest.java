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
package io.wcm.maven.plugins.i18n;

import static io.wcm.maven.plugins.i18n.FileUtil.getStringFromClasspath;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.common.collect.ImmutableMap;

public class SlingI18nMapTest {

  private SlingI18nMap underTest;

  @Before
  public void setUp() {
    underTest = new SlingI18nMap("en", ImmutableMap.<String, String>builder()
        .put("key1", "value1")
        .put("key2.key21.key211", "value2")
        .put("key3 with special chars äöüß€", "value3")
        .put("key4", "value4 äöüß€")
        .build());
  }

  @Test
  public void testGetI18nJsonString() throws Exception {
    JSONAssert.assertEquals(getStringFromClasspath("map/i18n-content.json"), underTest.getI18nJsonString(), true);
  }

  @Test
  public void testGetI18nXmlString() throws Exception {
    XMLAssert.assertXMLEqual(getStringFromClasspath("map/i18n-content.xml"), underTest.getI18nXmlString());
  }

}
