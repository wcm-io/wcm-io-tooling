/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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

import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableMap;

public class PageXmlBuilderTest {

  private PageXmlBuilder underTest;

  @Before
  public void setUp() {
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(PageXmlBuilder.BUILTIN_NAMESPACES));
    underTest = new PageXmlBuilder();
  }

  @Test
  public void testSimpleMap() throws Exception {
    Document doc = underTest.build(ImmutableMap.<String, Object>of("var1", "v1",
        "var2", 55,
        "var3", new String[] {
        "v1", "v2", "v3"
    }));
    XMLAssert.assertXpathEvaluatesTo("cq:Page", "/jcr:root/@jcr:primaryType", doc);
    XMLAssert.assertXpathEvaluatesTo("cq:PageContent", "/jcr:root/jcr:content/@jcr:primaryType", doc);
    XMLAssert.assertXpathEvaluatesTo("v1", "/jcr:root/jcr:content/@var1", doc);
    XMLAssert.assertXpathEvaluatesTo("{Long}55", "/jcr:root/jcr:content/@var2", doc);
    XMLAssert.assertXpathEvaluatesTo("[v1,v2,v3]", "/jcr:root/jcr:content/@var3", doc);
  }

  @Test
  public void testNestedMap() throws Exception {
    Document doc = underTest.build(ImmutableMap.<String, Object>of("var1", "v1",
        "var2", 55,
        "node1", ImmutableMap.<String, Object>of(PageXmlBuilder.PN_PRIMARY_TYPE, "myNodeType", "var3", "v3"),
        "node2", ImmutableMap.<String, Object>of("var4", "v4")
        ));

    XMLAssert.assertXpathEvaluatesTo("cq:Page", "/jcr:root/@jcr:primaryType", doc);
    XMLAssert.assertXpathEvaluatesTo("cq:PageContent", "/jcr:root/jcr:content/@jcr:primaryType", doc);
    XMLAssert.assertXpathEvaluatesTo("v1", "/jcr:root/jcr:content/@var1", doc);
    XMLAssert.assertXpathEvaluatesTo("{Long}55", "/jcr:root/jcr:content/@var2", doc);

    XMLAssert.assertXpathEvaluatesTo("myNodeType", "/jcr:root/jcr:content/node1/@jcr:primaryType", doc);
    XMLAssert.assertXpathEvaluatesTo("v3", "/jcr:root/jcr:content/node1/@var3", doc);

    XMLAssert.assertXpathEvaluatesTo("nt:unstructured", "/jcr:root/jcr:content/node2/@jcr:primaryType", doc);
    XMLAssert.assertXpathEvaluatesTo("v4", "/jcr:root/jcr:content/node2/@var4", doc);
  }

}
