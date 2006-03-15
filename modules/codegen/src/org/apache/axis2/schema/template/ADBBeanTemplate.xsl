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
                       gets the job done-->

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
                        <xsl:when test="@ours or @default or @array">
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
                        <!-- handle arrays -->
                        <!-- handle any-->
                         <xsl:when test="@any">
                             <!--todo Not sure whether this is right-->
                            elementList.add(null);
                            elementList.add(<xsl:value-of select="$varName"/>);
                        </xsl:when>
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
                        <xsl:when test="@anyAtt">
                            attribList.add(null);
                            attribList.add(<xsl:value-of select="$varName"/>);
                        </xsl:when>
                        <xsl:otherwise>
                            attribList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"));
                            attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>

                return org.apache.axis2.databinding.utils.ADBPullParser.createPullParser(qName, elementList.toArray(), attribList.toArray());
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
                                   return new org.apache.axis2.databinding.utils.NullablePullParser(MY_QNAME);
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
                                      return new org.apache.axis2.databinding.utils.NullablePullParser(MY_QNAME);
                                }else{
                                   return org.apache.axis2.databinding.utils.ADBPullParser.createPullParser(MY_QNAME,
                                       new Object[]{
                                       org.apache.axis2.databinding.utils.ADBPullParser.ELEMENT_TEXT,
                                       org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>)
                                       },
                                       new Object[]{});
                                }
                            </xsl:when>
                            <xsl:otherwise> return org.apache.axis2.databinding.utils.ADBPullParser.createPullParser(MY_QNAME,
                            new Object[]{
                            org.apache.axis2.databinding.utils.ADBPullParser.ELEMENT_TEXT,
                            org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>)
                            },
                            new Object[]{});</xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>

        }

        /**
        * utility method to http://www.w3.org/2001/XMLSchema-instance
        */

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
                                        //since this can occur zero times, this may well be empty.So return
                                        //the empty object
                                        return object;

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
                                       <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.Factory.parse(reader));
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
                            reader.next();
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
                            java.lang.String[] textArray = <xsl:value-of select="$stateMachineName"/>.getTextArray();
                            object.set<xsl:value-of select="$javaName"/>(
                             (<xsl:value-of select="$propertyType"/>)
                               org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                               <xsl:value-of select="$basePropertyType"/>.class,<xsl:value-of select="$stateMachineName"/>.getTextArray()));

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

        }
    </xsl:template>
</xsl:stylesheet>