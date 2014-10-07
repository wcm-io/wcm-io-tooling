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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.apache.commons.lang.StringUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.openide.filesystems.FileObject;

/**
 * Looks up all classes with the @Model annotation of sling (usage in data-sly-use)
 */
@MimeRegistrations(value = {
  @MimeRegistration(mimeType = "text/html", service = CompletionProvider.class),
  @MimeRegistration(mimeType = "text/x-jsp", service = CompletionProvider.class)
})
public class ClassLookupCompleter extends AbstractCompleter {

  private static final Pattern LAST_DOLLAR_CURLYBRACE = Pattern.compile("(\\$\\{)(?!.*\\})");
  private static final Pattern LAST_VARIABLE = Pattern.compile("([a-zA-Z0-9.])+$");

  @Override
  public List<CompletionItem> getCompletionItems(Document document, String filter, int startOffset, int caretOffset) {
    List<CompletionItem> ret = new ArrayList<>();
    // Use netbeans cache to load all classes of the current project
    FileObject fo = getFileObject(document);
    ClassPath bootCp = ClassPath.getClassPath(fo, ClassPath.BOOT);
    ClassPath compileCp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
    ClassPath sourcePath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
    if (bootCp == null || compileCp == null) {
      return ret;
    }
    final ClasspathInfo info = ClasspathInfo.create(bootCp, compileCp, sourcePath);
    final Set<ElementHandle<TypeElement>> result = info.getClassIndex().getDeclaredTypes("",
            ClassIndex.NameKind.PREFIX, EnumSet.of(ClassIndex.SearchScope.SOURCE));

    for (ElementHandle<TypeElement> te : result) {
      if (te.getKind().isClass()) {
        String binaryName = te.getBinaryName();
        if (!StringUtils.equals(binaryName, "") && StringUtils.startsWith(binaryName, filter)) {
          ret.add(new BasicCompletionItem(te.getBinaryName(), false, startOffset, caretOffset));
        }
      }
    }
    return ret;
  }

  @Override
  public int indexOfStartCharacter(char[] line) {
    //TODO: change to check if there is a data-sly-use before the class
    //TODO: this is wrong as classes can also be used in data-sly-use.foo="CLASS" and not only with ${
    final String text = String.copyValueOf(line);
    final Matcher matcher = LAST_DOLLAR_CURLYBRACE.matcher(text);

    if (matcher.find()) {
      final int beginIndex = matcher.start();
      final Matcher variableMatcher = LAST_VARIABLE.matcher(text);
      if (variableMatcher.find(beginIndex)) {
        return variableMatcher.start() - 1;
      }
    }
    return -1;
  }
}
