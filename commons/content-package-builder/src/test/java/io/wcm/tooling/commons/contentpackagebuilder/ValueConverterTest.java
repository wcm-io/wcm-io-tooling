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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

public class ValueConverterTest {

  private ValueConverter underTest;
  private Date sampleDate;

  @Before
  public void setUp() throws Exception {
    underTest = new ValueConverter();
    sampleDate = DateUtils.parseDate("05.09.2010 15:10:20", "dd.MM.yyyy HH:mm:ss");
  }

  @Test
  public void testNull() {
    assertEquals("", underTest.toString(null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalid() {
    underTest.toString(new Object());
  }

  @Test
  public void testString() {
    assertEquals("myString", underTest.toString("myString"));
  }

  @Test
  public void testStringWithBrackets() {
    assertEquals("\\{myString}", underTest.toString("{myString}"));
  }

  @Test
  public void testStringArray() {
    assertEquals("[myString1,myString2]", underTest.toString(new String[] {
        "myString1", "myString2"
    }));
  }

  @Test
  public void testStringArraySpecialChars() {
    assertEquals("[myString1\\,[]\\\\äöüß€,myString2]", underTest.toString(new String[] {
        "myString1,[]\\äöüß€", "myString2"
    }));
  }

  @Test
  public void testBoolean() {
    assertEquals("{Boolean}true", underTest.toString(true));
    assertEquals("{Boolean}true", underTest.toString(Boolean.TRUE));
  }

  @Test
  public void testBooleanArray() {
    assertEquals("{Boolean}[true,false]", underTest.toString(new boolean[] {
        true, false
    }));
    assertEquals("{Boolean}[true,false]", underTest.toString(new Boolean[] {
        Boolean.TRUE, Boolean.FALSE
    }));
  }

  @Test
  public void testInteger() {
    assertEquals("{Long}1", underTest.toString(1));
    assertEquals("{Long}2", underTest.toString(new Integer(2)));
  }

  @Test
  public void testIntegerArray() {
    assertEquals("{Long}[1,2]", underTest.toString(new int[] {
        1, 2
    }));
    assertEquals("{Long}[1,2]", underTest.toString(new Integer[] {
        new Integer(1), new Integer(2)
    }));
  }

  @Test
  public void testLong() {
    assertEquals("{Long}10000000000", underTest.toString(10000000000L));
    assertEquals("{Long}20000000000", underTest.toString(new Long(20000000000L)));
  }

  @Test
  public void testLongArray() {
    assertEquals("{Long}[10000000000,20000000000]", underTest.toString(new long[] {
        10000000000L, 20000000000L
    }));
    assertEquals("{Long}[10000000000,20000000000]", underTest.toString(new Long[] {
        new Long(10000000000L), new Long(20000000000L)
    }));
  }

  @Test
  public void testDouble() {
    assertEquals("{Decimal}1.234", underTest.toString(1.234d));
    assertEquals("{Decimal}2.345", underTest.toString(new Double(2.345d)));
  }

  @Test
  public void testDoubleArray() {
    assertEquals("{Decimal}[1.234,2.345]", underTest.toString(new double[] {
        1.234d, 2.345d
    }));
    assertEquals("{Decimal}[1.234,2.345]", underTest.toString(new Double[] {
        new Double(1.234d), new Double(2.345d)
    }));
  }

  @Test
  public void testBigDecimal() {
    assertEquals("{Decimal}2.345", underTest.toString(new BigDecimal(2.345d)));
  }

  @Test
  public void testBigDecimalArray() {
    assertEquals("{Decimal}[1.234,2.345]", underTest.toString(new BigDecimal[] {
        new BigDecimal(1.234d), new BigDecimal(2.345d)
    }));
  }

  @Test
  public void testDate() {
    assertTrue(StringUtils.startsWith(underTest.toString(sampleDate), "{Date}2010-09-05T15:10:20"));
  }

  @Test
  public void testCalendar() {
    assertTrue(StringUtils.startsWith(underTest.toString(DateUtils.toCalendar(sampleDate)), "{Date}2010-09-05T15:10:20"));
  }

}
