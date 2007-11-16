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

  <xsl:template match="/grammar">
    <html>
      <head>
        <title>Cernunnos Task &amp; Phrase Reference</title>
                <link rel="stylesheet" type="text/css" href="styles.css"/>
        <base target="mainFrame"/>
      </head>
      <body>
        <h1><img height="96" width="96" src="Cernunnos.jpg"/>Cernunnos</h1>
        <h2>Table of Contents</h2>
        <a href="introduction.html">Introduction</a>
        <h3>Phrases</h3>
        <xsl:apply-templates select="entry[@type = 'PHRASE']"><xsl:sort select="name"/></xsl:apply-templates>
        <br/>
        <h3>Tasks</h3>
        <xsl:apply-templates select="entry[@type = 'TASK']"><xsl:sort select="name"/></xsl:apply-templates>
        <br/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="entry">
    <a href="entries/{name}.html"><xsl:value-of select="name"/></a><br/>
  </xsl:template>

</xsl:transform>