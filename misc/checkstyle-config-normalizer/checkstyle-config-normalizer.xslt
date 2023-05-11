<?xml version="1.0" encoding="UTF-8"?>
<!--
  Prints all checkstyle rules of the source document normalized as a plain text file.
  All modules are ordered alphabetically with parameters as key value pairs inlined.
  This helps generating diffs between different checkstyle rulesets with different formattings.
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions">
	
	<xsl:output method="text" encoding="UTF-8" indent="yes"/>
	
  <xsl:template match="/">
    <xsl:apply-templates select="." mode="modulelist"/>
  </xsl:template>  
  
  <xsl:template match="*" mode="modulelist">
    <xsl:param name="indent"/>
    <xsl:apply-templates select="module">
      <xsl:with-param name="indent" select="$indent"/>
      <xsl:sort select="@name"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="module">
    <xsl:param name="indent"/>
    <xsl:value-of select="$indent"/>
    <xsl:value-of select="@name"/>
      <xsl:if test="property">
        <xsl:text> (</xsl:text>
        <xsl:for-each select="property">
          <xsl:sort select="@name"/>
          <xsl:value-of select="@name"/>
          <xsl:text>=</xsl:text>
          <xsl:value-of select="@value"/>
          <xsl:if test="position() &lt; last()">
            <xsl:text>, </xsl:text>
          </xsl:if>
        </xsl:for-each>
        <xsl:text>)</xsl:text>
      </xsl:if>
    <xsl:text>
</xsl:text>
    <xsl:apply-templates select="." mode="modulelist">
      <xsl:with-param name="indent" select="concat($indent, '  ')"/>
    </xsl:apply-templates>
  </xsl:template>
	
</xsl:stylesheet>
