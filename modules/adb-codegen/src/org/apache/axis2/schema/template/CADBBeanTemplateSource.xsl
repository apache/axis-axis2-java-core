<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

     <!-- cater for the multiple classes - wrappped mode - currently not well supported.-->
    <xsl:template match="/beans">
        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="axis2_name">adb_<xsl:value-of select="@name"/></xsl:variable>
       /**
        * <xsl:value-of select="$axis2_name"/>.c
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2/Java version: #axisVersion# #today#
        */

       /**
        *  <xsl:value-of select="$axis2_name"/>
        */
        #include "<xsl:value-of select="$axis2_name"/>.h"

        <xsl:apply-templates/>

    </xsl:template>
    <!--cater for the multiple classes - unwrappped mode -->
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="class">
        <xsl:variable name="name">_<xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="axis2_name">adb_<xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="istype"><xsl:value-of select="@type"/></xsl:variable>

        <xsl:variable name="originalName"><xsl:value-of select="@originalName"/></xsl:variable>
        <xsl:variable name="nsuri"><xsl:value-of select="@nsuri"/></xsl:variable>
        <xsl:variable name="nsprefix"><xsl:value-of select="@nsprefix"/></xsl:variable>
        <xsl:variable name="anon"><xsl:value-of select="@anon"/></xsl:variable>
        <xsl:variable name="ordered"><xsl:value-of select="@anon"/></xsl:variable>
       /**
        * <xsl:value-of select="$axis2_name"/>.h
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2/C version: #axisVersion# #today#
        *
        */

        #include "<xsl:value-of select="$axis2_name"/>.h"
          <xsl:choose>
             <xsl:when test="@type">
              /**
               * This type was generated from the piece of schema that had
               * name = <xsl:value-of select="$originalName"/>
               * Namespace URI = <xsl:value-of select="$nsuri"/>
               * Namespace Prefix = <xsl:value-of select="$nsprefix"/>
               */
             </xsl:when>
             <xsl:otherwise>
              /**
               * implmentation of the <xsl:value-of select="$originalName"/><xsl:if test="$nsuri">|<xsl:value-of select="$nsuri"/></xsl:if> element
               */
             </xsl:otherwise>
          </xsl:choose>


        struct <xsl:value-of select="$axis2_name"/>
        {
            <xsl:if test="not(@type)">
                axutil_qname_t* qname;
            </xsl:if>

            <xsl:for-each select="property">
                <xsl:variable name="propertyType">
                   <xsl:choose>
                     <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                     <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                     <xsl:when test="@ours">
                            adb_<xsl:value-of select="@type"/>_t*
                    </xsl:when>
                     <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                   </xsl:choose>
                </xsl:variable>
                <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
                <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

                <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text>attrib_<xsl:value-of select="$CName"/>;
            </xsl:for-each>
        };


       /************************* Function Implmentations ********************************/
        AXIS2_EXTERN <xsl:value-of select="$axis2_name"/>_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_create(
            const axutil_env_t *env )
        {
            <xsl:value-of select="$axis2_name"/>_t *<xsl:value-of select="$name"/> = NULL;
            <xsl:if test="not(@type)">
              axutil_qname_t* qname = NULL;
            </xsl:if>
            AXIS2_ENV_CHECK(env, NULL);

            <xsl:value-of select="$name"/> = (<xsl:value-of select="$axis2_name"/>_t *) AXIS2_MALLOC(env->
                allocator, sizeof(<xsl:value-of select="$axis2_name"/>_t));

            if(NULL == <xsl:value-of select="$name"/>)
            {
                AXIS2_ERROR_SET(env->error, AXIS2_ERROR_NO_MEMORY, AXIS2_FAILURE);
                return NULL;
            }

            memset(<xsl:value-of select="$name"/>, 0, sizeof(<xsl:value-of select="$axis2_name"/>_t));

            <xsl:for-each select="property">
                <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
                <xsl:choose>
                  <xsl:when test="@ours">
                    <xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/>  = NULL;
                  </xsl:when>
                  <!-- todo for others -->
                </xsl:choose>
            </xsl:for-each>

            <xsl:if test="not(@type)">
              <xsl:choose>
                <xsl:when test="$nsuri and $nsuri != ''">
                  qname =  axutil_qname_create (env,
                        "<xsl:value-of select="$originalName"/>",
                        "<xsl:value-of select="$nsuri"/>",
                        "<xsl:value-of select="$nsprefix"/>");
                </xsl:when>
                <xsl:otherwise>
                  qname =  axutil_qname_create (env,
                        "<xsl:value-of select="$originalName"/>",
                        NULL,
                        NULL);
                </xsl:otherwise>
              </xsl:choose>

              <xsl:value-of select="$name"/>->qname = qname;
            </xsl:if>

            return <xsl:value-of select="$name"/>;
        }

        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_free (
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env)
        {
            <!--<xsl:if test="property/@isarray and (property/@ours or property/@type='axis2_char_t*' or property/@type='axis2_date_time_t*' or property/@type='axis2_base64_binary_t*' or property/@type='axiom_node_t*')">-->
            <xsl:if test="property/@isarray">
                  int i = 0;
                  int count = 0;
                  void *element = NULL;
            </xsl:if>

            AXIS2_ENV_CHECK(env, AXIS2_FAILURE);

            <xsl:for-each select="property">
             <!-- please check all the freeing thing below -->
             <!--<xsl:if test="@isarray or not(@type='short' or @type='int' or @type='char' or @type='long' or @type='double' or @type='float' or @type='axis2_bool_t')">-->
             <xsl:if test="@isarray or @ours or @type='axis2_char_t*' or @type='axutil_qname_t*' or @type='axutil_uri_t*' or @type='axutil_date_time_t*' or @type='axutil_base64_binary_t*'">
              <xsl:variable name="propertyType">
                 <xsl:choose>
                   <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">
                    adb_<xsl:value-of select="@type"/>_t*
                    </xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">
                    adb_<xsl:value-of select="@type"/>_t*
                   </xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
              <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

              <xsl:variable name="attriName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="justAttriName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>



              <!-- handles arrays -->
              <xsl:if test="@isarray">
                if ( <xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/> != NULL)
                {
                    count = axutil_array_list_size( <xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/>, env);
                    for( i = 0; i &lt; count; i ++)
                    {
                       element = axutil_array_list_get( <xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/>, env, i);
              </xsl:if>

              <!-- the following element can be inside array or independent one -->
                 <!--xsl:if test="$nativePropertyType!='axis2_char_t*'"-->
              if( <xsl:value-of select="$justAttriName"/> != NULL)
              {
                 <!-- how to free all the ours things -->
                 <xsl:choose>
                   <xsl:when test="@ours">
                      adb_<xsl:value-of select="@type"/>_free( <xsl:value-of select="$attriName"/>, env);
                   </xsl:when>

                   <xsl:when test="$nativePropertyType='short' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='long'">
                     <xsl:if test="@isarray">
                      <!-- free ints, longs, float in array-->
                      /** we keep primtives as pointers in arrasy, so need to free them */
                      AXIS2_FREE( env-> allocator, element);
                     </xsl:if>
                   </xsl:when>

                   <!-- free axis2_char_t s -->
                   <xsl:when test="$nativePropertyType='axis2_char_t*' and not(@isarray)">
                      AXIS2_FREE( env-> allocator, <xsl:value-of select="$attriName"/>);
                   </xsl:when>

                   <!-- free nodes -->
                   <xsl:when test="$nativePropertyType='axiom_node_t*'">
                    axiom_node_free_tree ( <xsl:value-of select="$attriName"/>, env );
                   </xsl:when>

                   <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                    axutil_qname_free( <xsl:value-of select="$attriName"/>, env );
                   </xsl:when>

                   <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                    axutil_uri_free( <xsl:value-of select="$attriName"/>, env );
                   </xsl:when>

                   <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                    axutil_date_time_free( <xsl:value-of select="$attriName"/>, env );
                   </xsl:when>

                   <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                    axutil_base64_binary_free ( <xsl:value-of select="$attriName"/>, env );
                   </xsl:when>
                     <xsl:when test="$propertyType='axutil_duration_t*'">
                    axutil_duration_free ( <xsl:value-of select="$attriName"/>, env );
                   </xsl:when>

                   <!--TODO: This should be extended for all the types that should be freed.. -->
                   <xsl:otherwise>
                     /** This is an unknown type or a primitive. Please free this manually*/
                   </xsl:otherwise>
                 </xsl:choose>
                 <xsl:value-of select="$justAttriName"/> = NULL;
              }

              <!--/xsl:if-->
              <!-- close tags arrays -->
              <xsl:if test="@isarray">
                    }
                    axutil_array_list_free( <xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/>, env);
                }
              </xsl:if>
             </xsl:if> <!--close for test of primitive types -->
            </xsl:for-each>

            <xsl:if test="not(@type)">
              if(<xsl:value-of select="$name"/>->qname )
              {
                  axutil_qname_free (<xsl:value-of select="$name"/>->qname, env);
                  <xsl:value-of select="$name"/>->qname = NULL;
              }
            </xsl:if>

            if(<xsl:value-of select="$name"/>)
            {
                AXIS2_FREE( env->allocator, <xsl:value-of select="$name"/>);
                <xsl:value-of select="$name"/> = NULL;
            }
            return AXIS2_SUCCESS;
        }

        <xsl:if test="not(@type)">  <!-- this would only generate for elements -->
           axutil_qname_t* AXIS2_CALL
           <xsl:value-of select="$axis2_name"/>_get_qname (
                   <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                   const axutil_env_t *env)
           {
               AXIS2_ENV_CHECK(env, AXIS2_FAILURE);

               return <xsl:value-of select="$name"/>-> qname;
           }
        </xsl:if>

        <xsl:if test="@simple">
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_deserialize_from_string(
                            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                                            const axutil_env_t *env,
                                            axis2_char_t *node_value)
            {
              axis2_status_t status = AXIS2_SUCCESS;
            <xsl:if test="property/@type='axutil_date_time_t*' or property/@type='axutil_base64_binary_t*'">
               void *element = NULL;
            </xsl:if>
              <xsl:for-each select="property"> <!-- only one property would be in a simpletype -->
                <xsl:variable name="propertyType">
                   <xsl:choose>
                     <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                     <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                     <xsl:when test="@ours">
                     adb_<xsl:value-of select="@type"/>_t*
                     </xsl:when>
                     <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                   </xsl:choose>
                </xsl:variable>
                <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                   <xsl:choose>
                     <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                     <xsl:when test="@ours">
                      adb_<xsl:value-of select="@type"/>_t*
                     </xsl:when>
                     <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                   </xsl:choose>
                </xsl:variable>
                <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
                <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
  
                <!-- here only simple type possible -->
                <xsl:choose>
                  <!-- add int s -->
                  <xsl:when test="$nativePropertyType='int'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                    env, atoi(node_value));
                  </xsl:when>

                  <!-- add axis2_char_t s -->
                  <xsl:when test="$nativePropertyType='char'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                    env, (char)(*node_value)); <!-- This should be checked -->
                  </xsl:when>

                  <!-- add short s -->
                  <xsl:when test="$nativePropertyType='short'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                    env, atoi(node_value));
                  </xsl:when>

                  <!-- add long s -->
                  <xsl:when test="$nativePropertyType='long'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                    env, atol(node_value));
                  </xsl:when>

                  <!-- add float s -->
                  <xsl:when test="$nativePropertyType='float'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                    env, atof(node_value));
                  </xsl:when>
                  <!-- add double s -->
                  <xsl:when test="$nativePropertyType='double'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                    env, atof(node_value));
                  </xsl:when>

                  <!-- add axis2_char_t s -->
                  <xsl:when test="$nativePropertyType='axis2_char_t*'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                    env, node_value);
                  </xsl:when>

                  <!-- add axis2_qname_t s -->
                  <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                    env, axutil_qname_create_from_string( env, node_value));
                  </xsl:when>

                  <!-- add axis2_uri_t s -->
                  <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                    env, axutil_uri_parse_string(env, node_value));
                  </xsl:when>
                  <!-- add axis2_bool_t s -->
                  <xsl:when test="$nativePropertyType='axis2_bool_t'">
                     if ( !axutil_strcmp(node_value, "TRUE") || !axutil_strcmp(node_value, "true") )
                     {
                         <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                    env, AXIS2_TRUE);
                     }
                     else
                     {
                         <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                    env, AXIS2_FALSE);
                     }
                  </xsl:when>
                  <!-- add date_time_t* s -->
                  <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                     element = (void*)axutil_date_time_create( env);
                     axutil_date_time_deserialize_date_time( (axutil_date_time_t*)element, env,
                                                                node_value);
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                    env, (<xsl:value-of select="$nativePropertyType"/>)element);
                  </xsl:when>
                  <!-- add hex_binary_t* s -->
                  <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                     element = (void*)axutil_base64_binary_create( env);
                     axutil_base64_binary_set_encoded_binary( ( <xsl:value-of select="$nativePropertyType"/>)element, env,
                                                                node_value);
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                    env, ( <xsl:value-of select="$nativePropertyType"/>)element);
                  </xsl:when>
                  <xsl:when test="@ours">
                    <!-- It seems this is in an unreachable path -->
                  </xsl:when>
                  <xsl:otherwise>
                    <!--TODO: add new attributes types -->
                    /** can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
                    status = AXIS2_FAILURE;
                  </xsl:otherwise>
                </xsl:choose>   
              </xsl:for-each>
              return status;
            }
        </xsl:if>

        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_deserialize(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env,
                axiom_node_t* parent)
        {
          axis2_status_t status = AXIS2_SUCCESS;
          <xsl:if test="property/@attribute">
               axiom_attribute_t *parent_attri = NULL;
               axiom_element_t *parent_element = NULL;
               axis2_char_t *attrib_text = NULL;
          </xsl:if>
          <xsl:if test="property/@ours or (property/@isarray and (property/@type='short' or property/@type='int' or property/@type='char' or property/@type='long' or property/@type='float' or property/@type='double')) or property/@type='axutil_date_time_t*' or property/@type='axutil_base64_binary_t*'">
               void *element = NULL;
          </xsl:if>
          <!-- these two are requried -->

         <xsl:for-each select="property">
            <xsl:if test="position()=1"> <!-- check for at least one element exists -->
             axis2_char_t* text_value = NULL;
             axutil_qname_t *qname = NULL;
            </xsl:if>
          </xsl:for-each>
          <xsl:choose>
            <xsl:when test="@simple">
            axiom_element_t *text_element = NULL;
            axiom_node_t *text_node = NULL;
            
            status = AXIS2_FAILURE;
            if(parent)
            {
                text_node = axiom_node_get_first_child(parent, env);
                if (text_node &amp;&amp;
                        axiom_node_get_node_type(text_node, env) == AXIOM_TEXT)
                {
                    axiom_text_t *text_element = (axiom_text_t*)axiom_node_get_data_element(text_node, env);
                    if(text_element &amp;&amp; axiom_text_get_value(text_element, env))
                    {
                        text_value = (axis2_char_t*)axiom_text_get_value(text_element, env);
                        status = <xsl:value-of select="$axis2_name"/>_deserialize_from_string(<xsl:value-of select="$name"/>, env, text_value);
                    }
                }
            }
            </xsl:when>
            <xsl:otherwise>

            <xsl:for-each select="property">
              <xsl:if test="position()=1"> <!-- check for at least one element exists -->
                axiom_namespace_t *ns1 = NULL;
              </xsl:if>
            </xsl:for-each>
            <xsl:if test="property/@isarray">
               int i = 0;
               int element_found = 0;
               axutil_array_list_t *arr_list = NULL;
            </xsl:if>
            <xsl:if test="@ordered and property/@isarray">
               int sequence_broken = 0;
               axiom_node_t *tmp_node = NULL;
            </xsl:if>
            <!--<xsl:if test="property">
              <xsl:if test="not(property/@attribute) and  (not(property/@ours) or property/@ours='')">
                axis2_char_t* text_value = NULL;
              </xsl:if>
            </xsl:if>
            <xsl:if test="property and (not(@type) or @type='' or property/@attribute)">
               axis2_qname_t *qname = NULL;
            </xsl:if>-->
            <xsl:variable name="element_qname_var_requred">
                  <xsl:for-each select="property">
                    <xsl:if test="(not(@attribute) and @isarray) or not(../@ordered)">
                        yes
                    </xsl:if>
                  </xsl:for-each>
            </xsl:variable>
            <xsl:if test="contains($element_qname_var_requred, 'yes')">
                 <!-- TODO axutil_qname_t *element_qname = NULL; -->
            </xsl:if>
            axutil_qname_t *element_qname = NULL; 
            <xsl:for-each select="property">
             <xsl:if test="position()=1">
               axiom_node_t *first_node = NULL;
               <!--<xsl:if test="not(../@type) or ../@type='' or property/@attribute">
                 axis2_qname_t *qname = NULL;
               </xsl:if>
               axiom_node_t *current_node = NULL;
               axiom_element_t *current_element = NULL;-->
             </xsl:if>
            </xsl:for-each>
            <xsl:if test="property and (not(property/@attribute) or property/@attribute='' or property/@notattribute)">
               axiom_node_t *current_node = NULL;
               axiom_element_t *current_element = NULL;
            </xsl:if>
            AXIS2_ENV_CHECK(env, AXIS2_FAILURE);

            <xsl:if test="$nsuri and $nsuri != ''">
            <xsl:for-each select="property">
              <xsl:if test="position()=1"> <!-- check for at least one element exists -->
                ns1 = axiom_namespace_create (env,
                                         "<xsl:value-of select="$nsuri"/>",
                                         "<xsl:value-of select="$nsprefix"/>");
              </xsl:if>
            </xsl:for-each>
            </xsl:if>
            <xsl:if test="property">
              if ( NULL == parent )
              {
                /** This should be checked above */
                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL elemenet for <xsl:value-of select="$originalName"/>"
                                              " %d :: %s", env->error->error_number,
                                              AXIS2_ERROR_GET_MESSAGE(env->error));
                return AXIS2_FAILURE;
              }
            </xsl:if>
            <xsl:for-each select="property">
              <xsl:if test="position()=1"> <!-- check for at least one element exists -->
                 <xsl:choose>
                    <xsl:when test="not($istype)">
                    current_element = (axiom_element_t *)axiom_node_get_data_element( parent, env);
                    qname = axiom_element_get_qname( current_element, env, parent);
                    if ( axutil_qname_equals( qname, env, <xsl:value-of select="$name"/>-> qname ) )
                    {
                        <xsl:choose>
                          <xsl:when test="$anon">
                          first_node = axiom_node_get_first_child( parent, env);
                          </xsl:when>
                          <xsl:otherwise>
                          first_node = parent;
                          </xsl:otherwise>
                        </xsl:choose>
                    }
                    else
                    {
                        AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, 
                              "Failed in building adb object for <xsl:value-of select="$originalName"/> : "
                              "Wrong XML to deserialize");
                        <!-- TODO: ADB specific error should be defiend and set here -->
                        return AXIS2_FAILURE;
                    }
                    </xsl:when>
                    <xsl:otherwise>
                      <!-- for types, parent refers to the container element -->
                      first_node = axiom_node_get_first_child( parent, env);
                      if(first_node == NULL)
                      {
                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI,
                                            "Failed in building adb object for <xsl:value-of select="$originalName"/> : "
                                            "Wrong XML to deserialize");
                          <!-- TODO: ADB specific error should be defiend and set here -->
                          return AXIS2_FAILURE;
                      }
                    </xsl:otherwise>
                 </xsl:choose>
               </xsl:if>
            </xsl:for-each>
            
            </xsl:otherwise> <!--otherwise for @simple check -->
          </xsl:choose>

          <!-- attributes are common to simple types(when used in simple content) and other types -->
            <xsl:for-each select="property/@attribute">
             <xsl:if test="position()=1">
                 parent_element = (axiom_element_t *)axiom_node_get_data_element( parent, env);
             </xsl:if>
            </xsl:for-each>

            <xsl:for-each select="property">
              <xsl:variable name="propertyType">
                 <xsl:choose>
                   <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">
                    adb_<xsl:value-of select="@type"/>_t*
                   </xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">
                    adb_<xsl:value-of select="@type"/>_t*
                   </xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
              <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

              <xsl:variable name="attriName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="justAttriName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:choose>
                <xsl:when test="@attribute">
                  <xsl:choose>
                    <xsl:when test="@nsuri and @nsuri != ''">
                      qname = axutil_qname_create( env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>","<xsl:choose>
                                                                   <xsl:when test="@prefix!=''"><xsl:value-of select="@prefix"/></xsl:when>
                                                                   <xsl:when test="$nsuri and $nsuri != '' and @nsuri=$nsuri"><xsl:value-of select="../@nsprefix"/></xsl:when></xsl:choose>");
                    </xsl:when>
                    <xsl:otherwise>
                      qname = axutil_qname_create( env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                    </xsl:otherwise>
                  </xsl:choose>

                  parent_attri = axiom_element_get_attribute( parent_element, env, qname);
                  if( parent_attri != NULL)
                  {
                    attrib_text = axiom_attribute_get_value( parent_attri, env);
                  }
                  else
                  {
                    /** hope this work */
                    attrib_text = axiom_element_get_attribute_value_by_name( parent_element, env, "<xsl:value-of select="$propertyName"/>");
                  }
                  if( attrib_text != NULL)
                  {
                      <!-- here only simple type possible -->
                      <xsl:choose>
                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='int'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, atoi( attrib_text));
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='char'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, (char)(*attrib_text));
                        </xsl:when>

                        <!-- add short s -->
                        <xsl:when test="$nativePropertyType='short'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, atoi( attrib_text));
                        </xsl:when>

                        <!-- add long s -->
                        <xsl:when test="$nativePropertyType='long'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, atol( attrib_text));
                        </xsl:when>

                        <!-- add float s -->
                        <xsl:when test="$nativePropertyType='float'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, atof( attrib_text));
                        </xsl:when>
                        <!-- add double s -->
                        <xsl:when test="$nativePropertyType='double'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, atof( attrib_text));
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, attrib_text);
                        </xsl:when>

                        <!-- add axis2_qname_t s -->
                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, axutil_qname_create_from_string( env, attrib_text));
                        </xsl:when>

                        <!-- add axis2_uri_t s -->
                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, axutil_uri_parse_string( env, attrib_text));
                        </xsl:when>
                        <!-- add axis2_bool_t s -->
                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                           if ( !axutil_strcmp( attrib_text, "TRUE") || !axutil_strcmp( attrib_text, "true") )
                           {
                               <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, AXIS2_TRUE);
                           }
                           else
                           {
                               <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, AXIS2_FALSE);
                           }
                        </xsl:when>
                        <!-- add date_time_t* s -->
                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                           element = (void*)axutil_date_time_create( env);
                           axutil_date_time_deserialize_date_time( (axutil_date_time_t*)element, env,
                                                                      attrib_text);
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, ( <xsl:value-of select="$nativePropertyType"/>)element);
                        </xsl:when>
                        <!-- add hex_binary_t* s -->
                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                           element = (void*)axutil_base64_binary_create( env);
                           axutil_base64_binary_set_encoded_binary( ( <xsl:value-of select="$nativePropertyType"/>)element), env,
                                                                      attrib_text);
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, ( <xsl:value-of select="$nativePropertyType"/>)element);
                        </xsl:when>
                        <xsl:when test="@ours">
                            element =  (void*)adb_<xsl:value-of select="@type"/>_create(env);
                            adb_<xsl:value-of select="@type"/>_deserialize_from_string(element, env, attrib_text);
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>,
                                                          env, ( <xsl:value-of select="$nativePropertyType"/>)element);
                        </xsl:when>
                        <xsl:otherwise>
                          <!--TODO: add new attributes types -->
                          /** can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
                        </xsl:otherwise>
                      </xsl:choose>
                    }
                    <xsl:if test="not(@nillable)">
                    else
                    {
                        /** this is not a nillable attribute*/
                        AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "non nillable attribute <xsl:value-of select="$propertyName"/> missing"
                                              " %d :: %s", env->error->error_number,
                                              AXIS2_ERROR_GET_MESSAGE(env->error));
                        return AXIS2_FAILURE;
                    }
                    </xsl:if>
                </xsl:when>
                <xsl:when test="../@simple"></xsl:when> <!-- just to avoid preceeding code to be parsed in a simple type -->
                <xsl:otherwise> <!-- when it is an element not(@attribute) -->
                  <!-- handles arrays -->
                   <xsl:if test="@isarray">
                    /**
                     * building <xsl:value-of select="$CName"/> array
                     */
                       arr_list = axutil_array_list_create( env, 10);
                   </xsl:if>

                     <!-- for each non attribute properties there will always be an element-->
                     /**
                      * building <xsl:value-of select="$propertyName"/> element
                      */
                     <!-- array and non array build is so different so big choose, when is requried-->
                     <!-- the method of picking the element is depend on the ../@ordered -->
                     <xsl:choose>
                       <xsl:when test="not(@isarray)">  <!--not an array so continue normal -->
                           <xsl:choose>
                             <xsl:when test="../@ordered or not($anon or $istype)"> <!-- since non-anon has just only one sub element-->
                               <xsl:choose>
                                 <xsl:when test="position()=1">
                                   current_node = first_node;
                                 </xsl:when>
                                 <xsl:otherwise>
                                   /**
                                     * because elements are ordered this works fine
                                     */
                                   <!-- current node should contain the ordered value -->
                                   if( current_node != NULL)
                                   {
                                       current_node = axiom_node_get_next_sibling( current_node, env);
                                   }
                                 </xsl:otherwise>
                               </xsl:choose>
                             </xsl:when>
                             <xsl:otherwise>
                                   /**
                                     * because elements are not ordered we should surf all the sibling to pick the right one
                                     */
                               for ( current_node = first_node; current_node != NULL;
                                             current_node = axiom_node_get_next_sibling( current_node, env))
                               {
                                  current_element = (axiom_element_t *)axiom_node_get_data_element( current_node, env);
                                  qname = axiom_element_get_qname( current_element, env, current_node);
                                <xsl:choose>
                                  <xsl:when test="@nsuri and @nsuri != ''">
                                  element_qname = axutil_qname_create( env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>", "<xsl:choose>
                                                                   <xsl:when test="@prefix!=''"><xsl:value-of select="@prefix"/></xsl:when>
                                                                   <xsl:when test="@nsuri=../@nsuri"><xsl:value-of select="../@nsprefix"/></xsl:when></xsl:choose>");
                                  </xsl:when>
                                  <xsl:otherwise>
                                  element_qname = axutil_qname_create( env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                                  </xsl:otherwise>
                                </xsl:choose>
                                  if ( axutil_qname_equals( element_qname, env, qname))
                                  {
                                       /** found the requried element */
                                       break;
                                  }
                               }
                             </xsl:otherwise>
                           </xsl:choose>
                           
                           if (current_node <xsl:if test="(@minOccurs=0)"> &amp;&amp; axiom_node_get_data_element( current_node, env) &amp;&amp; !axutil_strcmp(&quot;<xsl:value-of select="$propertyName"/>&quot;, 
                           axiom_element_get_localname((axiom_element_t *)axiom_node_get_data_element( current_node, env), env))
                           </xsl:if>)
                           {
                              <xsl:if test="../@ordered or not($anon or $istype)">current_element = (axiom_element_t *)axiom_node_get_data_element( current_node, env);</xsl:if>
                              <!-- changes to following choose tag should be changed in another 2 places -->
                                 <xsl:choose>
                                    <xsl:when test="@ours">
                                      element = (void*)adb_<xsl:value-of select="@type"/>_create( env);

                                      status =  adb_<xsl:value-of select="@type"/>_deserialize(( <xsl:value-of select="$nativePropertyType"/>)element,
                                                                            env, current_node);
                                      if( AXIS2_FAILURE == status)
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                              " %d :: %s", env->error->error_number,
                                                              AXIS2_ERROR_GET_MESSAGE(env->error));
                                          return AXIS2_FAILURE;
                                      }
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                   ( <xsl:value-of select="$nativePropertyType"/>)element);
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axis2_char_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node );
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                   text_value);
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axis2_uri_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node );
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                   axutil_uri_parse_string( env, text_value));
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axis2_qname_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node );
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                   axutil_qname_create_from_string( env, text_value));
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='char'">
                                      text_value = axiom_element_get_text(current_element, env, current_node );
                                        <xsl:choose>
                                            <xsl:when test="@nillable">
                                            if(text_value == NULL)
                                            {
                                                status = AXIS2_FAILURE;
                                            }
                                            else
                                            {
                                                status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                    (char)(*text_value));
                                            }

                                            </xsl:when>
                                            <xsl:otherwise>
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                    (char)(*text_value));
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='int'">
                                      text_value = axiom_element_get_text(current_element, env, current_node );
                                        <xsl:choose>
                                            <xsl:when test="@nillable">
                                            if(text_value == NULL)
                                            {
                                                status = AXIS2_FAILURE;
                                            }
                                            else
                                            {
                                                status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                    atoi( text_value));
                                            }

                                            </xsl:when>
                                            <xsl:otherwise>
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                    atoi( text_value));
                                            </xsl:otherwise>
                                        </xsl:choose>
                                   </xsl:when>
                                    <xsl:when test="$nativePropertyType='short'">
                                      text_value = axiom_element_get_text(current_element, env, current_node );
                                      <xsl:choose>
                                            <xsl:when test="@nillable">
                                            if(text_value == NULL)
                                            {
                                                status = AXIS2_FAILURE;
                                            }
                                            else
                                            {
                                               status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                    atoi( text_value));
                                            }

                                            </xsl:when>
                                            <xsl:otherwise>
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                    atoi( text_value));
                                         </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='float'">
                                      text_value = axiom_element_get_text(current_element, env, current_node );
                                      <xsl:choose>
                                            <xsl:when test="@nillable">
                                            if(text_value == NULL)
                                            {
                                                status = AXIS2_FAILURE;
                                            }
                                            else
                                            {
                                               status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                     atof( text_value));
                                            }

                                            </xsl:when>
                                            <xsl:otherwise>
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                     atof( text_value));
                                        </xsl:otherwise>
                                        </xsl:choose>
                                     </xsl:when>
                                    <xsl:when test="$nativePropertyType='double'">
                                      text_value = axiom_element_get_text(current_element, env, current_node );
                                      <xsl:choose>
                                            <xsl:when test="@nillable">
                                            if(text_value == NULL)
                                            {
                                                status = AXIS2_FAILURE;
                                            }
                                            else
                                            {
                                               status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                     atof( text_value));
                                            }

                                            </xsl:when>
                                            <xsl:otherwise>
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                     atof( text_value));
                                        </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='long'">
                                      text_value = axiom_element_get_text(current_element, env, current_node );
                                      <xsl:choose>
                                            <xsl:when test="@nillable">
                                            if(text_value == NULL)
                                            {
                                                status = AXIS2_FAILURE;
                                            }
                                            else
                                            {
                                               status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                     atol( text_value));
                                            }

                                            </xsl:when>
                                            <xsl:otherwise>
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                     atol( text_value));
                                        </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axiom_node_t*'">
                                      text_value = NULL; /** just to avoid warning */
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                      current_node);
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axis2_bool_t'">
                                      text_value = axiom_element_get_text(current_element, env, current_node );
                                         <xsl:choose>
                                            <xsl:when test="@nillable">
                                            if(text_value == NULL)
                                            {
                                                status = AXIS2_FAILURE;
                                            }
                                            else if ( !strcmp ( text_value , "true" ) || !strcmp ( text_value, "TRUE") )
                                            {
                                                status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                 AXIS2_TRUE);
                                            }
                                            else
                                            {
                                                status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                      AXIS2_FALSE);
                                            }
                                            </xsl:when>
                                             <xsl:otherwise>
                                                 if ( !strcmp ( text_value , "true" ) || !strcmp ( text_value, "TRUE") )
                                                {
                                                    status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                      AXIS2_TRUE);
                                                }
                                                else
                                                {
                                                    status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                      AXIS2_FALSE);
                                                }
                                             </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                                      element = (void*)axutil_date_time_create( env);
                                      text_value = axiom_element_get_text(current_element, env, current_node );
                                      status = axutil_date_time_deserialize_date_time( (axutil_date_time_t*)element, env,
                                                                      text_value);
                                      if( AXIS2_FAILURE ==  status)
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                              " %d :: %s", env->error->error_number,
                                                              AXIS2_ERROR_GET_MESSAGE(env->error));
                                          return AXIS2_FAILURE;
                                      }
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                   ( <xsl:value-of select="$nativePropertyType"/>)element);
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                                      element = (void*)axutil_base64_binary_create( env);
                                      text_value = axiom_element_get_text(current_element, env, current_node );
                                      status = axutil_base64_binary_set_encoded_binary( (axutil_base64_binary_t*)element, env,
                                                                      text_value);
                                      if( AXIS2_FAILURE ==  status)
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                              " %d :: %s", env->error->error_number,
                                                              AXIS2_ERROR_GET_MESSAGE(env->error));
                                          return AXIS2_FAILURE;
                                      }
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                   ( <xsl:value-of select="$nativePropertyType"/>)element);
                                    </xsl:when>
                                    <xsl:otherwise>
                                      <!-- TODO: add other types here -->
                                      /** imposible to handle the request type - so please do it manually */
                                      text_value = NULL;
                                    </xsl:otherwise>
                                 </xsl:choose>
                                 if( AXIS2_FAILURE ==  status)
                                 {
                                     AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in setting the value for <xsl:value-of select="$propertyName"/> "
                                                         " %d :: %s", env->error->error_number,
                                                         AXIS2_ERROR_GET_MESSAGE(env->error));
                                     return AXIS2_FAILURE;
                                 }
                           }
                           <xsl:if test="not(@nillable) and not(@minOccurs=0)">
                               else
                               {
                                   /** this is not a nillable element*/
                                   AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "non nillable or minOuccrs != 0 element <xsl:value-of select="$propertyName"/> missing"
                                                         " %d :: %s", env->error->error_number,
                                                         AXIS2_ERROR_GET_MESSAGE(env->error));
                                   return AXIS2_FAILURE;
                               }
                           </xsl:if>
                        </xsl:when>
                        <xsl:otherwise> <!-- when it is all the way an array -->
                           <xsl:choose>
                             <xsl:when test="../@ordered or not($anon or $istype)"> <!-- all the elements should follow this -->
                                <xsl:choose>
                                  <xsl:when test="@nsuri and @nsuri != ''">
                                    element_qname = axutil_qname_create( env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>","<xsl:choose>
                                                                   <xsl:when test="@prefix!=''"><xsl:value-of select="@prefix"/></xsl:when>
                                                                   <xsl:when test="@nsuri=../@nsuri"><xsl:value-of select="../@nsprefix"/></xsl:when></xsl:choose>");
                                  </xsl:when>
                                  <xsl:otherwise>
                                    element_qname = axutil_qname_create( env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                                  </xsl:otherwise>
                                </xsl:choose>
                               for ( i = 0, sequence_broken = 0, tmp_node = current_node = <xsl:choose>
                                             <xsl:when test="position()=1">first_node</xsl:when>
                                             <xsl:otherwise>axiom_node_get_next_sibling( current_node, env)</xsl:otherwise></xsl:choose>; current_node != NULL; current_node = axiom_node_get_next_sibling( current_node, env))
                               {
                                  current_element = (axiom_element_t *)axiom_node_get_data_element( current_node, env);
                                  qname = axiom_element_get_qname( current_element, env, current_node);

                                  if ( axutil_qname_equals( element_qname, env, qname) )
                                  {
                                      if( sequence_broken)
                                      {
                                        /** found element out of order */
                                        AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "elements found out of order for array<xsl:value-of select="$propertyName"/> missing"
                                                         " %d :: %s", env->error->error_number,
                                                         AXIS2_ERROR_GET_MESSAGE(env->error));
                                        return AXIS2_FAILURE;
                                      }
                                      tmp_node = current_node; /** always update the current node */
                                      element_found = 1;
                                      <!-- changes to following choose tag should be changed in another 2 places -->
                                     <xsl:choose>
                                        <xsl:when test="@ours">
                                          element = (void*)adb_<xsl:value-of select="@type"/>_create( env);
                                          
                                          status =  adb_<xsl:value-of select="@type"/>_deserialize( ( <xsl:value-of select="$nativePropertyType"/>)element, env,
                                                                                 current_node);
                                          
                                          if( AXIS2_FAILURE ==  status)
                                          {
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                  " %d :: %s", env->error->error_number,
                                                                  AXIS2_ERROR_GET_MESSAGE(env->error));
                                              return AXIS2_FAILURE;
                                          }
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          axutil_array_list_add_at( arr_list, env, i, (void*)text_value);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_qname_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          axutil_array_list_add_at( arr_list, env, i, (void*)axutil_qname_create_from_string(env, text_value));
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_uri_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          axutil_array_list_add_at( arr_list, env, i, (void*)axutil_uri_parse_string(env, text_value));
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='char'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          /** we keeps ints in arrays from their pointers */
                                          element = AXIS2_MALLOC( env-> allocator, 64);
                                          (*(<xsl:value-of select="$nativePropertyType"/>*)element) = (char)(*text_value);
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='int'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          /** we keeps ints in arrays from their pointers */
                                          element = AXIS2_MALLOC( env-> allocator, sizeof(int));
                                          (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='short'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          /** we keeps ints in arrays from their pointers */
                                          element = AXIS2_MALLOC( env-> allocator, sizeof(short));
                                          (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='float'">
                                          /** we keeps float in arrays from their pointers */
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          element = AXIS2_MALLOC( env-> allocator, sizeof(float));
                                          (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atof(text_value);
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='double'">
                                          /** we keeps float in arrays from their pointers */
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          element = AXIS2_MALLOC( env-> allocator, sizeof(double));
                                          (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atof(text_value);
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='long'">
                                          /** we keeps long in arrays from their pointers */
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          element = AXIS2_MALLOC( env-> allocator, sizeof(long));
                                          (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atol(text_value);
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axiom_node_t*'">
                                          text_value = NULL; /** just to avoid warning */
                                          axutil_array_list_add_at( arr_list, env, i, (void*)current_node);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          if ( !strcmp ( text_value , "true" ) || !strcmp ( text_value, "TRUE") )
                                          {
                                             axutil_array_list_add_at( arr_list, env, i, (void*)AXIS2_TRUE);
                                          }
                                          else
                                          {
                                             axutil_array_list_add_at( arr_list, env, i, (void*)AXIS2_FALSE);
                                          }
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                                          element = (void*)axutil_date_time_create( env);
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          status = axutil_date_time_deserialize_date_time( (axutil_date_time_t*)element, env,
                                                                          text_value);
                                          if( AXIS2_FAILURE ==  status)
                                          {
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                  " %d :: %s", env->error->error_number,
                                                                  AXIS2_ERROR_GET_MESSAGE(env->error));
                                              return AXIS2_FAILURE;
                                          }
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                                          element = (void*)axutil_base64_binary_create( env);
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          status = axutil_base64_binary_set_encoded_binary( (axutil_base64_binary_t*)element, env,
                                                                          text_value);
                                          if( AXIS2_FAILURE ==  status)
                                          {
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                  " %d :: %s", env->error->error_number,
                                                                  AXIS2_ERROR_GET_MESSAGE(env->error));
                                              return AXIS2_FAILURE;
                                          }
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:otherwise>
                                          <!-- TODO: add other types here -->
                                          /** imposible to handle the request type - so please do it manually */
                                          text_value = NULL;
                                        </xsl:otherwise>
                                     </xsl:choose>
                                     if( AXIS2_FAILURE ==  status)
                                     {
                                         AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in setting the value for <xsl:value-of select="$propertyName"/> "
                                                             " %d :: %s", env->error->error_number,
                                                             AXIS2_ERROR_GET_MESSAGE(env->error));
                                         return AXIS2_FAILURE;
                                     }

                                     i ++;
                                  }
                                  else
                                  {
                                      sequence_broken = 1;
                                  }
                               }
                               current_node = tmp_node;
                               status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                   arr_list);

                             </xsl:when>
                             <xsl:otherwise> <!-- otherwse for "../@ordered or not($anon or $istype)" -->
                                <xsl:choose>
                                  <xsl:when test="@nsuri and @nsuri != ''">
                                    element_qname = axutil_qname_create( env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>","<xsl:choose>
                                                                   <xsl:when test="@prefix!=''"><xsl:value-of select="@prefix"/></xsl:when>
                                                                   <xsl:when test="@nsuri=../@nsuri"><xsl:value-of select="../@nsprefix"/></xsl:when></xsl:choose>");
                                  </xsl:when>
                                  <xsl:otherwise>
                                    element_qname = axutil_qname_create( env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                                  </xsl:otherwise>
                                </xsl:choose>
                               /**
                                 * because elements are not ordered we should surf all the sibling to pick the right one
                                 */
                               for ( i = 0, current_node = first_node; current_node != NULL; current_node = axiom_node_get_next_sibling( current_node, env))
                               {
                                  current_element = (axiom_element_t *)axiom_node_get_data_element( current_node, env);
                                  qname = axiom_element_get_qname( current_element, env, current_node);

                                  if ( axutil_qname_equals( element_qname, env, qname)
                                  {
                                       /** found the requried element */
                                       element_found = 1;
                                      <!-- changes to following choose tag should be changed in another 2 places -->
                                     <xsl:choose>
                                        <xsl:when test="@ours">
                                          element = (void*)adb_<xsl:value-of select="@type"/>_create( env);
                                          
                                          status =  adb_<xsl:value-of select="@type"/>_deserialize( ( <xsl:value-of select="$nativePropertyType"/>)element, env,
                                                                                 current_node);
                                          if( AXIS2_FAILURE ==  status)
                                          {
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                  " %d :: %s", env->error->error_number,
                                                                  AXIS2_ERROR_GET_MESSAGE(env->error));
                                              return AXIS2_FAILURE;
                                          }
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          axutil_array_list_add_at( arr_list, env, i, (void*)text_value);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_qname_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          axutil_array_list_add_at( arr_list, env, i, (void*)axutil_qname_create_from_string(env, text_value));
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_uri_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          axutil_array_list_add_at( arr_list, env, i, (void*)axutil_uri_parse_string(env, text_value));
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='char'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          /** we keeps ints in arrays from their pointers */
                                          element = AXIS2_MALLOC( env-> allocator, 64);
                                          (*(<xsl:value-of select="$nativePropertyType"/>*)element) = (char)(*text_value);
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='int'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          /** we keeps ints in arrays from their pointers */
                                          element = AXIS2_MALLOC( env-> allocator, sizeof(int));
                                          (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='short'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          /** we keeps ints in arrays from their pointers */
                                          element = AXIS2_MALLOC( env-> allocator, sizeof(short));
                                          (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='float'">
                                          /** we keeps float in arrays from their pointers */
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          element = AXIS2_MALLOC( env-> allocator, sizeof(float));
                                          (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atof(text_value);
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='double'">
                                          /** we keeps float in arrays from their pointers */
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          element = AXIS2_MALLOC( env-> allocator, sizeof(double));
                                          (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atof(text_value);
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='long'">
                                          /** we keeps long in arrays from their pointers */
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          element = AXIS2_MALLOC( env-> allocator, sizeof(long));
                                          (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atol(text_value);
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axiom_node_t*'">
                                          text_value = NULL; /** just to avoid warning */
                                          axutil_array_list_add_at( arr_list, env, i, (void*)current_node);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          if ( !strcmp ( text_value , "true" ) || !strcmp ( text_value, "TRUE") )
                                          {
                                             axutil_array_list_add_at( arr_list, env, i, (void*)AXIS2_TRUE);
                                          }
                                          else
                                          {
                                             axutil_array_list_add_at( arr_list, env, i, (void*)AXIS2_FALSE);
                                          }
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                                          element = (void*)axutil_date_time_create( env);
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          status = axutil_date_time_deserialize_date_time( (axutil_date_time_t*)element, env,
                                                                          text_value);
                                          if( AXIS2_FAILURE ==  status)
                                          {
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                  " %d :: %s", env->error->error_number,
                                                                  AXIS2_ERROR_GET_MESSAGE(env->error));
                                              return AXIS2_FAILURE;
                                          }
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                                          element = (void*)axutil_base64_binary_create( env);
                                          text_value = axiom_element_get_text(current_element, env, current_node );
                                          status = axutil_base64_binary_set_encoded_binary( (axutil_base64_binary_t*)element, env,
                                                                          text_value);
                                          if( AXIS2_FAILURE ==  status)
                                          {
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                  " %d :: %s", env->error->error_number,
                                                                  AXIS2_ERROR_GET_MESSAGE(env->error));
                                              return AXIS2_FAILURE;
                                          }
                                          axutil_array_list_add_at( arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:otherwise>
                                          <!-- TODO: add other types here -->
                                          /** imposible to handle the request type - so please do it manually */
                                          text_value = NULL;
                                        </xsl:otherwise>
                                     </xsl:choose>
                                     if( AXIS2_FAILURE ==  status)
                                     {
                                         AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in setting the value for <xsl:value-of select="$propertyName"/> "
                                                             " %d :: %s", env->error->error_number,
                                                             AXIS2_ERROR_GET_MESSAGE(env->error));
                                         return AXIS2_FAILURE;
                                     }

                                     i ++;
                                  }
                               }
                               status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>( <xsl:value-of select="$name"/>, env,
                                                                   arr_list);
                             </xsl:otherwise> <!--closing otherwise for "../@ordered  or not($anon or $istype)" -->
                           </xsl:choose> <!-- chooses for ordered or not @ordered or not($anon or $istype)-->
                        </xsl:otherwise> <!-- closing when it is all the way an array -->
                      </xsl:choose> <!-- check array or not -->
                   </xsl:otherwise> <!-- closing when it is an element not(@attribute) -->
                 </xsl:choose> <!--- chooosing for element or attribute -->
              </xsl:for-each> <!-- closing for each property -->

          return status;
       }

        <xsl:if test="@simple">
            axis2_char_t* AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_serialize_to_string(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env)
            {
                axis2_char_t *text_value = NULL;

                <xsl:for-each select="property">
                  <xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
                  <xsl:variable name="propertyType">
                     <xsl:choose>
                       <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                       <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                       <xsl:when test="@ours">
                        adb_<xsl:value-of select="@type"/>_t*
                        </xsl:when>
                       <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                     <xsl:choose>
                       <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                       <xsl:when test="@ours">
                        adb_<xsl:value-of select="@type"/>_t*
                        </xsl:when>
                       <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="propertyName"><xsl:value-of select="@originalName"></xsl:value-of></xsl:variable>
                  <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
    
                  <xsl:variable name="attriName"><!--these are used in arrays to take the native type-->
                     <xsl:choose>
                       <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                       <xsl:otherwise><xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="justAttriName"><!--these are used in arrays to take the native type-->
                     <xsl:choose>
                       <xsl:when test="@isarray">element</xsl:when>
                       <xsl:otherwise><xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>


                 <xsl:choose>
                    <!-- add int s -->
                    <xsl:when test="$nativePropertyType='int'">
                       text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 64);
                       sprintf ( text_value, "%d", <xsl:value-of select="$attriName"/> );
                    </xsl:when>

                    <!-- add char s -->
                    <xsl:when test="$nativePropertyType='char'">
                       text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 64);
                       sprintf ( text_value, "%c", <xsl:value-of select="$attriName"/> );
                    </xsl:when>

                    <!-- add short s -->
                    <xsl:when test="$nativePropertyType='short'">
                       text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 64);
                       sprintf ( text_value, "%d", <xsl:value-of select="$attriName"/> );
                    </xsl:when>

                    <!-- add long s -->
                    <xsl:when test="$nativePropertyType='long'">
                       text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 64);
                       sprintf ( text_value, "%d", (int)<xsl:value-of select="$attriName"/> );
                    </xsl:when>

                    <!-- add float s -->
                    <xsl:when test="$nativePropertyType='float'">
                       text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 64);
                       sprintf ( text_value, "%f", <xsl:value-of select="$attriName"/> );
                    </xsl:when>

                    <!-- add double s -->
                    <xsl:when test="$nativePropertyType='double'">
                       text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 64);
                       sprintf ( text_value, "%f", <xsl:value-of select="$attriName"/> );
                    </xsl:when>

                    <!-- add axis2_char_t* s -->
                    <xsl:when test="$nativePropertyType='axis2_char_t*'">
                       text_value = (axis2_char_t*)axutil_strdup(env, <xsl:value-of select="$attriName"/>);
                    </xsl:when>

                    <!-- add axis2_uri_t s -->
                    <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                       text_value = axutil_uri_to_string(<xsl:value-of select="$attriName"/>, env, AXIS2_URI_UNP_OMITUSERINFO);
                    </xsl:when>

                    <!-- add axis2_qname_t s -->
                    <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                       text_value = AXIS2_QNAME_TO_STRING(<xsl:value-of select="$attriName"/>, env);
                    </xsl:when>

                    <!-- add axis2_bool_t s -->
                    <xsl:when test="$nativePropertyType='axis2_bool_t'">
                       <!--text_value = (<xsl:value-of select="$attriName"/>)?"true":"false";-->
                       text_value = (axis2_char_t*)axutil_strdup(env, (<xsl:value-of select="$attriName"/>)?"true":"false");
                    </xsl:when>
                    <!-- add axis2_date_time_t s -->
                    <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                       text_value =  axutil_date_time_serialize_date_time( <xsl:value-of select="$attriName"/>, env);
                    </xsl:when>
                    <!-- add axis2_base64_binary_t s -->
                    <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                       text_value =  axutil_base64_binary_get_encoded_binary( <xsl:value-of select="$attriName"/>, env);
                    </xsl:when>
                    <xsl:when test="@ours">
                        <!-- This should be in an unreachable path -->
                    </xsl:when>
                    <xsl:otherwise>
                      <!--TODO: add new attributes types -->
                      /** can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
                      text_value = NULL;
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:for-each>
                return text_value;
            }
        </xsl:if>
        
        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_serialize(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, axiom_node_t* parent, int has_parent, int tag_closed)
        {
            <!-- first declaration part -->
            <xsl:for-each select="property/@attribute">
             <xsl:if test="position()=1">
               axiom_attribute_t *text_attri = NULL;
               axiom_element_t *parent_element = NULL;
             </xsl:if>
            </xsl:for-each>
            <xsl:if test="@type">
             axis2_char_t *string_to_stream;
            </xsl:if>
         <!--now distinguise the properties specific to simple types -->
         <xsl:choose>
           <xsl:when test="@simple">
            axiom_node_t *current_node = NULL;
            axiom_data_source_t *data_source = NULL;
            axutil_stream_t *stream = NULL;
            axis2_char_t *text_value;
             <xsl:for-each select="property/@attribute">
              <xsl:if test="position()=1">
               axiom_namespace_t *ns1 = NULL;
              </xsl:if>
             </xsl:for-each>
           </xsl:when>

           <!-- non simple types -->
           <xsl:otherwise>
             <xsl:for-each select="property">
              <xsl:if test="position()=1"> <!-- check for at least one element exists -->
                axiom_namespace_t *ns1 = NULL;
              </xsl:if>
             </xsl:for-each>
            <xsl:for-each select="property/@isarray">
             <xsl:if test="position()=1">
               long i = 0;
               long count = 0;
               void *element = NULL;
             </xsl:if>
            </xsl:for-each>
            <xsl:for-each select="property">
                <xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
                <xsl:choose>
                    <xsl:when test="not(@type) or (@ours='yes' and (@type='uri' or @type='qname' or @type='date_time' or @type='base64_binary' or @type='char')) or @type='char' or @type='axis2_char_t*' or @type='axutil_base64_binary_t*' or @type='axutil_date_time_t*' or @type='axiom_node_t*' or @type='axutil_uri_t*' or @type='axutil_qname_t*'">
                    axis2_char_t *text_value_<xsl:value-of select="$position"/>;
                    axis2_char_t *text_value_<xsl:value-of select="$position"/>_temp;
                    </xsl:when>
                    <xsl:otherwise>
                    axis2_char_t text_value_<xsl:value-of select="$position"/>[64];
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>

        <!--to remove (nothing done inside)-->
            <!--<xsl:if test="property">
              <xsl:if test="not(property/@ours) or property/@ours=''">
                axis2_char_t* text_value = NULL;
              </xsl:if>
            </xsl:if>-->

            <xsl:for-each select="property">
             <xsl:if test="position()=1">
               <!--axiom_node_t *current_node = NULL;
               axiom_element_t *current_element = NULL;-->
             </xsl:if>
            </xsl:for-each>
        <!--remove end-->
            
            <xsl:for-each select="property/@attribute">
             <xsl:if test="position()=1">
                axis2_char_t *text_value = NULL;
             </xsl:if>
            </xsl:for-each>
            <xsl:if test="property and (not(property/@attribute) or property/@attribute='' or property/@notattribute)">
               axiom_node_t *current_node = NULL;
               axiom_element_t *current_element = NULL;
               axis2_char_t *start_input_str = NULL;
               axis2_char_t *end_input_str = NULL;
               unsigned int start_input_str_len = 0;
               unsigned int end_input_str_len = 0;
            </xsl:if>
            <!-- Following is in special situatioin where no properties exist -->
            <xsl:if test="(property and (not(property/@attribute) or property/@attribute='' or property/@notattribute)) or count(property)=0"> 
               axiom_data_source_t *data_source = NULL;
               axutil_stream_t *stream = NULL;
            </xsl:if>
            AXIS2_ENV_CHECK(env, NULL);

            <xsl:if test="$nsuri and $nsuri != ''"> 
            <xsl:for-each select="property">
              <xsl:if test="position()=1"> <!-- check for at least one element exists -->
                ns1 = axiom_namespace_create (env,
                                         "<xsl:value-of select="$nsuri"/>",
                                         "<xsl:value-of select="$nsprefix"/>");
               </xsl:if>
            </xsl:for-each>
            </xsl:if>
            <xsl:if test="not(@type)">
              <xsl:for-each select="property">
               <xsl:if test="position()=1">
                if( parent == NULL)
                {
                    current_element = axiom_element_create (env, parent, "<xsl:value-of select="$originalName"/>", ns1 , &amp;current_node);
                    axiom_element_set_namespace( current_element, env, ns1, current_node);
                    <xsl:if test="@child-nsuri and @child-nsprefix and @ours">
                    ns1 = axiom_namespace_create (env,
                                         "<xsl:value-of select="@child-nsuri"/>",
                                         "<xsl:value-of select="@child-nsprefix"/>");
                    axiom_element_declare_namespace(current_element, env, current_node, ns1);    
                    </xsl:if>
                    parent = current_node;
                }
               </xsl:if>
              </xsl:for-each>
            </xsl:if>
            </xsl:otherwise> <!--otherwise for @simple -->
            </xsl:choose>

            <xsl:if test="@simple or (property and (not(property/@attribute) or property/@attribute='' or property/@notattribute)) or count(property)=0">
                if(has_parent)
                {
                    data_source = (axiom_data_source_t *)axiom_node_get_data_element(parent, env);
                    if (!data_source)
                        return NULL;
                    stream = axiom_data_source_get_stream(data_source, env); /* assume parent is of type data source */
                    if (!stream)
                        return NULL;
                    <xsl:if test="count(property)!=0">
                      current_node = parent;
                    </xsl:if>
                }
                <xsl:if test="count(property)!=0">
                else
                {
                    data_source = axiom_data_source_create(env, parent, &amp;current_node);
                    stream = axiom_data_source_get_stream(data_source, env);
                }
                </xsl:if>
             </xsl:if>


            <xsl:for-each select="property/@attribute">
             <xsl:if test="position()=1">
                parent_element = (axiom_element_t *)axiom_node_get_data_element( parent, env);
                <xsl:choose>
                  <xsl:when test="$nsuri and $nsuri != ''">
                    ns1 = axiom_namespace_create (env,
                                         "<xsl:value-of select="$nsuri"/>",
                                         "<xsl:value-of select="$nsprefix"/>");
                  </xsl:when>
                  <xsl:otherwise>
                    ns1 = NULL;
                  </xsl:otherwise>
                </xsl:choose>

             </xsl:if>
            </xsl:for-each>
            
            <!--first write attributes tothe parent-->
            <xsl:if test="count(property[@attribute])!=0 or @type">
            if(!tag_closed)
            {
            </xsl:if>
            <xsl:for-each select="property">
              <xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
              <xsl:variable name="propertyType">
                 <xsl:choose>
                   <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">
                    adb_<xsl:value-of select="@type"/>_t*
                    </xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">
                    adb_<xsl:value-of select="@type"/>_t*
                    </xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="propertyName"><xsl:value-of select="@originalName"></xsl:value-of></xsl:variable>
              <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

              <xsl:variable name="attriName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="justAttriName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>

              <xsl:variable name="namespacePrefix">
                <xsl:choose>
                    <xsl:when test="$nsprefix">
                        <xsl:value-of select="$nsprefix"/><xsl:text>:</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text></xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>

                <xsl:if test="@attribute">
                      <!-- here only simple type possible -->
                      <!-- 64 bytes is used to the store the string representation of the number and the namespace prefix + ":" -->
                      <xsl:choose>
                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='int'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 
                                                            (64 + strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " <xsl:value-of select="$namespacePrefix"/><xsl:value-of select="$propertyName"/>=\"%d\"", <xsl:value-of select="$attriName"/> );
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>

                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='char'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 
                                                            (64 + strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " <xsl:value-of select="$namespacePrefix"/><xsl:value-of select="$propertyName"/>=\"%c\"", <xsl:value-of select="$attriName"/> );
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>

                        <!-- add short s -->
                        <xsl:when test="$nativePropertyType='short'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 
                                                            (64 + strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " <xsl:value-of select="$namespacePrefix"/><xsl:value-of select="$propertyName"/>=\"%d\"", <xsl:value-of select="$attriName"/> );
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>

                        <!-- add long s -->
                        <xsl:when test="$nativePropertyType='long'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 
                                                            (64 + strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " <xsl:value-of select="$namespacePrefix"/><xsl:value-of select="$propertyName"/>=\"%d\"", <xsl:value-of select="$attriName"/> );
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>

                        <!-- add float s -->
                        <xsl:when test="$nativePropertyType='float'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 
                                                            (64 + strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " <xsl:value-of select="$namespacePrefix"/><xsl:value-of select="$propertyName"/>=\"%f\"", <xsl:value-of select="$attriName"/> );
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>

                        <!-- add double s -->
                        <xsl:when test="$nativePropertyType='double'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 
                                                            (64 + strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " <xsl:value-of select="$namespacePrefix"/><xsl:value-of select="$propertyName"/>=\"%f\"", <xsl:value-of select="$attriName"/> );
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 
                                                            (64 + strlen("<xsl:value-of select="$propertyName"/>" +
                                                             strlen(<xsl:value-of select="$attriName"/>))));
                           sprintf(text_value, " <xsl:value-of select="$namespacePrefix"/><xsl:value-of select="$propertyName"/>=\"%s\"", <xsl:value-of select="$attriName"/> );
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>

                        <!-- add axis2_uri_t s -->
                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                           text_value = axutil_uri_to_string(<xsl:value-of select="$attriName"/>, env, AXIS2_URI_UNP_OMITUSERINFO);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) *
                                                            (64 + strlen("<xsl:value-of select="$propertyName"/>" +
                                                             strlen(text_value))));
                           sprintf(string_to_stream, " <xsl:value-of select="$namespacePrefix"/><xsl:value-of select="$propertyName"/>=\"%s\"", text_value );
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE( env-> allocator, string_to_stream);
                        </xsl:when>

                        <!-- add axis2_qname_t s -->
                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                           text_value = AXIS2_QNAME_TO_STRING(<xsl:value-of select="$attriName"/>, env);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) *
                                                            (64 + strlen("<xsl:value-of select="$propertyName"/>" +
                                                             strlen(text_value))));
                           sprintf(string_to_stream, " <xsl:value-of select="$namespacePrefix"/><xsl:value-of select="$propertyName"/>=\"%s\"", text_value );
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE( env-> allocator, string_to_stream);
                        </xsl:when>

                        <!-- add axis2_bool_t s -->
                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                           <!--text_value = (<xsl:value-of select="$attriName"/>)?"true":"false";-->
                           text_value = (<xsl:value-of select="$attriName"/>)?"true":"false";
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) *
                                                            (64 + strlen("<xsl:value-of select="$propertyName"/>" +
                                                             strlen(text_value))));
                           sprintf(string_to_stream, " <xsl:value-of select="$namespacePrefix"/><xsl:value-of select="$propertyName"/>=\"%s\"", text_value );
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE( env-> allocator, string_to_stream);
                        </xsl:when>
                        <!-- add axis2_date_time_t s -->
                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                           text_value =  axutil_date_time_serialize_date_time( <xsl:value-of select="$attriName"/>, env);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) *
                                                            (64 + strlen("<xsl:value-of select="$propertyName"/>" +
                                                             strlen(text_value))));
                           sprintf(string_to_stream, " <xsl:value-of select="$namespacePrefix"/><xsl:value-of select="$propertyName"/>=\"%s\"", text_value );
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE( env-> allocator, string_to_stream);
                        </xsl:when>
                        <!-- add axis2_base64_binary_t s -->
                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                           text_value =  axutil_base64_binary_get_encoded_binary( <xsl:value-of select="$attriName"/>, env);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) *
                                                            (64 + strlen("<xsl:value-of select="$propertyName"/>" +
                                                             strlen(text_value))));
                           sprintf(string_to_stream, " <xsl:value-of select="$namespacePrefix"/><xsl:value-of select="$propertyName"/>=\"%s\"", text_value );
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE( env-> allocator, string_to_stream);
                        </xsl:when>
                        <xsl:when test="@ours">
                           text_value = adb_<xsl:value-of select="@type"/>_serialize_to_string(<xsl:value-of select="$attriName"/>, env);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) *
                                                            (64 + strlen("<xsl:value-of select="$propertyName"/>" +
                                                             strlen(text_value))));
                           sprintf(string_to_stream, " <xsl:value-of select="$namespacePrefix"/><xsl:value-of select="$propertyName"/>=\"%s\"", text_value );
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE( env-> allocator, string_to_stream);
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>
                        <xsl:otherwise>
                          <!--TODO: add new attributes types -->
                          /** can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
                           text_value = NULL;
                        </xsl:otherwise>
                      </xsl:choose>
                </xsl:if>
            </xsl:for-each>

            <xsl:if test="@type">
             string_to_stream = "&gt;"; <!-- The ending tag of the parent -->
             axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
            </xsl:if>
             <!-- end bracket for if(!tag_closed)-->
            <xsl:if test="count(property[@attribute])!=0 or @type">
            }
            </xsl:if>

            <xsl:if test="@simple">
               
               text_value = <xsl:value-of select="$axis2_name"/>_serialize_to_string(<xsl:value-of select="$name"/>, env);
               axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));

            </xsl:if>

            <xsl:for-each select="property">
              <xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
              <xsl:variable name="propertyType">
                 <xsl:choose>
                   <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">
                    adb_<xsl:value-of select="@type"/>_t*
                    </xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">
                    adb_<xsl:value-of select="@type"/>_t*
                    </xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="propertyName"><xsl:value-of select="@originalName"></xsl:value-of></xsl:variable>
              <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

              <xsl:variable name="attriName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="justAttriName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>

              <xsl:choose>
                <xsl:when test="@attribute">
                      <!-- here only simple type possible -->
                      if(tag_closed)
                      {
                      <xsl:choose>
                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='int'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 64);
                           sprintf ( text_value, "%d", <xsl:value-of select="$attriName"/> );
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>

                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='char'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 64);
                           sprintf ( text_value, "%c", <xsl:value-of select="$attriName"/> );
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>

                        <!-- add short s -->
                        <xsl:when test="$nativePropertyType='short'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 64);
                           sprintf ( text_value, "%d", <xsl:value-of select="$attriName"/> );
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>

                        <!-- add long s -->
                        <xsl:when test="$nativePropertyType='long'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 64);
                           sprintf ( text_value, "%d", (int)<xsl:value-of select="$attriName"/> );
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>

                        <!-- add float s -->
                        <xsl:when test="$nativePropertyType='float'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 64);
                           sprintf ( text_value, "%f", <xsl:value-of select="$attriName"/> );
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>

                        <!-- add double s -->
                        <xsl:when test="$nativePropertyType='double'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC ( env-> allocator, sizeof ( axis2_char_t) * 64);
                           sprintf ( text_value, "%f", <xsl:value-of select="$attriName"/> );
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                           text_value = <xsl:value-of select="$attriName"/>;
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>

                        <!-- add axis2_uri_t s -->
                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                           text_value = axutil_uri_to_string(<xsl:value-of select="$attriName"/>, env, AXIS2_URI_UNP_OMITUSERINFO);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>

                        <!-- add axis2_qname_t s -->
                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                           text_value = AXIS2_QNAME_TO_STRING(<xsl:value-of select="$attriName"/>, env);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>

                        <!-- add axis2_bool_t s -->
                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                           <!--text_value = (<xsl:value-of select="$attriName"/>)?"true":"false";-->
                           strcpy( text_value, (<xsl:value-of select="$attriName"/>)?"true":"false");
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>
                        <!-- add axis2_date_time_t s -->
                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                           text_value =  axutil_date_time_serialize_date_time( <xsl:value-of select="$attriName"/>, env);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>
                       <!-- add axis2_base64_binary_t s -->
                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                           text_value =  axutil_base64_binary_get_encoded_binary( <xsl:value-of select="$attriName"/>, env);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>
                        <xsl:when test="@ours">
                           text_value = adb_<xsl:value-of select="@type"/>_serialize_to_string(<xsl:value-of select="$attriName"/>, env);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE( env-> allocator, text_value);
                        </xsl:when>
                        <xsl:otherwise>
                          <!--TODO: add new attributes types -->
                          /** can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
                           text_value = NULL;
                           parent_element = NULL;
                           text_attri = NULL;
                        </xsl:otherwise>
                      </xsl:choose>
                      } <!-- End bracket for if(tag_closed)-->
                </xsl:when>
                <xsl:when test="../@simple"></xsl:when> <!--Just to ignore parsing following code at simple types-->
                <xsl:otherwise>

                   <!-- handles arrays -->
                   <xsl:if test="@isarray">


                     /**
                      * parsing <xsl:value-of select="$CName"/> array
                      */
                     if ( <xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/> != NULL)
                     {
                        <xsl:choose>
                            <xsl:when test="@ours">
                         start_input_str = "&lt;<xsl:if test="$nsprefix and $nsprefix != ''"><xsl:value-of select="$nsprefix"/>:</xsl:if><xsl:value-of select="$propertyName"/><xsl:if test="@child-nsuri and @child-nsprefix and @ours"> xmlns:<xsl:value-of select="@child-nsprefix"/>=\"<xsl:value-of select="@child-nsuri"/>\"</xsl:if>";
                            </xsl:when>
                            <xsl:otherwise>
                         start_input_str = "&lt;<xsl:if test="$nsprefix and $nsprefix != ''"><xsl:value-of select="$nsprefix"/>:</xsl:if><xsl:value-of select="$propertyName"/><xsl:if test="@child-nsuri and @child-nsprefix and @ours"> xmlns:<xsl:value-of select="@child-nsprefix"/>=\"<xsl:value-of select="@child-nsuri"/>\"</xsl:if>&gt;";
                            </xsl:otherwise>
                        </xsl:choose>
                         start_input_str_len = axutil_strlen(start_input_str);
                         end_input_str = "&lt;/<xsl:value-of select="$nsprefix"/>:<xsl:value-of select="$propertyName"/>&gt;";
                         end_input_str_len = axutil_strlen(end_input_str);
                         count = axutil_array_list_size( <xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/>, env);
                         for( i = 0; i &lt; count; i ++)
                         {
                            element = axutil_array_list_get( <xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/>, env, i);
                    </xsl:if>
                     <!-- for each non attribute properties there will always be an element-->
                     /**
                      * parsing <xsl:value-of select="$propertyName"/> element
                      */
                     <!--current_element = axiom_element_create (env, parent, "<xsl:value-of select="$propertyName"/>", ns1 , &amp;current_node);
                     axiom_element_set_namespace ( current_element, env, ns1, current_node);-->

                    <!-- how to build all the ours things -->
                    <xsl:if test="not(@isarray)">
                        <xsl:choose>
                            <xsl:when test="@ours">
                        start_input_str = "&lt;<xsl:if test="$nsprefix and $nsprefix != ''"><xsl:value-of select="$nsprefix"/>:</xsl:if><xsl:value-of select="$propertyName"/><xsl:if test="@child-nsuri and @child-nsprefix and @ours"> xmlns:<xsl:value-of select="@child-nsprefix"/>=\"<xsl:value-of select="@child-nsuri"/>\"</xsl:if>";
                            </xsl:when>
                            <xsl:otherwise>
                        start_input_str = "&lt;<xsl:if test="$nsprefix and $nsprefix != ''"><xsl:value-of select="$nsprefix"/>:</xsl:if><xsl:value-of select="$propertyName"/><xsl:if test="@child-nsuri and @child-nsprefix and @ours"> xmlns:<xsl:value-of select="@child-nsprefix"/>=\"<xsl:value-of select="@child-nsuri"/>\"</xsl:if>&gt;";
                            </xsl:otherwise>
                        </xsl:choose>
                        start_input_str_len = axutil_strlen(start_input_str);
                        end_input_str = "&lt;/<xsl:if test="$nsprefix and $nsprefix != ''"><xsl:value-of select="$nsprefix"/>:</xsl:if><xsl:value-of select="$propertyName"/>&gt;";
                        end_input_str_len = axutil_strlen(end_input_str);
                    </xsl:if>

                    <xsl:choose>
                        <xsl:when test="@ours">
                            <xsl:if test="$anon or $istype"> <!-- As this shows, elements are not writing their tags here from stream.
                                                                 It is done using axiom above..-->
                            axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                            </xsl:if>
                            
                            <xsl:variable name="element_closed">
                                <xsl:choose>
                                    <xsl:when test="../@type"> 
                                        AXIS2_FALSE
                                    </xsl:when>
                                    <xsl:when test="$anon or $istype"> <!-- this mean the anonymous header is writing -->
                                        AXIS2_FALSE
                                    </xsl:when>
                                    <xsl:otherwise>
                                        AXIS2_TRUE
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            adb_<xsl:value-of select="@type"/>_serialize(<xsl:value-of select="$attriName"/>, env, current_node,
                                                                                 AXIS2_TRUE, <xsl:value-of select="$element_closed"/>);
                            <xsl:if test="$anon or $istype">
                            axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                            </xsl:if>
                        </xsl:when>


                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='int'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%d", *((<xsl:value-of select="$nativePropertyType"/>*)element) );
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%d", <xsl:value-of select="$attriName"/> );
                             </xsl:otherwise>
                           </xsl:choose>
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='unsigned int'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%d", *((<xsl:value-of select="$nativePropertyType"/>*)element) );
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%d", <xsl:value-of select="$attriName"/> );
                             </xsl:otherwise>
                           </xsl:choose>
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <!-- add char s -->
                        <xsl:when test="$nativePropertyType='char'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%c", *((<xsl:value-of select="$nativePropertyType"/>*)element) );
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%c", <xsl:value-of select="$attriName"/> );
                             </xsl:otherwise>
                           </xsl:choose>
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='unsigned char'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%c", *((<xsl:value-of select="$nativePropertyType"/>*)element) );
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%c", <xsl:value-of select="$attriName"/> );
                             </xsl:otherwise>
                           </xsl:choose>
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='axis2_byte_t'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%c", *((<xsl:value-of select="$nativePropertyType"/>*)element) );
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%c", <xsl:value-of select="$attriName"/> );
                             </xsl:otherwise>
                           </xsl:choose>
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <!-- add short s -->
                        <xsl:when test="$nativePropertyType='short'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%d", *((<xsl:value-of select="$nativePropertyType"/>*)element) );
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%d", <xsl:value-of select="$attriName"/> );
                             </xsl:otherwise>
                           </xsl:choose>
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='unsigned short'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%hu", *((<xsl:value-of select="$nativePropertyType"/>*)element) );
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%hu", <xsl:value-of select="$attriName"/> );
                             </xsl:otherwise>
                           </xsl:choose>
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>


                        <!-- NOTE: arrays for long, float, int are handled differently. they are stored in pointers -->
                        <!-- add long s -->
                        <xsl:when test="$nativePropertyType='long'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%d", (int)*((<xsl:value-of select="$nativePropertyType"/>*)element) );
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%d", (int)<xsl:value-of select="$attriName"/> );
                             </xsl:otherwise>
                           </xsl:choose>
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <!-- add long s -->
                        <xsl:when test="$nativePropertyType='unsigned long'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%lu", (int)*((<xsl:value-of select="$nativePropertyType"/>*)element) );
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%lu", (int)<xsl:value-of select="$attriName"/> );
                             </xsl:otherwise>
                           </xsl:choose>
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <!-- add float s -->
                        <xsl:when test="$nativePropertyType='float'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%f", *((<xsl:value-of select="$nativePropertyType"/>*)element) );
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%f", <xsl:value-of select="$attriName"/> );
                             </xsl:otherwise>
                           </xsl:choose>
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <!-- add double s -->
                        <xsl:when test="$nativePropertyType='double'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%f", *((<xsl:value-of select="$nativePropertyType"/>*)element) );
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf ( text_value_<xsl:value-of select="$position"/>, "%f", <xsl:value-of select="$attriName"/> );
                             </xsl:otherwise>
                           </xsl:choose>
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                           text_value_<xsl:value-of select="$position"/> = <xsl:value-of select="$attriName"/>;
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                            
                           text_value_<xsl:value-of select="$position"/>_temp = axutil_xml_quote_string(env, text_value_<xsl:value-of select="$position"/>, AXIS2_TRUE);
                           if (text_value_<xsl:value-of select="$position"/>_temp)
                           {
                               axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>_temp, axutil_strlen(text_value_<xsl:value-of select="$position"/>_temp));
                               AXIS2_FREE(env->allocator, text_value_<xsl:value-of select="$position"/>_temp);
                           }
                           else
                           {
                               axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           }
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <!-- add axis2_uri_t s -->
                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                           text_value_<xsl:value-of select="$position"/> = axutil_uri_to_string(<xsl:value-of select="$attriName"/>, env, AXIS2_URI_UNP_OMITUSERINFO);
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <!-- add axis2_qname_t s -->
                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                           text_value_<xsl:value-of select="$position"/> = axutil_qname_to_string(<xsl:value-of select="$attriName"/>, env);
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                          <!-- add axis2_bool_t s -->
                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                           strcpy( text_value_<xsl:value-of select="$position"/>, (<xsl:value-of select="$attriName"/>)?"true":"false" );
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <!-- add nodes -->
                        <xsl:when test="$nativePropertyType='axiom_node_t*'">
                           text_value_<xsl:value-of select="$position"/> = NULL; /** just to bypass the warning unused variable */
                           axiom_node_add_child( current_node, env, <xsl:value-of select="$attriName"/>);
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                          text_value_<xsl:value-of select="$position"/> = axutil_date_time_serialize_date_time( <xsl:value-of select="$attriName"/>, env);
                          axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                          text_value_<xsl:value-of select="$position"/> =axutil_base64_binary_get_encoded_binary(<xsl:value-of select="$attriName"/>, env);
                          axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                        </xsl:when>

                        <!--TODO: This should be extended for all the types that should be freed.. -->
                        <xsl:otherwise>
                          /** This is an unknown type or a primitive. handle this manually for unknown type */
                          text_value_<xsl:value-of select="$position"/> = NULL;
                        </xsl:otherwise>
                      </xsl:choose>

                   <!-- close tags arrays -->
                   <xsl:if test="@isarray">
                         }
                     }
                   </xsl:if>
                </xsl:otherwise> <!-- othewise for non attributes -->
              </xsl:choose>
            </xsl:for-each>
            return parent;
        }


        <xsl:for-each select="property">
            <xsl:variable name="propertyType">
               <xsl:choose>
                    <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                    <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                    <xsl:when test="@ours">
                     adb_<xsl:value-of select="@type"/>_t*
                     </xsl:when>
                    <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
           
           <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">
                    adb_<xsl:value-of select="@type"/>_t*
                   </xsl:when>
                   <xsl:otherwise>
                    <xsl:value-of select="@type"/>
                   </xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>

              <!-- Just to identiy the pointer to arrays -->
              <!-- Simmilar to native property type except for shor, tint, float, double -->
              <xsl:variable name="PropertyTypeArrayParam"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">
                    adb_<xsl:value-of select="@type"/>_t*
                   </xsl:when>
                   <xsl:when test="@type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='long'">
                    <xsl:value-of select="@type"/><xsl:text>*</xsl:text>
                   </xsl:when>
                   <xsl:otherwise>
                    <xsl:value-of select="@type"/>
                   </xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>

              <xsl:variable name="attriName"><!--these are used in arrays to take the native type-->
                   <xsl:value-of select="$name"/>->attrib_<xsl:value-of select="$CName"/>
              </xsl:variable>
            /**
             * getter for <xsl:value-of select="$propertyName"/>.
             */
            <xsl:value-of select="$propertyType"/> AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env)
             {
                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);

                return <xsl:value-of select="$attriName"/>;
             }

            /**
             * setter for <xsl:value-of select="$propertyName"/>
             */
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env,
                    <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text> param_<xsl:value-of select="$CName"/>)
             {
                <xsl:if test="@isarray">
                 int size = 0;
                </xsl:if>

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                if(!<xsl:value-of select="$name"/>)
                {
                    return AXIS2_FAILURE;
                }

                 <xsl:if test="@isarray">
                  size = axutil_array_list_size( param_<xsl:value-of select="$CName"/>, env);
                  <xsl:if test="not(@unbound)">
                      if ( size &gt; <xsl:value-of select="@maxOccurs"/> )
                      {
                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "<xsl:value-of select="$propertyName"/> has exceed the maxOccurs(<xsl:value-of select="@maxOccurs"/>)"
                                                     " %d :: %s", env->error->error_number,
                                                     AXIS2_ERROR_GET_MESSAGE(env->error));
                          return AXIS2_FAILURE;
                      }
                  </xsl:if>
                  if ( size &lt; <xsl:value-of select="@minOccurs"/> )
                  {
                      AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "<xsl:value-of select="$propertyName"/> has less than minOccurs(<xsl:value-of select="@minOccurs"/>)"
                                                 " %d :: %s", env->error->error_number,
                                                 AXIS2_ERROR_GET_MESSAGE(env->error));
                      return AXIS2_FAILURE;
                  }
                </xsl:if>
                <xsl:if test="not(@nillable) and not(@minOccurs='0') and (@ours or not($propertyType='char' or $propertyType='int' or $propertyType='short' or $propertyType='float' or $propertyType='double' or $propertyType='long' or $propertyType='axis2_bool_t'))">
                  if( NULL == param_<xsl:value-of select="$CName"/> )
                  {
                      AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "<xsl:value-of select="$propertyName"/> is NULL, but not a nullable element"
                                                 " %d :: %s", env->error->error_number,
                                                 AXIS2_ERROR_GET_MESSAGE(env->error));
                      return AXIS2_FAILURE;
                  }
                </xsl:if>
                <xsl:if test="@isarray">
                <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env);
                </xsl:if>
                <xsl:choose>
                    <xsl:when test="@type='axis2_char_t*' and not(@isarray)">
                        <xsl:value-of select="$attriName"/> = (axis2_char_t *)axutil_strdup(env, param_<xsl:value-of select="$CName"/>);
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$attriName"/> = param_<xsl:value-of select="$CName"/>;
                    </xsl:otherwise>
                </xsl:choose>
                return AXIS2_SUCCESS;
             }

            <xsl:if test="@isarray">
            /**
             * Get ith element of <xsl:value-of select="$propertyName"/>.
             */
            <xsl:value-of select="$nativePropertyType"/> AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>_at(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env, int i)
            {
                <xsl:value-of select="$PropertyTypeArrayParam"/> ret_val;
                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                if(<xsl:value-of select="$attriName"/> == NULL)
                {
                    return (<xsl:value-of select="$nativePropertyType"/>)0;
                }
                ret_val = (<xsl:value-of select="$PropertyTypeArrayParam"/>)axutil_array_list_get(<xsl:value-of select="$attriName"/>, env, i);
                <xsl:choose>
                  <xsl:when test="@type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='long'">
                    return *ret_val;
                  </xsl:when>
                  <xsl:otherwise>
                    return ret_val;
                  </xsl:otherwise>
                </xsl:choose>
            }

            /**
             * Add to <xsl:value-of select="$propertyName"/>.
             */
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env,
                    <xsl:value-of select="$nativePropertyType"/><xsl:text> param_</xsl:text> <xsl:value-of select="$CName"/>)
             {
                <xsl:if test="@type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='long'">
                   <xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text> ptr_param_</xsl:text><xsl:value-of select="$CName"/>;
                </xsl:if>

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);

                if(<xsl:value-of select="$attriName"/> == NULL)
                {
                    <xsl:value-of select="$attriName"/> = axutil_array_list_create(env, 10);
                }
                if(<xsl:value-of select="$attriName"/> == NULL)
                {
                    AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "cannot allocate memory for <xsl:value-of select="$propertyName"/>"
                                               " %d :: %s", env->error->error_number,
                                               AXIS2_ERROR_GET_MESSAGE(env->error));
                    return AXIS2_FAILURE;
                    
                }
                <xsl:choose>
                  <xsl:when test="@type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='long'">
                   <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/> =  (<xsl:value-of select="$PropertyTypeArrayParam"/>)
                            AXIS2_MALLOC(env->allocator, sizeof(<xsl:value-of select="@type"/>));
                   if(<xsl:value-of select="$attriName"/> == NULL)
                   {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "cannot allocate memory for new value of <xsl:value-of select="$propertyName"/>"
                                                  " %d :: %s", env->error->error_number,
                                                  AXIS2_ERROR_GET_MESSAGE(env->error));
                       return AXIS2_FAILURE;
                       
                   }
                   <xsl:text>*ptr_param_</xsl:text><xsl:value-of select="$CName"/> = <xsl:text>param_</xsl:text><xsl:value-of select="$CName"/>;
                   axutil_array_list_add(<xsl:value-of select="$attriName"/> , env, <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:when>
                  <xsl:otherwise>
                   axutil_array_list_add(<xsl:value-of select="$attriName"/> , env, <xsl:text>param_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:otherwise>
                </xsl:choose>
                return AXIS2_SUCCESS;
             }

            /**
             * Get the size of the <xsl:value-of select="$propertyName"/> array.
             */
            int AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_sizeof_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env)
            {
                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                if(<xsl:value-of select="$attriName"/> == NULL)
                {
                    return 0;
                }
                return axutil_array_list_size(<xsl:value-of select="$attriName"/>, env);
            }

            /**
             * resetter for <xsl:value-of select="$propertyName"/>
             */
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env)
             {
                int i = 0;
                int count = 0;
                void *element = NULL;

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                if(!<xsl:value-of select="$name"/>)
                {
                    return AXIS2_FAILURE;
                }
                if(<xsl:value-of select="$attriName"/> != NULL)
                {
                    count = axutil_array_list_size( <xsl:value-of select="$attriName"/>, env);
                    for( i = 0; i &lt; count; i ++)
                    {
                       element = axutil_array_list_get(<xsl:value-of select="$attriName"/>, env, i);
                    }
                 <xsl:choose>
                   <xsl:when test="@ours">
                      adb_<xsl:value-of select="@type"/>_free( element, env);
                   </xsl:when>

                   <xsl:when test="$nativePropertyType='short' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='long'">
                     <xsl:if test="@isarray">
                      <!-- free ints, longs, float in array-->
                      /** we keep primtives as pointers in arrasy, so need to free them */
                      AXIS2_FREE( env-> allocator, element);
                     </xsl:if>
                   </xsl:when>

                   <!-- free axis2_char_t s -->
                   <xsl:when test="$nativePropertyType='axis2_char_t*' and not(@isarray)">
                      AXIS2_FREE( env-> allocator, element);
                   </xsl:when>

                   <!-- free nodes -->
                   <xsl:when test="$nativePropertyType='axiom_node_t*'">
                    axiom_node_free_tree ( element, env );
                   </xsl:when>

                   <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                    axutil_qname_free( element, env );
                   </xsl:when>

                   <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                    axutil_uri_free( element, env );
                   </xsl:when>

                   <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                    axutil_date_time_free( element, env );
                   </xsl:when>

                   <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                    axutil_base64_binary_free ( element, env );
                   </xsl:when>
                     <xsl:when test="$propertyType='axutil_duration_t*'">
                    axutil_duration_free ( element, env );
                   </xsl:when>

                   <!--TODO: This should be extended for all the types that should be freed.. -->
                   <xsl:otherwise>
                     /** This is an unknown type or a primitive. Please free this manually*/
                   </xsl:otherwise>
                 </xsl:choose>

                 <xsl:value-of select="$attriName"/> = NULL;
                }
                return AXIS2_SUCCESS;
             }

            </xsl:if>
        </xsl:for-each>

    </xsl:template>

</xsl:stylesheet>
