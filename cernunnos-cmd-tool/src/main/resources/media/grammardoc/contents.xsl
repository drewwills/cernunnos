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

    <xsl:template match="/">
        <html>
            <head>
                <title>Cernunnos Manual:  Contents Frame</title>
                <link rel="stylesheet" type="text/css" href="structural.css"/>
                <link rel="stylesheet" type="text/css" href="classes.css"/>
                <base target="mainFrame"/>
            </head>
            <body>
                <h2><img height="96" width="96" src="Cernunnos.jpg" alt="Cernunnos logo"/>Contents</h2>
                <a href="introduction.html">Introduction</a>
                <br/>
                <a href="request-attributes.html">Request Attributes</a>
                <br/>
                <a href="anatomy-of-a-task.html">Anatomy of a Task</a>
                <br/>
                <a href="cernunnos-and-maven.html">Cernunnos &amp; Maven</a>
                <br/>
            </body>
        </html>
    </xsl:template>

</xsl:transform>