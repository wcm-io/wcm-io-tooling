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
package io.wcm.tooling.netbeans.sightly.completion.dataSly;

import io.wcm.tooling.netbeans.sightly.completion.dataSly.DataSlyCompleter;
import io.wcm.tooling.netbeans.sightly.completion.BaseTest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for DataSlyCompleter
 */
public class DataSlyCompleterTest extends BaseTest {

  private DataSlyCompleter instance;

  @Before
  public void setUp() {
    instance = new DataSlyCompleter();
  }

  /**
   * Test of getCompletionItems method, of class DataSlyCompleter.
   */
  @Test
  public void testGetCompletionItems() {
    String command = "data-";
    assertEquals("Number of completion-items for \"" + command + "\"", 11, instance.getCompletionItems(null, command, 0, 0).size());
    command = "data-sly-";
    assertEquals("Number of completion-items for \"" + command + "\"", 11, instance.getCompletionItems(null, command, 0, 0).size());
    command = "data-sly-li";
    assertEquals("Number of completion-items for \"" + command + "\"", 1, instance.getCompletionItems(null, command, 0, 0).size());
    command = "data-sly-t";
    assertEquals("Number of completion-items for \"" + command + "\"", 3, instance.getCompletionItems(null, command, 0, 0).size());
    command = "data-sly-ß";
    assertEquals("Number of completion-items for \"" + command + "\"", 0, instance.getCompletionItems(null, command, 0, 0).size());
    command = "§{data-sly";
    assertEquals("Number of completion-items for \"" + command + "\"", 0, instance.getCompletionItems(null, command, 0, 0).size());
  }

  /**
   * Test of indexOfStartCharacter method, of class DataSlyCompleter.
   */
  @Test
  public void testIndexOfStartCharacter() {
    assertEquals(DATA_SLY_LINE_1, 4, instance.indexOfStartCharacter(DATA_SLY_LINE_1.toCharArray()));
    assertEquals(DATA_SLY_LINE_2, 4, instance.indexOfStartCharacter(DATA_SLY_LINE_2.toCharArray()));
    assertEquals(DATA_SLY_LINE_3, 23, instance.indexOfStartCharacter(DATA_SLY_LINE_3.toCharArray()));
    assertEquals(DATA_SLY_LINE_4, 8, instance.indexOfStartCharacter(DATA_SLY_LINE_4.toCharArray()));
    assertEquals(DATA_SLY_LINE_5_INVALID, -1, instance.indexOfStartCharacter(DATA_SLY_LINE_5_INVALID.toCharArray()));
  }

}
