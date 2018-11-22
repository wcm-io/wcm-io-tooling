<?xml version="1.0" encoding="UTF-8"?>
<!--
  #%L
  wcm.io
  %%
  Copyright (C) 2018 wcm.io
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="html" doctype-system="http://www.w3.org/TR/html4/strict.dtd" doctype-public="-//W3C//DTD HTML 4.01//EN" />

<xsl:variable name="spacing-level">
  <xsl:choose>
    <xsl:when test="/*/@spacing-level"><xsl:value-of select="/*/@spacing-level"/></xsl:when>
    <xsl:otherwise>4</xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<xsl:template match="repository-structure">
  <xsl:variable name="title">Repository structure: <xsl:value-of select="@project"/></xsl:variable>

  <html>
    <head>
      <title><xsl:value-of select="$title"/></title>
      <style type="text/css">
        body { font-family: Verdana, Helvetica, Sans Serif; }
        h1 { font-size: 18px; }
        .structure { font-family:Lucida Console, Courier, Monospaced;font-size:12px;line-height:12px; }
        .description { font-family:Verdana, Helvetica;font-size:10px; }
      </style>
    </head>
    <body>
      <h1><xsl:value-of select="$title"/></h1>
      <div class="structure">
        <xsl:apply-templates select="*" mode="structure"/>
      </div>
    </body>
  </html>
</xsl:template>

<xsl:template match="*" mode="structure">
  <xsl:param name="level" select="0"/>

  <xsl:if test="$level &lt; $spacing-level and ($level &gt; 0 or preceding-sibling::*)">
    <xsl:apply-templates select="." mode="indent_space">
      <xsl:with-param name="node" select="."/>
      <xsl:with-param name="level" select="$level"/>
    </xsl:apply-templates>
    <br/>
  </xsl:if>

  <xsl:apply-templates select="." mode="indent">
    <xsl:with-param name="node" select="."/>
    <xsl:with-param name="level" select="$level"/>
  </xsl:apply-templates>

  <xsl:choose>
    <xsl:when test="@label!=''">
      <xsl:value-of select="@label"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="name(.)"/>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:if test="@description">
    <xsl:text>&#160;&#160;</xsl:text>
    <span class="description">
      <xsl:text>(</xsl:text>
      <xsl:value-of select="@description"/>
      <xsl:text>)</xsl:text>
    </span>
  </xsl:if>
  <br/>

  <xsl:apply-templates select="*" mode="structure">
    <xsl:with-param name="level" select="$level + 1"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*" mode="indent">
  <xsl:param name="node"/>
  <xsl:param name="level"/>

  <xsl:if test="not(parent::repository-structure)">
    <xsl:apply-templates select="parent::*" mode="indent">
      <xsl:with-param name="node" select="$node"/>
    </xsl:apply-templates>
  </xsl:if>

  <xsl:choose>
    <xsl:when test="generate-id($node)=generate-id(.)">
      <xsl:text>+-&#160;</xsl:text>
    </xsl:when>
    <xsl:when test="following-sibling::*">
      <xsl:text>|&#160;&#160;</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>&#160;&#160;&#160;</xsl:text>
    </xsl:otherwise>
  </xsl:choose>

</xsl:template>

<xsl:template match="*" mode="indent_space">
  <xsl:param name="node"/>
  <xsl:param name="level"/>

  <xsl:if test="not(parent::repository-structure)">
    <xsl:apply-templates select="parent::*" mode="indent">
      <xsl:with-param name="node" select="$node"/>
    </xsl:apply-templates>
  </xsl:if>

  <xsl:choose>
    <xsl:when test="generate-id($node)=generate-id(.)">
      <xsl:text>|&#160;&#160;</xsl:text>
    </xsl:when>
    <xsl:when test="following-sibling::*">
      <xsl:text>|&#160;&#160;</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>&#160;&#160;&#160;</xsl:text>
    </xsl:otherwise>
  </xsl:choose>

</xsl:template>

</xsl:stylesheet>
