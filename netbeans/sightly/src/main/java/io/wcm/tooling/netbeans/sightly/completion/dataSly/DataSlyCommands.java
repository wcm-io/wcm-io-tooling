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

/**
 * enum with all data-sly commands
 */
public enum DataSlyCommands {
  /**
   * Attribute
   */
  DATA_SLY_ATTRIBUTE("data-sly-attribute", true),
  /**
   * Call
   */
  DATA_SLY_CALL("data-sly-call"),
  /**
   * Element
   */
  DATA_SLY_ELEMENT("data-sly-element"),
  /**
   * Include
   */
  DATA_SLY_INCLUDE("data-sly-include"),
  /**
   * List
   */
  DATA_SLY_LIST("data-sly-list", true),
  /**
   * Resource
   */
  DATA_SLY_RESOURCE("data-sly-resource"),
  /**
   * Test
   */
  DATA_SLY_TEST("data-sly-test"),
  /**
   * Text
   */
  DATA_SLY_TEXT("data-sly-text"),
  /**
   * Template
   */
  DATA_SLY_TEMPLATE("data-sly-template", true),
  /**
   * Use
   */
  DATA_SLY_USE("data-sly-use", true),
  /**
   * Unwrap
   */
  DATA_SLY_UNWRAP("data-sly-unwrap");

  private final String command;
  private final boolean allowsVariables;

  /**
   * constructor for commands which don't allow variables
   */
  DataSlyCommands(String command) {
    this(command, false);
  }

  /**
   * constructor
   *
   * @param command
   * @param allowsVariables
   */
  DataSlyCommands(String command, boolean allowsVariables) {
    this.command = command;
    this.allowsVariables = allowsVariables;
  }

  /**
   * @return the autocompletion text
   */
  public String getCommand() {
    return command;
  }

  /**
   *
   * @return if the command allows the usage of variables (e.g. data-sly-use.VARIABLE)
   */
  public boolean allowsVariables() {
    return allowsVariables;
  }

}
