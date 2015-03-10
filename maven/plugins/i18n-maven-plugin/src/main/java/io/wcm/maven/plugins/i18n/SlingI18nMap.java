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

import org.apache.commons.lang3.StringUtils;
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

  private static final String SLING_KEY = "sling:key";
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
    for (Entry<String, String> entry : properties.entrySet()) {
      String key = entry.getKey();
      String escapedKey = validName(key);
      JSONObject value = getJsonI18nValue(key, entry.getValue(), !StringUtils.equals(key, escapedKey));

      jsonDocument.put(escapedKey, value);
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

  private JSONObject getJsonI18nValue(String key, String value, boolean generatedKeyProperty) throws JSONException {
    JSONObject valueNode = new JSONObject();

    // add boiler plate
    valueNode.put(JCR_PRIMARY_TYPE, JCR_NODETYPE_FOLDER);
    valueNode.put(JCR_MIXIN_TYPES, SLING_MESSAGE_MIXIN_TYPE);

    // add extra key attribute
    if (generatedKeyProperty) {
      valueNode.put(SLING_KEY, key);
    }

    // add actual i18n value
    valueNode.put(SLING_MESSAGE, value);

    return valueNode;
  }

  /**
   * Creates a valid node name. Replaces all chars not in a-z, A-Z and 0-9 or '_', '.' with '-'.
   * @param value String to be labelized.
   * @return The labelized string.
   */
  private static String validName(String value) {

    // replace some special chars first
    String text = value;
    text = StringUtils.replace(text, "ä", "ae");
    text = StringUtils.replace(text, "ö", "oe");
    text = StringUtils.replace(text, "ü", "ue");
    text = StringUtils.replace(text, "ß", "ss");

    // replace all invalid chars
    StringBuilder sb = new StringBuilder(text);
    for (int i = 0; i < sb.length(); i++) {
      char ch = sb.charAt(i);
      if (!((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')
          || (ch == '_') || (ch == '.'))) {
        ch = '-';
        sb.setCharAt(i, ch);
      }
    }
    return sb.toString();
  }

}
