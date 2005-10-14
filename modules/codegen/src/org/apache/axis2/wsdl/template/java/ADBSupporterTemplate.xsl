<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
        package <xsl:value-of select="@package"/>;

        /**
        *  Auto generated supporter class for XML beans by the Axis code generator
        */

        public class <xsl:value-of select="@name"/> extends org.apache.axis2.clientapi.AbstractCallbackSupporter{
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
               if (param instanceof  org.apache.axis2.databinding.ADBBean){
                    return param.getPullParser(adbBeanQName);
                }
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
                <xsl:value-of select="@type"/> emptyObject= new <xsl:value-of select="@type"/>;
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

    </xsl:template>
</xsl:stylesheet>