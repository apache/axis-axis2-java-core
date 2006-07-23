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

    <xsl:template match="bean[not(@helperMode)]">

        <xsl:variable name="name" select="@name"/>
        <xsl:variable name="choice" select="@choice"/>
        <xsl:variable name="ordered" select="@ordered"/>
        <xsl:variable name="unordered" select="not($ordered)"/>  <!-- for convenience -->
        <xsl:variable name="isType" select="@type"/>
        <xsl:variable name="anon" select="@anon"/>

        <xsl:variable name="nsuri" select="@nsuri"/>
        <xsl:variable name="originalName" select="@originalName"/>
        <xsl:variable name="nsprefix" select="@nsprefix"/>
        <xsl:variable name="extension" select="@extension"/>
        <xsl:variable name="mapperClass" select="@mapperClass"/>
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
        public <xsl:if test="not(@unwrapped) or (@skip-write)">static</xsl:if> class <xsl:value-of select="$name"/> <xsl:if test="$extension"> extends <xsl:value-of select="$extension"/></xsl:if>
        implements org.apache.axis2.databinding.ADBBean{
        <xsl:choose>
            <xsl:when test="@type">/* This type was generated from the piece of schema that had
                name = <xsl:value-of select="$originalName"/>
                Namespace URI = <xsl:value-of select="$nsuri"/>
                Namespace Prefix = <xsl:value-of select="$nsprefix"/>
                */
            </xsl:when>
            <xsl:otherwise>
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "<xsl:value-of select="$nsuri"/>",
                "<xsl:value-of select="$originalName"/>",
                "<xsl:value-of select="$nsprefix"/>");

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
                       <!-- the updating of setting tracker for null values should
                            happen if the attribute is marked as nillable. Else
                            the user can set a null value and it is never marked
                            as set
                       -->
                       <xsl:choose>
                          <xsl:when test="not(@nillable) and not(@primitive)">
                              if (param !=null){
                              //update the setting tracker
                              <xsl:value-of select="$settingTracker"/> = true;
                               }
                           </xsl:when>
                           <xsl:otherwise>
                               //update the setting tracker
                              <xsl:value-of select="$settingTracker"/> = true;
                           </xsl:otherwise>
                       </xsl:choose>
                   </xsl:if>
                  this.<xsl:value-of select="$varName"/>=param;
                  }

                   <!--

                       we special case the 'array' scenario and generate a
                       convenience method for adding elements one by one to
                       the array. The current implementation is somewhat
                       inefficient but gets the job done.Since a primitive
                       cannot be treated as an object it has to be ignored!

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

                    <xsl:if test="$choice">
                           clearAllSettingTrackers();
                    </xsl:if>
                    <xsl:if test="$min=0 or $choice">
                         //update the setting tracker
                         <xsl:value-of select="$settingTracker"/> = true;
                    </xsl:if>

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

     <!-- ######################################################################################### -->
     <!-- get OMElement methods that allows direct writing -->
     /**
     *
     * @param parentQName
     * @param factory
     * @return
     */
    public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            <xsl:choose>
            <xsl:when test="@type or @anon">
                <!-- For a type write the passed in QName first-->
		
		        java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();
                
                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrifix();
                        }
                        
                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
		    xmlWriter.writeStartElement(parentQName.getLocalPart());
		}

                <!-- write the type attribute if needed -->
               <xsl:if test="$extension">
               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                       registerPrefix(xmlWriter,"<xsl:value-of select="$nsuri"/>")+":<xsl:value-of select="$originalName"/>",
                       xmlWriter);
               </xsl:if>
                <!--First serialize the attributes!-->
                <xsl:for-each select="property[@attribute]">
                    <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
                     <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                    <xsl:choose>
                        <!-- Note - It is assumed that any attributes are OMAttributes-->
                        <xsl:when test="@any and not(@array)">
                           writeAttribute(<xsl:value-of select="$varName"/>.getNamespace().getName(),
                                                     <xsl:value-of select="$varName"/>.getLocalName(),
                                                     <xsl:value-of select="$varName"/>.getAttributeValue(),
                                                     xmlWriter);
                        </xsl:when>
                         <xsl:when test="@any and @array">
                             for (int i=0;i &lt;<xsl:value-of select="$varName"/>.length;i++){
                              writeAttribute(<xsl:value-of select="$varName"/>[i].getNamespace().getName(),
                                                     <xsl:value-of select="$varName"/>[i].getLocalName(),
                                                     <xsl:value-of select="$varName"/>[i].getAttributeValue(),
                                                  xmlWriter);
                             }
                         </xsl:when>
                        <!-- there can never be attribute arrays in the normal case-->
                        <xsl:otherwise>
                             writeAttribute("<xsl:value-of select="$namespace"/>",
                                                     "<xsl:value-of select="$propertyName"/>",
                                                      org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>),
                            xmlWriter);
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>

                <!-- Now serialize the elements-->
                <xsl:for-each select="property[not(@attribute)]">
                    <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
                    <xsl:variable name="min"><xsl:value-of select="@minOccurs"/></xsl:variable>
                    <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                    <xsl:variable name="settingTracker">local<xsl:value-of select="@javaname"/>Tracker</xsl:variable>


                    <xsl:if test="$min=0 or $choice"> if (<xsl:value-of select="$settingTracker"/>){</xsl:if>
                    <xsl:choose>
                        <xsl:when test="@ours and not(@array) and not(@default)">
                            <xsl:choose>
                                <xsl:when test="@nillable">
                                    if (<xsl:value-of select="$varName"/>==null){
				    
				        java.lang.String namespace = "<xsl:value-of select="$namespace"/>";
			
					if (! namespace.equals("")) {
						java.lang.String prefix = xmlWriter.getPrefix(namespace);
				    
						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrifix();
					
							xmlWriter.writeStartElement(prefix,"<xsl:value-of select="$propertyName"/>", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
					    
						} else {
							xmlWriter.writeStartElement(namespace,"<xsl:value-of select="$propertyName"/>");
						}
				    
					} else {
						xmlWriter.writeStartElement("<xsl:value-of select="$propertyName"/>");
					}
					

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","true",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     <xsl:value-of select="$varName"/>.getOMElement(
                                       new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"),
                                        factory).serialize(xmlWriter);
                                    }
                                </xsl:when>
                                <xsl:otherwise>
                                    if (<xsl:value-of select="$varName"/>==null){
                                         throw new RuntimeException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                    }
                                   <xsl:value-of select="$varName"/>.getOMElement(
                                       new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"),
                                       factory).serialize(xmlWriter);
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:when test="@ours and @array and not(@default)">
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
                             <xsl:value-of select="$varName"/>[i].getOMElement(
                                       new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"),
                                       factory).serialize(xmlWriter);

                            }
                            <!--we've opened a bracket for the nulls - fix it here-->
                            <xsl:if test="@nillable">}</xsl:if>
                        </xsl:when>

                         <xsl:when test="@default and @array">
                            <!-- Note - Assumed to be OMElement-->
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
                                  <xsl:value-of select="$varName"/>[i].serialize(xmlWriter);
                              }

                            <!--we've opened a bracket for the nulls - fix it here-->
                            <xsl:if test="@nillable">}</xsl:if>

                        </xsl:when>

                        <xsl:when test="@default and not(@array)">
                            <!-- Note - Assumed to be OMElement-->
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
                             <xsl:value-of select="$varName"/>.serialize(xmlWriter);
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
                            	        namespace = "<xsl:value-of select="$namespace"/>";

					if (! namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrifix();

							xmlWriter.writeStartElement(prefix,"<xsl:value-of select="$propertyName"/>", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,"<xsl:value-of select="$propertyName"/>");
						}

					} else {
						xmlWriter.writeStartElement("<xsl:value-of select="$propertyName"/>");
					}



                              if (<xsl:value-of select="$varName"/>[i]==null){
                                  // write the nil attribute
                                  writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","true",xmlWriter);
                              }else{
                                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>[i]));
                              }
                              xmlWriter.writeEndElement();
                            }
                            <!--we've opened a bracket for the nulls - fix it here-->
                            <xsl:if test="@nillable">}</xsl:if>
                        </xsl:when>

                         <!-- handle non ADB arrays  - Any case  - any may not be
                         nillable -->
                        <xsl:when test="@array and @any">
                            <!-- Note - Assumed to be OMElement-->
                            for (int i = 0;i &lt; <xsl:value-of select="$varName"/>.length;i++){
                              <xsl:value-of select="$varName"/>[i].serialize(xmlWriter);
                            }
                        </xsl:when>
                        <!-- handle any - non array case-->
                         <xsl:when test="@any">
                            <!-- Note - Assumed to be OMElement-->
                            <xsl:value-of select="$varName"/>.serialize(xmlWriter);
                        </xsl:when>

                        <!-- handle all other cases including the binary case -->
                         <xsl:otherwise>
			 		namespace = "<xsl:value-of select="$namespace"/>";

					if (! namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrifix();

							xmlWriter.writeStartElement(prefix,"<xsl:value-of select="$propertyName"/>", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,"<xsl:value-of select="$propertyName"/>");
						}

					} else {
						xmlWriter.writeStartElement("<xsl:value-of select="$propertyName"/>");
					}

				<!-- TODO remove the following line -->
                            //xmlWriter.writeStartElement("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>");
                            xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                            xmlWriter.writeEndElement();
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:if test="$min=0 or $choice">}</xsl:if>

                </xsl:for-each>
                   <!-- write the end element for the type-->
               xmlWriter.writeEndElement();
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
				        java.lang.String namespace = "<xsl:value-of select="property/@nsuri"/>";

						if (! namespace.equals("")) {
							java.lang.String prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrifix();

								xmlWriter.writeStartElement(prefix,"<xsl:value-of select="property/@name"/>", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,"<xsl:value-of select="property/@name"/>");
							}

						} else {
							xmlWriter.writeStartElement("<xsl:value-of select="property/@name"/>");
						}

                                        // write the nil attribute
                                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","true",xmlWriter);
                                        xmlWriter.writeEndElement();
                                       }else{
                                         <xsl:value-of select="$varName"/>.getOMElement(
                                         MY_QNAME,
                                         factory).serialize(xmlWriter);
                                       }
                            </xsl:when>
                            <xsl:otherwise>
                                 if (<xsl:value-of select="$varName"/>==null){
                                   throw new RuntimeException("Property cannot be null!");
                                 }
                                 <xsl:value-of select="$varName"/>.getOMElement(
                                         MY_QNAME,
                                         factory).serialize(xmlWriter);
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <!-- end of ours block-->
                    <xsl:otherwise>
		        java.lang.String namespace = "<xsl:value-of select="property/@nsuri"/>";

			if (! namespace.equals("")) {
				java.lang.String prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrifix();

					xmlWriter.writeStartElement(prefix,"<xsl:value-of select="property/@name"/>", namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace,"<xsl:value-of select="property/@name"/>");
				}

			} else {
				xmlWriter.writeStartElement("<xsl:value-of select="property/@name"/>");
			}

			xmlWriter.writeCharacters(
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                       xmlWriter.writeEndElement();
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{

           registerPrefix(xmlWriter, namespace);

            xmlWriter.writeAttribute(namespace,attName,attValue);
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        <xsl:choose>
            <xsl:when test="@type">
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);
            </xsl:when>
            <xsl:otherwise>
               //ignore the QName passed in - we send only OUR QName!
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            </xsl:otherwise>
       </xsl:choose>
    }

  <!-- ######################################################################################### -->
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

  <!-- ######################################################################################### -->

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        // This is horrible, but the OM implementation of getElementText() does not obey the proper contract.  Specifically, it does
        // does not advance the reader to the END_ELEMENT.  This bug is triggered by calls to getElementText() unpredictably, e.g. it
        // happens with outer (document) elements, but not with inner elements.  The root bug is in OMStAXWrapper.java, which is now part
        // of commons and so cannot just be fixed in axis2.  This method should be removed and the calls to it below replaced with
        // simple calls to getElementText() as soon as this serious bug can be fixed.

        private static java.lang.String getElementTextProperly(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
            java.lang.String value = reader.getElementText();
            while (!reader.isEndElement())
                reader.next();
            return value;
        }

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static <xsl:value-of select="$name"/> parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            <xsl:value-of select="$name"/> object = new <xsl:value-of select="$name"/>();
            int event;
            try {
                <!-- Advance to our start element, or if we are a complex type, to our first property start element or the outer end element if no properties -->
                while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                    reader.next();

                <xsl:if test="@nillable">
                   if ("true".equals(reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil"))){
                         // Skip the element and report the null value.  It cannot have subelements.
                         while (!reader.isEndElement())
                             reader.next();
                         return null;
                   }
                </xsl:if>
                  <xsl:if test="$isType or $anon">
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"<xsl:value-of select="$originalName"/>".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (<xsl:value-of select="$name"/>)<xsl:value-of select="$mapperClass"/>.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                </xsl:if>

                <!-- populate attributes here!!!. The attributes are part of an element, not part of a type -->
                <xsl:for-each select="property[@attribute]">
                    <xsl:variable name="propertyName" select="@name"/>
                    <xsl:variable name="propertyType" select="@type"/>
                    <xsl:variable name="shortTypeNameUncapped"  select="@shorttypename"/>
                    <xsl:variable name="shortTypeName"  select="@shorttypename"/>
                    <xsl:variable name="javaName" select="@javaname"/>
                    <xsl:variable name="namespace" select="@nsuri"/>
                    <xsl:variable name="attribName">tempAttrib<xsl:value-of select="$propertyName"/></xsl:variable>

                    java.lang.String <xsl:value-of select="$attribName"/> =
                      reader.getAttributeValue("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>");
                   if (<xsl:value-of select="$attribName"/>!=null){
                         object.set<xsl:value-of select="$javaName"/>(
                           org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(
                                <xsl:value-of select="$attribName"/>));
                    }

                </xsl:for-each>

                boolean isReaderMTOMAware = false;
                <!-- workaround for the issues in the wstx reader!-->
                try{
                  isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
                }catch(java.lang.IllegalArgumentException e){
                  isReaderMTOMAware = false;
                }


                <xsl:if test="$isType or $anon">
                    <!-- Skip the outer start element in order to process the subelements. -->
                    reader.next();
                </xsl:if>
                      <!-- If we are not a type and not an element with anonymous type, then we are an element with one property for our named type. -->
                      <!-- Our single named-type property applies to our entire outer element, so don't skip it. -->
                <!-- First loop creates arrayLists for handling arrays -->
                <xsl:for-each select="property[not(@attribute)]">
                    <xsl:if test="@array">
                        java.util.ArrayList list<xsl:value-of select="position()"/> = new java.util.ArrayList();
                    </xsl:if>
                </xsl:for-each>

                <xsl:if test="property[not(@attribute)]">
                <xsl:if test="$unordered">   <!-- Properties can be in any order -->
                while(!reader.isEndElement()) {
                    if (reader.isStartElement()){
                </xsl:if>
                </xsl:if>

                        <!-- Now reloop and populate the code -->
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

                            <xsl:variable name="propQName">new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>")</xsl:variable>

                           <xsl:choose>
                                <xsl:when test="$unordered">  <!-- One property per iteration if unordered -->
                                    <xsl:if test="position()>1">
                                        else
                                    </xsl:if>
                                </xsl:when>
                                <xsl:otherwise>
                                    <!-- If sequence, advance to start of next property or to end of outer element -->
                                    while (!reader.isStartElement() &amp;&amp; !reader.isEndElement()) reader.next();
                                </xsl:otherwise>
                            </xsl:choose>
                            if (reader.isStartElement() &amp;&amp; <xsl:value-of select="$propQName"/>.equals(reader.getName())){
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
                                                // Step to next element event.
                                                while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    <xsl:value-of select="$loopBoolName"/> = true;
                                                } else {
                                                    if (<xsl:value-of select="$propQName"/>.equals(reader.getName())){
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
                                                // Step to next element event.
                                                while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    <xsl:value-of select="$loopBoolName"/> = true;
                                                } else {
                                                    if (<xsl:value-of select="$propQName"/>.equals(reader.getName())){
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
                                <!-- start of the simple types handling for binary content-->
                                <xsl:when test="@binary">
                                    if (isReaderMTOMAware
                                      &amp;&amp;
                                    java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_BINARY))){
                                         //MTOM aware reader - get the datahandler directly and put it in the object
                                         object.set<xsl:value-of select="$javaName"/>(
                                         (javax.activation.DataHandler)reader.getProperty(org.apache.axiom.om.OMConstants.DATA_HANDLER));
                                     }else{
                                        //Do the usual conversion
                                        java.lang.String content = getElementTextProperly(reader);
                                        object.set<xsl:value-of select="$javaName"/>(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(content));

                                     }

                                    <xsl:if test="$isType or $anon">  <!-- This is a subelement property to be consumed -->
                                        reader.next();
                                    </xsl:if>
                                </xsl:when>
                                <!-- start of the simple types handling -->
                                <xsl:otherwise>
                                    java.lang.String content = getElementTextProperly(reader);
                                    object.set<xsl:value-of select="$javaName"/>(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(content));
                                    <xsl:if test="$isType or $anon">  <!-- This is a subelement property to be consumed -->
                                        reader.next();
                                    </xsl:if>
                                </xsl:otherwise>
                            </xsl:choose>

                              }  // End of if for expected property start element

                            <xsl:if test="$ordered and $min!=0">
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            </xsl:if>
                        </xsl:for-each>

                        <xsl:if test="$ordered">  <!-- pick up trailing cruft after final property before outer endElement and verify no trailing properties -->
                            while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        </xsl:if>

                        <xsl:if test="property[not(@attribute)]">  <!-- this if is needed to skip all this when there are no propoerties-->
                        <xsl:if test="$unordered">
                             else{
                                        // A start element we are not expecting indicates an invalid parameter was passed
                                        throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                             }
                             } else reader.next();  <!-- At neither a start nor an end element, skip it -->
                            }  // end of while loop
                            </xsl:if>
                          </xsl:if>


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        <!-- end of template for choice/all -->

        }
           <!-- end of main template -->
          </xsl:template>

<!-- $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$-->
<!-- $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$-->
<!-- $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$-->
<!-- $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$-->
<!-- $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$-->
<!-- $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$-->


    <!-- this is the common template -->
    <xsl:template match="bean[@helperMode]">

        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="choice" select="@choice"/>
        <xsl:variable name="ordered" select="@ordered"/>
        <xsl:variable name="unordered" select="not($ordered)"/>  <!-- for convenience -->
        <xsl:variable name="isType" select="@type"/>
        <xsl:variable name="anon" select="@anon"/>

        <xsl:variable name="nsuri" select="@nsuri"/>
        <xsl:variable name="originalName" select="@originalName"/>
        <xsl:variable name="nsprefix" select="@nsprefix"/>
        <xsl:variable name="extension" select="@extension"/>
         <xsl:variable name="mapperClass" select="@mapperClass"/>
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


	<xsl:choose>
	<xsl:when test="not(@helper)">

	public <xsl:if test="not(@unwrapped) or (@skip-write)">static</xsl:if> class <xsl:value-of select="$name"/> <xsl:if test="$extension"> extends <xsl:value-of select="$extension"/></xsl:if>
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
                       <!-- the updating of setting tracker for null values should
                            happen if the attribute is marked as nillable. Else
                            the user can set a null value and it is never marked
                            as set
                       -->
                       <xsl:choose>
                          <xsl:when test="not(@nillable) and not(@primitive)">
                              if (param !=null){
                              //update the setting tracker
                              <xsl:value-of select="$settingTracker"/> = true;
                               }
                           </xsl:when>
                           <xsl:otherwise>
                               //update the setting tracker
                              <xsl:value-of select="$settingTracker"/> = true;
                           </xsl:otherwise>
                       </xsl:choose>
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

                    <xsl:if test="$choice">
                           clearAllSettingTrackers();
                    </xsl:if>
                    <xsl:if test="$min=0 or $choice">
                         //update the setting tracker
                         <xsl:value-of select="$settingTracker"/> = true;
                    </xsl:if>

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

     <!-- ######################################################################################### -->
     <!-- get OMElement methods that allows direct writing -->
     /**
     *
     * @param parentQName
     * @param factory
     * @return
     */
    public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){
                           public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            <xsl:choose>
            <xsl:when test="@type or @anon">
                <!-- For a type write the passed in QName first-->
                xmlWriter.writeStartElement(parentQName.getNamespaceURI(),
                                            parentQName.getLocalPart());

               <!-- write the type attribute if needed -->
               <xsl:if test="$extension">
               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                       registerPrefix(xmlWriter,"<xsl:value-of select="$nsuri"/>")+":<xsl:value-of select="$originalName"/>",
                       xmlWriter);
               </xsl:if>
                <!--First serialize the attributes!-->
                <xsl:for-each select="property[@attribute]">
                    <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
                     <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                    <xsl:choose>
                        <!-- Note - It is assumed that any attributes are OMAttributes-->
                        <xsl:when test="@any and not(@array)">
                            writeAttribute(<xsl:value-of select="$varName"/>.getNamespace().getName(),
                                                     <xsl:value-of select="$varName"/>.getLocalName(),
                                                     <xsl:value-of select="$varName"/>.getAttributeValue(),xmlWriter);
                        </xsl:when>
                         <xsl:when test="@any and @array">
                             for (int i=0;i &lt;<xsl:value-of select="$varName"/>.length;i++){
                              writeAttribute(<xsl:value-of select="$varName"/>[i].getNamespace().getName(),
                                                     <xsl:value-of select="$varName"/>[i].getLocalName(),
                                                     <xsl:value-of select="$varName"/>[i].getAttributeValue(),xmlWriter);
                             }
                         </xsl:when>
                        <!-- there can never be attribute arrays in the normal case-->
                        <xsl:otherwise>
                             writeAttribute("<xsl:value-of select="$namespace"/>",
                                                     "<xsl:value-of select="$propertyName"/>",
                                                      org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>),xmlWriter);
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>

                <!-- Now serialize the elements-->
                <xsl:for-each select="property[not(@attribute)]">
                    <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
                    <xsl:variable name="min"><xsl:value-of select="@minOccurs"/></xsl:variable>
                    <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                    <xsl:variable name="settingTracker">local<xsl:value-of select="@javaname"/>Tracker</xsl:variable>


                    <xsl:if test="$min=0 or $choice"> if (<xsl:value-of select="$settingTracker"/>){</xsl:if>
                    <xsl:choose>
                        <xsl:when test="@ours and not(@array) and not(@default)">
                            <xsl:choose>
                                <xsl:when test="@nillable">
                                    if (<xsl:value-of select="$varName"/>==null){
                                       xmlWriter.writeStartElement("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>");
                                      // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","true",xmlWriter);
                                       xmlWriter.writeEndElement();
                                    }else{
                                     <xsl:value-of select="$varName"/>.getOMElement(
                                       new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"),
                                        factory).serialize(xmlWriter);
                                    }
                                </xsl:when>
                                <xsl:otherwise>
                                    if (<xsl:value-of select="$varName"/>==null){
                                         throw new RuntimeException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                    }
                                   <xsl:value-of select="$varName"/>.getOMElement(
                                       new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"),
                                       factory).serialize(xmlWriter);
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:when test="@ours and @array and not(@default)">
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
                             <xsl:value-of select="$varName"/>[i].getOMElement(
                                       new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"),
                                       factory).serialize(xmlWriter);

                            }
                            <!--we've opened a bracket for the nulls - fix it here-->
                            <xsl:if test="@nillable">}</xsl:if>
                        </xsl:when>

                         <xsl:when test="@default and @array">
                            <!-- Note - Assumed to be OMElement-->
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
                                  <xsl:value-of select="$varName"/>[i].serialize(xmlWriter);
                              }

                            <!--we've opened a bracket for the nulls - fix it here-->
                            <xsl:if test="@nillable">}</xsl:if>

                        </xsl:when>

                        <xsl:when test="@default and not(@array)">
                            <!-- Note - Assumed to be OMElement-->
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
                             <xsl:value-of select="$varName"/>.serialize(xmlWriter);
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
                              xmlWriter.writeStartElement("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>");
                              if (<xsl:value-of select="$varName"/>[i]==null){
                                  // write the nil attribute
                                  writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","true",xmlWriter);
                              }else{
                                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>[i]));
                              }
                              xmlWriter.writeEndElement();
                            }
                            <!--we've opened a bracket for the nulls - fix it here-->
                            <xsl:if test="@nillable">}</xsl:if>
                        </xsl:when>

                         <!-- handle non ADB arrays  - Any case  - any may not be
                         nillable -->
                        <xsl:when test="@array and @any">
                            <!-- Note - Assumed to be OMElement-->
                            for (int i = 0;i &lt; <xsl:value-of select="$varName"/>.length;i++){
                              <xsl:value-of select="$varName"/>[i].serialize(xmlWriter);
                            }
                        </xsl:when>
                        <!-- handle any - non array case-->
                         <xsl:when test="@any">
                            <!-- Note - Assumed to be OMElement-->
                            <xsl:value-of select="$varName"/>.serialize(xmlWriter);
                        </xsl:when>

                        <!-- handle all other cases including the binary case -->
                         <xsl:otherwise>
                            xmlWriter.writeStartElement("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>");
                            xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                            xmlWriter.writeEndElement();
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:if test="$min=0 or $choice">}</xsl:if>

                </xsl:for-each>
                   <!-- write the end element for the type-->
               xmlWriter.writeEndElement();
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
                                          xmlWriter.writeStartElement("<xsl:value-of select="property/@namespace"/>","<xsl:value-of select="property/@name"/>");

                                         // write the nil attribute
                                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","true",xmlWriter);

                                        xmlWriter.writeEndElement();
                                       }else{
                                         <xsl:value-of select="$varName"/>.getOMElement(
                                         MY_QNAME,
                                         factory).serialize(xmlWriter);
                                       }
                            </xsl:when>
                            <xsl:otherwise>
                                 if (<xsl:value-of select="$varName"/>==null){
                                   throw new RuntimeException("Property cannot be null!");
                                 }
                                 <xsl:value-of select="$varName"/>.getOMElement(
                                         MY_QNAME,
                                         factory).serialize(xmlWriter);
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <!-- end of ours block-->
                    <xsl:otherwise>
                       xmlWriter.writeStartElement(
                       "<xsl:value-of select="property/@namespace"/>","<xsl:value-of select="property/@name"/>");
                       xmlWriter.writeCharacters(
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                       xmlWriter.writeEndElement();
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

          /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{

           registerPrefix(xmlWriter, namespace);

            xmlWriter.writeAttribute(namespace,attName,attValue);
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        <xsl:choose>
            <xsl:when test="@type">
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);
            </xsl:when>
            <xsl:otherwise>
               //ignore the QName passed in - we send only OUR QName!
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            </xsl:otherwise>
       </xsl:choose>
    }
    
    
    
    public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){
     	return <xsl:value-of select="@package"/>.<xsl:value-of select="@name"/>Helper.getPullParser(this, qName);
     }
    
    
    }
      	
	</xsl:when>
	<xsl:otherwise>
	
public <xsl:if test="not(@unwrapped) or (@skip-write)">static</xsl:if> class <xsl:value-of select="$name"/>Helper <xsl:if test="@extension"> extends <xsl:value-of select="@extension"/></xsl:if> {

	private static java.lang.String getElementTextProperly(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
            java.lang.String value = reader.getElementText();
            while (!reader.isEndElement())
                reader.next();
            return value;
        }

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static <xsl:value-of select="$name"/> parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            <xsl:value-of select="$name"/> object = new <xsl:value-of select="$name"/>();
            int event;
            try {
                <!-- Advance to our start element, or if we are a complex type, to our first property start element or the outer end element if no properties -->
                while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                    reader.next();

                <xsl:if test="@nillable">
                   if ("true".equals(reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil"))){
                         // Skip the element and report the null value.  It cannot have subelements.
                         while (!reader.isEndElement())
                             reader.next();
                         return null;
                   }
                </xsl:if>

                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);

                     if (!"<xsl:value-of select="$originalName"/>".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (<xsl:value-of select="$name"/>)<xsl:value-of select="$mapperClass"/>.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
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

                boolean isReaderMTOMAware = false;
                <!-- workaround for the issues in the wstx reader!-->
                try{
                  isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
                }catch(java.lang.IllegalArgumentException e){
                  isReaderMTOMAware = false;
                }


                <xsl:if test="$isType or $anon">
                    <!-- Skip the outer start element in order to process the subelements. -->
                    reader.next();
                </xsl:if>
                      <!-- If we are not a type and not an element with anonymous type, then we are an element with one property for our named type. -->
                      <!-- Our single named-type property applies to our entire outer element, so don't skip it. -->
                <!-- First loop creates arrayLists for handling arrays -->
                <xsl:for-each select="property[not(@attribute)]">
                    <xsl:if test="@array">
                        java.util.ArrayList list<xsl:value-of select="position()"/> = new java.util.ArrayList();
                    </xsl:if>
                </xsl:for-each>

                <xsl:if test="property[not(@attribute)]">
                <xsl:if test="$unordered">   <!-- Properties can be in any order -->
                while(!reader.isEndElement()) {
                    if (reader.isStartElement()){
                </xsl:if>
                </xsl:if>

                        <!-- Now reloop and populate the code -->
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

                            <xsl:variable name="propQName">new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>")</xsl:variable>

                           <xsl:choose>
                                <xsl:when test="$unordered">  <!-- One property per iteration if unordered -->
                                    <xsl:if test="position()>1">
                                        else
                                    </xsl:if>
                                </xsl:when>
                                <xsl:otherwise>
                                    <!-- If sequence, advance to start of next property or to end of outer element -->
                                    while (!reader.isStartElement() &amp;&amp; !reader.isEndElement()) reader.next();
                                </xsl:otherwise>
                            </xsl:choose>
                            if (reader.isStartElement() &amp;&amp; <xsl:value-of select="$propQName"/>.equals(reader.getName())){
                            <xsl:choose>
                                <xsl:when test="@array">
                                    <!-- We must be a named type or element with anonymous type. -->
                                    <!-- Elements with a named type have a single simple (non-array) property for their type -->
                                    // Process the array and step past its final element's end.
                                    <xsl:variable name="basePropertyType"><xsl:value-of select="@arrayBaseType"/></xsl:variable>
                                    <xsl:choose>
                                        <xsl:when test="@ours">
                                            <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>Helper.parse(reader));
                                            //loop until we find a start element that is not part of this array
                                            boolean <xsl:value-of select="$loopBoolName"/> = false;
                                            while(!<xsl:value-of select="$loopBoolName"/>){
                                                // We should be at the end element, but make sure
                                                while (!reader.isEndElement())
                                                    reader.next();
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    <xsl:value-of select="$loopBoolName"/> = true;
                                                } else {
                                                    if (<xsl:value-of select="$propQName"/>.equals(reader.getName())){
                                                        <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>Helper.parse(reader));
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
                                                // Step to next element event.
                                                while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    <xsl:value-of select="$loopBoolName"/> = true;
                                                } else {
                                                    if (<xsl:value-of select="$propQName"/>.equals(reader.getName())){
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
                                    object.set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/>Helper.parse(reader));
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
                                <!-- start of the simple types handling for binary content-->
                                <xsl:when test="@binary">
                                    if (isReaderMTOMAware
                                      &amp;&amp;
                                    java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_BINARY))){
                                         //MTOM aware reader - get the datahandler directly and put it in the object
                                         object.set<xsl:value-of select="$javaName"/>(
                                         (javax.activation.DataHandler)reader.getProperty(org.apache.axiom.om.OMConstants.DATA_HANDLER));
                                     }else{
                                        //Do the usual conversion
                                        java.lang.String content = getElementTextProperly(reader);
                                        object.set<xsl:value-of select="$javaName"/>(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(content));

                                     }

                                    <xsl:if test="$isType or $anon">  <!-- This is a subelement property to be consumed -->
                                        reader.next();
                                    </xsl:if>
                                </xsl:when>
                                <!-- start of the simple types handling -->
                                <xsl:otherwise>
                                    java.lang.String content = getElementTextProperly(reader);
                                    object.set<xsl:value-of select="$javaName"/>(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(content));
                                    <xsl:if test="$isType or $anon">  <!-- This is a subelement property to be consumed -->
                                        reader.next();
                                    </xsl:if>
                                </xsl:otherwise>
                            </xsl:choose>

                              }  // End of if for expected property start element

                            <xsl:if test="$ordered and $min!=0">
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            </xsl:if>
                        </xsl:for-each>
                        
                        <xsl:if test="$ordered">  <!-- pick up trailing cruft after final property before outer endElement and verify no trailing properties -->
                            while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        </xsl:if>

                        <xsl:if test="property[not(@attribute)]">  <!-- this if is needed to skip all this when there are no propoerties-->
                        <xsl:if test="$unordered">
                             else{
                                        // A start element we are not expecting indicates an invalid parameter was passed
                                        throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                             }
                             } else reader.next();  <!-- At neither a start nor an end element, skip it -->
                            }  // end of while loop
                            </xsl:if>
                          </xsl:if>


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }
	
	        public static javax.xml.stream.XMLStreamReader getPullParser(<xsl:value-of select="@package"/>.<xsl:value-of select="@name"/> bean, javax.xml.namespace.QName qName){


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
                                    elementList.add(bean.<xsl:value-of select="$varName"/>==null?null:
                                    bean.<xsl:value-of select="$varName"/>);
                                </xsl:when>
                                <xsl:otherwise>
                                    if (bean.<xsl:value-of select="$varName"/>==null){
                                         throw new RuntimeException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                    }
                                    elementList.add(bean.<xsl:value-of select="$varName"/>);
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:when test="(@ours or @default) and @array">
                             <xsl:choose>
                                <xsl:when test="@nillable">
                                    // this property is nillable
                                    if (bean.<xsl:value-of select="$varName"/>!=null){
                                    <!--this barcket needs to be closed!-->
                                </xsl:when>
                                <xsl:otherwise>
                                    if (bean.<xsl:value-of select="$varName"/>==null){
                                         throw new RuntimeException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                    }
                                </xsl:otherwise>
                            </xsl:choose>
                            for (int i = 0;i &lt; bean.<xsl:value-of select="$varName"/>.length;i++){
                              elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                               elementList.add(bean.<xsl:value-of select="$varName"/>[i]);
                            }
                            <!--we've opened a bracket for the nulls - fix it here-->
                            <xsl:if test="@nillable">}</xsl:if>
                        </xsl:when>
                        <!-- handle non ADB arrays - Not any however -->
                        <xsl:when test="@array and not(@any)">
                             <xsl:choose>
                                <xsl:when test="@nillable">
                                    // this property is nillable
                                    if (bean.<xsl:value-of select="$varName"/>!=null){
                                    <!--this bracket needs to be closed!-->
                                </xsl:when>
                                <xsl:otherwise>
                                    if (bean.<xsl:value-of select="$varName"/>==null){
                                         throw new RuntimeException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                    }
                                </xsl:otherwise>
                            </xsl:choose>
                            for (int i = 0;i &lt; bean.<xsl:value-of select="$varName"/>.length;i++){
                              elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                              elementList.add(
                              org.apache.axis2.databinding.utils.ConverterUtil.convertToString(bean.<xsl:value-of select="$varName"/>[i]));
                            }
                            <!--we've opened a bracket for the nulls - fix it here-->
                            <xsl:if test="@nillable">}</xsl:if>
                        </xsl:when>

                         <!-- handle non ADB arrays  - Any case  - any may not be
                         nillable -->
                        <xsl:when test="@array and @any">
                            for (int i = 0;i &lt; bean.<xsl:value-of select="$varName"/>.length;i++){
                              elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                              elementList.add(
                              org.apache.axis2.databinding.utils.ConverterUtil.convertToString(bean.<xsl:value-of select="$varName"/>[i]));
                            }
                            <!--we've opened a bracket for the nulls - fix it here-->
                        </xsl:when>
                        <!-- handle any - non array case-->
                         <xsl:when test="@any">
                            elementList.add(org.apache.axis2.databinding.utils.Constants.OM_ELEMENT_KEY);
                            elementList.add(bean.<xsl:value-of select="$varName"/>);
                        </xsl:when>
                        <!-- handle binary - Since it is a Datahandler, we can just add it to the list
                          and the ADB pullparser would handle it right-->
                         <xsl:when test="@binary">
                            elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                            elementList.add(bean.<xsl:value-of select="$varName"/>);
                        </xsl:when>
                        <!-- the usual case!!!!-->
                        <xsl:otherwise>
                             elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                            <xsl:choose>
                                <xsl:when test="@nillable and not(@primitive)">
                                    elementList.add(bean.<xsl:value-of select="$varName"/>==null?null:
                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToString(bean.<xsl:value-of select="$varName"/>));
                                </xsl:when>
                                <xsl:otherwise>
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(bean.<xsl:value-of select="$varName"/>));
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
                            attribList.add(bean.<xsl:value-of select="$varName"/>);
                        </xsl:when>
                         <xsl:when test="@any and @array">
                             for (int i=0;i &lt;bean.<xsl:value-of select="$varName"/>.length;i++){
                               attribList.add(org.apache.axis2.databinding.utils.Constants.OM_ATTRIBUTE_KEY);
                               attribList.add(bean.<xsl:value-of select="$varName"/>[i]);
                             }
                         </xsl:when>
                        <xsl:otherwise>
                            attribList.add(
                            new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"));
                            attribList.add(
                            org.apache.axis2.databinding.utils.ConverterUtil.convertToString(bean.<xsl:value-of select="$varName"/>));
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
                                if (bean.<xsl:value-of select="$varName"/>==null){
                                   return new org.apache.axis2.databinding.utils.reader.NullXMLStreamReader(bean.MY_QNAME);
                                }else{
                                   return <xsl:value-of select="property/@type"/>Helper.getPullParser(bean.<xsl:value-of select="$varName"/>, bean.MY_QNAME);
                                }
                            </xsl:when>
                            <xsl:otherwise>return <xsl:value-of select="property/@type"/>Helper.getPullParser(bean.<xsl:value-of select="$varName"/>, bean.MY_QNAME);</xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$nillable and not($primitive)">
                                if (bean.<xsl:value-of select="$varName"/>==null){
                                      return new org.apache.axis2.databinding.utils.reader.NullXMLStreamReader(bean.MY_QNAME);
                                }else{
                                   return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(bean.MY_QNAME,
                                       new Object[]{
                                      org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT,
                                       org.apache.axis2.databinding.utils.ConverterUtil.convertToString(bean.<xsl:value-of select="$varName"/>)
                                       },
                                       null);
                                }
                            </xsl:when>
                            <xsl:otherwise> return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(bean.MY_QNAME,
                            new Object[]{
                            org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT,
                            org.apache.axis2.databinding.utils.ConverterUtil.convertToString(bean.<xsl:value-of select="$varName"/>)
                            },
                            null);</xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>

        }
	
}	
	</xsl:otherwise>
	</xsl:choose>
           <!-- end of main template -->
  </xsl:template>

  <xsl:template match="mapper">
        <xsl:variable name="name" select="@name"/>
        <xsl:variable name="helperMode" select="@helpermode"/>

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
        public <xsl:if test="not(@unwrapped) or (@skip-write)">static</xsl:if> class <xsl:value-of select="$name"/>{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              <xsl:for-each select="type">
                <xsl:if test="position() &gt; 1">else </xsl:if> if (
                  "<xsl:value-of select="@nsuri"/>".equals(namespaceURI) &amp;&amp;
                  "<xsl:value-of select="@shortname"/>".equals(typeName)){
                   <xsl:choose>
                       <xsl:when test="$helperMode">
                           return  <xsl:value-of select="@classname"/>Helper.parse(reader); 
                       </xsl:when>
                        <xsl:otherwise>
                            return  <xsl:value-of select="@classname"/>.Factory.parse(reader);
                        </xsl:otherwise>
                   </xsl:choose>

                  }

              </xsl:for-each>
             return null;
          }

        }
    </xsl:template>
</xsl:stylesheet>
