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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

/**
 * Reads i18n resources from JSON files.
 */
public class JsonI18nReader implements I18nReader {

  @Override
  public Map<String, String> read(File sourceFile) throws IOException {
    String fileContent = IOUtils.toString(sourceFile.toURI().toURL(), CharEncoding.UTF_8);
    try {
      JSONObject root = new JSONObject(fileContent);
      Map<String, String> map = new HashMap<String, String>();
      parseJson(root, map, "");
      return map;
    }
    catch (JSONException ex) {
      throw new IOException("Unable to read JSON from " + sourceFile.getAbsolutePath(), ex);
    }
  }

  private void parseJson(JSONObject node, Map<String, String> map, String prefix) throws IOException, JSONException {
    JSONArray names = node.names();
    if (names == null) {
      return;
    }
    for (int i = 0; i < names.length(); i++) {
      String key = names.getString(i);
      Object item = node.get(key);
      if (item instanceof JSONObject) {
        parseJson((JSONObject)item, map, prefix + key + ".");
      }
      else if (item instanceof String) {
        map.put(prefix + key, (String)item);
      }
      else {
        throw new IOException("Unsupported JSON value: " + item.getClass().getName());
      }
    }
  }

}
