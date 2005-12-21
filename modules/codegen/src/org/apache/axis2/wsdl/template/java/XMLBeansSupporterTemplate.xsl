<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
        /**
         * <xsl:value-of select="@name"/>.java
         *
         * This file was auto-generated from WSDL
         * by the Apache Axis2 version: #axisVersion# #today#
         */
        package <xsl:value-of select="@package"/>;

        /**
         *  <xsl:value-of select="@name"/> Supporter class for XML beans 
         */

        import org.apache.axis2.om.OMElement;
        import org.apache.axis2.om.OMNode;
        import org.apache.axis2.om.OMText;
        import javax.xml.namespace.QName;
        import java.util.Iterator;

        public class <xsl:value-of select="@name"/> {
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
        public  static org.apache.axis2.om.OMElement  toOM(<xsl:value-of select="@type"/> param){
        org.apache.axis2.om.impl.llom.builder.StAXOMBuilder builder = new org.apache.axis2.om.impl.llom.builder.StAXOMBuilder
        (org.apache.axis2.om.OMAbstractFactory.getOMFactory(),new org.apache.axis2.util.StreamWrapper(param.newXMLStreamReader())) ;
        org.apache.axis2.om.OMElement documentElement = builder.getDocumentElement();

        <xsl:if test="$base64">
        optimizeContent(documentElement,qNameArray);
        </xsl:if>
        return documentElement;
        }
            </xsl:if>
        </xsl:for-each>

        public static org.apache.xmlbeans.XmlObject fromOM(org.apache.axis2.om.OMElement param,
        java.lang.Class type){
        try{
        <xsl:for-each select="param">
            <xsl:if test="@type!=''">
                if (<xsl:value-of select="@type"/>.class.equals(type)){
                return <xsl:value-of select="@type"/>.Factory.parse(param.getXMLStreamReader()) ;
                }
            </xsl:if>
        </xsl:for-each>
        }catch(java.lang.Exception e){
        throw new RuntimeException("Data binding error",e);
        }
        return null;
        }

        //Generates an empty object for testing
        // Caution - need some manual editing to work properly
        public static org.apache.xmlbeans.XmlObject getTestObject(java.lang.Class type){
        try{
        <xsl:for-each select="param">
            <xsl:if test="@type!=''">
                if (<xsl:value-of select="@type"/>.class.equals(type)){
                <xsl:value-of select="@type"/> emptyObject= <xsl:value-of select="@type"/>.Factory.newInstance();
                ////////////////////////////////////////////////
                // TODO
                // Fill in the empty object with necessaey values. Empty XMLBeans objects do not generate proper events
                ////////////////////////////////////////////////
                return emptyObject;
                }
            </xsl:if>
        </xsl:for-each>
        }catch(java.lang.Exception e){
        throw new RuntimeException("Test object creation failure",e);
        }
        return null;
        }

        /**
     *
     * @param element
     * @param qNames
     */
    protected static void optimizeContent(OMElement element, QName[] qNames){
        for (int i = 0; i &lt; qNames.length; i++) {
            markElementsAsOptimized(qNames[i],element);
        }
    }

    /**
     *
     * @param qName
     * @param rootElt
     */
    private static void markElementsAsOptimized(QName qName,OMElement rootElt){
        if (rootElt.getQName().equals(qName)){
            //get the text node and mark it
            OMNode node = rootElt.getFirstOMChild();
            if (node.getType()==OMNode.TEXT_NODE){
                ((OMText)node).setOptimize(true);
            }

        }
        Iterator childElements = rootElt.getChildElements();
        while (childElements.hasNext()) {
            markElementsAsOptimized(qName,(OMElement)childElements.next());
        }
    }
        }
    </xsl:template>

    <xsl:template match="param">

    </xsl:template>
</xsl:stylesheet>