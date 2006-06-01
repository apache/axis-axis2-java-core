<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <!-- #################################################################################  -->
    <!-- ############################   xmlbeans template   ##############################  -->
    <xsl:template match="databinders[@dbtype='xmlbeans']">

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

                private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="@type"/> param, boolean optimizeContent){
                    org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
                    if (param != null){
                        envelope.getBody().addChild(toOM(param, optimizeContent));
                    }
                    return envelope;
                }
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
        <xsl:for-each select="param">
            <xsl:if test="@type!=''">
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
            </xsl:if>
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