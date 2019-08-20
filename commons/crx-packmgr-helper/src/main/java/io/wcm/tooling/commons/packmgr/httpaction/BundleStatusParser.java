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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Parses bundle status JSON response.
 */
final class BundleStatusParser {

  private final List<Pattern> bundleStatusWhitelistBundleNames;

  BundleStatusParser(List<Pattern> bundleStatusWhitelistBundleNames) {
    this.bundleStatusWhitelistBundleNames = bundleStatusWhitelistBundleNames;
  }

  BundleStatus parse(String jsonString) {
    JSONObject json = new JSONObject(jsonString);

    String statusLine = json.getString("status");

    // get bundle stats
    int total = 0;
    int active = 0;
    int activeFragment = 0;
    int resolved = 0;
    int installed = 0;
    int ignored = 0;

    // get list of all bundle names
    Set<String> bundleSymbolicNames = new HashSet<>();
    JSONArray data = json.getJSONArray("data");
    for (int i = 0; i < data.length(); i++) {
      JSONObject item = data.getJSONObject(i);

      String symbolicName = item.optString("symbolicName");
      String state = item.optString("state");
      boolean fragment = item.optBoolean("fragment");
      boolean whitelisted = isWhitelisted(symbolicName);

      total++;
      if (fragment) {
        activeFragment++;
      }
      else if (isActive(state)) {
        active++;
      }
      else if (isResolved(state)) {
        if (whitelisted) {
          ignored++;
        }
        else {
          resolved++;
        }
      }
      else if (isInstalled(state)) {
        if (whitelisted) {
          ignored++;
        }
        else {
          installed++;
        }
      }

      if (StringUtils.isNotBlank(symbolicName) && !whitelisted) {
        bundleSymbolicNames.add(symbolicName);
      }
    }

    return new BundleStatus(
        statusLine,
        total, active, activeFragment, resolved, installed, ignored,
        bundleSymbolicNames);
  }

  private boolean isActive(String actual) {
    return StringUtils.equalsIgnoreCase(actual, "Active");
  }

  private boolean isResolved(String actual) {
    return StringUtils.equalsIgnoreCase(actual, "Resolved");
  }

  private boolean isInstalled(String actual) {
    return StringUtils.equalsIgnoreCase(actual, "Installed");
  }

  private boolean isWhitelisted(String symbolicName) {
    for (Pattern pattern : bundleStatusWhitelistBundleNames) {
      if (pattern.matcher(symbolicName).matches()) {
        return true;
      }
    }
    return false;
  }

}
