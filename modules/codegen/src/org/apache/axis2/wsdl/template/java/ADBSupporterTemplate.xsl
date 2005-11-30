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
         *  <xsl:value-of select="@name"/> supporter class for Axis2's databinding
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
            <xsl:choose>
                <xsl:when test="@default">
                      <!-- The  fact is that default is the OMElement -->
                public  static org.apache.axis2.om.OMElement  toOM(<xsl:value-of select="@type"/> param){
                           return param;
                }
                </xsl:when>
                 <xsl:when test="@type!=''">
                 public  static org.apache.axis2.om.OMElement  toOM(<xsl:value-of select="@type"/> param){
                     if (param instanceof  org.apache.axis2.databinding.ADBBean){
                        org.apache.axis2.om.impl.llom.builder.StAXOMBuilder builder = new org.apache.axis2.om.impl.llom.builder.StAXOMBuilder
                            (org.apache.axis2.om.OMAbstractFactory.getOMFactory(), ((org.apache.axis2.databinding.ADBBean)param).getPullParser(null)); //todo need to change this
                        return builder.getDocumentElement();
                     }else{
                        //handle the other types of beans here. Perhaps the reflective builder is
                        //a good choice here
                     }
                    return null;
                }
                </xsl:when>
                <xsl:otherwise>
                    //we don't need to generate code for this!
                </xsl:otherwise>
            </xsl:choose>
           </xsl:for-each>

        public static java.lang.Object fromOM(org.apache.axis2.om.OMElement param,
                java.lang.Class type){

        try{

        <xsl:if test="param[@default]">
             if (<xsl:value-of select="param[@default]/@type"/>.class.equals(type)){
                  return param;
             }
        </xsl:if>

        <xsl:for-each select="param[not(@default)]">
            <xsl:choose>
            <xsl:when test="@type!=''">
                if (<xsl:value-of select="@type"/>.class.equals(type)){
                <!-- Assume it's an ADBBean -->
                java.lang.reflect.Method parseMethod = <xsl:value-of select="@type"/>.class.getMethod("parse",new Class[]{javax.xml.stream.XMLStreamReader.class});
                java.lang.Object obj=null;
                if (parseMethod!=null){
                obj = parseMethod.invoke(null,new java.lang.Object[]{param.getXMLStreamReader()});
                }else{
                //oops! we don't know how to deal with this. Perhaps the reflective one is a good choice here
                }
                return obj;
                }
            </xsl:when>
            <xsl:otherwise>
                //we don't need to generate code for this!
            </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
        }catch(java.lang.Exception e){
            throw new RuntimeException("Data binding error",e);
        }
            return null;
        }

    protected static void optimizeContent(OMElement element, QName[] qNames){
        for (int i = 0; i &lt; qNames.length; i++) {
            markElementsAsOptimized(qNames[i],element);
        }
    }

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
</xsl:stylesheet>