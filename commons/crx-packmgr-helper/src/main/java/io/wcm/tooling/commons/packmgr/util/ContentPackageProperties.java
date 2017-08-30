/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.tooling.commons.packmgr.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Reads package properties from AEM content package file.
 */
public final class ContentPackageProperties {

  private static final String ZIP_ENTRY_PROPERTIES = "META-INF/vault/properties.xml";

  private ContentPackageProperties() {
    // constants only
  }

  /**
   * Get properties of AEM package.
   * @param packageFile AEM package file.
   * @return Map with properties or empty map if none found.
   * @throws IOException I/O exception
   */
  public static Map<String, Object> get(File packageFile) throws IOException {
    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(packageFile);
      ZipArchiveEntry entry = zipFile.getEntry(ZIP_ENTRY_PROPERTIES);
      if (entry != null && !entry.isDirectory()) {
        Map<String, Object> props = getPackageProperties(zipFile, entry);
        return new TreeMap<>(transformPropertyTypes(props));
      }
      return Collections.emptyMap();
    }
    finally {
      IOUtils.closeQuietly(zipFile);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getPackageProperties(ZipFile zipFile, ZipArchiveEntry entry) throws IOException {
    InputStream entryStream = null;
    try {
      entryStream = zipFile.getInputStream(entry);
      Properties props = new Properties();
      props.loadFromXML(entryStream);
      return (Map)props;
    }
    finally {
      IOUtils.closeQuietly(entryStream);
    }
  }

  private static Map<String, Object> transformPropertyTypes(Map<String, Object> props) {
    Map<String, Object> transformedProps = new HashMap<>();
    for (Map.Entry<String, Object> entry : props.entrySet()) {
      transformedProps.put(entry.getKey(), transformType(entry.getValue()));
    }
    return transformedProps;
  }

  /**
   * Detects if string values are boolean or integer and transforms them to correct types.
   * @param value Value
   * @return Transformed value
   */
  private static Object transformType(Object value) {
    if (value == null) {
      return null;
    }
    String valueString = value.toString();

    // check for boolean
    boolean boolValue = BooleanUtils.toBoolean(valueString);
    if (StringUtils.equals(valueString, Boolean.toString(boolValue))) {
      return boolValue;
    }

    // check for integer
    int intValue = NumberUtils.toInt(valueString);
    if (StringUtils.equals(valueString, Integer.toString(intValue))) {
      return intValue;
    }

    return value;
  }

}
