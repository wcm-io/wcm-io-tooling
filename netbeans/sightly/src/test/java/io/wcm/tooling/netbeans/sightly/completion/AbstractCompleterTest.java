/*
 * Copyright 2014 wcm.io.
 *
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
 */

package io.wcm.tooling.netbeans.sightly.completion;

import io.wcm.tooling.netbeans.sightly.completion.AbstractCompleter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.spi.editor.completion.CompletionItem;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class AbstractCompleterTest extends BaseTest {

  private AbstractCompleter instance;

  @Before
  public void setUp() {
    instance = new AbstractCompleter() {

      @Override
      public List<CompletionItem> getCompletionItems(Document document, String filter, int startOffset, int caretOffset) {
        return new ArrayList<>();
      }
    };
  }

  /**
   * Test of getRowFirstNonWhite method, of class AbstractCompleter.
   *
   * @throws Exception
   */
  @Test
  public void testGetRowFirstNonWhite() throws Exception {
    StyledDocument doc = getTestDocument("AbstractCompleterTest.html");
    //<head>
    assertEquals(600, instance.getRowFirstNonWhite(doc, 604));
    //<div data-sly-use.foo="${io.wcm.tooling.netbeans.sightly.BaseTest}">TODO write content</div>
    assertEquals(778, instance.getRowFirstNonWhite(doc, 790));
    // empty line
    assertEquals(912, instance.getRowFirstNonWhite(doc, 912));
  }

}
