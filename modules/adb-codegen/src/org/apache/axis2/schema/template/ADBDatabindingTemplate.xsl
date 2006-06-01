<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
<!-- #################################################################################  -->
       <!-- ############################   ADB template   ##############################  -->
       <xsl:template match="databinders[@dbtype='adb']">

            <xsl:variable name="base64"><xsl:value-of select="base64Elements/name"/></xsl:variable>
            <xsl:if test="$base64">
                private static javax.xml.namespace.QName[] qNameArray = {
                <xsl:for-each select="base64Elements/name">
                    <xsl:if test="position()>1">,</xsl:if>new javax.xml.namespace.QName("<xsl:value-of select="@ns-url"/>","<xsl:value-of select="@localName"/>")
                </xsl:for-each>
                };
            </xsl:if>

            <xsl:for-each select="param">
                <xsl:if test="@type!=''">
                    private  org.apache.axiom.om.OMElement  toOM(<xsl:value-of select="@type"/> param, boolean optimizeContent){
                        if (param instanceof org.apache.axis2.databinding.ADBBean){
                            org.apache.axiom.om.impl.builder.StAXOMBuilder builder
                                       = new org.apache.axiom.om.impl.builder.StAXOMBuilder
                            (org.apache.axiom.om.OMAbstractFactory.getOMFactory(),
                               new org.apache.axis2.util.StreamWrapper(param.getPullParser(<xsl:value-of select="@type"/>.MY_QNAME)));
                            org.apache.axiom.om.OMElement documentElement = builder.getDocumentElement();
                            ((org.apache.axiom.om.impl.OMNodeEx) documentElement).setParent(null); // remove the parent link
                            return documentElement;
                        }else{
                           <!-- treat this as a plain bean. use the reflective bean converter -->
                           //todo finish this onece the bean serializer has the necessary methods
                            return null;
                        }
                    }

                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="@type"/> param, boolean optimizeContent){
                        if (param instanceof org.apache.axis2.databinding.ADBBean){
                            org.apache.axis2.databinding.ADBSOAPModelBuilder builder = new
                                    org.apache.axis2.databinding.ADBSOAPModelBuilder(param.getPullParser(<xsl:value-of select="@type"/>.MY_QNAME),
                                                                                     factory);
                            return builder.getEnvelope();
                        }else{
                           <!-- treat this as a plain bean. use the reflective bean converter -->
                           //todo finish this onece the bean serializer has the necessary methods
                            return null;
                        }
                    }
                </xsl:if>
            </xsl:for-each>

           /**
           *  get the default envelope
           */
           private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
                return factory.getDefaultEnvelope();
           }


            private  java.lang.Object fromOM(
            org.apache.axiom.om.OMElement param,
            java.lang.Class type,
            java.util.Map extraNamespaces){

                try {
                       <xsl:for-each select="param">
                              <xsl:if test="@type!=''">
                      if (<xsl:value-of select="@type"/>.class.equals(type)){
                           return <xsl:value-of select="@type"/>.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                      }
                              </xsl:if>
                     </xsl:for-each>
                } catch (Exception e) {
                     throw new RuntimeException(e);
                }

                return null;
            }

        </xsl:template>
 </xsl:stylesheet>