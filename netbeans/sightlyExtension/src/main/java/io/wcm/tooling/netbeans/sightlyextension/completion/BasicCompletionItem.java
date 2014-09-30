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
package io.wcm.tooling.netbeans.sightlyextension.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 * basic completion item
 */
public class BasicCompletionItem implements CompletionItem {

  private final String completionText;
  private final boolean hasParameters;
  private final int caretOffset;
  private final int dotOffset;
  private static final ImageIcon FIELD_ICON = new ImageIcon(ImageUtilities.loadImage("io/wcm/tooling/netbeans/sightlyextension/icon.png"));
  private static final Color FIELD_COLOR = Color.decode("0x0088cc");

  /**
   * constructor
   *
   * @param completionText the completion text
   * @param hasParameters
   * @param dotOffset offset of start
   * @param caretOffset caret offset
   */
  public BasicCompletionItem(String completionText, boolean hasParameters, int dotOffset, int caretOffset) {
    this.completionText = completionText;
    this.hasParameters = hasParameters;
    this.dotOffset = dotOffset;
    this.caretOffset = caretOffset;
  }

  @Override
  public void defaultAction(JTextComponent component) {
    substituteText(component, null);
  }

  @Override
  public void processKeyEvent(KeyEvent evt) {
    if (hasParameters && evt.getID() == KeyEvent.KEY_TYPED) {
      if (evt.getKeyChar() == '.') {
        Completion.get().hideDocumentation();
        JTextComponent component = (JTextComponent)evt.getSource();
        substituteText(component, Character.toString(evt.getKeyChar()));
        Completion.get().showCompletion();
        evt.consume();
      }
    }
  }

  /**
   * Substitutes the text inside the component.
   *
   * @param component
   * @param toAdd optional text which is appended to the completion text
   */
  protected void substituteText(JTextComponent component, String toAdd) {
    String text = completionText;
    if (toAdd != null) {
      text += toAdd;
    }
    try {
      StyledDocument doc = (StyledDocument)component.getDocument();
      //Here we remove the characters starting at the start offset
      //and ending at the point where the caret is currently found:
      doc.remove(dotOffset, caretOffset - dotOffset);
      doc.insertString(dotOffset, text, null);
      Completion.get().hideAll();
    }
    catch (BadLocationException ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  @Override
  public int getPreferredWidth(Graphics graphics, Font font) {
    return CompletionUtilities.getPreferredWidth(completionText, null, graphics, font);
  }

  @Override
  public void render(Graphics g, Font defaultFont, Color defaultColor,
          Color backgroundColor, int width, int height, boolean selected) {
    CompletionUtilities.renderHtml(FIELD_ICON, completionText, null, g, defaultFont,
            (selected ? Color.white : FIELD_COLOR), width, height, selected);
  }

  @Override
  public CompletionTask createDocumentationTask() {
    //TODO implement
    return null;
  }

  @Override
  public CompletionTask createToolTipTask() {
    return null;
  }

  @Override
  public boolean instantSubstitution(JTextComponent jtc) {
    return false;
  }

  @Override
  public int getSortPriority() {
    return 0;
  }

  @Override
  public CharSequence getSortText() {
    return completionText;
  }

  @Override
  public CharSequence getInsertPrefix() {
    return completionText;
  }

}
