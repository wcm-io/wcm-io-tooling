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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Test potentional programming problems
 * Expected warnings: 11
 */
class PotentionalProgrammingProblems {

  boolean comparingIdenticalValues() {
    // 1 warning
    return 1 == 1;
  }

  void assignmentHasNoEffect() {
    int x = 1;
    // 1 warning
    x = x;
  }

  boolean possibleAccidentalBooleanAssignment() {
    boolean x = true;
    // 1 warning
    if (x = false) {
      x = true;
    }
    return x;
  }

  String usingCharArrayInStringConcatenation() {
    // 1 warning
    return "hello" + new char[]{'w','o','r','l','d'};
  }

  boolean emptyStatement() {
    // 1 warning
    boolean x = true;;
    return x;
  }

  void hiddenCatchBlock() {
    try {
      throw new java.io.CharConversionException();
    }
    catch (java.io.CharConversionException e) {
      // ignore
    } catch (java.io.IOException e) {
      // 1 warning
    }
  }

  int finallyDosNotCompleteNormally() {
    try {
      // nothing to do
    }
    // 1 warning
    finally {
      return 1;
    }
  }

  int deadCode() {
    if (false) {
      // 1 warning
      return 1;
    }
    else {
      return 0;
    }
  }

  int resourceLeak() throws IOException {
    // 1 warning (eclipse 4.2)
    InputStream is = new FileInputStream("dummy");
    return is.available();
  }

  public static class SerializableClassWithoutSerialVersionId extends Exception {
    // 1 warning
  }

  public static class EqualsNoHashCode {
    // 1 warning
    @Override
    public boolean equals(Object pObj) {
      return false;
    }
  }

}
