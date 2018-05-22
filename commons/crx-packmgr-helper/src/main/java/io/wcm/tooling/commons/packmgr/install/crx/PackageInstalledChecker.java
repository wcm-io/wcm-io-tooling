/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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
package io.wcm.tooling.commons.packmgr.install.crx;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.wcm.tooling.commons.packmgr.PackageManagerException;

/**
 * Check if given package and version is already installed by inspecting the JSON result of
 * <code>/crx/packmgr/list.jsp</code>.
 */
class PackageInstalledChecker {

  private final JSONArray results;

  static final String PACKMGR_LIST_URL = "/crx/packmgr/list.jsp";

  private static final long NOT_FOUND_DATE = -1;
  private static final long NOT_UNPACKED_DATE = 0;

  PackageInstalledChecker(JSONObject result) {
    try {
      this.results = result.getJSONArray("results");
    }
    catch (JSONException ex) {
      throw new PackageManagerException("JSON response from " + PACKMGR_LIST_URL + " does not contain 'results' array.");
    }
  }

  public PackageInstalledStatus getStatus(String group, String name, String version) {
    Map<String, Long> map = getVersionUnpackedDates(group, name);

    if (map.isEmpty()) {
      return PackageInstalledStatus.NOT_FOUND;
    }

    long versionUnpackDate = getVersionUnpackDate(map, version);
    if (versionUnpackDate == NOT_FOUND_DATE) {
      return PackageInstalledStatus.NOT_FOUND;
    }
    if (versionUnpackDate == NOT_UNPACKED_DATE) {
      return PackageInstalledStatus.UPLOADED;
    }

    long lastUnpackDate = getLastUnpackDate(map);
    if (lastUnpackDate > versionUnpackDate) {
      return PackageInstalledStatus.INSTALLED_OTHER_VERSION;
    }
    else {
      return PackageInstalledStatus.INSTALLED;
    }
  }

  private Map<String, Long> getVersionUnpackedDates(String group, String name) {
    Map<String, Long> map = new HashMap<>();
    for (int i = 0; i < results.length(); i++) {
      JSONObject item = results.getJSONObject(i);
      String itemGroup = item.optString("group");
      String itemName = item.optString("name");
      String itemVersion = item.optString("version");
      long itemLastUnpacked = item.optLong("lastUnpacked", NOT_UNPACKED_DATE);
      if (StringUtils.equals(group, itemGroup) && StringUtils.equals(name, itemName) && StringUtils.isNotBlank(itemVersion)) {
        map.put(itemVersion, itemLastUnpacked);
      }
    }
    return map;
  }

  private long getVersionUnpackDate(Map<String, Long> map, String version) {
    Long value = map.get(version);
    if (value == null) {
      return NOT_FOUND_DATE;
    }
    else {
      return value;
    }
  }

  private long getLastUnpackDate(Map<String, Long> map) {
    long last = 0;
    for (Long date : map.values()) {
      if (date > last) {
        last = date;
      }
    }
    return last;
  }

}
