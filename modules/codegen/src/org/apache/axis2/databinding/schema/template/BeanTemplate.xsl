<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/bean">
    package <xsl:value-of select="@package"/>;
    <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>

    /**
     *  Auto generated bean class by the Axis code generator
     */

    public class <xsl:value-of select="$name"/> <xsl:if test="@extension"> extends <xsl:value-of select="@extension"/></xsl:if>
        implements org.apache.axis2.databinding.ADBBean{

      private static final javax.xml.namespace.QName qName = new javax.xml.namespace.QName(
                                      "<xsl:value-of select="@nsuri"/>",
                                      "<xsl:value-of select="$name"/>",
                                      "<xsl:value-of select="@nsprefix"/>");

     <xsl:for-each select="property">
         <xsl:variable name="propertyType"><xsl:value-of select="@type"></xsl:value-of></xsl:variable>
         <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
        /**
         * field for <xsl:value-of select="$propertyName"/>
         */
         private <xsl:value-of select="$propertyType"/> local<xsl:value-of select="$propertyName"/>;

        /**
         * Auto generated getter method
         * @return <xsl:value-of select="$propertyType"/>
         */
        public  <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text>get<xsl:value-of select="$propertyName"/>(){
             return local<xsl:value-of select="$propertyName"/>;
        }

        /**
         * Auto generated setter method
         * @param param<xsl:value-of select="$propertyName"/>
         */
        public void set<xsl:value-of select="$propertyName"/>(<xsl:value-of select="$propertyType"/> param<xsl:value-of select="$propertyName"/>){
             this.local<xsl:value-of select="$propertyName"/>=param<xsl:value-of select="$propertyName"/>;
        }
     </xsl:for-each>

        /**
         * databinding method to get an XML representation of this object
         * Note - this is not complete
         */
        public javax.xml.stream.XMLStreamReader getPullParser(){

          java.util.List list = new  java.util.ArrayList();
          <xsl:for-each select="property">
           <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
           list.add(org.apache.axis2.databinding.schema.util.ConverterUtil.convertToObject(local<xsl:value-of select="$propertyName"></xsl:value-of>));
          </xsl:for-each>

          return null;

        }

        /**
         * static method to create the object
         * Note -  This is not complete
         */
        public static <xsl:value-of select="$name"/> parse(javax.xml.stream.XMLStreamReader reader){
             


             return new <xsl:value-of select="$name"/>();
        }

    }
    </xsl:template>
 </xsl:stylesheet>