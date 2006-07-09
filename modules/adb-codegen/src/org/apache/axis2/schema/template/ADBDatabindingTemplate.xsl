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
        <!--  generate toOM for only non parts and non primitives!!! -->
        <xsl:for-each select="param[@type!='' and not(@primitive) and not(@partname)]">
            private  org.apache.axiom.om.OMElement  toOM(<xsl:value-of select="@type"/> param, boolean optimizeContent){
            return param.getOMElement(param.MY_QNAME,
            org.apache.axiom.om.OMAbstractFactory.getOMFactory());
            }
        </xsl:for-each>

        <xsl:for-each select="opnames/name">
            <xsl:variable name="opname"><xsl:value-of select="."/></xsl:variable>
            <xsl:variable name="opnsuri"><xsl:value-of select="@opnsuri"/></xsl:variable>
            <xsl:choose>
                <xsl:when test="count(../../param[@type!='' and @direction='in' and @opname=$opname])=1">
                    <!-- Assumption - The ADBBean here is always an element based bean -->
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="../../param[@type!='' and @direction='in' and @opname=$opname]/@type"/> param, boolean optimizeContent){
                     org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                     emptyEnvelope.getBody().addChild(param.getOMElement(<xsl:value-of select="../../param[@type!='' and @direction='in' and @opname=$opname]/@type"/>.MY_QNAME,factory));
                     return emptyEnvelope;
                    }
                </xsl:when>
                <xsl:when test="count(../../param[@type!='' and @direction='in' and @opname=$opname]) &gt; 1">
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                    <xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]"><xsl:value-of select="@type"/> param<xsl:value-of select="position()"/>,</xsl:for-each>
                    boolean optimizeContent){
                    org.apache.axiom.om.OMElement elt;
                    //make the OMfactory and generate the wrapper element
                    org.apache.axiom.om.OMFactory fac = org.apache.axiom.om.OMAbstractFactory.getOMFactory();
                    org.apache.axiom.om.OMElement wrapperElt =
                    fac.createOMElement("<xsl:value-of select="$opname"/>","<xsl:value-of select="$opnsuri"/>",null);
                    <xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]">

                        <xsl:choose>
                            <xsl:when test="@primitive">
                                elt = fac.createOMElement("<xsl:value-of select="@partname"/>","",null);
                                elt.setText(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(param<xsl:value-of select="position()"/>));
                            </xsl:when>
                            <xsl:otherwise>
                                elt = param<xsl:value-of select="position()"/>.getOMElement(
                                new javax.xml.namespace.QName("","<xsl:value-of select="@partname"/>"),
                                org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                            </xsl:otherwise>
                        </xsl:choose>
                        wrapperElt.addChild(elt);
                    </xsl:for-each>

                    org.apache.axis2.databinding.ADBSOAPModelBuilder builder =
                    new org.apache.axis2.databinding.ADBSOAPModelBuilder(wrapperElt.getXMLStreamReader(),
                    factory);
                    return builder.getEnvelope();

                    }
                </xsl:when>
               <xsl:otherwise>
                    /**
                    *  get the default envelope
                    */
                    private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
                    return factory.getDefaultEnvelope();
                    }
                </xsl:otherwise>
            </xsl:choose>
             <xsl:choose>
			      <xsl:when test="count(../../param[@type!='' and @direction='out' and @opname=$opname])=1">
                    <!-- Assumption - The ADBBean here is always an element based bean -->
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"/> param, boolean optimizeContent){
                     org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                     emptyEnvelope.getBody().addChild(param.getOMElement(<xsl:value-of select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"/>.MY_QNAME,factory));
                     return emptyEnvelope;
                    }
                </xsl:when>
	   </xsl:choose>
        </xsl:for-each>

        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces){

        try {
        <xsl:for-each select="param[not(@primitive)]">
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