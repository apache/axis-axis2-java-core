<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
<!-- #################################################################################  -->
    <!-- ############################   JAXB-RI template   ##############################  -->
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
        
        private static final javax.xml.bind.JAXBContext wsContext;
        static {
            javax.xml.bind.JAXBContext jc;
            jc = null;
            try {
				jc = javax.xml.bind.JAXBContext.newInstance(
            <xsl:for-each select="param[not(@type = preceding-sibling::param/@type)]">
                <xsl:if test="@type!=''">
                        <xsl:value-of select="@type"/>.class<xsl:if test="position() != last()">,
                        </xsl:if>
                </xsl:if>
            </xsl:for-each>
				);
            }
            catch ( javax.xml.bind.JAXBException ex ) {
                System.err.println("Unable to create JAXBContext: " + ex.getMessage());
                ex.printStackTrace(System.err);
                Runtime.getRuntime().exit(-1);
            }
            finally {
                wsContext = jc;
			}
        }

        <xsl:for-each select="param[not(@type = preceding-sibling::param/@type)]">
            <xsl:if test="@type!=''">

                private org.apache.axiom.om.OMElement toOM(<xsl:value-of select="@type"/> param, boolean optimizeContent, javax.xml.namespace.QName elementQName)
                    throws org.apache.axis2.AxisFault {
                        org.apache.axiom.om.OMFactory factory = org.apache.axiom.om.OMAbstractFactory.getOMFactory();

                        java.lang.Object object = param; <!-- This is necessary to convert primitive types to their corresponding wrapper types (so that we can call getClass()) -->
                        org.apache.axiom.om.ds.jaxb.JAXBOMDataSource source = new org.apache.axiom.om.ds.jaxb.JAXBOMDataSource( wsContext,
                                new javax.xml.bind.JAXBElement(elementQName, object.getClass(), object));
                        org.apache.axiom.om.OMNamespace namespace = factory.createOMNamespace(elementQName.getNamespaceURI(), null);
                        return factory.createOMElement(source, elementQName.getLocalPart(), namespace);
                    }

                private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="@type"/> param, boolean optimizeContent, javax.xml.namespace.QName elementQName)
                throws org.apache.axis2.AxisFault {
                    org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
                    envelope.getBody().addChild(toOM(param, optimizeContent, elementQName));
                    return envelope;
                }

                <xsl:variable name="propertyType" select="@type"/>
                <xsl:choose>
                    <xsl:when test="$propertyType='byte'">
                        private byte toByte (
                            org.apache.axiom.om.OMElement param) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Byte ret = (java.lang.Integer)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), byte.class).getValue();
                                return ret.byteValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='char'">
                        private char toChar (
                            org.apache.axiom.om.OMElement param) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Character ret = (java.lang.Character)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), char.class).getValue();
                                return ret.charValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='double'">
                        private double toDouble (
                            org.apache.axiom.om.OMElement param) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Double ret = (java.lang.Double)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), double.class).getValue();
                                return ret.doubleValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='float'">
                        private float toFloat (
                            org.apache.axiom.om.OMElement param) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Float ret = (java.lang.Float)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), float.class).getValue();
                                return ret.floatValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='int'">
                        private int toInt (
                            org.apache.axiom.om.OMElement param) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Integer ret = (java.lang.Integer)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), int.class).getValue();
                                return ret.intValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='long'">
                        private long toLong (
                            org.apache.axiom.om.OMElement param) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Long ret = (java.lang.Long)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), long.class).getValue();
                                return ret.longValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='short'">
                        private short toShort (
                            org.apache.axiom.om.OMElement param) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Short ret = (java.lang.Short)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), short.class).getValue();
                                return ret.shortValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='boolean'">
                        private boolean toBoolean (
                            org.apache.axiom.om.OMElement param) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Boolean ret = (java.lang.Boolean)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), boolean.class).getValue();
                                return ret.booleanValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>                    
                </xsl:choose>
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
            java.lang.Class type) throws org.apache.axis2.AxisFault{
            try {
                javax.xml.bind.JAXBContext context = wsContext;
                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();
                org.apache.axiom.util.jaxb.UnmarshallerAdapter adapter = org.apache.axiom.util.jaxb.JAXBUtils.getUnmarshallerAdapter(param.getXMLStreamReaderWithoutCaching());
                unmarshaller.setAttachmentUnmarshaller(adapter.getAttachmentUnmarshaller());
                return unmarshaller.unmarshal(adapter.getReader(), type).getValue();
            } catch (javax.xml.bind.JAXBException bex){
                throw org.apache.axis2.AxisFault.makeFault(bex);
            }
        }
    </xsl:template>
    </xsl:stylesheet>
