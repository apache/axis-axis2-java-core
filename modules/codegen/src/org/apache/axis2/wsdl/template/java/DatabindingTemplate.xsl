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
                public  org.apache.axis2.om.OMElement  toOM(<xsl:value-of select="@type"/> param){
                org.apache.axis2.om.impl.llom.builder.StAXOMBuilder builder = new org.apache.axis2.om.impl.llom.builder.StAXOMBuilder
                (org.apache.axis2.om.OMAbstractFactory.getOMFactory(),new org.apache.axis2.util.StreamWrapper(param.newXMLStreamReader())) ;

                org.apache.axis2.om.OMElement documentElement = builder.getDocumentElement();
                <xsl:choose>
                    <xsl:when test="$base64">
                         optimizeContent(documentElement,qNameArray);
                    </xsl:when>
                </xsl:choose>
                  ((org.apache.axis2.om.impl.OMNodeEx)documentElement).setParent(null);
                  return documentElement;
                }
                
                public org.apache.axis2.soap.SOAPEnvelope toEnvelope(org.apache.axis2.soap.SOAPFactory factory, <xsl:value-of select="@type"/> param){
                    org.apache.axis2.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
                    envelope.getBody().addChild(toOM(param));
                    return envelope;
                }
            </xsl:if>

        </xsl:for-each>

        public org.apache.xmlbeans.XmlObject fromOM(org.apache.axis2.om.OMElement param,
        java.lang.Class type){
        try{
        <xsl:for-each select="param">
            <xsl:if test="@type!=''">
                if (<xsl:value-of select="@type"/>.class.equals(type)){
                return <xsl:value-of select="@type"/>.Factory.parse(param.getXMLStreamReaderWithoutCaching()) ;
                }
            </xsl:if>
        </xsl:for-each>
        }catch(java.lang.Exception e){
        throw new RuntimeException("Data binding error",e);
        }
        return null;
        }

     private void optimizeContent(org.apache.axis2.om.OMElement element, javax.xml.namespace.QName[] qNames){
        for (int i = 0; i &lt; qNames.length; i++) {
            markElementsAsOptimized(qNames[i],element);
        }
    }

    private void markElementsAsOptimized(javax.xml.namespace.QName qName,org.apache.axis2.om.OMElement rootElt){
        if (rootElt.getQName().equals(qName)){
            //get the text node and mark it
            org.apache.axis2.om.OMNode node = rootElt.getFirstOMChild();
            if (node.getType()==org.apache.axis2.om.OMNode.TEXT_NODE){
                ((org.apache.axis2.om.OMText)node).setOptimize(true);
            }

        }
        java.util.Iterator childElements = rootElt.getChildElements();
        while (childElements.hasNext()) {
            markElementsAsOptimized(qName,(org.apache.axis2.om.OMElement)childElements.next());
        }
    }

    </xsl:template>
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
                    public  org.apache.axis2.om.OMElement  toOM(<xsl:value-of select="@type"/> param){
                        if (param instanceof org.apache.axis2.databinding.ADBBean){
                            org.apache.axis2.om.impl.llom.builder.StAXOMBuilder builder = new org.apache.axis2.om.impl.llom.builder.StAXOMBuilder
                            (org.apache.axis2.om.OMAbstractFactory.getOMFactory(), param.getPullParser(<xsl:value-of select="@type"/>.MY_QNAME));
                            org.apache.axis2.om.OMElement documentElement = builder.getDocumentElement();
                            ((org.apache.axis2.om.impl.OMNodeEx) documentElement).setParent(null); // remove the parent link
                            return documentElement;
                        }else{
                           <!-- treat this as a plain bean. use the reflective bean converter -->
                           //todo finish this onece the bean serializer has the necessary methods
                            return null;
                        }
                    }
                    
                    public  org.apache.axis2.soap.SOAPEnvelope toEnvelope(org.apache.axis2.soap.SOAPFactory factory, <xsl:value-of select="@type"/> param){
                        if (param instanceof org.apache.axis2.databinding.ADBBean){
                            org.apache.axis2.databinding.ADBSOAPModelBuilder builder = new 
                                    org.apache.axis2.databinding.ADBSOAPModelBuilder(param.getPullParser(param.MY_QNAME),
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

            public  java.lang.Object fromOM(org.apache.axis2.om.OMElement param,
            java.lang.Class type){
                 Object obj;
                try {
                    java.lang.reflect.Method parseMethod = type.getMethod("parse",new Class[]{javax.xml.stream.XMLStreamReader.class});
                    obj = null;
                    if (parseMethod!=null){
                        obj = parseMethod.invoke(null,new Object[]{param.getXMLStreamReaderWithoutCaching()});
                    }else{
                        //oops! we don't know how to deal with this. Perhaps the reflective one is a good choice here
                    }
                } catch (Exception e) {
                     throw new RuntimeException(e);
                }

                return obj;
            }

        </xsl:template>
       <!-- #################################################################################  -->
       <!-- ############################   none template!!!   ##############################  -->
       <xsl:template match="databinders[@dbtype='none']">
           public  org.apache.axis2.om.OMElement fromOM(org.apache.axis2.om.OMElement param, java.lang.Class type){
              return param;
           }

           public  org.apache.axis2.om.OMElement  toOM(org.apache.axis2.om.OMElement param){
               return param;
           }
           
           public org.apache.axis2.soap.SOAPEnvelope toEnvelope(org.apache.axis2.soap.SOAPFactory factory, org.apache.axis2.om.OMElement param){
               return param;
           }
       </xsl:template>

     </xsl:stylesheet>