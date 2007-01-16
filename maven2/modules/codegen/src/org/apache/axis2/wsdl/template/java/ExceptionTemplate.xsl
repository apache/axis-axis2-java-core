<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/fault">
/**
 * <xsl:value-of select="@name"/>.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */
package <xsl:value-of select="@package"/>;

public class <xsl:value-of select="@shortName"/> extends java.lang.Exception{
    
    private <xsl:value-of select="@type"/> faultMessage;
    
    public <xsl:value-of select="@shortName"/>() {
        super("<xsl:value-of select="@shortName"/>");
    }
           
    public <xsl:value-of select="@shortName"/>(java.lang.String s) {
       super(s);
    }
    
    public <xsl:value-of select="@shortName"/>(java.lang.String s, java.lang.Throwable ex) {
      super(s, ex);
    }
    
    public void setFaultMessage(<xsl:value-of select="@type"/> msg){
       faultMessage = msg;
    }
    
    public <xsl:value-of select="@type"/> getFaultMessage(){
       return faultMessage;
    }
}
    </xsl:template>
</xsl:stylesheet>