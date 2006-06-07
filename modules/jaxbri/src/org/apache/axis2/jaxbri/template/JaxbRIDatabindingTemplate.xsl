<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
<!-- #################################################################################  -->
    <!-- ############################   JiBX template   ##############################  -->
    <xsl:template match="databinders[@dbtype='jaxbri']">

        <xsl:variable name="base64"><xsl:value-of select="base64Elements/name"/></xsl:variable>
        <xsl:if test="$base64">
            private static javax.xml.namespace.QName[] qNameArray = {
            <xsl:for-each select="base64Elements/name">
                <xsl:if test="position()>1">,</xsl:if>new javax.xml.namespace.QName("<xsl:value-of select="@ns-url"/>","<xsl:value-of select="@localName"/>")
            </xsl:for-each>
            };
        </xsl:if>

        <xsl:variable name="firstType"><xsl:value-of select="param[1]/@type"/></xsl:variable>

        <xsl:for-each select="param">
            <xsl:if test="@type!=''">

                private org.apache.axiom.om.OMElement toOM(<xsl:value-of select="@type"/> param, org.apache.axiom.soap.SOAPFactory factory, boolean optimizeContent) {
                    try {
                        javax.xml.bind.JAXBContext context = javax.xml.bind.JAXBContext.newInstance( <xsl:value-of select="@type"/>.class );
                        org.apache.axiom.om.impl.builder.SAXOMBuilder builder = new org.apache.axiom.om.impl.builder.SAXOMBuilder();
                        javax.xml.bind.Marshaller marshaller = context.createMarshaller();
                        marshaller.marshal(param, builder);
                        return builder.getRootElement();
                    } catch (javax.xml.bind.JAXBException bex){
                        throw new RuntimeException(bex);
                    }
                }

                private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="@type"/> param, boolean optimizeContent) {
                    org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
                    if (param != null){
                        envelope.getBody().addChild(toOM(param, factory, optimizeContent));
                    }
                    return envelope;
                }

            </xsl:if>
        </xsl:for-each>

        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory) {
            return factory.getDefaultEnvelope();
        }

        private java.lang.Object fromOM (
            org.apache.axiom.om.OMElement param,
            java.lang.Class type,
            java.util.Map extraNamespaces) {
            try {
                javax.xml.bind.JAXBContext context = javax.xml.bind.JAXBContext.newInstance( type );
                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                return unmarshaller.unmarshal(param.getXMLStreamReader(), type).getValue();
            } catch (javax.xml.bind.JAXBException bex){
                throw new RuntimeException(bex);
            }
        }

    </xsl:template>
    </xsl:stylesheet>