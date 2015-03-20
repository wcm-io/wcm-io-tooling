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
package io.wcm.maven.plugins.i18n.readers;

import static io.wcm.maven.plugins.i18n.FileUtil.getFileFromClasspath;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class I18nReaderTest {

  private static final Map<String, String> EXPECTED_MAP = ImmutableMap.<String, String>builder()
      .put("key1", "value1")
      .put("key21.key22.key23", "value 2")
      .put("key3", "valueäöüß€")
      .build();

  @Test
  public void testProperties() throws Exception {
    File sampleFile = getFileFromClasspath("readers/sampleI18n.properties");
    Map<String, String> result = ImmutableMap.copyOf(new PropertiesI18nReader().read(sampleFile));
    assertEquals(EXPECTED_MAP, result);
  }

  @Test
  public void testXml() throws Exception {
    File sampleFile = getFileFromClasspath("readers/sampleI18n.xml");
    Map<String, String> result = ImmutableMap.copyOf(new XmlI18nReader().read(sampleFile));
    assertEquals(EXPECTED_MAP, result);
  }

  @Test
  public void testJson() throws Exception {
    File sampleFile = getFileFromClasspath("readers/sampleI18n.json");
    Map<String, String> result = ImmutableMap.copyOf(new JsonI18nReader().read(sampleFile));
    assertEquals(EXPECTED_MAP, result);
  }

}
