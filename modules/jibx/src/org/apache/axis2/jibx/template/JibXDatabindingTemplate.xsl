<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
<!-- #################################################################################  -->
    <!-- ############################   JiBX template   ##############################  -->
    <xsl:template match="databinders[@dbtype='jibx']">

        <xsl:variable name="base64"><xsl:value-of select="base64Elements/name"/></xsl:variable>
        <xsl:if test="$base64">
            private static javax.xml.namespace.QName[] qNameArray = {
            <xsl:for-each select="base64Elements/name">
                <xsl:if test="position()>1">,</xsl:if>new javax.xml.namespace.QName("<xsl:value-of select="@ns-url"/>","<xsl:value-of select="@localName"/>")
            </xsl:for-each>
            };
        </xsl:if>

        <xsl:variable name="firstType"><xsl:value-of select="param[1]/@type"/></xsl:variable>

        <xsl:for-each select="param[not(@type = preceding-sibling::param/@type)]">
            <xsl:if test="@type!=''">

                <xsl:if test="position()=1">
                    private static final org.jibx.runtime.IBindingFactory bindingFactory;
                    static {
                        org.jibx.runtime.IBindingFactory factory = null;
                        try {
                            factory = org.jibx.runtime.BindingDirectory.getFactory(<xsl:value-of select="@type"/>.class);
                        } catch (Exception e) { /** intentionally empty - report error on usage attempt */ }
                        bindingFactory = factory;
                    };
                </xsl:if>

                private org.apache.axiom.om.OMElement toOM(<xsl:value-of select="@type"/> param, org.apache.axiom.soap.SOAPFactory factory, boolean optimizeContent) {
                    if (param instanceof org.jibx.runtime.IMarshallable){
                        if (bindingFactory == null) {
                            throw new RuntimeException("Could not find JiBX binding information for <xsl:value-of select='$firstType'/>, JiBX binding unusable");
                        }
                        org.jibx.runtime.IMarshallable marshallable =
                            (org.jibx.runtime.IMarshallable)param;
                        int index = marshallable.JiBX_getIndex();
                        org.apache.axis2.jibx.JiBXDataSource source =
                            new org.apache.axis2.jibx.JiBXDataSource(marshallable, bindingFactory);
                        org.apache.axiom.om.OMNamespace namespace = factory.createOMNamespace(bindingFactory.getElementNamespaces()[index], null);
                        return factory.createOMElement(source, bindingFactory.getElementNames()[index], namespace);
                    } else {
                        throw new RuntimeException("No JiBX &lt;mapping> defined for class <xsl:value-of select='@type'/>");
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

        private java.lang.Object fromOM(
            org.apache.axiom.om.OMElement param,
            java.lang.Class type,
            java.util.Map extraNamespaces) {
            try {
                if (bindingFactory == null) {
                    throw new RuntimeException("Could not find JiBX binding information for com.sosnoski.seismic.jibxsoap.Query, JiBX binding unusable");
                }
                org.jibx.runtime.impl.UnmarshallingContext ctx =
                    (org.jibx.runtime.impl.UnmarshallingContext)bindingFactory.createUnmarshallingContext();
                org.jibx.runtime.IXMLReader reader = new org.jibx.runtime.impl.StAXReaderWrapper(param.getXMLStreamReaderWithoutCaching(), "SOAP-message", true);
                ctx.setDocument(reader);
                return ctx.unmarshalElement(type);
            } catch (Exception e) {
                 throw new RuntimeException(e);
            }
        }

    </xsl:template>
    </xsl:stylesheet>