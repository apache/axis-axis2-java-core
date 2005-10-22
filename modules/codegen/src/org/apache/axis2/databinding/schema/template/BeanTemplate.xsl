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
        <xsl:choose>
            <xsl:when test="@type">/* This type was generated from the piece of schema that had
                name = <xsl:value-of select="$name"/>
                Namespace URI = <xsl:value-of select="@nsuri"/>
                Namespace Prefix = <xsl:value-of select="@nsprefix"/>
                */
            </xsl:when>
            <xsl:otherwise>
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "<xsl:value-of select="@nsuri"/>",
                "<xsl:value-of select="$name"/>",
                "<xsl:value-of select="@nsprefix"/>");

            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="@type"></xsl:if>

        <xsl:for-each select="property">
            <xsl:variable name="propertyType"><xsl:value-of select="@type"></xsl:value-of></xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
            <xsl:variable name="varName">local<xsl:value-of select="$javaName"/></xsl:variable>
            /**
            * field for <xsl:value-of select="$javaName"/>
            <xsl:if test="@attribute">* This was an Attribute!</xsl:if>
            <xsl:if test="@array">* This was an Array!</xsl:if>

            */
            private <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text><xsl:value-of select="$varName" /> ;

            /**
            * Auto generated getter method
            * @return <xsl:value-of select="$propertyType"/>
            */
            public  <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text>get<xsl:value-of select="$javaName"/>(){
            return <xsl:value-of select="$varName"/>;
            }

            /**
            * Auto generated setter method
            * @param param<xsl:value-of select="$javaName"/>
            */
            public void set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/> param){
            <!--Add the validation code. For now we only add the validation code for arrays-->
             <xsl:if test="@array">
                 <xsl:if test="not(@unbound)">
                     if (param.length &gt; <xsl:value-of select="@maxOccurs"></xsl:value-of>){
                        throw new java.lang.RuntimeException();
                     }
                 </xsl:if>
                 <xsl:if test="@minOccurs">
                     if (param.length &lt; <xsl:value-of select="@minOccurs"></xsl:value-of>){
                        throw new java.lang.RuntimeException();
                     }
                 </xsl:if>
             </xsl:if>
            this.<xsl:value-of select="$varName"/>=param;
            }
        </xsl:for-each>

        /**
        * databinding method to get an XML representation of this object
        * Note - this is not complete
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){

        Object[] elementList = new Object[]{
        <xsl:for-each select="property[not(@attribute)]">
            <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
            <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>

            <xsl:if test="position()>1">,</xsl:if>
            <xsl:choose>
                <xsl:when test="@ours">
                            new javax.xml.namespace.QName("<xsl:value-of select="$propertyName"/>"),<xsl:value-of select="$varName"/>
                </xsl:when>
                <xsl:when test="@any">
                            new javax.xml.namespace.QName("<xsl:value-of select="$propertyName"/>"),<xsl:value-of select="$varName"/>
                </xsl:when>
                <xsl:otherwise>
                            "<xsl:value-of select="$propertyName"/>",org.apache.axis2.databinding.schema.util.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>)
                </xsl:otherwise>
            </xsl:choose>
            </xsl:for-each>};

        Object[] attribList = new Object[]{
        <xsl:for-each select="property[@attribute]">
            <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
            <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
              <xsl:if test="position()>1">,</xsl:if>
               <xsl:choose>
                   <xsl:when test="@anyAtt">
                       null,<xsl:value-of select="$varName"/>
                   </xsl:when>
                   <xsl:otherwise>
                      new javax.xml.namespace.QName("<xsl:value-of select="$propertyName"/>"),org.apache.axis2.databinding.schema.util.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>)
                   </xsl:otherwise>
               </xsl:choose>
             </xsl:for-each>
         };

         return org.apache.axis2.databinding.utils.ADBPullParser.createPullParser(qName, elementList, attribList);
        }

        /**
        * static method to create the object
        * Note -  This is not complete
        */
        public static <xsl:value-of select="$name"/> parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
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
        <!-- First loop creates arrayLists for handling arrays -->
        <xsl:for-each select="property">
            <xsl:if test="@array">
        java.util.ArrayList list<xsl:value-of select="position()"></xsl:value-of> = new java.util.ArrayList();
            </xsl:if>
        </xsl:for-each>
        while(!done){
        if (javax.xml.stream.XMLStreamConstants.START_ELEMENT==event){
        <!-- Now reloop and populate the code -->
        <xsl:for-each select="property">
            <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
            <xsl:variable name="propertyType"><xsl:value-of select="@type"/></xsl:variable>
            <xsl:variable name="shortTypeName"><xsl:value-of select="@shorttypename"/></xsl:variable>
            <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
            <xsl:variable name="listName">list<xsl:value-of select="position()"/></xsl:variable>
            <xsl:variable name="loopBoolName">loopDone<xsl:value-of select="position()"/></xsl:variable>

            if ("<xsl:value-of select="$propertyName"/>".equals(reader.getLocalName())){
            <xsl:choose>
                <xsl:when test="@array">
                    <xsl:variable name="basePropertyType"><xsl:value-of select="@arrayBaseType"/></xsl:variable>
                   <xsl:choose>
                    <xsl:when test="@ours">
                        <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.parse(reader));
                   //loop until we find a start element that is not part of this array
                   boolean <xsl:value-of select="$loopBoolName"/> = false;
                   while(!<xsl:value-of select="$loopBoolName"/>){
                      //loop to the end element
                      while (!reader.isEndElement()){
                            event = reader.next();
                      }
                      //step one event
                      event = reader.next();
                      if (reader.isEndElement()){
                           //two continuous end elements means we are exiting the xml structure
                           <xsl:value-of select="$loopBoolName"/> = true;
                      }else if (reader.isStartElement()){
                           if ("<xsl:value-of select="$propertyName"/>".equals(reader.getLocalName())){
                             <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.parse(reader));
                           }else{
                             <xsl:value-of select="$loopBoolName"/> = true;
                           }
                      }
                     }


                     // call the converter utility  to convert and set the array
                      object.set<xsl:value-of select="$javaName"/>(
                        (<xsl:value-of select="$propertyType"/>)
                        org.apache.axis2.databinding.schema.util.ConverterUtil.convertToArray(
                         <xsl:value-of select="$basePropertyType"/>.class,
                         <xsl:value-of select="$listName"/>));

                     count++;
                       </xsl:when>
                       <xsl:otherwise>
                   <xsl:value-of select="$listName"/>.add(reader.getElementText());
                   //loop until we find a start element that is not part of this array
                   boolean <xsl:value-of select="$loopBoolName"/> = false;
                   while(!<xsl:value-of select="$loopBoolName"/>){
                      //loop to the end element
                      while (!reader.isEndElement()){
                            event = reader.next();
                      }
                      //step one event
                      event = reader.next();
                      if (reader.isEndElement()){
                           //two continuous end elements means we are exiting the xml structure
                           <xsl:value-of select="$loopBoolName"/> = true;
                      }else if (reader.isStartElement()){
                           if ("<xsl:value-of select="$propertyName"/>".equals(reader.getLocalName())){
                              <xsl:value-of select="$listName"/>.add(reader.getElementText());
                           }else{
                                <xsl:value-of select="$loopBoolName"/> = true;
                           }
                      }
                     }

                   // call the converter utility  to convert and set the array
                   object.set<xsl:value-of select="$javaName"/>(
                      (<xsl:value-of select="$propertyType"/>)
                      org.apache.axis2.databinding.schema.util.ConverterUtil.convertToArray(
                      <xsl:value-of select="$basePropertyType"/>.class,
                      <xsl:value-of select="$listName"/>));
                     count++;
                       </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:when test="@ours">
                    object.set<xsl:value-of select="$javaName"/>(
                    <xsl:value-of select="$propertyType"/>.parse(reader));
                     count++;
                </xsl:when>
                <xsl:when test="@any">
                    //do nothing yet!!!!
                </xsl:when>
                <xsl:otherwise>
                    String content = reader.getElementText();
                    object.set<xsl:value-of select="$javaName"/>(
                    org.apache.axis2.databinding.schema.util.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(content));
                    count++;
                </xsl:otherwise>
            </xsl:choose>

            }

        </xsl:for-each>
        }

        if (argumentCount==count){
            done=true;
        }

         if (!done){
            event = reader.next();
         }

        }

        } catch (javax.xml.stream.XMLStreamException e) {
            throw new java.lang.Exception(e);
        }

        return object;
        }

        }
    </xsl:template>
</xsl:stylesheet>