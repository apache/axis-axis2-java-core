<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <!-- #################################################################################  -->
    <!-- ############################   ADB template   ##############################  -->
    <xsl:template match="databinders[@dbtype='adb']">
        <xsl:variable name="serverside"  select="@isserverside"></xsl:variable>
        <xsl:variable name="helpermode"  select="extra/@h"></xsl:variable>

        <!--  generate toOM for only non parts and non primitives!!! -->
        <xsl:for-each select="param[not(@type = preceding-sibling::param/@type) and @type!='' and not(@primitive)]">
            private  org.apache.axiom.om.OMElement  toOM(<xsl:value-of select="@type"/> param, boolean optimizeContent){
            <xsl:choose>
                    <xsl:when test="$helpermode">
                            return <xsl:value-of select="@type"/>Helper.getOMElement(
                                        param,
                                        <xsl:value-of select="@type"/>.MY_QNAME,
                                        org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    </xsl:when>
                    <xsl:otherwise>
                     return param.getOMElement(<xsl:value-of select="@type"/>.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    </xsl:otherwise>
            </xsl:choose>

            }
        </xsl:for-each>

        <xsl:for-each select="opnames/name">

            <xsl:variable name="opname" select="."/>
            <xsl:variable name="opnsuri" select="@opnsuri"/>
            <xsl:variable name="paramcount" select="count(../../param[@type!='' and @direction='in' and @opname=$opname])"/>

            <xsl:if test="not($serverside)">
            <xsl:choose>
                <xsl:when test="$paramcount &gt; 0">
                    <xsl:variable name="inputElementType" select="../../param[@type!='' and @direction='in' and @opname=$opname]/@type"></xsl:variable>
                    <xsl:variable name="wrappedParameterCount" select="count(../../param[@type!='' and @direction='in' and @opname=$opname]/param)"></xsl:variable>
                     <xsl:choose>
						<xsl:when test="$wrappedParameterCount &gt; 0">
                            <!-- geneate the toEnvelope method-->
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
							<xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]/param">
							 <xsl:value-of select="@type"/> param<xsl:value-of select="position()"/>,
							</xsl:for-each>
						 boolean optimizeContent){

						<xsl:value-of select="$inputElementType"/> wrappedType = new <xsl:value-of select="$inputElementType"/>();
						 	<xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]/param">
							  wrappedType.set<xsl:value-of select="@partname"/>(param<xsl:value-of select="position()"/>);
						 </xsl:for-each>


                       org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                          <xsl:choose>
                            <xsl:when test="$helpermode">
                                emptyEnvelope.getBody().addChild(<xsl:value-of select="$inputElementType"/>Helper.getOMElement(
                                wrappedType,
                                <xsl:value-of select="$inputElementType"/>.MY_QNAME,factory));
                            </xsl:when>
                            <xsl:otherwise>
                                emptyEnvelope.getBody().addChild(wrappedType.getOMElement(<xsl:value-of select="$inputElementType"/>.MY_QNAME,factory));
                            </xsl:otherwise>
                        </xsl:choose>

                       return emptyEnvelope;
                       }



                        </xsl:when>
						<xsl:otherwise>
						<!-- Assumption - the parameter is always an ADB element-->
				    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="$inputElementType"/> param, boolean optimizeContent){
                    org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                         <xsl:choose>
                            <xsl:when test="$helpermode">
                                emptyEnvelope.getBody().addChild(<xsl:value-of select="$inputElementType"/>Helper.getOMElement(
                                param,
                                <xsl:value-of select="$inputElementType"/>.MY_QNAME,factory));
                            </xsl:when>
                            <xsl:otherwise>
                                emptyEnvelope.getBody().addChild(param.getOMElement(<xsl:value-of select="$inputElementType"/>.MY_QNAME,factory));
                            </xsl:otherwise>
                    </xsl:choose>
                     return emptyEnvelope;
                    }

						</xsl:otherwise>
					 </xsl:choose>
               </xsl:when>
               <xsl:otherwise>
                  <!-- Do nothing here -->
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
                       <xsl:choose>
                            <xsl:when test="$helpermode">
                                emptyEnvelope.getBody().addChild(
                                <xsl:value-of select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"/>Helper.getOMElement(
                                param,
                                <xsl:value-of select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"/>.MY_QNAME,factory));
                            </xsl:when>
                            <xsl:otherwise>
                                emptyEnvelope.getBody().addChild(param.getOMElement(<xsl:value-of select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"/>.MY_QNAME,factory));
                            </xsl:otherwise>
                    </xsl:choose>

                     return emptyEnvelope;
                    }
                </xsl:when>
       </xsl:choose>
            <xsl:if test="count(../../param[@type!='' and @direction='in' and @opname=$opname])=1">
                <!-- generate the get methods -->
                <xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]/param">
                    private <xsl:value-of select="@type"/> get<xsl:value-of select="@partname"/>(
                    <xsl:value-of select="../@type"/> wrappedType){
                    return wrappedType.get<xsl:value-of select="@partname"/>();
                    }
                </xsl:for-each>
            </xsl:if>
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
        <xsl:for-each select="param[not(@primitive) and @type!='']">
                if (<xsl:value-of select="@type"/>.class.equals(type)){
                <xsl:choose>
                    <xsl:when test="$helpermode">
                           return <xsl:value-of select="@type"/>Helper.parse(param.getXMLStreamReaderWithoutCaching());
                    </xsl:when>
                    <xsl:otherwise>
                           return <xsl:value-of select="@type"/>.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    </xsl:otherwise>
                </xsl:choose>

                }
           </xsl:for-each>
        } catch (Exception e) {
        throw new RuntimeException(e);
        }
           return null;
        }



    </xsl:template>

</xsl:stylesheet>