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
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHttpActionException;
import io.wcm.tooling.commons.packmgr.PackageManagerProperties;

/**
 * Call that parses a packager manager HTML response and returns the contained message as plain text.
 */
public final class PackageManagerHtmlMessageCall implements HttpCall<String> {

  private final CloseableHttpClient httpClient;
  private final HttpClientContext context;
  private final HttpRequestBase method;
  private final PackageManagerProperties props;

  private static final String PACKAGE_MANAGER_ERROR_INDICATION = "Error during processing.";
  private static final Logger log = LoggerFactory.getLogger(PackageManagerHtmlMessageCall.class);

  private static final Pattern HTML_STYLE = Pattern.compile("<style[^<>]*>[^<>]*</style>", Pattern.MULTILINE | Pattern.DOTALL);
  private static final Pattern HTML_JAVASCRIPT = Pattern.compile("<script[^<>]*>[^<>]*</script>", Pattern.MULTILINE | Pattern.DOTALL);
  private static final Pattern HTML_ANYTAG = Pattern.compile("<[^<>]*>");

  /**
   * @param httpClient HTTP client
   * @param context HTTP client context
   * @param method HTTP method
   * @param props Package manager properties
   */
  public PackageManagerHtmlMessageCall(CloseableHttpClient httpClient, HttpClientContext context, HttpRequestBase method,
      PackageManagerProperties props) {
    this.httpClient = httpClient;
    this.context = context;
    this.method = method;
    this.props = props;
  }

  @Override
  public String execute() {
    log.debug("Call URL: {}", method.getURI());

    try (CloseableHttpResponse response = httpClient.execute(method, context)) {
      String responseString = EntityUtils.toString(response.getEntity());

      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

        // debug output whole xml
        log.trace("CRX Package Manager Response:\n{}", responseString);

        // remove all HTML tags and special conctent
        responseString = HTML_STYLE.matcher(responseString).replaceAll("");
        responseString = HTML_JAVASCRIPT.matcher(responseString).replaceAll("");
        responseString = HTML_ANYTAG.matcher(responseString).replaceAll("");
        responseString = StringUtils.replace(responseString, "&nbsp;", " ");

        if (StringUtils.equalsIgnoreCase(props.getPackageManagerOutputLogLevel(), "debug")) {
          log.debug(responseString);
        }
        else {
          log.info(responseString);
        }

        if (StringUtils.contains(responseString, PACKAGE_MANAGER_ERROR_INDICATION)) {
          throw new PackageManagerException("Package installation failed: " + PACKAGE_MANAGER_ERROR_INDICATION + "\n"
              + method.getURI());
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
