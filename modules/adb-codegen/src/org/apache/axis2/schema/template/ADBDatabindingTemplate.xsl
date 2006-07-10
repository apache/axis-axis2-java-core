<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <!-- #################################################################################  -->
    <!-- ############################   ADB template   ##############################  -->
    <xsl:template match="databinders[@dbtype='adb']">
        <xsl:variable name="serverside"><xsl:value-of select="@isserverside"/></xsl:variable>

        <!--  generate toOM for only non parts and non primitives!!! -->
        <xsl:for-each select="param[@type!='' and not(@primitive) and not(@partname)]">
            private  org.apache.axiom.om.OMElement  toOM(<xsl:value-of select="@type"/> param, boolean optimizeContent){
            return param.getOMElement(<xsl:value-of select="@type"/>.MY_QNAME,
            org.apache.axiom.om.OMAbstractFactory.getOMFactory());
            }
        </xsl:for-each>

        <xsl:for-each select="opnames/name">

            <xsl:variable name="opname"><xsl:value-of select="."/></xsl:variable>
            <xsl:variable name="opnsuri"><xsl:value-of select="@opnsuri"/></xsl:variable>
            <xsl:variable name="paramcount"><xsl:value-of select="count(../../param[@type!='' and @direction='in' and @opname=$opname])"/></xsl:variable>
            <xsl:if test="not($serverside)">
            <xsl:choose>
                <xsl:when test="$paramcount=1">
                    <!-- Assumption - The ADBBean here is always an element based bean -->
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="../../param[@type!='' and @direction='in' and @opname=$opname]/@type"/> param, boolean optimizeContent){
                     org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                     emptyEnvelope.getBody().addChild(param.getOMElement(<xsl:value-of select="../../param[@type!='' and @direction='in' and @opname=$opname]/@type"/>.MY_QNAME,factory));
                     return emptyEnvelope;
                    }
                </xsl:when>
                <xsl:when test="$paramcount &gt; 1">
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
                               <!-- elt = param<xsl:value-of select="position()"/>.getOMElement(
                                new javax.xml.namespace.QName("","<xsl:value-of select="@partname"/>"),
                                org.apache.axiom.om.OMAbstractFactory.getOMFactory());  -->
                                <xsl:variable name="paramname">param<xsl:value-of select="position()"/></xsl:variable>
                                <xsl:variable name="buildername">builder<xsl:value-of select="position()"/></xsl:variable>
                                <xsl:variable name="docEltName">docElt<xsl:value-of select="position()"/></xsl:variable>
                                elt = fac.createOMElement("<xsl:value-of select="@partname"/>","",null);

                               org.apache.axiom.om.impl.builder.StAXOMBuilder <xsl:value-of select="$buildername"/> = new org.apache.axiom.om.impl.builder.StAXOMBuilder(factory,
                                         <xsl:value-of select="$paramname"/>.getPullParser(elt.getQName()));

                                org.apache.axiom.om.OMElement <xsl:value-of select="$docEltName"/> = <xsl:value-of select="$buildername"/>.getDocumentElement();
                              (( org.apache.axiom.om.impl.OMNodeEx) <xsl:value-of select="$docEltName"/>).setParent(null);
                               <xsl:value-of select="$docEltName"/>.build();
                               elt.addChild(<xsl:value-of select="$docEltName"/>);

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
            </xsl:if>
            <!-- this piece of logic needs to be generated only for the server side-->
            <xsl:if test="$serverside">
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
      </xsl:if>
      </xsl:for-each>


        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces){

        try {
        <xsl:for-each select="param[not(@primitive) and @type!='']">
                if (<xsl:value-of select="@type"/>.class.equals(type)){
                return <xsl:value-of select="@type"/>.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                }
           </xsl:for-each>
        } catch (Exception e) {
        throw new RuntimeException(e);
        }

        return null;
        }

        <!-- generate convert methods to convert from the primitive types -->
       <xsl:for-each select="param[@primitive and @type!='']">
        private <xsl:value-of select="@type"/> convertTo<xsl:value-of select="@shorttype"/>(
            org.apache.axiom.om.OMElement param
           ){
             return org.apache.axis2.databinding.utils.ConverterUtil.
                           convertTo<xsl:value-of select="@shorttype"/>(param.getText());
         }
           </xsl:for-each>

    </xsl:template>
</xsl:stylesheet>