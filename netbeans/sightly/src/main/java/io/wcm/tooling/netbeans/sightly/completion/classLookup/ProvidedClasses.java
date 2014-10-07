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

/**
 * Enum to hold classes available for autocompletion
 */
public enum ProvidedClasses {
//TODO: add additional classes like request, see http://docs.adobe.com/docs/en/aem/6-0/develop/sightly.html Java-backed Objects

  /**
   * com.day.cq.wcm.api.Page
   */
  PAGE("currentPage",
          "language",
          "lastModifiedBy",
          "listChildren",
          "name",
          "navigationTitle",
          "pageTitle",
          "parent",
          "path",
          "properties",
          "tags",
          "title",
          "vanityUrl",
          "hasChild",
          "hasContent",
          "isHideInNav",
          "isLocked",
          "isValid"),
  /**
   * com.day.cq.wcm.api.WCMMode
   */
  WCMMODE("wcmmode",
          "edit",
          "preview",
          "disabled",
          "design");

  private final String name;
  private final String[] members;

  /**
   *
   * @param name used in sightly
   * @param members available members
   */
  ProvidedClasses(String name, String... members) {
    this.name = name;
    this.members = members;
  }

  /**
   * @return autocompletion text
   */
  public String getName() {
    return name;
  }

  /**
   * @return available members (name.member)
   */
  public String[] getMembers() {
    return members;
  }

}
