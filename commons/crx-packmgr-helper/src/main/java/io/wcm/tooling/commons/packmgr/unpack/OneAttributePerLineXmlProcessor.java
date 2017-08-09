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
package io.wcm.tooling.commons.packmgr.unpack;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format.TextMode;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.Walker;
import org.jdom2.util.NamespaceStack;

/**
 * XML output processor that renders one attribute per line for easier diff-ing on content changes.
 */
class OneAttributePerLineXmlProcessor extends AbstractXMLOutputProcessor {

  /**
   * This will handle printing of an {@link Element}.
   * <p>
   * This method arranges for outputting the Element infrastructure including
   * Namespace Declarations and Attributes.
   * @param out
   *          <code>Writer</code> to use.
   * @param fstack
   *          the FormatStack
   * @param nstack
   *          the NamespaceStack
   * @param element
   *          <code>Element</code> to write.
   * @throws IOException
   *           if the destination Writer fails
   */
  @Override
  protected void printElement(final Writer out, final FormatStack fstack,
      final NamespaceStack nstack, final Element element) throws IOException {

    nstack.push(element);
    try {
      final List<Content> content = element.getContent();

      // Print the beginning of the tag plus attributes and any
      // necessary namespace declarations
      write(out, "<");

      write(out, element.getQualifiedName());

      // Print the element's namespace, if appropriate
      for (final Namespace ns : nstack.addedForward()) {
        printNamespace(out, fstack, ns);
      }

      // Print out attributes
      if (element.hasAttributes()) {
        boolean printMultiLine = element.getAttributes().size() > 1
            || nstack.addedForward().iterator().hasNext();
        for (final Attribute attribute : element.getAttributes()) {
          printAttribute(out, fstack, attribute, printMultiLine);
        }
      }

      if (content.isEmpty()) {
        // Case content is empty
        if (fstack.isExpandEmptyElements()) {
          write(out, "></");
          write(out, element.getQualifiedName());
          write(out, ">");
        }
        else {
          write(out, "/>");
        }
        // nothing more to do.
        return;
      }

      // OK, we have real content to push.
      fstack.push();
      try {

        // Check for xml:space and adjust format settings
        final String space = element.getAttributeValue("space",
            Namespace.XML_NAMESPACE);

        if ("default".equals(space)) {
          fstack.setTextMode(fstack.getDefaultMode());
        }
        else if ("preserve".equals(space)) {
          fstack.setTextMode(TextMode.PRESERVE);
        }

        // note we ensure the FStack is right before creating the walker
        Walker walker = buildWalker(fstack, content, true);

        if (!walker.hasNext()) {
          // the walker has formatted out whatever content we had
          if (fstack.isExpandEmptyElements()) {
            write(out, "></");
            write(out, element.getQualifiedName());
            write(out, ">");
          }
          else {
            write(out, "/>");
          }
          // nothing more to do.
          return;
        }
        // we have some content.
        write(out, ">");
        if (!walker.isAllText()) {
          // we need to newline/indent
          textRaw(out, fstack.getPadBetween());
        }

        printContent(out, fstack, nstack, walker);

        if (!walker.isAllText()) {
          // we need to newline/indent
          textRaw(out, fstack.getPadLast());
        }
        write(out, "</");
        write(out, element.getQualifiedName());
        write(out, ">");

      }
      finally {
        fstack.pop();
      }
    }
    finally {
      nstack.pop();
    }

  }

  private void printAttribute(Writer out, FormatStack fstack, Attribute attribute, boolean printMultiLine) throws IOException {
    if (!attribute.isSpecified() && fstack.isSpecifiedAttributesOnly()) {
      return;
    }

    if (printMultiLine) {
      write(out, StringUtils.defaultString(fstack.getLineSeparator()));
      write(out, StringUtils.defaultString(fstack.getLevelIndent()));
      write(out, StringUtils.defaultString(fstack.getIndent()));
    }
    else {
      write(out, " ");
    }

    write(out, attribute.getQualifiedName());
    write(out, "=");

    write(out, "\"");
    attributeEscapedEntitiesFilter(out, fstack, attribute.getValue());
    write(out, "\"");
  }

}
