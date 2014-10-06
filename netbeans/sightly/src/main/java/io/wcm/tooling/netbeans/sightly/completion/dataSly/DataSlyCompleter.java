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

import io.wcm.tooling.netbeans.sightly.completion.AbstractCompleter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 * Completer for sightly commands
 */
@MimeRegistrations(value = {
  @MimeRegistration(mimeType = "text/html", service = CompletionProvider.class),
  @MimeRegistration(mimeType = "text/x-jsp", service = CompletionProvider.class)
})
public class DataSlyCompleter extends AbstractCompleter {

  @Override
  public List<CompletionItem> getCompletionItems(Document document, String filter, int startOffset, int caretOffset) {
    List<CompletionItem> ret = new ArrayList<>();
    for (DataSlyCommands sightlyCommand : DataSlyCommands.values()) {
      if (sightlyCommand.getCommand().startsWith(filter)) {
        ret.add(new DataSlyCompetionItem(sightlyCommand, startOffset, caretOffset));
      }
    }
    return ret;
  }

  /**
   * data-sly is not allowed in a ${ statement
   */
  private static final Pattern LAST_DOLLAR_CURLYBRACE = Pattern.compile("(\\$\\{)(?!.*\\})");

  @Override
  public int indexOfStartCharacter(char[] line) {
    final String text = String.copyValueOf(line);
    final Matcher matcher = LAST_DOLLAR_CURLYBRACE.matcher(text);
    if (matcher.find()) {
      return -1;
    }

    int i = line.length;
    while (--i > -1) {
      final char c = line[i];
      if (Character.isWhitespace(c)) {
        return i;
      }
    }
    return -1;
  }

}
