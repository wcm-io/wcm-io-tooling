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
package io.wcm.maven.buildtools.testcase.eclipsecheckstyle.javadoc;

/**
 * Test for malformed javadoc comments.
 * Expected warnings: 10
 */
public class MalformedJavadocPublic {

  /**
   * This is a malformed javadoc comment. {@
   */
  public void malformedJavadocComment1() {
    // 2 warnings
  }

  /**
   * This is a malformed javadoc comment. {@}
   */
  public void malformedJavadocComment2() {
    // 2 warnings
  }

  /**
   * Invalid tag argument
   * @param pParamInvalid
   * @param param
   */
  public void invalidTagArgumentParam(int param) {
    // 1 warning
  }

  /**
   * Invalid tag argument
   * @throws Exception
   */
  public void invalidTagArgumentThrows() {
    // 1 warning
  }

  /**
   * Invalid tag argument
   * @exception Exception
   */
  public void invalidTagArgumentException() {
    // 1 warning
  }

  /**
   * Invalid tag argument
   * @see InvalidClass
   */
  public void invalidTagArgumentSee() {
    // 1 warning
  }

  /**
   * Invalid tag argument
   * @link
   */
  public void invalidTagArgumentLink() {
    // 1 warning
  }

  /**
   * Invalid tag argument
   * @return
   */
  public int missingReturngTagDescription() {
    // 1 warning
    return 0;
  }

}
