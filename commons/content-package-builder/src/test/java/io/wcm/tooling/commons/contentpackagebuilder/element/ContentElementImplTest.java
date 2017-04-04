/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.tooling.commons.contentpackagebuilder.element;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class ContentElementImplTest {

  @Test
  public void testGetChild() throws Exception {
    ContentElement root = new ContentElementImpl(null, ImmutableMap.<String, Object>of("prop1", "value1"));
    ContentElement child1 = new ContentElementImpl("child1", ImmutableMap.<String, Object>of("prop1", "value2"));
    ContentElement child11 = new ContentElementImpl("child11", ImmutableMap.<String, Object>of("prop1", "value3"));
    ContentElement child2 = new ContentElementImpl("child2", ImmutableMap.<String, Object>of("prop1", "value4"));
    root.getChildren().put("child1", child1);
    root.getChildren().put("child2", child2);
    child1.getChildren().put("child11", child11);

    assertEquals("value1", root.getProperties().get("prop1"));

    ContentElement fetchedChild1 = root.getChild("child1");
    assertEquals("child1", fetchedChild1.getName());
    assertEquals("value2", fetchedChild1.getProperties().get("prop1"));

    ContentElement fetchedChild11 = root.getChild("child1/child11");
    assertEquals("child11", fetchedChild11.getName());
    assertEquals("value3", fetchedChild11.getProperties().get("prop1"));
  }

}
