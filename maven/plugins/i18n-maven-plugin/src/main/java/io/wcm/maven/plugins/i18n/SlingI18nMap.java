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

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

/**
 * Helper class integrating i18n JSON generation into a sorted map.
 */
public class SlingI18nMap {

  private static final String JCR_LANGUAGE = "jcr:language";
  private static final JSONArray JCR_MIX_LANGUAGE = new JSONArray().put("mix:language");
  private static final String JCR_MIXIN_TYPES = "jcr:mixinTypes";
  private static final String JCR_NODETYPE_FOLDER = "nt:folder";
  private static final String JCR_PRIMARY_TYPE = "jcr:primaryType";

  private static final String SLING_MESSAGE = "sling:message";
  private static final JSONArray SLING_MESSAGE_MIXIN_TYPE = new JSONArray().put("sling:Message");

  private String languageKey;
  private final SortedMap<String, String> properties;

  /**
   * @param languageKey Language key
   */
  public SlingI18nMap(String languageKey, Map<String, String> properties) {
    this.languageKey = languageKey;
    this.properties = new TreeMap<>(properties);
  }

  /**
   * Build i18n resource JSON in Sling i18n Message format.
   * @return JSON
   * @throws JSONException
   */
  public String getI18nJsonString() throws JSONException {
    return getI18nJson().toString(2);
  }

  private JSONObject getI18nJson() throws JSONException {
    return buildI18nJson();
  }

  private JSONObject buildI18nJson() throws JSONException {

    // get root
    JSONObject jsonDocument = getMixLanguageJsonDocument();

    // add entries
    for (Entry<String, String> i18nEntry : properties.entrySet()) {
      String key = i18nEntry.getKey();
      JSONObject value = getJsonI18nValue(i18nEntry);

      jsonDocument.put(key, value);
    }

    // return result
    return jsonDocument;
  }

  private JSONObject getMixLanguageJsonDocument() throws JSONException {
    JSONObject root = new JSONObject();

    // add boiler plate
    root.put(JCR_PRIMARY_TYPE, JCR_NODETYPE_FOLDER);
    root.put(JCR_MIXIN_TYPES, JCR_MIX_LANGUAGE);

    // add language
    root.put(JCR_LANGUAGE, languageKey);

    return root;
  }

  private JSONObject getJsonI18nValue(Entry<String, String> i18nEntry) throws JSONException {
    JSONObject i18nValueAsJson = new JSONObject();

    // add boiler plate
    i18nValueAsJson.put(JCR_PRIMARY_TYPE, JCR_NODETYPE_FOLDER);
    i18nValueAsJson.put(JCR_MIXIN_TYPES, SLING_MESSAGE_MIXIN_TYPE);

    // add actual i18n value
    i18nValueAsJson.put(SLING_MESSAGE, i18nEntry.getValue());

    return i18nValueAsJson;
  }

}
