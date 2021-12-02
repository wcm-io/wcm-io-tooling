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
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerHttpActionException;

/**
 * Get bundle status from web console.
 */
public final class BundleStatusCall implements HttpCall<BundleStatus> {

  private final CloseableHttpClient httpClient;
  private final HttpClientContext context;
  private final String bundleStatusURL;
  private final List<Pattern> bundleStatusWhitelistBundleNames;
  private final Logger log;

  /**
   * @param httpClient HTTP client
   * @param context HTTP client context
   * @param bundleStatusURL Bundle status URL
   * @param bundleStatusWhitelistBundleNames Patterns of bundle names to be ignored
   * @param log Logger
   */
  public BundleStatusCall(CloseableHttpClient httpClient, HttpClientContext context, String bundleStatusURL,
      List<Pattern> bundleStatusWhitelistBundleNames, Logger log) {
    this.httpClient = httpClient;
    this.context = context;
    this.bundleStatusURL = bundleStatusURL;
    this.bundleStatusWhitelistBundleNames = bundleStatusWhitelistBundleNames;
    this.log = log;
  }

  @Override
  public BundleStatus execute() {
    if (log.isDebugEnabled()) {
      log.debug("Call URL: " + bundleStatusURL);
    }

    HttpGet method = new HttpGet(bundleStatusURL);
    try (CloseableHttpResponse response = httpClient.execute(method, context)) {

      String responseString = EntityUtils.toString(response.getEntity());
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw PackageManagerHttpActionException.forHttpError(bundleStatusURL, response.getStatusLine(), responseString);
      }

      return toBundleStatus(responseString);
    }
    catch (IOException ex) {
      throw PackageManagerHttpActionException.forIOException(bundleStatusURL, ex);
    }
  }

  private BundleStatus toBundleStatus(String jsonString) {
    BundleStatusParser parser = new BundleStatusParser(bundleStatusWhitelistBundleNames);
    return parser.parse(jsonString);
  }

}
