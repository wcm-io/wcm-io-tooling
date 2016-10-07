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

import io.wcm.tooling.netbeans.sightly.completion.BasicCompletionItem;
import io.wcm.tooling.netbeans.sightly.editor.SightlyEditorUtil;
import java.io.IOException;
import java.net.URL;
import javax.lang.model.element.Element;
import javax.swing.Action;
import javax.swing.text.Document;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;

/**
 * Completion item for class lookups keeping a handle for the element to resolve JavaDoc
 *
 * @author amuthmann
 */
public class JavaElementCompletionItem extends BasicCompletionItem {

  private ElementHandle<? extends Element> elemHandle;
  private Element element;

  /**
   *
   * @param elemHandle
   * @param completionText
   * @param hasParameters
   * @param dotOffset
   * @param caretOffset
   */
  public JavaElementCompletionItem(ElementHandle<? extends Element> elemHandle, String completionText, boolean hasParameters, int dotOffset, int caretOffset) {
    super(completionText, hasParameters, dotOffset, caretOffset);
    this.elemHandle = elemHandle;
  }

  /**
   *
   * @param element
   * @param completionText
   * @param hasParameters
   * @param dotOffset
   * @param caretOffset
   */
  public JavaElementCompletionItem(Element element, String completionText, boolean hasParameters, int dotOffset, int caretOffset) {
    super(completionText, hasParameters, dotOffset, caretOffset);
    this.element = element;
  }

  @Override
  public CompletionTask createDocumentationTask() {
    return new AsyncCompletionTask(new AsyncCompletionQuery() {

      @Override
      protected void query(final CompletionResultSet resultSet, Document doc, int caretOffset) {
        try {
          JavaSource js = SightlyEditorUtil.getJavaSource(doc);
          if (js == null) {
            return;
          }

          js.runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController cc) throws Exception {
              cc.toPhase(JavaSource.Phase.RESOLVED);

              Element elem = element != null ? element : elemHandle.resolve(cc);
              if (elem == null) {
                return;
              }
              JavaElementDoc doc = new JavaElementDoc(ElementJavadoc.create(cc, elem));
              resultSet.setDocumentation(doc);
            }
          }, false);
          resultSet.finish();
        }
        catch (IOException ex) {
          Exceptions.printStackTrace(ex);
        }
      }
    }, EditorRegistry.lastFocusedComponent());
  }

  private static class JavaElementDoc implements CompletionDocumentation {

    private final ElementJavadoc elementJavadoc;

    public JavaElementDoc(ElementJavadoc elementJavadoc) {
      this.elementJavadoc = elementJavadoc;
    }

    @Override
    public JavaElementDoc resolveLink(String link) {
      ElementJavadoc doc = elementJavadoc.resolveLink(link);
      return doc != null ? new JavaElementDoc(doc) : null;
    }

    @Override
    public URL getURL() {
      return elementJavadoc.getURL();
    }

    public String getText() {
      return elementJavadoc.getText();
    }

    @Override
    public Action getGotoSourceAction() {
      return elementJavadoc.getGotoSourceAction();
    }
  }
}
