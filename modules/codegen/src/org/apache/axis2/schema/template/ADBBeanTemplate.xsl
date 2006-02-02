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

            /**
            * Auto generated setter method
            * @param param <xsl:value-of select="$javaName"/>
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

             <xsl:if test="$choice">
                 clearAllSettingTrackers();
             </xsl:if>
             <xsl:if test="$min=0 or $choice">
             //update the setting tracker
             <xsl:value-of select="$settingTracker"/> = true;
             </xsl:if>
            this.<xsl:value-of select="$varName"/>=param;
            }
            </xsl:if>
        </xsl:for-each>

        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        <xsl:choose>
            <xsl:when test="@type|@anon">
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
                        <xsl:when test="@ours or @any or @default">
                            elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                            elementList.add(<xsl:value-of select="$varName"/>);
                        </xsl:when>
                        <xsl:when test="@array">
                            elementList.add("<xsl:value-of select="$propertyName"/>");
                            elementList.add(<xsl:value-of select="$varName"/>);
                        </xsl:when>
                        <xsl:otherwise>
                             elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                             elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:if test="$min=0 or $choice">}</xsl:if>
                </xsl:for-each>

                <xsl:for-each select="property[@attribute]">
                    <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
                     <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                    <xsl:if test="position()>1">,</xsl:if>
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
            </xsl:when>
            <xsl:otherwise>
                <!-- if the element is associated with a type, then its gonna be only one -->
                //We can safely assume an element has only one type associated with it
                <!-- This better be only one!!-->
                <xsl:for-each select="property[@ours]">
                    <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
                    return <xsl:value-of select="$varName"/>.getPullParser(MY_QNAME);
                </xsl:for-each>

                <!-- What do we do for the other case ???? -->
                <xsl:for-each select="property[not(@ours)]">
                    <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
                    return org.apache.axis2.databinding.utils.ADBPullParser.createPullParser(MY_QNAME,
                    new Object[]{
                    org.apache.axis2.databinding.utils.ADBPullParser.ELEMENT_TEXT,
                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>)
                    },
                    new Object[]{});


                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>

        }

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
                java.util.ArrayList list<xsl:value-of select="position()"></xsl:value-of> = new java.util.ArrayList(5000);
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
                            <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.Factory.parse(reader));
                            //loop until we find a start element that is not part of this array
                            boolean <xsl:value-of select="$loopBoolName"/> = false;
                            while(!<xsl:value-of select="$loopBoolName"/>){
                            //loop to the end element
                            while (!reader.isEndElement()){
                            event = reader.next();
                            }
                            //step one event
                            event = reader.next();
                            if (reader.isEndElement() &amp;&amp;  !"<xsl:value-of select="$propertyName"/>".equals(reader.getLocalName())){
                            //two continuous end elements means we are exiting the xml structure
                            <xsl:value-of select="$loopBoolName"/> = true;
                            }else if (reader.isStartElement()){
                            if ("<xsl:value-of select="$propertyName"/>".equals(reader.getLocalName())){
                            <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.Factory.parse(reader));
                            }else{
                            <xsl:value-of select="$loopBoolName"/> = true;
                            }
                            }
                            }


                            // call the converter utility  to convert and set the array
                            object.set<xsl:value-of select="$javaName"/>(
                            (<xsl:value-of select="$propertyType"/>)
                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
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
                            if (reader.isEndElement() &amp;&amp;  !"<xsl:value-of select="$propertyName"/>".equals(reader.getLocalName())){
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
                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                            <xsl:value-of select="$basePropertyType"/>.class,
                            <xsl:value-of select="$listName"/>));
                            count++;
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:when test="@ours">
                    object.set<xsl:value-of select="$javaName"/>(
                    <xsl:value-of select="$propertyType"/>.Factory.parse(reader));
                    count++;
                </xsl:when>
                <xsl:when test="@any">
                    //do nothing yet!!!!
                </xsl:when>
                <xsl:otherwise>
                    String content = reader.getElementText();
                    object.set<xsl:value-of select="$javaName"/>(
                    org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(content));
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
        }//end of factory class

        }
    </xsl:template>
</xsl:stylesheet>