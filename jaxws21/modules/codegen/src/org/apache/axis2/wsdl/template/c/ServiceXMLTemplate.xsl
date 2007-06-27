<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <xsl:template match="/">
        <xsl:comment> This file was auto-generated from WSDL </xsl:comment>
        <xsl:comment> by the Apache Axis2 version: #axisVersion# #today# </xsl:comment>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="interface">

        <service>
            <xsl:attribute name="name"><xsl:value-of select="@servicename"/></xsl:attribute>

            <parameter name="ServiceClass">
                        <xsl:value-of select="@servicename"/>
            </parameter>
			<xsl:for-each select="method">
				<operation>
					<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
				</operation>
			</xsl:for-each>
        </service>
    </xsl:template>
</xsl:stylesheet>