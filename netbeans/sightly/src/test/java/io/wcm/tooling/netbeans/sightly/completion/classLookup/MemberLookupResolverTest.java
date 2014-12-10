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
import java.io.IOException;
import java.net.URL;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test for MemberLookupResolver
 */
public class MemberLookupResolverTest extends BaseTest {

  /**
   * Test of performMemberLookup method, of class MemberLookupResolver.
   *
   * @throws IOException
   */
  @Test
  public void testPerformMemberLookup() throws IOException {
    String text = getTestDocumentContent("MemberLookupCompleterTest.html");
    assertFalse(StringUtils.isBlank(text));
    MemberLookupResolver resolver = new MemberLookupResolver(text, ClassPathSupport.createClassPath(new URL[0]));
    assertEquals("testclass", 8, resolver.performMemberLookup("testclass").size());
    assertEquals("testclassWithoutBraces", 8, resolver.performMemberLookup("testclassWithoutBraces").size());
    assertEquals("multipleLines", 8, resolver.performMemberLookup("multipleLines").size());
    assertEquals("list", 2, resolver.performMemberLookup("list").size());
    assertEquals("unknown", 0, resolver.performMemberLookup("unknown").size());
    assertEquals("Empty String", 0, resolver.performMemberLookup("").size());
    assertEquals("NonExisting variable", 0, resolver.performMemberLookup("someRandomFoo").size());
  }

  /**
   * Tests recursive member lookup
   *
   * @throws IOException
   */
  @Test
  public void testRecursiveMemberLookup() throws IOException {
    String text = getTestDocumentContent("MemberLookupCompleterTest.html");
    assertFalse(StringUtils.isBlank(text));
    MemberLookupResolver resolver = new MemberLookupResolver(text, ClassPathSupport.createClassPath(new URL[0]));
    assertEquals("data-sly-list.recursive=\"${string.class\"", 51, resolver.performMemberLookup("recursive").size());
    assertEquals("data-sly-list.recursive2=\"${recursive.class}\"", 51, resolver.performMemberLookup("recursive2").size());
    assertEquals("data-sly-list.recursive3=\"${recursive2.class}\"", 51, resolver.performMemberLookup("recursive3").size());
  }

  /**
   * Tests nested lookups
   *
   * @throws IOException
   */
  @Test
  public void testNestedMemberLookup() throws IOException {
    String text = getTestDocumentContent("MemberLookupCompleterTest.html");
    assertFalse(StringUtils.isBlank(text));
    MemberLookupResolver resolver = new MemberLookupResolver(text, ClassPathSupport.createClassPath(new URL[0]));
    assertEquals("testclass", 8, resolver.performMemberLookup("testclass").size());
    assertEquals("testclass.class", 51, resolver.performMemberLookup("testclass.class").size());
    assertEquals("testclass.anotherTestClass", 3, resolver.performMemberLookup("testclass.anotherTestClass").size());
    assertEquals("testclass.anotherTestClass.testClass", 8, resolver.performMemberLookup("testclass.anotherTestClass.testClass").size());
    assertEquals("testclass.anotherTestClass.testClass.anotherTestClass", 3, resolver.performMemberLookup("testclass.anotherTestClass.testClass.anotherTestClass").size());
  }

}
