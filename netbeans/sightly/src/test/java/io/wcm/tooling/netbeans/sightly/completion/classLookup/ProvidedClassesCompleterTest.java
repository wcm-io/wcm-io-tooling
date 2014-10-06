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
package io.wcm.tooling.netbeans.sightly.completion.classLookup;

import static org.junit.Assert.assertEquals;
import io.wcm.tooling.netbeans.sightly.completion.BaseTest;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for ProvidedClassesCompleter
 */
public class ProvidedClassesCompleterTest extends BaseTest {

  private ProvidedClassesCompleter instance;

  @Before
  public void setUp() {
    instance = new ProvidedClassesCompleter();
  }

  /**
   * Tests the autocompletion for names (e.g. currentP to currentPage)
   */
  @Test
  public void testNameLookup() {
    String command = "";
    assertEquals("Number of completion-items for \"" + command + "\"", 0, instance.getCompletionItems(null, command, 0, 0).size());
    command = null;
    assertEquals("Number of completion-items for \"" + command + "\"", 0, instance.getCompletionItems(null, command, 0, 0).size());
    command = "invalidFoo";
    assertEquals("Number of completion-items for \"" + command + "\"", 0, instance.getCompletionItems(null, command, 0, 0).size());
    command = "currentP";
    assertEquals("Number of completion-items for \"" + command + "\"", 1, instance.getCompletionItems(null, command, 0, 0).size());
    command = "wc";
    assertEquals("Number of completion-items for \"" + command + "\"", 1, instance.getCompletionItems(null, command, 0, 0).size());
  }

  /**
   * tests the autocompletion for members (e.g. currentPage. to currentPage.title)
   */
  @Test
  public void testMemberLookup() {
    String command = "";
    assertEquals("Number of completion-items for \"" + command + "\"", 0, instance.getCompletionItems(null, command, 0, 0).size());
    command = null;
    assertEquals("Number of completion-items for \"" + command + "\"", 0, instance.getCompletionItems(null, command, 0, 0).size());
    command = "invalidFoo.";
    assertEquals("Number of completion-items for \"" + command + "\"", 0, instance.getCompletionItems(null, command, 0, 0).size());
    command = "currentPage.";
    assertEquals("Number of completion-items for \"" + command + "\"", 17, instance.getCompletionItems(null, command, 0, 0).size());
    command = "wcmmode.";
    assertEquals("Number of completion-items for \"" + command + "\"", 4, instance.getCompletionItems(null, command, 0, 0).size());
  }

  /**
   * Test of indexOfStartCharacter method, of class ProvidedClassesCompleter.
   */
  @Test
  public void testIndexOfStartCharacter() {
    //TODO: implement
  }

}
