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

import org.openide.util.NbBundle;

/**
 * enum with all data-sly commands
 */
public enum DataSlyCommands {
  /**
   * Attribute
   */
  DATA_SLY_ATTRIBUTE("data-sly-attribute", "dataSlyAttribute_documentation", true),
  /**
   * Call
   */
  DATA_SLY_CALL("data-sly-call", "dataSlyCall_documentation"),
  /**
   * Element
   */
  DATA_SLY_ELEMENT("data-sly-element", "dataSlyElement_documentation"),
  /**
   * Include
   */
  DATA_SLY_INCLUDE("data-sly-include", "dataSlyInclude_documentation"),
  /**
   * List
   */
  DATA_SLY_LIST("data-sly-list", "dataSlyList_documentation", true),
  /**
   * Resource
   */
  DATA_SLY_RESOURCE("data-sly-resource", "dataSlyResource_documentation"),
  /**
   * Repeat
   */
  DATA_SLY_REPEAT("data-sly-repeat", "dataSlyRepeat_documentation"),
  /**
   * Test
   */
  DATA_SLY_TEST("data-sly-test", "dataSlyTest_documentation"),
  /**
   * Text
   */
  DATA_SLY_TEXT("data-sly-text", "dataSlyText_documentation"),
  /**
   * Template
   */
  DATA_SLY_TEMPLATE("data-sly-template", "dataSlyTemplate_documentation", true),
  /**
   * Use
   */
  DATA_SLY_USE("data-sly-use", "dataSlyUse_documentation", true),
  /**
   * Unwrap
   */
  DATA_SLY_UNWRAP("data-sly-unwrap", "dataSlyUnwrap_documentation");

  private final String command;
  private final String documentation;
  private final boolean allowsVariables;

  /**
   * constructor for commands which don't allow variables
   */
  DataSlyCommands(String command, String documentation) {
    this(command, documentation, false);
  }

  /**
   * constructor
   *
   * @param command
   * @param allowsVariables
   */
  DataSlyCommands(String command, String documentationKey, boolean allowsVariables) {
    this.command = command;
    this.documentation = documentationKey != null ? NbBundle.getMessage(DataSlyCommands.class, documentationKey) : null;
    this.allowsVariables = allowsVariables;
  }

  /**
   * @return the autocompletion text
   */
  public String getCommand() {
    return command;
  }

  public String getDocumentation() {
    return documentation;
  }

  /**
   *
   * @return if the command allows the usage of variables (e.g. data-sly-use.VARIABLE)
   */
  public boolean allowsVariables() {
    return allowsVariables;
  }

}
