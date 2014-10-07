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
 * Test null analysis
 * Expected warnings: 4
 */
class NullAnalysis {

  void nullPointerAccess() {
    Boolean x = null;
    // 1 warning
    x.booleanValue();
  }

  void redundantNullCheck1() {
    Boolean x = true;
    if (x == null) {
      // 2 warning (redundant null check + dead code)
    }
  }

  void redundantNullCheck2() {
    Boolean x = Boolean.valueOf(true);
    if (x == null) {
      if (x == null) {
        // 1 warning
      }
    }
  }

}
