<xsl:stylesheet version="1.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" xalan:indent-amount="4"/>

    <xsl:template match="/">
        <xsl:comment> This file was auto-generated from WSDL </xsl:comment>
        <xsl:comment> by the Apache Axis2 version: #axisVersion# #today# </xsl:comment>
        <serviceGroup>
            <xsl:apply-templates/>
        </serviceGroup>
    </xsl:template>

    <xsl:template match="interface">
        <xsl:variable name="package"><xsl:value-of select="@classpackage"/></xsl:variable>

        <service>
            <xsl:attribute name="name"><xsl:value-of select="@servicename"/></xsl:attribute>
            <messageReceivers>
                <xsl:for-each select="messagereceiver">
                    <xsl:if test=".">
                        <messageReceiver>
                            <xsl:attribute name="mep"><xsl:value-of select="@mep"/></xsl:attribute>
                            <xsl:choose>
                                <xsl:when test="$package=''">
                                    <xsl:attribute name="class"><xsl:value-of select="."/></xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class"><xsl:value-of select="$package"/>.<xsl:value-of select="."/></xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                        </messageReceiver>
                    </xsl:if>
                </xsl:for-each>
             </messageReceivers>

            <parameter name="ServiceClass" locked="false">
                <xsl:choose>
                    <xsl:when test="$package=''">
                        <xsl:value-of select="@name"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$package"/>.<xsl:value-of select="@name"/>
                    </xsl:otherwise>
                </xsl:choose>
            </parameter>
            <xsl:comment>All public methods of the service class are exposed by default</xsl:comment>
        </service>
    </xsl:template>
</xsl:stylesheet>