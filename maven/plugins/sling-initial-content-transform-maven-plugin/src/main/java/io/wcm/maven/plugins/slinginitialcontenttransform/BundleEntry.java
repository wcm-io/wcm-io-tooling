/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.maven.plugins.slinginitialcontenttransform;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class BundleEntry {

  private final String path;
  private final JarFile jarFile;
  private final JarEntry entry;

  BundleEntry(String path, JarFile jarFile, JarEntry entry) {
    this.path = path;
    this.jarFile = jarFile;
    this.entry = entry;
  }

  public String getPath() {
    return this.path;
  }

  public boolean isDirectory() {
    return entry.isDirectory();
  }

  public InputStream getInputStream() throws IOException {
    return jarFile.getInputStream(entry);
  }

}
