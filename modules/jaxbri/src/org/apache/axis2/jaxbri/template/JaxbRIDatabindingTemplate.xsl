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
                        javax.xml.bind.JAXBContext context = javax.xml.bind.JAXBContext.newInstance(<xsl:value-of select="@type"/>.class);
                        javax.xml.bind.Marshaller marshaller = context.createMarshaller();
                        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT, Boolean.TRUE);            
        
                        JaxbRIDataSource source = new JaxbRIDataSource(param, marshaller);
                        javax.xml.namespace.QName elementName = context.createJAXBIntrospector().getElementName(param);
                        org.apache.axiom.om.OMNamespace namespace = factory.createOMNamespace(elementName.getNamespaceURI(), null);
                        return factory.createOMElement(source, elementName.getNamespaceURI(), namespace);
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

        class JaxbRIDataSource implements org.apache.axiom.om.OMDataSource {
            /**
             * Bound object for output.
             */
            private final Object outObject;

            /**
             * Marshaller.
             */
            private final javax.xml.bind.Marshaller marshaller;

            /**
             * Constructor from object and marshaller.
             *
             * @param obj
             * @param marshaller
             */
            public JaxbRIDataSource(Object obj, javax.xml.bind.Marshaller marshaller) {
                this.outObject = obj;
                this.marshaller = marshaller;
            }

            public void serialize(java.io.OutputStream output, org.apache.axiom.om.OMOutputFormat format) throws javax.xml.stream.XMLStreamException {
                try {
                    marshaller.marshal(outObject, output);
                } catch (javax.xml.bind.JAXBException e) {
                    throw new javax.xml.stream.XMLStreamException("Error in JAXB marshalling", e);
                }
            }

            public void serialize(java.io.Writer writer, org.apache.axiom.om.OMOutputFormat format) throws javax.xml.stream.XMLStreamException {
                try {
                    marshaller.marshal(outObject, writer);
                } catch (javax.xml.bind.JAXBException e) {
                    throw new javax.xml.stream.XMLStreamException("Error in JAXB marshalling", e);
                }
            }

            public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                try {
                    marshaller.marshal(outObject, xmlWriter);
                } catch (javax.xml.bind.JAXBException e) {
                    throw new javax.xml.stream.XMLStreamException("Error in JAXB marshalling", e);
                }
            }

            public javax.xml.stream.XMLStreamReader getReader() throws javax.xml.stream.XMLStreamException {
                try {
                    javax.xml.bind.JAXBContext context = javax.xml.bind.JAXBContext.newInstance(edu.indiana.extreme.www.wsdl.benchmark1.EchoVoid.class);
                    org.apache.axiom.om.impl.builder.SAXOMBuilder builder = new org.apache.axiom.om.impl.builder.SAXOMBuilder();
                    javax.xml.bind.Marshaller marshaller = context.createMarshaller();
                    marshaller.marshal(outObject, builder);

                    return builder.getRootElement().getXMLStreamReader();
                } catch (javax.xml.bind.JAXBException e) {
                    throw new javax.xml.stream.XMLStreamException("Error in JAXB marshalling", e);
                }
            }
        }
        
    </xsl:template>
    </xsl:stylesheet>