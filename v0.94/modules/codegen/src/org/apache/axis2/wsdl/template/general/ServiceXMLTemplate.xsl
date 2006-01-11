<xsl:stylesheet version="1.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" xalan:indent-amount="4"/>

    <xsl:template match="/interfaces">
        <xsl:comment>Auto generated Axis Services XML</xsl:comment>
        <serviceGroup>
            <xsl:apply-templates/>
        </serviceGroup>
    </xsl:template>

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="interface">
        <xsl:variable name="receiver"><xsl:value-of select="@messagereceiver"/></xsl:variable>
        <xsl:variable name="package"><xsl:value-of select="@classpackage"/></xsl:variable>

        <service>
            <xsl:attribute name="name"><xsl:value-of select="@servicename"/></xsl:attribute>
            <messageReceivers>
                <messageReceiver mep="http://www.w3.org/2004/08/wsdl/in-out">
                        <xsl:choose>
                            <xsl:when test="$package=''">
                                <xsl:attribute name="class"><xsl:value-of select="$receiver"/></xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="class"><xsl:value-of select="$package"/>.<xsl:value-of select="$receiver"/>
                                </xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                </messageReceiver>

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