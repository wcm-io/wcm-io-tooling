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

import io.wcm.tooling.netbeans.sightly.completion.AbstractCompleter;
import io.wcm.tooling.netbeans.sightly.completion.BasicCompletionItem;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

@MimeRegistrations(value = {
  @MimeRegistration(mimeType = "text/html", service = CompletionProvider.class),
  @MimeRegistration(mimeType = "text/x-jsp", service = CompletionProvider.class)
})
public class RequestAttributeCompleter extends AbstractCompleter {

  @Override
  public List<CompletionItem> getCompletionItems(Document document, String filter, int startOffset, int caretOffset) {
    List<CompletionItem> ret = new ArrayList<>();
    try {
      String text = document.getText(0, caretOffset);
      FileObject fo = getFileObject(document);
      ClassPath sourcePath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
      ClassPath compilePath = ClassPath.getClassPath(fo, ClassPath.COMPILE);
      ClassPath bootPath = ClassPath.getClassPath(fo, ClassPath.BOOT);
      if (sourcePath == null) {
        return ret;
      }
      ClassPath cp = ClassPathSupport.createProxyClassPath(sourcePath, compilePath, bootPath);
      RequestAttributeResolver resolver = new RequestAttributeResolver(text, cp);
      for (String result : resolver.resolve(filter)) {
        ret.add(new BasicCompletionItem(result, false, startOffset, caretOffset));
      }
    }
    catch (BadLocationException ex) {
      Exceptions.printStackTrace(ex);
    }
    return ret;
  }

  
}
