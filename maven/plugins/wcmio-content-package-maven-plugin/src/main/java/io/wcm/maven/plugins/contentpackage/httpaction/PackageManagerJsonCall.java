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
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Call to package manager JSON interface.
 */
public class PackageManagerJsonCall implements HttpCall<JSONObject> {

  private final CloseableHttpClient httpClient;
  private final HttpRequestBase method;
  private final Log log;

  /**
   * @param httpClient HTTP client
   * @param method HTTP method
   * @param log Logger
   */
  public PackageManagerJsonCall(CloseableHttpClient httpClient, HttpRequestBase method, Log log) {
    this.httpClient = httpClient;
    this.method = method;
    this.log = log;
  }

  @Override
  public JSONObject execute() throws MojoExecutionException {
    if (log.isDebugEnabled()) {
      log.debug("Call URL: " + method.getURI());
    }

    try (CloseableHttpResponse response = httpClient.execute(method)) {
      JSONObject jsonResponse = null;

      String responseString = EntityUtils.toString(response.getEntity());
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

        // get response JSON
        if (responseString != null) {
          try {
            jsonResponse = new JSONObject(responseString);
          }
          catch (JSONException ex) {
            throw new MojoExecutionException("Error parsing JSON response.\n" + responseString, ex);
          }
        }
        if (jsonResponse == null) {
          jsonResponse = new JSONObject();
          jsonResponse.put("success", false);
          jsonResponse.put("msg", "Invalid response (null).");
        }

      }
      else {
        jsonResponse = new JSONObject();
        jsonResponse.put("success", false);
        jsonResponse.put("msg", responseString);
      }

      return jsonResponse;
    }
    catch (IOException ex) {
      throw new MojoExecutionException("Http method failed.", ex);
    }
  }

}
