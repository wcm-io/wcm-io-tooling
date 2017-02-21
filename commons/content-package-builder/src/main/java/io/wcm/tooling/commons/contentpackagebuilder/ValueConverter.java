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
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.ISO8601;

/**
 * Converts an value to string for a content property in XML including type prefix.
 */
final class ValueConverter {

  static final String PN_PRIVILEGES = "rep:privileges";

  /**
   * Converts an object to a string representation.
   * Supported are String, Boolean, Integer, Long, Double, BigDecimal, Date, Calendar and arrays of them.
   * @param value value
   * @return Converted value
   */
  public String toString(String propertyName, Object value) {
    if (value == null) {
      return "";
    }
    else if (value instanceof boolean[]) {
      return arrayToString(propertyName, ArrayUtils.toObject((boolean[])value));
    }
    else if (value instanceof int[]) {
      return arrayToString(propertyName, ArrayUtils.toObject((int[])value));
    }
    else if (value instanceof long[]) {
      return arrayToString(propertyName, ArrayUtils.toObject((long[])value));
    }
    else if (value instanceof double[]) {
      return arrayToString(propertyName, ArrayUtils.toObject((double[])value));
    }
    else if (value.getClass().isArray()) {
      return arrayToString(propertyName, (Object[])value);
    }
    else {
      return getTypePrefix(propertyName, value) + singleObjectToString(value, false);
    }
  }

  private <T> String arrayToString(String propertyName, T[] values) {
    if (values.length == 0) {
      return "";
    }
    else {
      String typePrefix = getTypePrefix(propertyName, values[0]);
      StringBuilder arrayString = new StringBuilder();
      arrayString.append("[");
      for (int i = 0; i < values.length; i++) {
        if (i > 0) {
          arrayString.append(",");
        }
        String convertedArrayValue = singleObjectToString(values[i], true);
        arrayString.append(convertedArrayValue);
      }
      arrayString.append("]");
      String stringValue = arrayString.toString();
      return typePrefix + stringValue;
    }
  }

  private String singleObjectToString(final Object value, final boolean inArray) {
    if (value == null) {
      return "";
    }
    if (value instanceof String) {
      return escapeStringValue((String)value, inArray);
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
      Calendar calendar = Calendar.getInstance();
      calendar.setTime((Date)value);
      return ISO8601.format(calendar);
    }
    if (value instanceof Calendar) {
      return ISO8601.format((Calendar)value);
    }
    throw new IllegalArgumentException("Type not supported: " + value.getClass().getName());
  }

  private String escapeStringValue(final String value, final boolean inArray) {
    String escapedValue = StringUtils.replace(value, "\\", "\\\\");
    if (inArray) {
      escapedValue = StringUtils.replace(escapedValue, ",", "\\,");
    }
    else if (StringUtils.startsWith(escapedValue, "{") || StringUtils.startsWith(escapedValue, "[")) {
      escapedValue = "\\" + escapedValue;
    }
    return escapedValue;
  }

  private String getTypePrefix(String propertyName, Object value) {
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
    else if (StringUtils.equals(propertyName, PN_PRIVILEGES)) {
      return "{Name}";
    }
    return "";
  }

}
