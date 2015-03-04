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
package io.wcm.maven.plugins.contentpackage.httpaction;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Get bundle status from web console.
 */
public class BundleStatusCall implements HttpCall<BundleStatus> {

  private final CloseableHttpClient httpClient;
  private final String bundleStatusURL;
  private final Log log;

  /**
   * @param httpClient HTTP client
   * @param bundleStatusURL Bundle status URL
   * @param log Logger
   */
  public BundleStatusCall(CloseableHttpClient httpClient, String bundleStatusURL, Log log) {
    this.httpClient = httpClient;
    this.bundleStatusURL = bundleStatusURL;
    this.log = log;
  }

  @Override
  public BundleStatus execute() throws MojoExecutionException {
    if (log.isDebugEnabled()) {
      log.debug("Call URL: " + bundleStatusURL);
    }

    HttpGet method = new HttpGet(bundleStatusURL);
    try (CloseableHttpResponse response = httpClient.execute(method)) {

      String responseString = EntityUtils.toString(response.getEntity());
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new MojoExecutionException("Failure:\n" + responseString);
      }

      JSONObject jsonResponse = new JSONObject(responseString);
      return toBundleStatus(jsonResponse);
    }
    catch (IOException ex) {
      throw new MojoExecutionException("Can't determine bundles state via URL: " + bundleStatusURL, ex);
    }
  }

  private static BundleStatus toBundleStatus(JSONObject response) {
    String statusLine = response.getString("status");
    JSONArray statusArray = response.getJSONArray("s");
    return new BundleStatus(
        statusLine,
        statusArray.getInt(0),
        statusArray.getInt(1),
        statusArray.getInt(2),
        statusArray.getInt(3),
        statusArray.getInt(4));
  }

}
