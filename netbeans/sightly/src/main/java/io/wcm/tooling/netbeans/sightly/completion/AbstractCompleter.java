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

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
/**
 *
 */
public abstract class AbstractCompleter implements CompletionProvider {

  /**
   *
   * @param filter
   * @param startOffset
   * @param caretOffset
   */
  public abstract List<CompletionItem> getCompletionItems(Document document, String filter, int startOffset, int caretOffset);

  @Override
  public CompletionTask createTask(int queryType, JTextComponent jtc) {

    if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
      return null;
    }
    return new AsyncCompletionTask(new AsyncCompletionQuery() {
      @Override
      protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {
        // find start of autocompletion
        String filter = null;
        int startOffset = caretOffset - 1;
        try {
          final StyledDocument bDoc = (StyledDocument)document;
          // extract the text of the current line
          final int lineStartOffset = getRowFirstNonWhite(bDoc, caretOffset);
          if (lineStartOffset >= caretOffset) {
            completionResultSet.finish();
            return;
          }
          final char[] line = bDoc.getText(lineStartOffset, caretOffset - lineStartOffset).toCharArray();

          final int completionOffset = indexOfStartCharacter(line);
          filter = new String(line, completionOffset + 1, line.length - completionOffset - 1);
          if (completionOffset > 0) {
            startOffset = lineStartOffset + completionOffset + 1;
          }
          else {
            startOffset = lineStartOffset;
          }
        }
        catch (BadLocationException ex) {
          Exceptions.printStackTrace(ex);
        }
        completionResultSet.addAllItems(getCompletionItems(document, filter, startOffset, caretOffset));
        completionResultSet.finish();
      }
    }, jtc);
  }

  @Override
  public int getAutoQueryTypes(JTextComponent component, String string) {
    return CompletionProvider.COMPLETION_QUERY_TYPE;
  }

  /**
   * iterates through the text to find first nonwhite
   *
   * @param doc
   * @param offset
   * @return the offset of the first non-white
   * @throws BadLocationException
   */
  protected int getRowFirstNonWhite(StyledDocument doc, int offset) throws BadLocationException {
    Element lineElement = doc.getParagraphElement(offset);
    int start = lineElement.getStartOffset();
    while (start + 1 < lineElement.getEndOffset()) {
      try {
        if (doc.getText(start, 1).charAt(0) != ' ') {
          break;
        }
      }
      catch (BadLocationException ex) {
        throw (BadLocationException)new BadLocationException("calling getText(" + start + ", " + (start + 1) + ") on doc of length: " + doc.getLength(), start)
                .initCause(ex);
      }
      start++;
    }
    return start;
  }

  /**
   * @param line
   * @return index of last whitespace
   */
  public int indexOfStartCharacter(char[] line) {
    int i = line.length;
    while (--i > -1) {
      final char c = line[i];
      if (Character.isWhitespace(c)) {
        return i;
      }
    }
    return -1;
  }

  /**
   *
   * @param doc
   * @return fileobject for the given document (using NbEditorUtilities)
   */
  public FileObject getFileObject(Document doc) {
    return NbEditorUtilities.getFileObject(doc);
  }


}
