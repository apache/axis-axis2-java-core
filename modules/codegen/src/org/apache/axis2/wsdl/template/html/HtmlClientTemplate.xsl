<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>
    <xsl:template match="/class">
        <html>
            <header>
                <title>Auto generated access page for <xsl:value-of select="@name"/></title>

                <!-- generate a javascript here -->

            </header>
            <body>
                <xsl:variable name="endpoint"><xsl:value-of select="endpoint"/></xsl:variable>
                <xsl:for-each select="method">
                    <h2>Access Form for <xsl:value-of select="@name"/></h2>
                    <form method="post" action="#dosomething">
                        <xsl:attribute name="action"><xsl:value-of select="$endpoint"/></xsl:attribute>
                    <xsl:for-each select="input/param[@location='body']">
                        parameter(<xsl:value-of select="@name"/>[<xsl:value-of select="@type"/> ]) <input type="text"/>
                    </xsl:for-each>
                     <input type="submit" value="Send Request"></input>
                     <input type="reset" value="Clear all"></input>
                    </form>
                </xsl:for-each>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>