<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/class">
    package <xsl:value-of select="@package"/>;

    /*
     *  Auto generated java implementation by the Axis code generator
    */

    public class <xsl:value-of select="@name"></xsl:value-of> implements <xsl:value-of select="@interfaceName"></xsl:value-of>{
     <xsl:for-each select="method">
        public <xsl:value-of select="output/param/@type"></xsl:value-of><xsl:text> </xsl:text><xsl:value-of select="@name"></xsl:value-of>(<xsl:value-of select="input/param/@type"></xsl:value-of><xsl:text> </xsl:text><xsl:value-of select="output/param/@name"></xsl:value-of>) throws java.rmi.RemoteException{
            return null; <!-- this needs to be changed -->
        }
     </xsl:for-each>
    }
    </xsl:template>
 </xsl:stylesheet>