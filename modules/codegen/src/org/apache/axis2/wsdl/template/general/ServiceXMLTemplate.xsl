<xsl:stylesheet version="1.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" xalan:indent-amount="4"/>
    <xsl:template match="/interface">
    <xsl:variable name="receiver"><xsl:value-of select="@messagereceiver"/></xsl:variable>
    <xsl:variable name="package"><xsl:value-of select="@classpackage"/></xsl:variable>

    <xsl:comment>Auto generated Axis Service XML</xsl:comment>
    <service><xsl:attribute name="name"><xsl:value-of select="@servicename"/></xsl:attribute>
    <parameter name="ServiceClass" locked="false">
         <xsl:choose>
            <xsl:when test="$package=''"><xsl:value-of select="@name"/></xsl:when>
            <xsl:otherwise> <xsl:value-of select="$package"/>.<xsl:value-of select="@name"/></xsl:otherwise>
        </xsl:choose>
       </parameter>
    <xsl:for-each select="method">
         <xsl:comment>Mounting the method <xsl:value-of select="@name"/> </xsl:comment>
         <operation><xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
             <xsl:if test="$receiver!=''">
             <messageReceiver>
              <xsl:choose>
                    <xsl:when test="$package=''"><xsl:attribute name="class"><xsl:value-of select="$receiver"/></xsl:attribute></xsl:when>
                    <xsl:otherwise><xsl:attribute name="class"><xsl:value-of select="$package"/>.<xsl:value-of select="$receiver"/></xsl:attribute></xsl:otherwise>
              </xsl:choose>
              </messageReceiver>
            </xsl:if>
         </operation>
     </xsl:for-each>
    </service>
    </xsl:template>
 </xsl:stylesheet>