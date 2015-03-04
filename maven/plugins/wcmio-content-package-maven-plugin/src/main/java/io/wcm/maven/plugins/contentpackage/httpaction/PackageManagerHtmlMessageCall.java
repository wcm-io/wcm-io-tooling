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
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Call that parses a packager manager HTML response and returns the contained message as plain text.
 */
public class PackageManagerHtmlMessageCall implements HttpCall<String> {

  private final CloseableHttpClient httpClient;
  private final HttpRequestBase method;
  private final Log log;

  /**
   * @param httpClient HTTP client
   * @param method HTTP method
   * @param log Logger
   */
  public PackageManagerHtmlMessageCall(CloseableHttpClient httpClient, HttpRequestBase method, Log log) {
    this.httpClient = httpClient;
    this.method = method;
    this.log = log;
  }

  @Override
  public String execute() throws MojoExecutionException {
    if (log.isDebugEnabled()) {
      log.debug("Call URL: " + method.getURI());
    }

    try (CloseableHttpResponse response = httpClient.execute(method)) {
      String responseString = EntityUtils.toString(response.getEntity());

      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

        // debug output whole xml
        if (log.isDebugEnabled()) {
          log.debug("CRX Package Manager Response:\n" + responseString);
        }

        // remove all HTML tags and special conctent
        final Pattern HTML_STYLE = Pattern.compile("<style[^<>]*>[^<>]*</style>", Pattern.MULTILINE | Pattern.DOTALL);
        final Pattern HTML_JAVASCRIPT = Pattern.compile("<script[^<>]*>[^<>]*</script>", Pattern.MULTILINE | Pattern.DOTALL);
        final Pattern HTML_ANYTAG = Pattern.compile("<[^<>]*>");

        responseString = HTML_STYLE.matcher(responseString).replaceAll("");
        responseString = HTML_JAVASCRIPT.matcher(responseString).replaceAll("");
        responseString = HTML_ANYTAG.matcher(responseString).replaceAll("");
        responseString = StringUtils.replace(responseString, "&nbsp;", " ");

        return responseString;
      }
      else {
        throw new MojoExecutionException("Failure:\n" + responseString);
      }

    }
    catch (IOException ex) {
      throw new MojoExecutionException("Http method failed.", ex);
    }
  }

}
