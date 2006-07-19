<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <!-- #################################################################################  -->
    <!-- ############################   xmlbeans template   ##############################  -->
    <xsl:template match="databinders[@dbtype='xmlbeans']">
        <xsl:variable name="serverside" select="@isserverside"/>
        <xsl:variable name="base64" select="base64Elements/name"/>
        <xsl:if test="$base64">
            private static javax.xml.namespace.QName[] qNameArray = {
            <xsl:for-each select="base64Elements/name">
                <xsl:if test="position()>1">,</xsl:if>new javax.xml.namespace.QName("<xsl:value-of select="@ns-url"/>","<xsl:value-of select="@localName"/>")
            </xsl:for-each>
            };
        </xsl:if>

        <xsl:for-each select="param[@type!='']">

            private  org.apache.axiom.om.OMElement  toOM(<xsl:value-of select="@type"/> param, boolean optimizeContent){
            org.apache.axiom.om.impl.builder.StAXOMBuilder builder = new org.apache.axiom.om.impl.builder.StAXOMBuilder
            (org.apache.axiom.om.OMAbstractFactory.getOMFactory(),new org.apache.axis2.util.StreamWrapper(param.newXMLStreamReader())) ;
            org.apache.axiom.om.OMElement documentElement = builder.getDocumentElement();

            <xsl:if test="$base64">
                if (optimizeContent) {
                optimizeContent(documentElement,qNameArray);
                }
            </xsl:if>

            ((org.apache.axiom.om.impl.OMNodeEx)documentElement).setParent(null);
            return documentElement;
            }
        </xsl:for-each>


        <xsl:for-each select="opnames/name">

            <xsl:variable name="opname" select="."/>
            <xsl:variable name="opnsuri" select="@opnsuri"/>
            <xsl:variable name="paramcount" select="count(../../param[@type!='' and @direction='in' and @opname=$opname])"/>
             <!-- get the opname capitalized -->
            <xsl:variable name="opnameCapitalized" select="concat(translate(substring($opname, 1, 1 ),'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' ), substring($opname, 2, string-length($opname)))"></xsl:variable>

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

                                <xsl:value-of select="$inputElementType"/> wrappedType = <xsl:value-of select="$inputElementType"/>.Factory.newInstance();
                                <xsl:value-of select="$inputElementType"/>.<xsl:value-of select="$opnameCapitalized"/>  wrappedEltType = wrappedType.addNew<xsl:value-of select="$opnameCapitalized"/>();
                                <xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]/param">
                                    <xsl:choose>
                                        <xsl:when test="@primitive">wrappedEltType.xset<xsl:value-of select="@partname"/>(param<xsl:value-of select="position()"/>);</xsl:when>
                                        <xsl:otherwise>wrappedEltType.set<xsl:value-of select="@partname"/>(param<xsl:value-of select="position()"/>);</xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>


                                org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
                                envelope.getBody().addChild(toOM(wrappedType, optimizeContent));
                                return envelope;

                                }



                            </xsl:when>
                            <xsl:otherwise>
                                <!-- Assumption - the parameter is always an XMLBeans -->
                                private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="$inputElementType"/> param, boolean optimizeContent){
                                org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
                                if (param != null){
                                envelope.getBody().addChild(toOM(param, optimizeContent));
                                }
                                return envelope;
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
                        <!-- Assumption - This is an XMLBeans element-->
                        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"/> param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
                        if (param != null){
                        envelope.getBody().addChild(toOM(param, optimizeContent));
                        }
                        return envelope;
                        }

                    </xsl:when>
                </xsl:choose>
                <xsl:if test="count(../../param[@type!='' and @direction='in' and @opname=$opname])=1">
                    <!-- generate the get methods -->
                    <xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]/param">
                        private <xsl:value-of select="@type"/> get<xsl:value-of select="@partname"/>(
                        <xsl:value-of select="../@type"/> wrappedType){
                        <xsl:value-of select="../@type"/>.<xsl:value-of select="$opnameCapitalized"/> innerType =
                                    wrappedType.get<xsl:value-of select="$opnameCapitalized"/>();
                        <xsl:choose>
                            <xsl:when test="@primitive">return innerType.xget<xsl:value-of select="@partname"/>();</xsl:when>
                            <xsl:otherwise>return innerType.get<xsl:value-of select="@partname"/>();</xsl:otherwise>
                        </xsl:choose>

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

        public org.apache.xmlbeans.XmlObject fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces){
        try{
        <xsl:for-each select="param[@type!='' and not(@primitive)]">

            if (<xsl:value-of select="@type"/>.class.equals(type)){
            if (extraNamespaces!=null){
            return <xsl:value-of select="@type"/>.Factory.parse(
            param.getXMLStreamReaderWithoutCaching(),
            new org.apache.xmlbeans.XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));
            }else{
            return <xsl:value-of select="@type"/>.Factory.parse(
            param.getXMLStreamReaderWithoutCaching());
            }
            }

        </xsl:for-each>
        }catch(java.lang.Exception e){
        throw new RuntimeException("Data binding error",e);
        }
        return null;
        }

        <!-- Generate the base 64 optimize methods only if the base64 items are present -->
        <xsl:if test="$base64">

            private void optimizeContent(org.apache.axiom.om.OMElement element, javax.xml.namespace.QName[] qNames){
            for (int i = 0; i &lt; qNames.length; i++) {
            markElementsAsOptimized(qNames[i],element);
            }
            }

            private void markElementsAsOptimized(javax.xml.namespace.QName qName,org.apache.axiom.om.OMElement rootElt){
            if (rootElt.getQName().equals(qName)){
            //get the text node and mark it
            org.apache.axiom.om.OMNode node = rootElt.getFirstOMChild();
            if (node.getType()==org.apache.axiom.om.OMNode.TEXT_NODE){
            ((org.apache.axiom.om.OMText)node).setOptimize(true);
            }

            }
            java.util.Iterator childElements = rootElt.getChildElements();
            while (childElements.hasNext()) {
            markElementsAsOptimized(qName,(org.apache.axiom.om.OMElement)childElements.next());
            }
            }
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>