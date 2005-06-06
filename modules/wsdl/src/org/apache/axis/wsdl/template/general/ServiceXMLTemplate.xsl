<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="/interface">
    <xsl:comment>Auto generated Axis Service XML</xsl:comment>
    <service><xsl:attribute name="name"><xsl:value-of select="@servicename"/></xsl:attribute>
    <parameter name="ServiceClass" locked="xsd:false"><xsl:value-of select="@package"/>.<xsl:value-of select="@name"/></parameter>
    <xsl:for-each select="method">
         <xsl:comment>Mounting the method <xsl:value-of select="@name"/> </xsl:comment>
         <operation><xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute></operation>
     </xsl:for-each>
    </service>
    </xsl:template>
 </xsl:stylesheet>