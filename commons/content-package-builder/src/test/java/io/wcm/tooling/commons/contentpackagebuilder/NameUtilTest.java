/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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

import static io.wcm.tooling.commons.contentpackagebuilder.NameUtil.isValidName;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NameUtilTest {

  @Test
  void testValidNames() {
    assertTrue(isValidName("my-name"));
    assertTrue(isValidName("abc def"));
    assertTrue(isValidName("my-äöüß€"));
    assertTrue(isValidName("jcr:primaryType"));
  }

  @Test
  void testInvalidNames() {
    assertFalse(isValidName(null));
    assertFalse(isValidName(""));
    assertFalse(isValidName("*"));
    assertFalse(isValidName("my/name"));
    assertFalse(isValidName("my[name]"));
    assertFalse(isValidName("jcr:primary:Type"));
  }

}
