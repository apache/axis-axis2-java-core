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
         <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
        /**
         * field for <xsl:value-of select="$javaName"/>
         */
         private <xsl:value-of select="$propertyType"/> local<xsl:value-of select="$javaName"/>;

        /**
         * Auto generated getter method
         * @return <xsl:value-of select="$propertyType"/>
         */
        public  <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text>get<xsl:value-of select="$javaName"/>(){
             return local<xsl:value-of select="$javaName"/>;
        }

        /**
         * Auto generated setter method
         * @param param<xsl:value-of select="$javaName"/>
         */
        public void set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/> param<xsl:value-of select="$javaName"/>){
             this.local<xsl:value-of select="$javaName"/>=param<xsl:value-of select="$javaName"/>;
        }
     </xsl:for-each>

        /**
         * databinding method to get an XML representation of this object
         * Note - this is not complete
         */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){

          Object[] objectList = new Object[]{
          <xsl:for-each select="property">
           <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
           <xsl:if test="position()>1">,</xsl:if>
              <xsl:choose>
                  <xsl:when test="@ours">
                      new javax.xml.namespace.QName("<xsl:value-of select="$propertyName"/>"),local<xsl:value-of select="@javaname"/>
                  </xsl:when>
                  <xsl:otherwise>
                       "<xsl:value-of select="$propertyName"/>",org.apache.axis2.databinding.schema.util.ConverterUtil.convertToString(local<xsl:value-of select="@javaname"/>)
                  </xsl:otherwise>
              </xsl:choose>

          </xsl:for-each>};

         return org.apache.axis2.databinding.utils.ADBPullParser.createPullParser(qName, objectList);

        }

        /**
         * static method to create the object
         * Note -  This is not complete
         */
        public static <xsl:value-of select="$name"/> parse(javax.xml.stream.XMLStreamReader reader){
        <xsl:value-of select="$name"/> object = new <xsl:value-of select="$name"/>();
        try {
            int event = reader.getEventType();
            int count = 0;
            int argumentCount = <xsl:value-of select="count(property)"/> ;
            boolean done =false;
            //event better be a START_ELEMENT. if not we should go up to the start element here
            while (!reader.isStartElement()){
                event = reader.next();
            }

            while(!done){
                if (javax.xml.stream.XMLStreamConstants.START_ELEMENT==event){
           <xsl:for-each select="property">
           <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
           <xsl:variable name="propertyType"><xsl:value-of select="@type"/></xsl:variable>
           <xsl:variable name="shortTypeName"><xsl:value-of select="@shorttypename"/></xsl:variable>
          <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>

               if ("<xsl:value-of select="$propertyName"/>".equals(reader.getLocalName())){
              <xsl:choose>
                   <xsl:when test="@ours">
                     object.set<xsl:value-of select="$javaName"/>(
                          <xsl:value-of select="$propertyType"/>.parse(reader));
                  </xsl:when>
                  <xsl:otherwise>
                      String content = reader.getElementText();
                      object.set<xsl:value-of select="$javaName"/>(
                      org.apache.axis2.databinding.schema.util.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(content));
                  </xsl:otherwise>
                 </xsl:choose>
                     count++;
               }
          </xsl:for-each>
                event = reader.next();
                }

                if (argumentCount==count){
                   done=true;
                }


            }

        } catch (javax.xml.stream.XMLStreamException e) {
            e.printStackTrace();
        }

        return object;
        }

    }
    </xsl:template>
 </xsl:stylesheet>