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
package io.wcm.maven.buildtools.testcase.eclipsecheckstyle.warnings;

/**
 * Test code style checks
 * Expected warnings: 5
 */
class CodeStyle {

  public static final int STATIC_MEMBER = 1;

  int directStaticAccessToStaticMember() {
    // no warning
    return CodeStyle.STATIC_MEMBER;
  }

  int nonStaticAccessToStaticMember() {
    // 1 warning
    return this.STATIC_MEMBER;
  }

  int indirectStaticAccessToStaticMember() {
    // 1 warning
    return CodeStyleSub.STATIC_MEMBER;
  }

  // 1 warning
  void undocumentedEmptyBlock() {

  }

  // 2 warning (method has constructor name, method name not correct)
  void CodeStyle() {
    // empty
  }

  private static class CodeStyleSub extends CodeStyle {
    // empty
  }

}
