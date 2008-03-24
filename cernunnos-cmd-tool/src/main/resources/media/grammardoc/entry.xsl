<!--
   Copyright 2007 Andrew Wills

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="entry">
    <html>
      <head>
        <title><xsl:value-of select="name"/></title>
                <link rel="stylesheet" type="text/css" href="../styles.css"/>
      </head>
      <body>
		<div align="center">
			<h1>Cernunnos Manual</h1>
		</div>
		<hr/>
		<xsl:choose>
		  <xsl:when test="@type = 'PHRASE'">
			<xsl:apply-templates select="name" mode="Phrase"/>
		  </xsl:when>
		  <xsl:when test="@type = 'TASK'">
			<xsl:apply-templates select="name" mode="Task"/>
		  </xsl:when>
		</xsl:choose>
        <h3>Description:</h3>
        <xsl:copy-of select="description/*"/>
        <h3>Reagents:</h3>
        <table with="100%" border="1" cellspacing="0" cellpadding="0">
          <tr>
            <th>Name</th>
            <th>XPath</th>
            <th>Description</th>
            <th>Reagent Type</th>
            <th>Expected Type</th>
            <th>Required</th>
          </tr>
          <xsl:apply-templates select="formula/reagents/reagent"/>
        </table>
        <h3>Examples:</h3>
		<xsl:apply-templates select="example"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="name" mode="Phrase">
	<h2><code>${<xsl:value-of select="."/>()}</code></h2>
  </xsl:template>

  <xsl:template match="name" mode="Task">
	<h2><code>&lt;<xsl:value-of select="."/>&gt;</code></h2>
  </xsl:template>

  <xsl:template match="reagent">
    <tr>
      <td><xsl:value-of select="@name"/></td>
      <td><xsl:value-of select="@xpath"/></td>
      <td><xsl:value-of select="description"/></td>
      <td><xsl:value-of select="@reagent-type"/></td>
      <td><xsl:value-of select="@expected-type"/></td>
      <td><xsl:value-of select="@required"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="example">
  	<p><xsl:value-of select="@caption"/>:</p>
	<xsl:copy-of select="*"/>
  </xsl:template>

</xsl:transform>