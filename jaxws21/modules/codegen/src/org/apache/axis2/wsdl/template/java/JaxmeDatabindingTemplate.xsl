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
    <!-- ############################   jaxme template   ##############################  -->
    <xsl:template match="databinders[@dbtype='jaxme']">

        <xsl:variable name="base64"><xsl:value-of select="base64Elements/name"/></xsl:variable>
        <xsl:if test="$base64">
            private static javax.xml.namespace.QName[] qNameArray = {
            <xsl:for-each select="base64Elements/name">
                <xsl:if test="position()>1">,</xsl:if>new javax.xml.namespace.QName("<xsl:value-of select="@ns-url"/>","<xsl:value-of select="@localName"/>")
            </xsl:for-each>
            };
        </xsl:if>

        private org.apache.axiom.om.OMElement toOM(Object param) {
            try {
                javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(param.getClass().getInterfaces()[0].getPackage()
                                                                                            .getName());
                org.apache.axiom.om.impl.builder.SAXOMBuilder builder = new org.apache.axiom.om.impl.builder.SAXOMBuilder();
                javax.xml.bind.Marshaller marshaller = ctx.createMarshaller();
                marshaller.marshal(param, builder);
                return builder.getRootElement();
            } catch (javax.xml.bind.JAXBException e) {
                throw new RuntimeException(e);
            }
        }

        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
            org.apache.axiom.soap.SOAPFactory factory, Object param, boolean optimizeContent) {
            org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
            if (param != null){
                envelope.getBody().addChild(toOM(param));
            }

            return envelope;
        }

        public java.lang.Object fromOM(org.apache.axiom.om.OMElement param,
        java.lang.Class type,
         java.util.Map extraNamespaces){
            try{
                javax.xml.transform.Source source =
                        new javanet.staxutils.StAXSource(param.getXMLStreamReader());
                javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(
                        type.getPackage().getName());
                javax.xml.bind.Unmarshaller u = ctx.createUnmarshaller();
                return u.unmarshal(source);
            } catch(java.lang.Exception e) {
                throw new RuntimeException("Data binding error",e);
            }
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
