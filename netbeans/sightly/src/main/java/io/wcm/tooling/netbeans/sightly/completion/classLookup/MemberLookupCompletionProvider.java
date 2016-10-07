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

import io.wcm.tooling.netbeans.sightly.completion.AbstractCompletionProvider;
import io.wcm.tooling.netbeans.sightly.completion.BasicCompletionItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

import static io.wcm.tooling.netbeans.sightly.completion.classLookup.ParsedStatement.PATTERN;

/**
 * This completer tries to find usages of the entered text in data-sly commands which support variables and then tries to resolve the class
 */
@MimeRegistrations(value = {
  @MimeRegistration(mimeType = "text/html", service = CompletionProvider.class),
  @MimeRegistration(mimeType = "text/x-jsp", service = CompletionProvider.class)
})
public class MemberLookupCompletionProvider extends AbstractCompletionProvider {

  /**
   * Pattern to find the command
   */
  public static final Pattern COMMAND_PATTERN = Pattern.compile(".*(data-sly-.*)\\.(.*)=\\\"");
  /**
   * Pattern to detect getter
   */
  public static final Pattern GETTER_PATTERN = Pattern.compile("(get|is)(.*)");

  @Override
  public List<CompletionItem> getCompletionItems(Document document, String filter, int startOffset, int caretOffset) {
    List<CompletionItem> ret = new ArrayList<>();
    try {
      String text = document.getText(0, caretOffset);
      List<String> variables = resolveVariable(filter, text);
      // if the filter is not the complete variable, we first autocomplete the actual variable
      for (String variable : variables) {
        if (!StringUtils.equals(filter, variable)) {
          ret.add(new BasicCompletionItem(variable, true, startOffset, caretOffset));
        }
      }
      // otherwise we lookup the defined class and return all of it's members
      if (ret.isEmpty()) {
        ret.addAll(resolveClass(filter, text, document, startOffset, caretOffset));
      }
    }
    catch (BadLocationException ex) {
      Exceptions.printStackTrace(ex);
    }
    return ret;
  }

  /**
   * this method returns the variables defined by the filter. e.g. data-sly-use.variable
   *
   * @param filter
   * @param text
   * @return the variable defined by the filter
   */
  private List<String> resolveVariable(String filter, String text) {
    List<String> ret = new ArrayList<>();
    Matcher m = PATTERN.matcher(text);
    while (m.find()) {
      ParsedStatement statement = ParsedStatement.fromMatcher(m);
      if (statement != null && statement.getVariable().startsWith(filter)) {
        ret.add(statement.getVariable());
      }
    }
    return ret;
  }

  /**
   * This method tries to find the class which is defined for the given filter and returns a set with all methods and fields of the class
   *
   * @param variableName
   * @param text
   * @param document
   * @return Set of methods and fields, never null
   */
  private Collection<CompletionItem> resolveClass(String variableName, String text, Document document, int startOffset, int caretOffset) {
    Map<String, CompletionItem> items = new LinkedHashMap<>();
    FileObject fo = getFileObject(document);
    ClassPath sourcePath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
    ClassPath compilePath = ClassPath.getClassPath(fo, ClassPath.COMPILE);
    ClassPath bootPath = ClassPath.getClassPath(fo, ClassPath.BOOT);
    if (sourcePath == null) {
      return items.values();
    }
    ClassPath cp = ClassPathSupport.createProxyClassPath(sourcePath, compilePath, bootPath);
    MemberLookupResolver resolver = new MemberLookupResolver(text, cp);
    Set<MemberLookupResult> results = resolver.performMemberLookup(StringUtils.defaultString(StringUtils.substringBeforeLast(variableName, "."), variableName));
    for (MemberLookupResult result : results) {
      Matcher m = GETTER_PATTERN.matcher(result.getMethodName());
      String memberName = result.getVariableName() + ".";
      if (m.matches() && m.groupCount() >= 2) {
        memberName += WordUtils.uncapitalize(m.group(2));
      }
      else {
        memberName += WordUtils.uncapitalize(result.getMethodName());
      }
      if (StringUtils.startsWith(memberName,variableName) && !items.containsKey(memberName)) {
        items.put(memberName, new JavaElementCompletionItem(result.getElement(), memberName, false, startOffset, caretOffset));
      }
    }
    return items.values();
  }

  private static final Pattern LAST_DOLLAR_CURLYBRACE = Pattern.compile("(\\$\\{)(?!.*\\})");
  private static final Pattern LAST_VARIABLE = Pattern.compile("([a-zA-Z0-9.])+$");

  @Override
  public int indexOfStartCharacter(char[] line) {
    //TODO: change to check if there is a data-sly-use before the class
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
