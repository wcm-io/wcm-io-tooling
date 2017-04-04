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

import java.io.InputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.jcr.Binary;
import javax.jcr.Item;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.jackrabbit.vault.util.DocViewProperty;

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

    Value[] values;
    boolean multiple = value.getClass().isArray();
    if (multiple) {
      values = new Value[Array.getLength(value)];
      int lastPropertyType = PropertyType.UNDEFINED;
      for (int i = 0; i < values.length; i++) {
        values[i] = toValue(propertyName, Array.get(value, i));
        if (lastPropertyType == PropertyType.UNDEFINED) {
          lastPropertyType = values[i].getType();
        }
        else if (lastPropertyType != values[i].getType()) {
          throw new RuntimeException("Mixing different value types within array not allowed: " +
              PropertyType.nameFromValue(lastPropertyType) + ", " + PropertyType.nameFromValue(values[i].getType())
              + ", propertyName=" + propertyName + ", value=" + value);
        }
      }
    }
    else {
      values = new Value[] { toValue(propertyName, value) };
    }

    Property prop = new MockProperty(propertyName, multiple, values);
    try {
      return DocViewProperty.format(prop);
    }
    catch (RepositoryException ex) {
      throw new RuntimeException("Unable to format property value (" + propertyName + "): " + value, ex);
    }
  }

  private Value toValue(String propertyName, Object value) {
    if (value instanceof String) {
      if (StringUtils.equals(propertyName, PN_PRIVILEGES)) {
        return new MockValue(value.toString(), PropertyType.NAME);
      }
      else {
        return new MockValue(value.toString(), PropertyType.STRING);
      }
    }
    if (value instanceof Boolean) {
      return new MockValue(((Boolean)value).toString(), PropertyType.BOOLEAN);
    }
    if (value instanceof Integer || value instanceof Long) {
      return new MockValue(Long.toString(((Number)value).longValue()), PropertyType.LONG);
    }
    if (value instanceof Float || value instanceof Double) {
      return new MockValue(Double.toString(((Number)value).doubleValue()), PropertyType.DECIMAL);
    }
    if (value instanceof BigDecimal) {
      return new MockValue(((BigDecimal)value).toString(), PropertyType.DECIMAL);
    }
    if (value instanceof Date) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime((Date)value);
      return new MockValue(ISO8601.format(calendar), PropertyType.DATE);
    }
    if (value instanceof Calendar) {
      return new MockValue(ISO8601.format((Calendar)value), PropertyType.DATE);
    }
    if (value instanceof UUID) {
      return new MockValue(((UUID)value).toString(), PropertyType.REFERENCE);
    }
    if (value instanceof URI) {
      return new MockValue(((URI)value).toString(), PropertyType.URI);
    }
    throw new IllegalArgumentException("Type not supported: " + value.getClass().getName());
  }


  /**
   * Mock implementations of JCR property and value to be handed over to {@link DocViewProperty#format(Property)}
   * method.
   */
  private static class MockProperty implements Property, PropertyDefinition {

    private final String name;
    private final boolean multiple;
    private final Value[] values;

    MockProperty(String name, boolean multiple, Value[] values) {
      this.name = name;
      this.multiple = multiple;
      this.values = values;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public int getType() {
      if (values.length > 0) {
        return values[0].getType();
      }
      return PropertyType.UNDEFINED;
    }

    @Override
    public boolean isMultiple() {
      return multiple;
    }

    @Override
    public Value getValue() throws ValueFormatException {
      if (multiple) {
        throw new ValueFormatException("Property is multiple.");
      }
      return values[0];
    }

    @Override
    public Value[] getValues() throws ValueFormatException {
      if (!multiple) {
        throw new ValueFormatException("Property is not multiple.");
      }
      return values;
    }

    @Override
    public PropertyDefinition getDefinition() {
      return this;
    }


    // -- unsupported methods --

    @Override
    public String getPath() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Item getAncestor(int depth) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Node getParent() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getDepth() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Session getSession() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNode() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNew() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isModified() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSame(Item otherItem) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void accept(ItemVisitor visitor) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void save() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(boolean keepChanges) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Value value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Value[] value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(String[] value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(InputStream value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Binary value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(long value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(double value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(BigDecimal value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Calendar value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(boolean value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Node value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getString() {
      throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getStream() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Binary getBinary() {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getLong() {
      throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble() {
      throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getDecimal() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Calendar getDate() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Node getNode() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Property getProperty() {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getLength() {
      throw new UnsupportedOperationException();
    }

    @Override
    public long[] getLengths() {
      throw new UnsupportedOperationException();
    }

    @Override
    public NodeType getDeclaringNodeType() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAutoCreated() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMandatory() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getOnParentVersion() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isProtected() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getRequiredType() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String[] getValueConstraints() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Value[] getDefaultValues() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String[] getAvailableQueryOperators() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFullTextSearchable() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isQueryOrderable() {
      throw new UnsupportedOperationException();
    }

  }

  private static class MockValue implements Value {

    private final String value;
    private final int type;

    MockValue(String value, int type) {
      this.value = value;
      this.type = type;
    }

    @Override
    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
      return value;
    }

    @Override
    public int getType() {
      return type;
    }


    // -- unsupported methods --

    @Override
    public InputStream getStream() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Binary getBinary() {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getLong() {
      throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble() {
      throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getDecimal() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Calendar getDate() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean() {
      throw new UnsupportedOperationException();
    }

  }

}
