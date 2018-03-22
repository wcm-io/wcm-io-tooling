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
package io.wcm.tooling.commons.packmgr;

import java.util.StringTokenizer;

/**
 * Proxy definition - e.g. for maven proxy settings.
 */
public final class Proxy {

  private final String id;
  private final String protocol;
  private final String host;
  private final int port;
  private final String username;
  private final String password;
  private final String nonProxyHosts;

  /**
   * @param id Proxy identifier
   * @param protocol Protocol
   * @param host Host
   * @param port Port
   * @param username User name
   * @param password Password
   * @param nonProxyHosts List of non-proxy hosts
   */
  public Proxy(String id, String protocol, String host, int port, String username, String password, String nonProxyHosts) {
    this.host = host;
    this.id = id;
    this.protocol = protocol;
    this.port = port;
    this.username = username;
    this.password = password;
    this.nonProxyHosts = nonProxyHosts;
  }

  public String getId() {
    return this.id;
  }

  public String getProtocol() {
    return this.protocol;
  }

  public String getHost() {
    return this.host;
  }

  public int getPort() {
    return this.port;
  }

  public String getUsername() {
    return this.username;
  }

  public String getPassword() {
    return this.password;
  }

  public String getNonProxyHosts() {
    return this.nonProxyHosts;
  }

  boolean useAuthentication() {
    return username != null && !username.isEmpty();
  }

  boolean isNonProxyHost(String givenHost) {
    if (givenHost != null && nonProxyHosts != null && nonProxyHosts.length() > 0) {
      for (StringTokenizer tokenizer = new StringTokenizer(nonProxyHosts, "|"); tokenizer.hasMoreTokens();) {
        String pattern = tokenizer.nextToken();
        pattern = pattern.replace(".", "\\.").replace("*", ".*");
        if (givenHost.matches(pattern)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return id + "{" +
        "protocol='" + protocol + '\'' +
        ", host='" + host + '\'' +
        ", port=" + port +
        (useAuthentication() ? ", with username/passport authentication" : "") +
        '}';
  }

}
