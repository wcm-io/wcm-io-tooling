/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.tooling.commons.contentpackagebuilder;

/**
 * Acess control (ACL) handling when importing packages with rep:policy nodes.
 */
public enum AcHandling {

  /**
   * Ignores the packaged access control and leaves the target unchanged.
   */
  IGNORE("ignore"),

  /**
   * Applies the access control provided with the package to the target. This also removes existing access control.
   */
  OVERWRITE("overwrite"),

  /**
   * Tries to merge access control provided with the package with the one on the target. This is currently not fully
   * supported and behaves like Overwrite for existing ACLs. ACLs not in the package are retained.
   */
  MERGE("merge"),

  /**
   * Tries to merge access control provided with the package with the one on the target. This is currently not fully
   * supported and behaves like Ignore for existing ACLs. ACLs not in the package are retained.
   */
  MERGE_PRESERVE("merge_preserve"),

  /**
   * Clears all access control on the target system.
   */
  CLEAR("clear");


  private final String mode;

  AcHandling(String mode) {
    this.mode = mode;
  }

  /**
   * @return ACL handling mode string for package properties.
   */
  public String getMode() {
    return mode;
  }

}
