<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/interface">
    namespace <xsl:value-of select="@package"/>;

    //
    // Auto generated C# interface by the Axis code generator
    //

    public interface <xsl:value-of select="@name"></xsl:value-of> {
     <xsl:for-each select="method">
        <xsl:value-of select="output/param/@type"></xsl:value-of><xsl:text> </xsl:text><xsl:value-of select="@name"></xsl:value-of>(<xsl:value-of select="input/param/@type"></xsl:value-of><xsl:text> </xsl:text><xsl:value-of select="output/param/@name"></xsl:value-of>);
     </xsl:for-each>
    }
    </xsl:template>
 </xsl:stylesheet>