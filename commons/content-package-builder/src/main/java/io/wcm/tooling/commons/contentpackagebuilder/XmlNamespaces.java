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

import com.google.common.collect.ImmutableMap;
import java.util.Map;

/**
 * XML namespaces supported by default.
 */
public final class XmlNamespaces {

  /**
   * JCR namespace
   */
  public static final String NS_JCR = "http://www.jcp.org/jcr/1.0";

  /**
   * JCR nodetype namespace
   */
  public static final String NS_JCR_NT = "http://www.jcp.org/jcr/nt/1.0";

  /**
   * JCR "rep" internal namespace
   */
  public static final String NS_JCR_REP = "internal";

  /**
   * CQ namespace
   */
  public static final String NS_CQ = "http://www.day.com/jcr/cq/1.0";

  /**
   * CRX namespace
   */
  public static final String NS_CRX = "http://www.day.com/crx/1.0";

  /**
   * Sling namespace
   */
  public static final String NS_SLING = "http://sling.apache.org/jcr/sling/1.0";

  /**
   * OAK namespace
   */
  public static final String NS_OAK = "http://jackrabbit.apache.org/oak/ns/1.0";

  /**
   * XML Namespaces support by default for JCR content XML.
   */
  public static final Map<String, String> DEFAULT_NAMESPACES = ImmutableMap.<String, String>builder()
      .put("jcr", NS_JCR)
      .put("nt", NS_JCR_NT)
      .put("rep", NS_JCR_REP)
      .put("cq", NS_CQ)
      .put("crx", NS_CRX)
      .put("sling", NS_SLING)
      .put("oak", NS_OAK)
      .build();

  private XmlNamespaces() {
    // constants only
  }

}
