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
package io.wcm.maven.plugins.i18n;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

public final class FileUtil {

  private FileUtil() {
    //Static methods only
  }

  public static File getFileFromClasspath(String resourcePath) throws URISyntaxException {
    URL url = FileUtil.class.getClassLoader().getResource(resourcePath);
    return new File(url.toURI());
  }

  public static String getStringFromClasspath(String resourcePath) throws IOException {
    try (InputStream is = FileUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
      return IOUtils.toString(is, CharEncoding.UTF_8);
    }
  }

}
