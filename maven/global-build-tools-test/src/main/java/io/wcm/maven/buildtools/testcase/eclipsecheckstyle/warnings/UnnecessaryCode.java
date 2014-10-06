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

import java.io.IOException;

// 1 warning: unused import
import java.io.InputStream;

/**
 * Test unnecessary code
 * Expected warnings: 6
 *
 * *** WARNING: Please do not modify and save this class with default eclipse "save actions" enabled ***
 *
 */
class UnnecessaryCode {

  void valueOfLocalVariableNotUsed() {
    // 1 warning
    int x = 1;
  }

  void vaueOfParameterNotUsed(int pParam) {
    // no warning
  }

  // 1 warning
  private int mUnusedPrivateMember = 1;

  String unnecessaryCastOperation() {
    // 1 warning
    return (String)"dummy";
  }

  boolean unnecessaryInstanceOfOperation() {
    String x = "dummy";
    // 1 warning
    return (x instanceof String);
  }

  void unnecessaryDeclarationOfThrownException() throws IOException {
    // 1 warning
  }

  int unusedBreakContinueLabel() {
    // 1 warning
    label: return 0;
  }

}
