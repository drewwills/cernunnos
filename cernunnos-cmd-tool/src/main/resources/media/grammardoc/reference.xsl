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

    <xsl:template name="main">
        <xsl:param name="showdeprecated"/>
        <html>
            <head>
                <title>Cernunnos Manual:  Reference Frame</title>
                <link rel="stylesheet" type="text/css" href="structural.css"/>
                <link rel="stylesheet" type="text/css" href="classes.css"/>
                <base target="mainFrame"/>
            </head>
            <body>
                <h2>Reference</h2>
                <input id="hideDeprecated" type="checkbox">
                    <xsl:choose>
                        <xsl:when test="$showdeprecated = 'true'">
                            <xsl:attribute name="onclick">window.location = 'reference-hidedeprecated.html'</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="checked">true</xsl:attribute>
                            <xsl:attribute name="onclick">window.location = 'reference-showdeprecated.html'</xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </input>
                <label for="hideDeprecated">Hide Deprecated Items</label>
                <h3>Phrases</h3>
                <xsl:apply-templates select="entry[@type = 'PHRASE']">
                    <xsl:sort select="name"/>
                    <xsl:with-param name="showdeprecated"><xsl:value-of select="$showdeprecated"/></xsl:with-param>
                </xsl:apply-templates>
                <br/>
                <h3>Tasks</h3>
                <xsl:apply-templates select="entry[@type = 'TASK']">
                    <xsl:sort select="name"/>
                    <xsl:with-param name="showdeprecated"><xsl:value-of select="$showdeprecated"/></xsl:with-param>
                </xsl:apply-templates>
                <br/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="entry">
        <xsl:param name="showdeprecated"/>
        <xsl:if test="not(deprecation) or $showdeprecated = 'true'">
            <a href="entries/{name}-{@type}.html"><xsl:value-of select="name"/><xsl:if test="deprecation"><img src="deprecated.jpg" style="border: 0px"/></xsl:if></a><br/>
        </xsl:if>
    </xsl:template>

</xsl:transform>