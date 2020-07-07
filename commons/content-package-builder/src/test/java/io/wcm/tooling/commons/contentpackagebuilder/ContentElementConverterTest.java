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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElement;
import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElementImpl;

class ContentElementConverterTest {

  @Test
  void testToMap() {
    ContentElement root = new ContentElementImpl("root", ImmutableMap.of("kra", "vra", "krb", "vrb"));
    ContentElement o1 = new ContentElementImpl("o1", ImmutableMap.of("k1a", "v1a", "k1b", "v1b"));
    ContentElement o11 = new ContentElementImpl("o11", ImmutableMap.of("k11a", "v11a", "k11b", "v11b"));
    ContentElement o2 = new ContentElementImpl("o2", ImmutableMap.of("k2a", "v2a", "k2b", "v2b"));
    root.getChildren().put("o1", o1);
    root.getChildren().put("o2", o2);
    o1.getChildren().put("o11", o11);

    Map<String, Object> expected = ImmutableMap.of("kra", "vra", "krb", "vrb",
        "o1", ImmutableMap.of("k1a", "v1a", "k1b", "v1b",
            "o11", ImmutableMap.of("k11a", "v11a", "k11b", "v11b")),
        "o2", ImmutableMap.of("k2a", "v2a", "k2b", "v2b"));

    Map<String, Object> result = ContentElementConverter.toMap(root);
    assertEquals(expected, result);
  }

}
