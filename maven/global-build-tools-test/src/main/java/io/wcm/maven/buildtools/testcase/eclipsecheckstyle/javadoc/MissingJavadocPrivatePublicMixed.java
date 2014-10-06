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

/*
 * Test for missing javadoc comments.
 * Expected warnings: 0
 */
class MissingJavadocPrivatePublicMixed<T> implements Comparable<T> {

  // no warning
  public static final int CONSTANT = 1;

  // no warning
  protected final int variable1 = 1;

  // no warning
  protected int variable2 = 1;

  public void simpleMethod() {
    // no warning
  }

  /**
   */
  public void simpleMethodEmptyComment() {
    // no warning
  }

  /**
   * Missing method parameter javadoc
   */
  public void methodParameter(int param) {
    // no warning
  }

  /**
   * Missing return javadoc
   */
  public int returnValue() {
    // no warning
    return 0;
  }

  @Override
  public int compareTo(T o) {
    // no warning
    return 0;
  }

  /**
   * @return Object
   */
  public <T2 extends Object> T2 methodTypeParameter() {
    // no warning
    return null;
  }

  public static class InnerClass {
    // no warning
  }

}
