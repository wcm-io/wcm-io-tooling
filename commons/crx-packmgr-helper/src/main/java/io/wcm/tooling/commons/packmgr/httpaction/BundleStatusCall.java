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
package io.wcm.tooling.commons.packmgr.httpaction;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerException;

/**
 * Get bundle status from web console.
 */
public final class BundleStatusCall implements HttpCall<BundleStatus> {

  private final CloseableHttpClient httpClient;
  private final String bundleStatusURL;
  private final Logger log;

  /**
   * @param httpClient HTTP client
   * @param bundleStatusURL Bundle status URL
   * @param log Logger
   */
  public BundleStatusCall(CloseableHttpClient httpClient, String bundleStatusURL, Logger log) {
    this.httpClient = httpClient;
    this.bundleStatusURL = bundleStatusURL;
    this.log = log;
  }

  @Override
  public BundleStatus execute() {
    if (log.isDebugEnabled()) {
      log.debug("Call URL: " + bundleStatusURL);
    }

    HttpGet method = new HttpGet(bundleStatusURL);
    try (CloseableHttpResponse response = httpClient.execute(method)) {

      String responseString = EntityUtils.toString(response.getEntity());
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new PackageManagerException("Failure:\n" + responseString);
      }

      JSONObject jsonResponse = new JSONObject(responseString);
      return toBundleStatus(jsonResponse);
    }
    catch (IOException ex) {
      throw new PackageManagerException("Can't determine bundle state via URL: " + bundleStatusURL, ex);
    }
  }

  private static BundleStatus toBundleStatus(JSONObject response) {
    String statusLine = response.getString("status");
    JSONArray statusArray = response.getJSONArray("s");

    // get bundle stats
    int total = statusArray.getInt(0);
    int active = statusArray.getInt(0);
    int activeFragment = statusArray.getInt(2);
    int resolved = statusArray.getInt(3);
    int installed = statusArray.getInt(4);

    // get list of all bundle names
    Set<String> bundleSymbolicNames = new HashSet<>();
    JSONArray data = response.getJSONArray("data");
    for (int i = 0; i < data.length(); i++) {
      JSONObject item = data.getJSONObject(i);
      String symbolicName = item.optString("symbolicName");
      if (StringUtils.isNotBlank(symbolicName)) {
        bundleSymbolicNames.add(symbolicName);
      }
    }

    return new BundleStatus(
        statusLine,
        total, active, activeFragment, resolved, installed,
        bundleSymbolicNames);
  }

}
