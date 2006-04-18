<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <!-- cater for the multiple classes - wrappped mode -->
    <xsl:template match="/beans">
        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        /**
        * <xsl:value-of select="$name"/>.java
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2 version: #axisVersion# #today#
        */

        package <xsl:value-of select="@package"/>;

        /**
        *  <xsl:value-of select="$name"/> wrapped bean classes
        */
        public class <xsl:value-of select="$name"/>{

        <xsl:apply-templates/>

        }
    </xsl:template>

    <!--cater for the multiple classes - unwrappped mode -->
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <!-- this is the common template -->
    <xsl:template match="bean">

        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="choice"><xsl:value-of select="@choice"/></xsl:variable>
        <xsl:variable name="isType" select="@type"/>
        <xsl:variable name="anon" select="@anon"/>
    <!-- write the class header. this should be done only when unwrapped -->

        <xsl:if test="not(not(@unwrapped) or (@skip-write))">
            /**
            * <xsl:value-of select="$name"/>.java
            *
            * This file was auto-generated from WSDL
            * by the Apache Axis2 version: #axisVersion# #today#
            */

            package <xsl:value-of select="@package"/>;
            /**
            *  <xsl:value-of select="$name"/> bean class
            */
        </xsl:if>
        public <xsl:if test="not(@unwrapped) or (@skip-write)">static</xsl:if> class <xsl:value-of select="$name"/> <xsl:if test="@extension"> extends <xsl:value-of select="@extension"/></xsl:if>
        implements org.apache.axis2.databinding.ADBBean{
        <xsl:choose>
            <xsl:when test="@type">/* This type was generated from the piece of schema that had
                name = <xsl:value-of select="@originalName"/>
                Namespace URI = <xsl:value-of select="@nsuri"/>
                Namespace Prefix = <xsl:value-of select="@nsprefix"/>
                */
            </xsl:when>
            <xsl:otherwise>
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "<xsl:value-of select="@nsuri"/>",
                "<xsl:value-of select="@originalName"/>",
                "<xsl:value-of select="@nsprefix"/>");

            </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="$choice">
            /** Whenever a new property is set ensure all others are unset
             *  There can be only one choice and the last one wins
             */
            private void clearAllSettingTrackers() {
            <xsl:for-each select="property">
                local<xsl:value-of select="@javaname"/>Tracker = false;
           </xsl:for-each>
            }
        </xsl:if>


        <xsl:for-each select="property">
            <!-- Write only the NOT inherited properties-->
            <xsl:if test="not(@inherited)">

            <xsl:variable name="propertyType"><xsl:value-of select="@type"></xsl:value-of></xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>

            <xsl:variable name="min"><xsl:value-of select="@minOccurs"/></xsl:variable>
            <xsl:variable name="varName">local<xsl:value-of select="$javaName"/></xsl:variable>
            <xsl:variable name="settingTracker">local<xsl:value-of select="$javaName"/>Tracker</xsl:variable>


            /**
            * field for <xsl:value-of select="$javaName"/>
            <xsl:if test="@attribute">* This was an Attribute!</xsl:if>
            <xsl:if test="@array">* This was an Array!</xsl:if>
            */

            protected <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text><xsl:value-of select="$varName" /> ;
           <!-- Generate a tracker only if the min occurs is zero, which means if the user does
                not bother to set that value, we do not send it -->
           <xsl:if test="$min=0 or $choice">
           /*  This tracker boolean wil be used to detect whether the user called the set method
               for this attribute. It will be used to determine whether to include this field
               in the serialized XML
           */
           protected boolean <xsl:value-of select="$settingTracker"/> = false ;
           </xsl:if>

           /**
           * Auto generated getter method
           * @return <xsl:value-of select="$propertyType"/>
           */
           public  <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text>get<xsl:value-of select="$javaName"/>(){
               return <xsl:value-of select="$varName"/>;
           }

           <!-- When generating the setters, we have to cater differently for the array!-->
            <xsl:choose>
               <xsl:when test="@array">
                   <xsl:variable name="basePropertyType"><xsl:value-of select="@arrayBaseType"/></xsl:variable>


                   <!-- generate the validator Method, this is specifiacally for validating the arrays-->
                  /**
                   * validate the array for <xsl:value-of select="$javaName"/>
                   */
                  protected void validate<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/> param){
                     <xsl:if test="not(@unbound)">
                          if (param.length &gt; <xsl:value-of select="@maxOccurs"/>){
                            throw new java.lang.RuntimeException();
                          }
                      </xsl:if>
                      <xsl:if test="$min!=0">
                          if (param.length &lt; <xsl:value-of select="$min"/>){
                            throw new java.lang.RuntimeException();
                          }
                      </xsl:if>
                  }


                 /**
                  * Auto generated setter method
                  * @param param <xsl:value-of select="$javaName"/>
                  */
                  public void set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/> param){
                   <!-- call the validator-->
                   validate<xsl:value-of select="$javaName"/>(param);

                   <xsl:if test="$choice">
                       clearAllSettingTrackers();
                   </xsl:if>
                   <xsl:if test="$min=0 or $choice">
                   //update the setting tracker
                   <xsl:value-of select="$settingTracker"/> = true;
                   </xsl:if>
                  this.<xsl:value-of select="$varName"/>=param;
                  }

                   <!-- we special case the 'array' scenario and generate a convenience
                       method for adding elements one by one to the array. The
                       current implementation is somewhat inefficient but
                       gets the job done.Since a primitive cannot be
                       treated as an object it has to be ignored!

                 -->
                 <xsl:if test="not(@primitive)">
                 /**
                 * Auto generated add method for the array for convenience
                 * @param param <xsl:value-of select="$basePropertyType"/>
                 */
                 public void add<xsl:value-of select="$javaName"/>(<xsl:value-of select="$basePropertyType"/> param){
                   if (<xsl:value-of select="$varName"/> == null){
                       <xsl:value-of select="$varName"/> = new <xsl:value-of select="$propertyType"/>{};
                   }
                   java.util.List list =
                        org.apache.axis2.databinding.utils.ConverterUtil.toList(<xsl:value-of select="$varName"/>);
                   list.add(param);
                   this.<xsl:value-of select="$varName"/> =
                     (<xsl:value-of select="$propertyType"/>)list.toArray(
                        new <xsl:value-of select="$basePropertyType"/>[list.size()]);

                 }
                 </xsl:if>
                     <!-- end of special casing for the array-->

               </xsl:when>
                <!-- Non array setter method-->
                <xsl:otherwise>
                    /**
                   * Auto generated setter method
                   * @param param <xsl:value-of select="$javaName"/>
                   */
                   public void set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/> param){
                    <xsl:if test="$choice">
                        clearAllSettingTrackers();
                    </xsl:if>
                    <xsl:if test="$min=0 or $choice">
                    //update the setting tracker
                    <xsl:value-of select="$settingTracker"/> = true;
                    </xsl:if>
                   this.<xsl:value-of select="$varName"/>=param;
                   }
                </xsl:otherwise>
            </xsl:choose>


             <!-- end of xsl:if for not(@inherited) -->
            </xsl:if>



        </xsl:for-each>

        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        <xsl:choose>
            <xsl:when test="@type or @anon">
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                <xsl:for-each select="property[not(@attribute)]">
                    <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
                    <xsl:variable name="min"><xsl:value-of select="@minOccurs"/></xsl:variable>
                    <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                    <xsl:variable name="settingTracker">local<xsl:value-of select="@javaname"/>Tracker</xsl:variable>


                    <xsl:if test="$min=0 or $choice"> if (<xsl:value-of select="$settingTracker"/>){</xsl:if>
                    <xsl:choose>
                        <xsl:when test="(@ours or @default) and not(@array)">
                            elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                            <!-- Arraylist can handle null's -->
                            <xsl:choose>
                                <xsl:when test="@nillable">
                                    elementList.add(<xsl:value-of select="$varName"/>==null?null:
                                    <xsl:value-of select="$varName"/>);
                                </xsl:when>
                                <xsl:otherwise>
                                    if (<xsl:value-of select="$varName"/>==null){
                                         throw new RuntimeException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                    }
                                    elementList.add(<xsl:value-of select="$varName"/>);
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:when test="(@ours or @default) and @array">
                             <xsl:choose>
                                <xsl:when test="@nillable">
                                    // this property is nillable
                                    if (<xsl:value-of select="$varName"/>!=null){
                                    <!--this barcket needs to be closed!-->
                                </xsl:when>
                                <xsl:otherwise>
                                    if (<xsl:value-of select="$varName"/>==null){
                                         throw new RuntimeException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                    }
                                </xsl:otherwise>
                            </xsl:choose>
                            for (int i = 0;i &lt; <xsl:value-of select="$varName"/>.length;i++){
                              elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                               elementList.add(<xsl:value-of select="$varName"/>[i]);
                            }
                            <!--we've opened a bracket for the nulls - fix it here-->
                            <xsl:if test="@nillable">}</xsl:if>
                        </xsl:when>
                        <!-- handle non ADB arrays - Not any however -->
                        <xsl:when test="@array and not(@any)">
                             <xsl:choose>
                                <xsl:when test="@nillable">
                                    // this property is nillable
                                    if (<xsl:value-of select="$varName"/>!=null){
                                    <!--this bracket needs to be closed!-->
                                </xsl:when>
                                <xsl:otherwise>
                                    if (<xsl:value-of select="$varName"/>==null){
                                         throw new RuntimeException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                    }
                                </xsl:otherwise>
                            </xsl:choose>
                            for (int i = 0;i &lt; <xsl:value-of select="$varName"/>.length;i++){
                              elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                              elementList.add(
                              org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>[i]));
                            }
                            <!--we've opened a bracket for the nulls - fix it here-->
                            <xsl:if test="@nillable">}</xsl:if>
                        </xsl:when>

                         <!-- handle non ADB arrays  - Any case  - any may not be
                         nillable -->
                        <xsl:when test="@array and @any">
                            for (int i = 0;i &lt; <xsl:value-of select="$varName"/>.length;i++){
                              elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                              elementList.add(
                              org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>[i]));
                            }
                            <!--we've opened a bracket for the nulls - fix it here-->
                        </xsl:when>
                        <!-- handle any - non array case-->
                         <xsl:when test="@any">
                            elementList.add(org.apache.axis2.databinding.utils.Constants.OM_ELEMENT_KEY);
                            elementList.add(<xsl:value-of select="$varName"/>);
                        </xsl:when>
                        <!-- handle binary - Since it is a Datahandler, we can just add it to the list
                          and the ADB pullparser would handle it right-->
                         <xsl:when test="@binary">
                            elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                            elementList.add(<xsl:value-of select="$varName"/>);
                        </xsl:when>
                        <!-- the usual case!!!!-->
                        <xsl:otherwise>
                             elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                            <xsl:choose>
                                <xsl:when test="@nillable and not(@primitive)">
                                    elementList.add(<xsl:value-of select="$varName"/>==null?null:
                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                                </xsl:when>
                                <xsl:otherwise>
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:if test="$min=0 or $choice">}</xsl:if>
                </xsl:for-each>

                <xsl:for-each select="property[@attribute]">
                    <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
                     <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                    <xsl:choose>
                        <xsl:when test="@any and not(@array)">
                            attribList.add(org.apache.axis2.databinding.utils.Constants.OM_ATTRIBUTE_KEY);
                            attribList.add(<xsl:value-of select="$varName"/>);
                        </xsl:when>
                         <xsl:when test="@any and @array">
                             for (int i=0;i &lt;<xsl:value-of select="$varName"/>.length;i++){
                               attribList.add(org.apache.axis2.databinding.utils.Constants.OM_ATTRIBUTE_KEY);
                               attribList.add(<xsl:value-of select="$varName"/>[i]);
                             }
                         </xsl:when>
                        <xsl:otherwise>
                            attribList.add(
                            new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"));
                            attribList.add(
                            org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            <!-- end of when for type & anon -->
            </xsl:when>
            <!-- Not a type and not anon. So it better be only one inclusion-->
            <xsl:otherwise>
                <!-- if the element is associated with a type, then its gonna be only one -->
                //We can safely assume an element has only one type associated with it
                <xsl:variable name="varName">local<xsl:value-of select="property/@javaname"/></xsl:variable>
                <xsl:variable name="nillable"><xsl:value-of select="property/@nillable"/></xsl:variable>
                <xsl:variable name="primitive"><xsl:value-of select="property/@primitive"/></xsl:variable>

                <xsl:choose>
                    <!-- This better be only one!!-->
                    <xsl:when test="property/@ours">

                        <xsl:choose>
                            <xsl:when test="$nillable">
                                if (<xsl:value-of select="$varName"/>==null){
                                   return new org.apache.axis2.databinding.utils.reader.NullXMLStreamReader(MY_QNAME);
                                }else{
                                   return <xsl:value-of select="$varName"/>.getPullParser(MY_QNAME);
                                }
                            </xsl:when>
                            <xsl:otherwise>return <xsl:value-of select="$varName"/>.getPullParser(MY_QNAME);</xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$nillable and not($primitive)">
                                if (<xsl:value-of select="$varName"/>==null){
                                      return new org.apache.axis2.databinding.utils.reader.NullXMLStreamReader(MY_QNAME);
                                }else{
                                   return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(MY_QNAME,
                                       new Object[]{
                                      org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT,
                                       org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>)
                                       },
                                       null);
                                }
                            </xsl:when>
                            <xsl:otherwise> return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(MY_QNAME,
                            new Object[]{
                            org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT,
                            org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>)
                            },
                            null);</xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>

        }




    <xsl:choose>
        <xsl:when test="@choice">

    <!-- start of template for choice/all. Select either for the
         presence of choice or the absence of ordered
     -->
     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        // This is horrible, but the OM implementation of getElementText() does not obey the proper contract.  Specifically, it does
        // does not advance the reader to the END_ELEMENT.  This bug is triggered by calls to getElementText() unpredictably, e.g. it
        // happens with outer (document) elements, but not with inner elements.  The root bug is in OMStAXWrapper.java, which is now part
        // of commons and so cannot just be fixed in axis2.  This method should be removed and the calls to it below replaced with
        // simple calls to getElementText() as soon as this serious bug can be fixed.
        private static String getElementTextProperly(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
            String value = reader.getElementText();
            while (!reader.isEndElement())
                reader.next();
            return value;
        }

        /**
        * static method to create the object
        */
        public static <xsl:value-of select="$name"/> parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            <xsl:value-of select="$name"/> object = new <xsl:value-of select="$name"/>();
            try {
                // We should already be at our outer StartElement, but make sure
                while (!reader.isStartElement())
                    reader.next();

                <xsl:if test="@nillable">
                   if ("true".equals(reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil"))){
                         return null;
                   }
                </xsl:if>

                <!-- populate attributes here!!!. The attributes are part of an element, not part of a type -->
                <xsl:for-each select="property[@attribute]">
                    <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="propertyType"><xsl:value-of select="@type"/></xsl:variable>
                    <xsl:variable name="shortTypeName"><xsl:value-of select="@shorttypename"/></xsl:variable>
                    <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
                    <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                    <xsl:variable name="attribName">tempAttrib<xsl:value-of select="$propertyName"/></xsl:variable>

                    java.lang.String <xsl:value-of select="$attribName"/> =
                      reader.getAttributeValue("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>");
                   if (<xsl:value-of select="$attribName"/>!=null){
                         object.set<xsl:value-of select="$javaName"/>(
                           org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(
                                <xsl:value-of select="$attribName"/>));
                    }

                </xsl:for-each>

                <xsl:if test="$isType or $anon">
                    <!-- Skip the outer start element in order to process the subelements. -->
                    reader.next();
                </xsl:if>  <!-- If we are not a type and not an element with anonymous type, then we are an element with one property for our named type. -->
                           <!-- Our single named-type property applies to our entire outer element, so don't skip it. -->
                <!-- First loop creates arrayLists for handling arrays -->
                <xsl:for-each select="property">
                    <xsl:if test="@array">
                        java.util.ArrayList list<xsl:value-of select="position()"/> = new java.util.ArrayList();
                    </xsl:if>
                </xsl:for-each>
                while(!reader.isEndElement()) {
                    if (reader.isStartElement()){
                        <!-- Now reloop and populate the code -->
                        <xsl:for-each select="property">
                            <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                            <xsl:variable name="propertyType"><xsl:value-of select="@type"/></xsl:variable>
                            <xsl:variable name="shortTypeName"><xsl:value-of select="@shorttypename"/></xsl:variable>
                            <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
                            <xsl:variable name="listName">list<xsl:value-of select="position()"/></xsl:variable>
                            <xsl:variable name="loopBoolName">loopDone<xsl:value-of select="position()"/></xsl:variable>
                            <xsl:variable name="startQname">startQname<xsl:value-of select="position()"/></xsl:variable>
                            <xsl:variable name="stateMachineName">stateMachine<xsl:value-of select="position()"/></xsl:variable>
                            <xsl:variable name="builderName">builder<xsl:value-of select="position()"/></xsl:variable>
                            <xsl:variable name="basePropertyType"><xsl:value-of select="@arrayBaseType"/></xsl:variable>
                            <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                            <xsl:variable name="min"><xsl:value-of select="@minOccurs"/></xsl:variable>

                            <xsl:if test="position()>1">else</xsl:if> if ("<xsl:value-of select="$propertyName"/>".equals(reader.getLocalName())){
                            <xsl:choose>
                                <xsl:when test="@array">
                                    <!-- We must be a named type or element with anonymous type. -->
                                    <!-- Elements with a named type have a single simple (non-array) property for their type -->
                                    // Process the array and step past its final element's end.
                                    <xsl:variable name="basePropertyType"><xsl:value-of select="@arrayBaseType"/></xsl:variable>
                                    <xsl:choose>
                                        <xsl:when test="@ours">
                                            <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.Factory.parse(reader));
                                            //loop until we find a start element that is not part of this array
                                            boolean <xsl:value-of select="$loopBoolName"/> = false;
                                            while(!<xsl:value-of select="$loopBoolName"/>){
                                                // We should be at the end element, but make sure
                                                while (!reader.isEndElement())
                                                    reader.next();
                                                // Step out of this element
                                                reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    <xsl:value-of select="$loopBoolName"/> = true;
                                                } else if (reader.isStartElement()){
                                                    if ("<xsl:value-of select="$propertyName"/>".equals(reader.getLocalName())){
                                                        <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.Factory.parse(reader));
                                                    }else{
                                                        <xsl:value-of select="$loopBoolName"/> = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                    <xsl:value-of select="$basePropertyType"/>.class,
                                                    <xsl:value-of select="$listName"/>));
                                        </xsl:when>
                                        <!-- End of Array handling of ADB classes -->
                                        <xsl:when test="@default">
                                             boolean <xsl:value-of select="$loopBoolName"/>=false;
                                             javax.xml.namespace.QName <xsl:value-of select="$startQname"/> = new javax.xml.namespace.QName(
                                                    "<xsl:value-of select="$namespace"/>",
                                                    "<xsl:value-of select="$propertyName"/>");

                                             while (!<xsl:value-of select="$loopBoolName"/>){
                                                 event = reader.getEventType();
                                                 if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event
                                                         &amp;&amp; <xsl:value-of select="$startQname"/>.equals(reader.getName())){

                                                      <!-- if-block that handles nillable -->
                                                      <xsl:if test="@nillable">
                                                          if ("true".equals(reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil"))){
                                                              <xsl:value-of select="$listName"/>.add(null);
                                                          }else{
                                                      </xsl:if>

                                                      // We need to wrap the reader so that it produces a fake START_DOCUEMENT event
                                                      org.apache.axis2.databinding.utils.NamedStaxOMBuilder <xsl:value-of select="$builderName"/>
                                                         = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                                                              new org.apache.axis2.util.StreamWrapper(reader), <xsl:value-of select="$startQname"/>);

                                                       <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$builderName"/>.getOMElement());
                                                       <xsl:if test="@nillable">}</xsl:if>
                                                 } else if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event &amp;&amp;
                                                            !<xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                                     <xsl:value-of select="$loopBoolName"/> = true;
                                                 }else if (javax.xml.stream.XMLStreamConstants.END_ELEMENT == event &amp;&amp;
                                                           !<xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                                     <xsl:value-of select="$loopBoolName"/> = true;
                                                 }else if (javax.xml.stream.XMLStreamConstants.END_DOCUMENT == event){
                                                     <xsl:value-of select="$loopBoolName"/> = true;
                                                 }else{
                                                     reader.next();
                                                 }

                                             }

                                             object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                 org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                     <xsl:value-of select="$basePropertyType"/>.class,<xsl:value-of select="$listName"/>));

                                        </xsl:when>
                                        <!-- End of Array handling of default class - that is the OMElement -->
                                        <xsl:otherwise>
                                            <xsl:value-of select="$listName"/>.add(getElementTextProperly(reader));
                                            //loop until we find a start element that is not part of this array
                                            boolean <xsl:value-of select="$loopBoolName"/> = false;
                                            while(!<xsl:value-of select="$loopBoolName"/>){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    <xsl:value-of select="$loopBoolName"/> = true;
                                                }else if (reader.isStartElement()){
                                                    if ("<xsl:value-of select="$propertyName"/>".equals(reader.getLocalName())){
                                                        <xsl:value-of select="$listName"/>.add(getElementTextProperly(reader));
                                                    }else{
                                                        <xsl:value-of select="$loopBoolName"/> = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                    <xsl:value-of select="$basePropertyType"/>.class,
                                                    <xsl:value-of select="$listName"/>));
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:when test="@ours">
                                    object.set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/>.Factory.parse(reader));
                                    <xsl:if test="$isType or $anon">  <!-- This is a subelement property to be consumed -->
                                        reader.next();
                                    </xsl:if>
                                </xsl:when>
                                <!-- start of any handling. Any can also be @default so we need to handle the any case before default! -->
                                <xsl:when test="@any">
                                    <!--No concerns of being nillable here. if it's ours and if the nillable attribute was present
                                        we would have outputted a null already-->
                                     <!--This can be any element and we may not know the name. so we pick the name of the element from the parser-->
                                     //use the QName from the parser as the name for the builder
                                     javax.xml.namespace.QName <xsl:value-of select="$startQname"/> = reader.getName();

                                     // We need to wrap the reader so that it produces a fake START_DOCUMENT event
                                     // this is needed by the builder classes
                                     org.apache.axis2.databinding.utils.NamedStaxOMBuilder <xsl:value-of select="$builderName"/> =
                                         new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                                             new org.apache.axis2.util.StreamWrapper(reader),<xsl:value-of select="$startQname"/>);
                                     object.set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$builderName"/>.getOMElement());
                                     <xsl:if test="$isType or $anon">  <!-- This is a subelement property to be consumed -->
                                         reader.next();
                                     </xsl:if>
                                </xsl:when>
                                <!-- end of adb type handling code -->
                                <!-- start of OMelement handling -->
                                 <xsl:when test="@default">
                                     boolean <xsl:value-of select="$loopBoolName"/> = false;
                                     javax.xml.namespace.QName <xsl:value-of select="$startQname"/> = new javax.xml.namespace.QName(
                                                                         "<xsl:value-of select="$namespace"/>",
                                                                         "<xsl:value-of select="$propertyName"/>");

                                     while(!<xsl:value-of select="$loopBoolName"/>){
                                         if (reader.isStartElement() &amp;&amp; <xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                             <xsl:value-of select="$loopBoolName"/> = true;
                                         }else{
                                             reader.next();
                                         }
                                     }

                                     <!-- todo  put the code here for nillable -->
                                     // We need to wrap the reader so that it produces a fake START_DOCUEMENT event
                                     // this is needed by the builder classes
                                     org.apache.axis2.databinding.utils.NamedStaxOMBuilder <xsl:value-of select="$builderName"/> =
                                         new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                                             new org.apache.axis2.util.StreamWrapper(reader),<xsl:value-of select="$startQname"/>);
                                     object.set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$builderName"/>.getOMElement());
                                     <xsl:if test="$isType or $anon">  <!-- This is a subelement property to be consumed -->
                                         reader.next();
                                     </xsl:if>
                                </xsl:when>
                                <!-- end of OMelement handling -->
                                <!-- start of the simple types handling -->
                                <xsl:otherwise>
                                    String content = getElementTextProperly(reader);
                                    object.set<xsl:value-of select="$javaName"/>(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(content));
                                    <xsl:if test="$isType or $anon">  <!-- This is a subelement property to be consumed -->
                                        reader.next();
                                    </xsl:if>
                                </xsl:otherwise>
                            </xsl:choose>

                            }

                        </xsl:for-each>
                        else
                            // A start element we are not expecting indicates an invalid parameter was passed
                            throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                    } else reader.next();  <!-- At neither a start nor an end element, skip it -->

                }

            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        <!-- end of template for choice/all -->
        </xsl:when>
    <!--
        Start of the template that generates the factory class for the sequence
        we select this template if the ordered attribute is present
    -->
    <xsl:otherwise>

        /**
             *  Factory class that keeps the parse method
             */
           public static class Factory{

               /**
               * static method to create the object
               */
               public static <xsl:value-of select="$name"/> parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
               <xsl:value-of select="$name"/> object = new <xsl:value-of select="$name"/>();
               try {
               int event = reader.getEventType();

              //event better be a START_ELEMENT. if not we should go up to the start element here
               while (event!= javax.xml.stream.XMLStreamReader.START_ELEMENT) {
                   event = reader.next();
               }

               <xsl:if test="not(@type)">
               if (!MY_QNAME.equals(reader.getName())){
                           throw new Exception("Wrong QName");
               }
               </xsl:if>

               <xsl:if test="@nillable">
                  if ("true".equals(reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil"))){
                        return null;
                  }
               </xsl:if>

               <!-- populate attributes here!!!. The attributes are part of an element, not part of a
              type -->
               <xsl:for-each select="property[@attribute]">
                   <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                   <xsl:variable name="propertyType"><xsl:value-of select="@type"/></xsl:variable>
                   <xsl:variable name="shortTypeName"><xsl:value-of select="@shorttypename"/></xsl:variable>
                   <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
                   <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                   <xsl:variable name="attribName">tempAttrib<xsl:value-of select="$propertyName"/></xsl:variable>

                   java.lang.String <xsl:value-of select="$attribName"/> =
                     reader.getAttributeValue("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>");
                  if (<xsl:value-of select="$attribName"/>!=null){
                        object.set<xsl:value-of select="$javaName"/>(
                          org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(
                               <xsl:value-of select="$attribName"/>));
                   }

               </xsl:for-each>

               <!-- Now reloop and populate the code for non-attribute values-->
               <xsl:for-each select="property[not(@attribute)]">
                   <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                   <xsl:variable name="propertyType"><xsl:value-of select="@type"/></xsl:variable>
                   <xsl:variable name="shortTypeName"><xsl:value-of select="@shorttypename"/></xsl:variable>
                   <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
                   <xsl:variable name="listName">list<xsl:value-of select="position()"/></xsl:variable>
                   <xsl:variable name="loopBoolName">loopDone<xsl:value-of select="position()"/></xsl:variable>
                   <xsl:variable name="startQname">startQname<xsl:value-of select="position()"/></xsl:variable>
                   <xsl:variable name="stateMachineName">stateMachine<xsl:value-of select="position()"/></xsl:variable>
                   <xsl:variable name="builderName">builder<xsl:value-of select="position()"/></xsl:variable>
                   <xsl:variable name="basePropertyType"><xsl:value-of select="@arrayBaseType"/></xsl:variable>
                   <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                   <xsl:variable name="min"><xsl:value-of select="@minOccurs"/></xsl:variable>
                   <xsl:variable name="nillable"><xsl:value-of select="@nillable"/></xsl:variable>


                   <xsl:choose>
                       <!-- Start of array handling code -->
                       <xsl:when test="@array">
                           <xsl:choose>
                               <xsl:when test="@ours">
                                      <!-- Somebody put the magic number 5000 here. I wonder who did that! -->
                                    java.util.ArrayList <xsl:value-of select="$listName"/> = new java.util.ArrayList();
                                   <!-- Start of Array handling of ADB classes -->
                                    boolean <xsl:value-of select="$loopBoolName"/>=false;
                                    javax.xml.namespace.QName <xsl:value-of select="$startQname"/> = new javax.xml.namespace.QName(
                                           "<xsl:value-of select="$namespace"/>",
                                           "<xsl:value-of select="$propertyName"/>");

                                    // Find the first element
                                    while (!<xsl:value-of select="$loopBoolName"/>){
                                           event = reader.getEventType();
                                           if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event
                                                   &amp;&amp; <xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                              //we are at the first element
                                               <xsl:value-of select="$loopBoolName"/> = true;
                                          <xsl:if test="$min=0">
                                            }else if (javax.xml.stream.XMLStreamConstants.END_ELEMENT == event
                                                 &amp;&amp;  !<xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                               //we've found an end element that does not belong to this type
                                               //since this can occur zero times, this may well be empty.

                                               object.set<xsl:value-of select="$javaName"/>(
                                                        (<xsl:value-of select="$propertyType"/>)
                                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                        <xsl:value-of select="$basePropertyType"/>.class,<xsl:value-of select="$listName"/>));
                                                break;

                                            </xsl:if>
                                           }else{
                                               reader.next();
                                           }

                                       }

                                       //Now loop and populate the array
                                       <xsl:value-of select="$loopBoolName"/> = false;
                                       while (!<xsl:value-of select="$loopBoolName"/>){
                                           event = reader.getEventType();
                                           if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event
                                                   &amp;&amp; <xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                            if (org.apache.axis2.databinding.utils.Constants.TRUE.equals(
                                     reader.getAttributeValue(
                                           org.apache.axis2.databinding.utils.Constants.XSI_NAMESPACE,
                                          org.apache.axis2.databinding.utils.Constants.NIL))){
                                            <xsl:value-of select="$listName"/>.add(null);
                                        }else{
                                             <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.Factory.parse(reader));
                                        }


                                           }else if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event
                                                   &amp;&amp; !<xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                              <xsl:value-of select="$loopBoolName"/> = true;
                                           }else if (javax.xml.stream.XMLStreamConstants.END_ELEMENT == event &amp;&amp;
                                               !<xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                               <xsl:value-of select="$loopBoolName"/> = true;
                                           }else{
                                               reader.next();
                                           }

                                       }

                                   object.set<xsl:value-of select="$javaName"/>(
                                       (<xsl:value-of select="$propertyType"/>)
                                      org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                      <xsl:value-of select="$basePropertyType"/>.class,<xsl:value-of select="$listName"/>));

                                   //move to the next event, probably past the last end_element event
                                   if (reader.getEventType()== javax.xml.stream.XMLStreamConstants.END_ELEMENT){
                                      reader.next();
                                   }
                               </xsl:when>
                               <xsl:when test="@default">
                                    <!-- Somebody put the magic number 5000 here. I wonder who did that! -->
                                    java.util.ArrayList <xsl:value-of select="$listName"/> = new java.util.ArrayList();
                                    boolean <xsl:value-of select="$loopBoolName"/>=false;
                                    javax.xml.namespace.QName <xsl:value-of select="$startQname"/> = new javax.xml.namespace.QName(
                                           "<xsl:value-of select="$namespace"/>",
                                           "<xsl:value-of select="$propertyName"/>");
                                   <xsl:variable name="internalLoopVar"><xsl:value-of select="$loopBoolName"/>_internal</xsl:variable>
                                           boolean <xsl:value-of select="$internalLoopVar"/> = false;

                                           while(!<xsl:value-of select="$internalLoopVar"/>){
                                               if (reader.isStartElement() &amp;&amp; <xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                                   <xsl:value-of select="$internalLoopVar"/> = true;
                                               }else{
                                                   reader.next();
                                               }
                                       }

                                       while (!<xsl:value-of select="$loopBoolName"/>){
                                           event = reader.getEventType();
                                           if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event
                                                   &amp;&amp; <xsl:value-of select="$startQname"/>.equals(reader.getName())){

                                           <!-- if-block that handles nillable -->
                                           <xsl:if test="@nillable">
                                              if ("true".equals(reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil"))){
                                                    <xsl:value-of select="$listName"/>.add(null);
                                              }else{
                                           </xsl:if>

                                           // We need to wrap the reader so that it produces a fake START_DOCUEMENT event
                                           org.apache.axis2.databinding.utils.NamedStaxOMBuilder <xsl:value-of select="$builderName"/>
                                              = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                                                   new org.apache.axis2.util.StreamWrapper(reader), <xsl:value-of select="$startQname"/>);

                                            <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$builderName"/>.getOMElement());
                                            <xsl:if test="@nillable">}</xsl:if>
                                           } else if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event &amp;&amp;
                                               !<xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                               <xsl:value-of select="$loopBoolName"/> = true;
                                           }else if (javax.xml.stream.XMLStreamConstants.END_ELEMENT == event &amp;&amp;
                                               !<xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                               <xsl:value-of select="$loopBoolName"/> = true;
                                           }else if (javax.xml.stream.XMLStreamConstants.END_DOCUMENT == event){
                                               <xsl:value-of select="$loopBoolName"/> = true;
                                           }else{
                                               reader.next();
                                           }

                                       }

                                   object.set<xsl:value-of select="$javaName"/>(
                                       (<xsl:value-of select="$propertyType"/>)
                                      org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                      <xsl:value-of select="$basePropertyType"/>.class,<xsl:value-of select="$listName"/>));


                               </xsl:when>
                               <!-- End of Array handling of ADB classes -->
                               <xsl:otherwise>
                                   <xsl:variable name="arrayVarName">textArray<xsl:value-of select="position()"/></xsl:variable>
                                   <xsl:if test="position()>1">

                                   // Move to a start element
                                   event = reader.getEventType();
                                   while (event!= javax.xml.stream.XMLStreamReader.START_ELEMENT) {
                                       event = reader.next();
                                   }
                                   </xsl:if>
                                  <!-- Start of Array handling of simple types -->
                                   org.apache.axis2.databinding.utils.SimpleArrayReaderStateMachine <xsl:value-of select="$stateMachineName"/> = new
                                                                   org.apache.axis2.databinding.utils.SimpleArrayReaderStateMachine();
                                   <xsl:value-of select="$stateMachineName"/>.setElementNameToTest(new javax.xml.namespace.QName(
                                   "<xsl:value-of select="$namespace"/>",
                                   "<xsl:value-of select="$propertyName"/>"));
                                   <xsl:if test="@nillable">
                                      <xsl:value-of select="$stateMachineName"/>.setNillable();
                                   </xsl:if>
                                   <xsl:value-of select="$stateMachineName"/>.read(reader);
                                   java.lang.String[] <xsl:value-of select="$arrayVarName"/> =
                                                <xsl:value-of select="$stateMachineName"/>.getTextArray();
                                   object.set<xsl:value-of select="$javaName"/>(
                                    (<xsl:value-of select="$propertyType"/>)
                                      org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                      <xsl:value-of select="$basePropertyType"/>.class,<xsl:value-of select="$arrayVarName"/>));
                                  <!-- end of Array handling of simple types -->
                               </xsl:otherwise>
                           </xsl:choose>
                    </xsl:when>
                    <!--  end of array handling -->

                    <!-- start of adb handling-->
                     <xsl:when test="@ours">

                         object.set<xsl:value-of select="$javaName"/>(
                         <xsl:value-of select="$propertyType"/>.Factory.parse(reader));


                     </xsl:when>

                     <!-- start of any handling. Any can also be @default so we need to
               handle the any case before default! -->
                     <xsl:when test="@any">
                         <!--No concerns of being nillable here. if it's ours and if the nillable attribute was present
                             we would have outputted a null already-->
                          boolean <xsl:value-of select="$loopBoolName"/> = false;
                          //move to the start element
                          while(!<xsl:value-of select="$loopBoolName"/>){
                           if (reader.isStartElement()){
                               <xsl:value-of select="$loopBoolName"/> = true;
                           }else{
                               reader.next();
                           }
                       }

                          <!--This can be any element and we may not know the name. so we pick the name of the
                          element from the parser-->
                       //use the QName from the parser as the name for the builder
                       javax.xml.namespace.QName <xsl:value-of select="$startQname"/> = reader.getName();

                       // We need to wrap the reader so that it produces a fake START_DOCUEMENT event
                       // this is needed by the builder classes
                       org.apache.axis2.databinding.utils.NamedStaxOMBuilder <xsl:value-of select="$builderName"/> = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                               new org.apache.axis2.util.StreamWrapper(reader),<xsl:value-of select="$startQname"/>);
                       object.set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$builderName"/>.getOMElement());


                     </xsl:when>
                     <!-- end of adb type handling code -->
                     <!-- start of OMelement handling -->
                      <xsl:when test="@default">
                       boolean <xsl:value-of select="$loopBoolName"/> = false;
                       javax.xml.namespace.QName <xsl:value-of select="$startQname"/> = new javax.xml.namespace.QName(
                                                              "<xsl:value-of select="$namespace"/>",
                                                              "<xsl:value-of select="$propertyName"/>");

                       while(!<xsl:value-of select="$loopBoolName"/>){
                           if (reader.isStartElement() &amp;&amp; <xsl:value-of select="$startQname"/>.equals(reader.getName())){
                               <xsl:value-of select="$loopBoolName"/> = true;
                           }else{
                               reader.next();
                           }
                       }

                       <!-- todo  put the code here for nillable -->
                       // We need to wrap the reader so that it produces a fake START_DOCUEMENT event
                       // this is needed by the builder classes
                       org.apache.axis2.databinding.utils.NamedStaxOMBuilder <xsl:value-of select="$builderName"/> = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                               new org.apache.axis2.util.StreamWrapper(reader),<xsl:value-of select="$startQname"/>);
                       object.set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$builderName"/>.getOMElement());

                       //step one more event from the current position
                       reader.next();
                     </xsl:when>
                     <!-- end of OMelement handling -->
                     <!-- start of the simple types handling -->
                     <xsl:otherwise>
                         <xsl:if test="position()>1">
                             // Move to a start element
                             event = reader.getEventType();
                             while (event!= javax.xml.stream.XMLStreamReader.START_ELEMENT) {
                               event = reader.next();
                             }
                         </xsl:if>
                       org.apache.axis2.databinding.utils.SimpleElementReaderStateMachine <xsl:value-of select="$stateMachineName"/>
                         = new org.apache.axis2.databinding.utils.SimpleElementReaderStateMachine();
                       javax.xml.namespace.QName <xsl:value-of select="$startQname"/> = new javax.xml.namespace.QName(
                                            "<xsl:value-of select="$namespace"/>",
                                           "<xsl:value-of select="$propertyName"/>");
                       <xsl:value-of select="$stateMachineName"/>.setElementNameToTest(<xsl:value-of select="$startQname"/>);
                       <xsl:if test="@nillable">
                               <xsl:value-of select="$stateMachineName"/>.setNillable();
                       </xsl:if>
                       <xsl:value-of select="$stateMachineName"/>.read(reader);
                       object.set<xsl:value-of select="$javaName"/>(
                         <xsl:choose>
                             <xsl:when test="@nillable and not(@primitive)">
                                  <xsl:value-of select="$stateMachineName"/>.getText()==null?null:
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(
                                  <xsl:value-of select="$stateMachineName"/>.getText()));
                             </xsl:when>
                             <xsl:otherwise>
                            org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(
                                  <xsl:value-of select="$stateMachineName"/>.getText()));
                             </xsl:otherwise>
                         </xsl:choose>

                     </xsl:otherwise>
                      <!-- end of simple type handling -->

                   </xsl:choose>

                </xsl:for-each>
               } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
               }

               return object;
               }
               }//end of factory class


     <!-- end of the template -->

        </xsl:otherwise>
    </xsl:choose>
        }
           <!-- end of main template -->
          </xsl:template>


</xsl:stylesheet>