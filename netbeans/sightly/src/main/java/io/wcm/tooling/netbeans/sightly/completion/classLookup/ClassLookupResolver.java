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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.apache.commons.lang.StringUtils;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 * Resolver for Class lookup
 */
public class ClassLookupResolver {

  private final ClasspathInfo classpath;

  public ClassLookupResolver(ClasspathInfo classpath) {
    this.classpath = classpath;
  }

  /**
   * returns a list with all classes matchting the given filter
   */
  public List<CompletionItem> resolve(String filter, int startOffset, int caretOffset) {
    final Set<ElementHandle<TypeElement>> result = classpath.getClassIndex().getDeclaredTypes("",
            ClassIndex.NameKind.PREFIX, EnumSet.of(ClassIndex.SearchScope.SOURCE));
    List<CompletionItem> ret = new ArrayList<>();

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

}
