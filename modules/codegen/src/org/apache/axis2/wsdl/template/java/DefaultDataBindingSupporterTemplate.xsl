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
     *  <xsl:value-of select="@name"/> supporter class for XML beans data binding
     */

    public class <xsl:value-of select="@name"/> {
             <xsl:apply-templates />

          public static org.apache.axis2.om.OMElement fromOM(org.apache.axis2.om.OMElement param,
               java.lang.Class type){
               //Just return the OMElement as it is
               return param;
          }

        // Generates an empty object for testing
        // Caution - need some manual editing to work properly
         public static org.apache.axis2.om.OMElement getTestObject(java.lang.Class type){
                try{
                    org.apache.axis2.om.OMFactory factory = org.apache.axis2.om.OMAbstractFactory.getOMFactory();
                    org.apache.axis2.om.OMNamespace emptyNamespace = factory.createOMNamespace("",null);
                   <xsl:for-each select="param">
                    <xsl:if test="@type!=''">
                    if (<xsl:value-of select="@type"/>.class.equals(type)){
                        <xsl:value-of select="@type"/> emptyObject= factory.createOMElement("test",emptyNamespace);
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
          public  static org.apache.axis2.om.OMElement  toOM(<xsl:value-of select="@type"/> param){
            //Just return the object as it is. it's supposed to be an OMElement
		    return param;
          }
       </xsl:if>
    </xsl:template>
 </xsl:stylesheet>