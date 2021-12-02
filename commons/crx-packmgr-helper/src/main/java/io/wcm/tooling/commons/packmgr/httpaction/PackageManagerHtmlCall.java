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

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import io.wcm.tooling.commons.packmgr.Logger;
import io.wcm.tooling.commons.packmgr.PackageManagerHttpActionException;

/**
 * Call that parses a packager manager HTML response and returns the contained message as plain text.
 */
public final class PackageManagerHtmlCall implements HttpCall<String> {

  private final CloseableHttpClient httpClient;
  private final HttpClientContext context;
  private final HttpRequestBase method;
  private final Logger log;

  /**
   * @param httpClient HTTP client
   * @param context HTTP client context
   * @param method HTTP method
   * @param log Logger
   */
  public PackageManagerHtmlCall(CloseableHttpClient httpClient, HttpClientContext context, HttpRequestBase method, Logger log) {
    this.httpClient = httpClient;
    this.context = context;
    this.method = method;
    this.log = log;
  }

  @Override
  public String execute() {
    if (log.isDebugEnabled()) {
      log.debug("Call URL: " + method.getURI());
    }

    try (CloseableHttpResponse response = httpClient.execute(method, context)) {
      String responseString = EntityUtils.toString(response.getEntity());

      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

        // debug output whole xml
        if (log.isDebugEnabled()) {
          log.debug("CRX Package Manager Response:\n" + responseString);
        }

        return responseString;
      }
      else {
        throw PackageManagerHttpActionException.forHttpError(method.getURI().toString(), response.getStatusLine(), responseString);
      }

    }
    catch (IOException ex) {
      throw PackageManagerHttpActionException.forIOException(method.getURI().toString(), ex);
    }
  }

}
