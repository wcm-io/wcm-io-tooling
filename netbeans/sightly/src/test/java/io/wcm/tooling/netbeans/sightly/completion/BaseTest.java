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
package io.wcm.tooling.netbeans.sightly.completion;

import java.io.IOException;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.io.IOUtils;

public abstract class BaseTest {

  /**
   * Lines to test completion for data-sly- entries
   */
  protected static final String DATA_SLY_LINE_1 = "<div data-sly";
  protected static final String DATA_SLY_LINE_2 = "<div data-sly-li";
  protected static final String DATA_SLY_LINE_3 = "<div attribute=\"foobar\" data-sly-li";
  protected static final String DATA_SLY_LINE_4 = "    <div data-sly";
  protected static final String DATA_SLY_LINE_5_INVALID = "<div ${data-sly";

  public String getTestDocumentContent(String filename) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream("/" + filename));
  }

  public StyledDocument getTestDocument(String filename) throws IOException, BadLocationException {
    String content = IOUtils.toString(getClass().getResourceAsStream("/" + filename));
    return createDocument(content);
  }

  public StyledDocument createDocument(String content) throws BadLocationException {
    StyledDocument doc = new HTMLDocument();
    doc.insertString(0, content, null);
    return doc;
  }

}
