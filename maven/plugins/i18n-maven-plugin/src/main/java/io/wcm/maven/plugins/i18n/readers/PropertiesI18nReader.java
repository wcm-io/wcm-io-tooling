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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.CharEncoding;

/**
 * Reads i18n resources from Java properties files.
 */
public class PropertiesI18nReader implements I18nReader {

  @Override
  public Map<String, String> read(File sourceFile) throws IOException {
    // read properties
    Properties props = new Properties();
    try (FileInputStream is = new FileInputStream(sourceFile);
        InputStreamReader reader = new InputStreamReader(is, CharEncoding.UTF_8)) {
      props.load(reader);
    }

    // convert to map
    Map<String, String> map = new HashMap<>();
    for (Map.Entry<Object, Object> entry : props.entrySet()) {
      map.put(entry.getKey().toString(), entry.getValue().toString());
    }
    return map;
  }

}
