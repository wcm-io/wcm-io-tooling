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

import io.wcm.maven.buildtools.testcase.eclipsecheckstyle.dummy.ClassWithPackageVisibleMethod;

/**
 * Test name shadowing and conflicts
 * Expected warnings: 4
 */
class NameShadowingConflicts {

  protected int memberVariable;

  public static class FieldDeclarationHidesAnotherFieldVariable extends NameShadowingConflicts {
    // 1 warning
    protected int memberVariable;
  }

  int localVariableHidesAnotherFieldOrVariable() {
    int x = 1;
    new Comparable() {

      @Override
      public int compareTo(Object o) {
        // 1 warning
        int x = 2;
        return x;
      }

    };
    return x;
  }

  static class MethodDoesNotOveridePackageVisibleMethod extends ClassWithPackageVisibleMethod {
    public void packageVisibleMethod() {
      // 1 warning
    }
  }

  interface InterfaceMethodConflictsWithProtectedObjectMethod {
    // 1 warning
    int clone();
  }

}
