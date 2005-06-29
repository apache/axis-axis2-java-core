<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
    package <xsl:value-of select="@package"/>;

    /**
     *  Auto generated supporter class for XML beans by the Axis code generator
     */

    public class <xsl:value-of select="@name"/> {
             <xsl:apply-templates />

          public static org.apache.xmlbeans.XmlObject fromOM(org.apache.axis.om.OMElement param,
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
     }
    </xsl:template>
    
    <xsl:template match="param">
        <xsl:if test="@type!=''">
          public  static org.apache.axis.om.OMElement  toOM(<xsl:value-of select="@type"/> param){
		    org.apache.axis.om.impl.llom.builder.StAXOMBuilder builder = org.apache.axis.om.impl.llom.factory.OMXMLBuilderFactory.createStAXOMBuilder
            (org.apache.axis.om.OMAbstractFactory.getOMFactory(),new org.apache.axis.clientapi.StreamWrapper(param.newXMLStreamReader())) ;
		    org.apache.axis.om.OMElement documentElement = builder.getDocumentElement();
            //Building the element is needed to avoid certain stream errors!
            documentElement.build();
            return documentElement;
          }
       </xsl:if>
    </xsl:template>
 </xsl:stylesheet>