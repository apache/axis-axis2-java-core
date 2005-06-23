<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
    package <xsl:value-of select="@package"/>;

    /**
     *  Auto generated supporter class for XML beans by the Axis code generator
     */

    public class <xsl:value-of select="@name"/> {
             <xsl:apply-templates />

          public org.apache.xmlbeans.XmlObject fromOM(org.apache.axis.om.OMElement param,
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
                    e.printStackTrace();
                }
             return null;
          }

        //Generates an empty object for testing
         public org.apache.xmlbeans.XmlObject getTestObject(java.lang.Class type){
                try{
                   <xsl:for-each select="param">
                    <xsl:if test="@type!=''">
                    if (<xsl:value-of select="@type"/>.class.equals(type)){
                        return <xsl:value-of select="@type"/>.Factory.newInstance() ;
                    }
                     </xsl:if>
                    </xsl:for-each>
                 }catch(java.lang.Exception e){
                    e.printStackTrace();
                }
             return null;
          }
     }
    </xsl:template>
    
    <xsl:template match="param">
        <xsl:if test="@type!=''">
          public  org.apache.axis.om.OMElement  toOM(<xsl:value-of select="@type"/> param){
		    org.apache.axis.om.impl.llom.builder.StAXOMBuilder builder = org.apache.axis.om.impl.llom.factory.OMXMLBuilderFactory.createStAXOMBuilder
            (org.apache.axis.om.OMAbstractFactory.getOMFactory(),param.newXMLStreamReader()) ;
		    return builder.getDocumentElement();
          }
       </xsl:if>
    </xsl:template>
 </xsl:stylesheet>