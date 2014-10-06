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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 * Wrapper-Class for a Statement which has been parsed. A Statement is a combination of a command, a variable and a value. e.g. data-sly-use.foo=${bar}
 */
public final class ParsedStatement {

  /**
   * Pattern to find statements
   */
  public static final Pattern PATTERN = Pattern.compile(".*(data-sly-(use|list)).(.*)=\\\"(\\$\\{)?'?([^'\\}\\\"]*)'?(.*\\})?\\\"");

  /**
   * The command issued before the usage of the statement (e.g. data-sly-use)
   */
  private final String command;
  /**
   * The variable name (e.g. data-sly-use.VARIABLE)
   */
  private final String variable;
  /**
   * The variable's value (e.g. data-sly-use.variable=${value})
   */
  private final String value;

  /**
   *
   * @param command
   * @param variable
   * @param value
   */
  private ParsedStatement(String command, String variable, String value) {
    this.command = command;
    this.variable = variable;
    this.value = value;
  }

  /**
   *
   * @param matcher
   * @return
   */
  public static ParsedStatement fromMatcher(Matcher matcher) {
    if (matcher.groupCount() >= 5) {
      String command = matcher.group(1);
      String variable = matcher.group(3);
      String value = matcher.group(5);
      if (StringUtils.isNotBlank(command) && StringUtils.isNotBlank(variable) && StringUtils.isNotBlank(value)) {
        return new ParsedStatement(command, variable, value);
      }
    }
    return null;
  }

  public String getCommand() {
    return command;
  }

  public String getVariable() {
    return variable;
  }

  public String getValue() {
    return value;
  }


}
