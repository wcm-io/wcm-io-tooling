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
package io.wcm.maven.plugins.contentpackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;

/**
 * Read maven proxy settings.
 */
final class ProxySupport {

  private ProxySupport() {
    // static methods only
  }

  static List<io.wcm.tooling.commons.packmgr.Proxy> getMavenProxies(MavenSession mavenSession, SettingsDecrypter decrypter) {
    if (mavenSession == null ||
        mavenSession.getSettings() == null ||
        mavenSession.getSettings().getProxies() == null ||
        mavenSession.getSettings().getProxies().isEmpty()) {
      return Collections.emptyList();
    }
    else {
      final List<Proxy> mavenProxies = mavenSession.getSettings().getProxies();

      final List<io.wcm.tooling.commons.packmgr.Proxy> proxies = new ArrayList<>(mavenProxies.size());

      for (Proxy mavenProxy : mavenProxies) {
        if (mavenProxy.isActive()) {
          Proxy decryptedMavenProxy = decryptProxy(mavenProxy, decrypter);
          proxies.add(new io.wcm.tooling.commons.packmgr.Proxy(decryptedMavenProxy.getId(),
              decryptedMavenProxy.getProtocol(),
              decryptedMavenProxy.getHost(),
              decryptedMavenProxy.getPort(),
              decryptedMavenProxy.getUsername(),
              decryptedMavenProxy.getPassword(),
              decryptedMavenProxy.getNonProxyHosts()));
        }
      }

      return proxies;
    }
  }

  private static Proxy decryptProxy(Proxy proxy, SettingsDecrypter decrypter) {
    final DefaultSettingsDecryptionRequest decryptionRequest = new DefaultSettingsDecryptionRequest(proxy);
    SettingsDecryptionResult decryptedResult = decrypter.decrypt(decryptionRequest);
    return decryptedResult.getProxy();
  }

}
