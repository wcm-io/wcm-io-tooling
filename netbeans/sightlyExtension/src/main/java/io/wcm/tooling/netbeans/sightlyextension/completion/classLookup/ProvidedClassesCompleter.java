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
package io.wcm.tooling.netbeans.sightlyextension.completion.classLookup;

import io.wcm.tooling.netbeans.sightlyextension.completion.AbstractCompleter;
import io.wcm.tooling.netbeans.sightlyextension.completion.BasicCompletionItem;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.apache.commons.lang.StringUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 * Completer used for classes like Page or request. Currently uses static parameters and fixed object names (e.g. currentPage and wcmmode)
 *
 */
@MimeRegistrations(value = {
  @MimeRegistration(mimeType = "text/html", service = CompletionProvider.class),
  @MimeRegistration(mimeType = "text/x-jsp", service = CompletionProvider.class)
})
public class ProvidedClassesCompleter extends AbstractCompleter {

  private static final Pattern LAST_DOLLAR_CURLYBRACE = Pattern.compile("(\\$\\{)(?!.*\\})");
  private static final Pattern LAST_VARIABLE = Pattern.compile("([a-zA-Z0-9.])+$");

  @Override
  public List<CompletionItem> getCompletionItems(Document document, String filter, int startOffset, int caretOffset) {
    List<CompletionItem> ret = new ArrayList<>();
    if (filter == null || StringUtils.isBlank(filter)) {
      return ret;
    }
    for (ProvidedClasses sightlyClass : ProvidedClasses.values()) {
      if (sightlyClass.getName().startsWith(filter) || filter.startsWith(sightlyClass.getName())) {
        // if there is a . in the text or the text is completed, we add the parameters, otherwise the actual classname
        if (filter.contains(".") || filter.equalsIgnoreCase(sightlyClass.getName())) {
          for (String parameter : sightlyClass.getMembers()) {
            String full = sightlyClass.getName() + "." + parameter;
            if (full.startsWith(filter)) {
              ret.add(new BasicCompletionItem(full, false, startOffset, caretOffset));
            }
          }
        }
        else {
          ret.add(new BasicCompletionItem(sightlyClass.getName(), true, startOffset, caretOffset));
        }
      }
    }
    return ret;
  }

  @Override
  public int indexOfStartCharacter(char[] line) {
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
