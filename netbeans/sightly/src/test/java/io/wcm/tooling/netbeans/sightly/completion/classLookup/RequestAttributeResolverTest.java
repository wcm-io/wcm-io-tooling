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
package io.wcm.tooling.netbeans.sightly.completion.classLookup;

import io.wcm.tooling.netbeans.sightly.completion.BaseTest;
import java.net.URL;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

/**
 *
 */
public class RequestAttributeResolverTest extends BaseTest {

  @Test
  public void testRequestAttributeLookup() {
    String text = "data-sly-use.foo=${'io.wcm.tooling.netbeans.sightly.TestClass' @ ";
    RequestAttributeResolver resolver = new RequestAttributeResolver(text, ClassPathSupport.createClassPath(new URL[0]));
    Set<String> results = resolver.resolve("");
    assertEquals("io.wcm.tooling.netbeans.sightly.TestClass' @", 3, results.size());
    assertEquals("requestAttribute1", results.iterator().next());
    results = resolver.resolve("requestAttr");
    assertEquals("requestAttr ", 2, results.size());
    assertEquals("requestAttribute1", results.iterator().next());
    results = resolver.resolve("requestAttribute2");
    assertEquals("requestAttribute2 ", 1, results.size());
    assertEquals("requestAttribute2", results.iterator().next());
    text = "data-sly-use.foo=${'io.wcm.tooling.netbeans.sightly.TestClass' @ requestAttribute1 = foo, ";
    resolver = new RequestAttributeResolver(text, ClassPathSupport.createClassPath(new URL[0]));
    results = resolver.resolve("");
    assertEquals("io.wcm.tooling.netbeans.sightly.TestClass' @ requestAttribute1 = foo, ", 2, results.size());
    assertEquals("requestAttribute2", results.iterator().next());
  }

  public void testRequestAttributeLookupNoResults() {
    String text = "data-sly-use.foo=${'io.wcm.tooling.netbeans.sightly.AnotherTestClass' @ ";
    RequestAttributeResolver resolver = new RequestAttributeResolver(text, ClassPathSupport.createClassPath(new URL[0]));
    assertEquals("io.wcm.tooling.netbeans.sightly.AnotherTestClass", 0, resolver.resolve("").size());
  }
  public void testInvalidClassLookup() {
    String text = "data-sly-use.foo=${'io.wcm.tooling.netbeans.sightly.NonExistingClass' @ ";
    RequestAttributeResolver resolver = new RequestAttributeResolver(text, ClassPathSupport.createClassPath(new URL[0]));
    assertEquals("io.wcm.tooling.netbeans.sightly.NonExistingClass", 0, resolver.resolve("").size());
  }

}