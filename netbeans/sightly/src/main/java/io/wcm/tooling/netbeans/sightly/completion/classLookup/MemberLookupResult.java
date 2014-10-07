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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 * Wrapper for the result of a lookup
 */
public class MemberLookupResult {

  private final String methodName;
  private final String variableName;
  private final String returnType;
  /**
   *
   * @param variableName
   * @param methodName
   * @param returnType
   */
  public MemberLookupResult(String variableName, String methodName, String returnType) {
    this.variableName = variableName;
    this.methodName = methodName;

    //TODO: this is a hack to get support for list, set, map, enumeration, array and collection
    String tmp = returnType;
    if (StringUtils.startsWith(returnType, List.class.getName())
            || StringUtils.startsWith(returnType, Set.class.getName())
            || StringUtils.startsWith(returnType, Map.class.getName())
            || StringUtils.startsWith(returnType, Iterator.class.getName())
            || StringUtils.startsWith(returnType, Enum.class.getName())
            || StringUtils.startsWith(returnType, Collection.class.getName())) {
      while (StringUtils.contains(tmp, "<") && StringUtils.contains(tmp, ">")) {
        tmp = StringUtils.substringBetween(tmp, "<", ">");
      }
      if (StringUtils.contains(tmp, ",")) {
        // we want the first variable
        tmp = StringUtils.substringBefore(tmp, ",");
      }
    }
    else if (StringUtils.endsWith(returnType, "[]")) {
      tmp = StringUtils.substringBeforeLast(returnType, "[]");
    }

    this.returnType = tmp;
  }

  /**
   *
   * @return the method's name
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * @return the variableName which was used for lookup
   */
  public String getVariableName() {
    return variableName;
  }

  /**
   *
   * @return the return type
   */
  public String getReturnType() {
    return returnType;
  }

  /**
   *
   * @param candidate
   * @return if the methodname mathces the candidate. This is the case if either the methodname is (get|is)candidate or candidate;
   */
  public boolean matches(String candidate) {
    return StringUtils.equalsIgnoreCase("get" + candidate, methodName)
            || StringUtils.equalsIgnoreCase("is" + candidate, methodName)
            || StringUtils.equalsIgnoreCase(candidate, this.variableName);
  }

}
