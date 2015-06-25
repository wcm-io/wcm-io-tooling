/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.tooling.commons.contentpackagebuilder;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Converts an value to string for a content property in XML including type prefix.
 */
final class ValueConverter {

  private final DateFormat jcrTimestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmmZ", Locale.US);

  /**
   * Converts an object to a string representation.
   * Supported are String, Boolean, Integer, Long, Double, BigDecimal, Date, Calendar and arrays of them.
   * @param value value
   * @return Converted value
   */
  public String toString(Object value) {
    if (value == null) {
      return "";
    }
    else if (value instanceof boolean[]) {
      return arrayToString(ArrayUtils.toObject((boolean[])value));
    }
    else if (value instanceof int[]) {
      return arrayToString(ArrayUtils.toObject((int[])value));
    }
    else if (value instanceof long[]) {
      return arrayToString(ArrayUtils.toObject((long[])value));
    }
    else if (value instanceof double[]) {
      return arrayToString(ArrayUtils.toObject((double[])value));
    }
    else if (value.getClass().isArray()) {
      return arrayToString((Object[])value);
    }
    else {
      return getTypePrefix(value) + singleObjectToString(value);
    }
  }

  private <T> String arrayToString(T[] values) {
    if (values.length == 0) {
      return "";
    }
    else {
      String typePrefix = getTypePrefix(values[0]);
      StringBuilder arrayString = new StringBuilder();
      arrayString.append("[");
      for (int i = 0; i < values.length; i++) {
        if (i > 0) {
          arrayString.append(",");
        }
        String convertedArrayValue = singleObjectToString(values[i]);
        arrayString.append(escapeSpecialCharsInArray(convertedArrayValue));
      }
      arrayString.append("]");
      String stringValue = arrayString.toString();
      return typePrefix + stringValue;
    }
  }

  private String escapeSpecialCharsInArray(String value) {
    return StringUtils.replace(StringUtils.replace(value, "\\", "\\\\"), ",", "\\,");
  }

  private String singleObjectToString(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof String) {
      return (String)value;
    }
    if (value instanceof Boolean) {
      return ((Boolean)value).toString();
    }
    if (value instanceof Integer || value instanceof Long) {
      return Long.toString(((Number)value).longValue());
    }
    if (value instanceof Double) {
      return Double.toString(((Double)value).doubleValue());
    }
    if (value instanceof BigDecimal) {
      return Double.toString(((BigDecimal)value).doubleValue());
    }
    if (value instanceof Date) {
      return jcrTimestampFormat.format((Date)value);
    }
    if (value instanceof Calendar) {
      return jcrTimestampFormat.format(((Calendar)value).getTime());
    }
    throw new IllegalArgumentException("Type not supported: " + value.getClass().getName());
  }

  private String getTypePrefix(Object value) {
    if (value instanceof Boolean) {
      return "{Boolean}";
    }
    else if (value instanceof Integer || value instanceof Long) {
      return "{Long}";
    }
    else if (value instanceof Number || value instanceof BigDecimal) {
      return "{Decimal}";
    }
    else if (value instanceof Date || value instanceof Calendar) {
      return "{Date}";
    }
    return "";
  }

  /**
   * @return Date format used for formatting timestamps in content package.
   */
  public DateFormat getJcrTimestampFormat() {
    return this.jcrTimestampFormat;
  }

}
