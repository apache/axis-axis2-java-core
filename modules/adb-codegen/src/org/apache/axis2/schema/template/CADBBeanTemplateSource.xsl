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
        <xsl:variable name="ordered"><xsl:value-of select="@ordered"/></xsl:variable>
        <xsl:variable name="particleClass"><xsl:value-of select="@particleClass"/></xsl:variable> <!-- particle classes are used to represent schema groups -->
        <xsl:variable name="hasParticleType"><xsl:value-of select="@hasParticleType"/></xsl:variable> <!-- particle classes are used to represent schema groups -->
       
        /**
         * <xsl:value-of select="$axis2_name"/>.c
         *
         * This file was auto-generated from WSDL
         * by the Apache Axis2/C version: #axisVersion# #today#
         */

        #include "<xsl:value-of select="$axis2_name"/>.h"
        <xsl:choose>
            <xsl:when test="$istype">
                /*
                 * This type was generated from the piece of schema that had
                 * name = <xsl:value-of select="$originalName"/>
                 * Namespace URI = <xsl:value-of select="$nsuri"/>
                 * Namespace Prefix = <xsl:value-of select="$nsprefix"/>
                 */
           </xsl:when>
           <xsl:otherwise>
               /*
                * implmentation of the <xsl:value-of select="$originalName"/><xsl:if test="$nsuri">|<xsl:value-of select="$nsuri"/></xsl:if> element
                */
           </xsl:otherwise>
        </xsl:choose>


        struct <xsl:value-of select="$axis2_name"/>
        {
            <xsl:if test="not($istype)">
                axutil_qname_t* qname;
            </xsl:if>

            <xsl:for-each select="property">
                <xsl:variable name="propertyType">
                   <xsl:choose>
                     <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                     <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                     <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                     <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                   </xsl:choose>
                </xsl:variable>
                <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
                <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

                <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text>property_<xsl:value-of select="$CName"/>;

                <!-- For arrays is_valid_* tracks for whether at least one element of the array is non-NULL -->
                <xsl:text>axis2_bool_t is_valid_</xsl:text><xsl:value-of select="$CName"/>;

            </xsl:for-each>
        };


       /************************* Private Function prototypes ********************************/
        <xsl:for-each select="property">
            <xsl:variable name="propertyType">
            <xsl:choose>
                <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
            </xsl:choose>
            </xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

            <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
               <xsl:choose>
                 <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                 <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                 <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
              <xsl:variable name="PropertyTypeArrayParam"> <!--these are used in arrays to take the type stored in the arraylist-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                   <xsl:when test="@type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='long'"><xsl:value-of select="@type"/><xsl:text>*</xsl:text></xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
            <xsl:variable name="paramComment">
                <xsl:choose>
                    <xsl:when test="@isarray"><xsl:text>Array of </xsl:text><xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text>s.</xsl:text></xsl:when>
                    <xsl:otherwise><xsl:value-of select="$nativePropertyType"/></xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:if test="not(@nillable or @optional)">
                axis2_status_t AXIS2_CALL
                <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_nil_at(
                        <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>, 
                        const axutil_env_t *env, int i);

                axis2_status_t AXIS2_CALL
                <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_nil(
                        <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                        const axutil_env_t *env);
            </xsl:if>

          </xsl:for-each>


       /************************* Function Implmentations ********************************/
        <xsl:value-of select="$axis2_name"/>_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_create(
            const axutil_env_t *env)
        {
            <xsl:value-of select="$axis2_name"/>_t *<xsl:value-of select="$name"/> = NULL;
            <xsl:if test="not($istype)">
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
                  <xsl:when test="@ours or @type='axis2_char_t*' or @type='axutil_qname_t*' or @type='axutil_duration_t*' or @type='axutil_uri_t*' or @type='axutil_date_time_t*' or @type='axutil_base64_binary_t*'">
                    <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>  = NULL;
                  </xsl:when>
                  <!-- todo for others -->
                </xsl:choose>
                <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/>  = AXIS2_FALSE;
            </xsl:for-each>

            <xsl:if test="not($istype)">
              <xsl:choose>
                <xsl:when test="$nsuri and $nsuri != ''">
                  qname =  axutil_qname_create (env,
                        "<xsl:value-of select="$originalName"/>",
                        "<xsl:value-of select="$nsuri"/>",
                        NULL);
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
            <xsl:if test="property/@isarray">
                int i = 0;
                int count = 0;
                void *element = NULL;
            </xsl:if>

            AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
            AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);

            <xsl:for-each select="property">
                <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
                <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env);
            </xsl:for-each>

            <xsl:if test="not(@type)">
              if(<xsl:value-of select="$name"/>->qname)
              {
                  axutil_qname_free (<xsl:value-of select="$name"/>->qname, env);
                  <xsl:value-of select="$name"/>->qname = NULL;
              }
            </xsl:if>

            if(<xsl:value-of select="$name"/>)
            {
                AXIS2_FREE(env->allocator, <xsl:value-of select="$name"/>);
                <xsl:value-of select="$name"/> = NULL;
            }
            return AXIS2_SUCCESS;
        }


        <xsl:if test="@simple">
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_deserialize_from_string(
                            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                                            const axutil_env_t *env,
                                            axis2_char_t *node_value,
                                            axiom_node_t *parent)
            {
              axis2_status_t status = AXIS2_SUCCESS;
            <xsl:if test="property/@type='axutil_date_time_t*' or property/@type='axutil_base64_binary_t*'">
              void *element = NULL;
            </xsl:if>
            <xsl:if test="property/@type='axutil_qname_t*'">
              axis2_char_t *cp = NULL;
              axis2_bool_t prefix_found = AXIS2_FALSE;
              axiom_namespace_t *qname_ns;
            </xsl:if>
              <xsl:for-each select="property"> <!-- only one property would be in a simpletype -->
                <xsl:variable name="propertyType">
                   <xsl:choose>
                     <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                     <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                     <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                     <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                   </xsl:choose>
                </xsl:variable>
                <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                   <xsl:choose>
                     <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                     <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                     <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                   </xsl:choose>
                </xsl:variable>
                <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
                <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
  
                <!-- here only simple types possible -->
                <xsl:choose>
                  <!-- add int s -->
                  <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, atoi(node_value));
                  </xsl:when>

                  <!-- add axis2_char_t s -->
                  <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, (char)(*node_value)); <!-- This should be checked -->
                  </xsl:when>

                  <!-- add short s -->
                  <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, atoi(node_value));
                  </xsl:when>

                  <!-- add long s -->
                  <xsl:when test="$nativePropertyType='long' or $nativePropertyType='unsigned long'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, atol(node_value));
                  </xsl:when>

                  <!-- add float s -->
                  <xsl:when test="$nativePropertyType='float'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, atof(node_value));
                  </xsl:when>
                  <!-- add double s -->
                  <xsl:when test="$nativePropertyType='double'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, atof(node_value));
                  </xsl:when>

                  <!-- add axis2_char_t s -->
                  <xsl:when test="$nativePropertyType='axis2_char_t*'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, node_value);
                  </xsl:when>

                  <!-- add axutil_qname_t s -->
                  <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                    prefix_found = AXIS2_FALSE;
                    for(cp = node_value; *cp; cp ++)
                    {
                        if(*cp == ':')
                        {
                            *cp = '\0';
                            cp ++;
                            prefix_found  = AXIS2_TRUE;
                            break;
                        }
                    }

                    if(prefix_found)
                    {
                        /* node value contain the prefix */
                        qname_ns = axiom_element_find_namespace_uri(axiom_node_get_data_element(parent, env), env, node_value, parent);
                    }
                    else
                    {
                        /* Then it is the default namespace */
                        cp = node_value;
                        qname_ns = axiom_element_get_default_namespace(axiom_node_get_data_element(parent, env), env, parent);
                    }

                     <!-- we are done extracting info, just set the extracted value to the qname -->

                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env,
                                                    axutil_qname_create(
                                                          env, 
                                                          cp, /* cp contain the localname */
                                                          axiom_namespace_get_uri(qname_ns, env),
                                                          axiom_namespace_get_prefix(qname_ns, env)));
                  </xsl:when>

                  <!-- add axutil_uri_t s -->
                  <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, axutil_uri_parse_string(env, node_value));
                  </xsl:when>

                  <!-- add axutil_duration_t s -->
                  <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, axutil_duration_create_from_string(env, node_value));
                  </xsl:when>

                  <!-- add axis2_bool_t s -->
                  <xsl:when test="$nativePropertyType='axis2_bool_t'">
                     if (!axutil_strcmp(node_value, "TRUE") || !axutil_strcmp(node_value, "true"))
                     {
                         <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, AXIS2_TRUE);
                     }
                     else
                     {
                         <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, AXIS2_FALSE);
                     }
                  </xsl:when>
                  <!-- add axis2_byte_t s -->
                  <xsl:when test="$nativePropertyType='axis2_byte_t'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, atoi(node_value));
                  </xsl:when>
                  <!-- add date_time_t* s -->
                  <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                     element = (void*)axutil_date_time_create(env);
                     axutil_date_time_deserialize_date_time((axutil_date_time_t*)element, env,
                                                                node_value);
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, (<xsl:value-of select="$nativePropertyType"/>)element);
                  </xsl:when>
                  <!-- add hex_binary_t* s -->
                  <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                     element = (void*)axutil_base64_binary_create(env);
                     axutil_base64_binary_set_encoded_binary((<xsl:value-of select="$nativePropertyType"/>)element, env,
                                                                node_value);
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, (<xsl:value-of select="$nativePropertyType"/>)element);
                  </xsl:when>
                  <xsl:when test="@ours">
                     <!-- It seems this is in an unreachable path -->
                  </xsl:when>
                  <xsl:otherwise>
                     <!--TODO: add new attributes types -->
                     /* can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
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
                axiom_node_t **dp_parent)
        {
          axiom_node_t *parent = *dp_parent;
          
          axis2_status_t status = AXIS2_SUCCESS;
          <xsl:if test="count(property[@attribute])!=0">
              axiom_attribute_t *parent_attri = NULL;
              axiom_element_t *parent_element = NULL;
              axis2_char_t *attrib_text = NULL;

              axutil_hash_t *attribute_hash = NULL;

          </xsl:if>
          <xsl:if test="property/@ours or (property/@isarray and (property/@type='unsigned short' or property/@type='unsigned char' or property/@type='unsigned int' or property/@type='unsigned long' or property/@type='short' or property/@type='int' or property/@type='char' or property/@type='long' or property/@type='float' or property/@type='axis2_byte_t' or property/@type='axis2_bool_t' or property/@type='double')) or property/@type='axutil_date_time_t*' or property/@type='axutil_base64_binary_t*'">
              void *element = NULL;
          </xsl:if>

          <!-- these two are requried -->
          <xsl:if test="count(property)!=0"> <!-- check for at least one element exists -->
             axis2_char_t* text_value = NULL;
             axutil_qname_t *qname = NULL;
          </xsl:if>

          <!-- qname specifc values -->
            <xsl:if test="property/@type='axutil_qname_t*'">
              axis2_char_t *cp = NULL;
              axis2_bool_t prefix_found = AXIS2_FALSE;
              axiom_namespace_t *qname_ns;
            </xsl:if>
          <xsl:choose>
            <xsl:when test="@simple and count(property)!=0">
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
                        status = <xsl:value-of select="$axis2_name"/>_deserialize_from_string(<xsl:value-of select="$name"/>, env, text_value, parent);
                    }
                }
            }
            </xsl:when>
            <xsl:otherwise>

            <xsl:if test="property/@isarray">
               int i = 0;
               int element_found = 0;
               axutil_array_list_t *arr_list = NULL;
            </xsl:if>
            <xsl:if test="@ordered and property/@isarray">
               int sequence_broken = 0;
               axiom_node_t *tmp_node = NULL;
            </xsl:if>
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
            <xsl:if test="count(property)!=0">
               axiom_node_t *first_node = NULL;
               axis2_bool_t is_early_node_valid = AXIS2_TRUE;
               axiom_node_t *current_node = NULL;
               axiom_element_t *current_element = NULL;
            </xsl:if>
            AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
            AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);

            <xsl:if test="property">
              <!-- We are expected to have NULL elements in particle classes -->
              <xsl:if test="not($particleClass)">
              <!-- Wait until AXIOM_ELEMENT -->
              while(parent &amp;&amp; axiom_node_get_node_type(parent, env) != AXIOM_ELEMENT)
              {
                  parent = axiom_node_get_next_sibling(parent, env);
              }
              if (NULL == parent)
              {
                /* This should be checked before everything */
                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, 
                            "Failed in building adb object for <xsl:value-of select="$originalName"/> : "
                            "NULL elemenet can not be passed to deserialize");
                return AXIS2_FAILURE;
              }
              </xsl:if>
            </xsl:if>
            <xsl:for-each select="property">
              <xsl:if test="position()=1"> <!-- check for at least one element exists -->
                 <xsl:choose>
                    <xsl:when test="not($istype)">

                    current_element = (axiom_element_t *)axiom_node_get_data_element(parent, env);
                    qname = axiom_element_get_qname(current_element, env, parent);
                    if (axutil_qname_equals(qname, env, <xsl:value-of select="$name"/>-> qname)<xsl:if test="not($nsuri) or $nsuri=''"> || !axutil_strcmp("<xsl:value-of select="$originalName"/>", axiom_element_get_localname(current_element, env))</xsl:if>)
                    {
                        <xsl:choose>
                          <xsl:when test="$anon">
                          first_node = axiom_node_get_first_child(parent, env);
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
                              "Expected %s but returned %s", 
                              axutil_qname_to_string(qname, env),
                              axutil_qname_to_string(<xsl:value-of select="$name"/>-> qname, env));
                        <!-- TODO: ADB specific error should be defined and set here -->
                        return AXIS2_FAILURE;
                    }
                    </xsl:when>
                    <xsl:when test="$particleClass">
                         first_node = parent;
                    </xsl:when>
                    <xsl:otherwise>
                      <!-- for types, parent refers to the container element -->
                      first_node = axiom_node_get_first_child(parent, env);
                      <!-- Let followers to check the situation
                      if(first_node == NULL)
                      {
                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI,
                                            "Failed in building adb object for <xsl:value-of select="$originalName"/> : "
                                            "It is expected to have a child element");
                          TODO: ADB specific error should be defined and set here 
                          return AXIS2_FAILURE; 
                      } 
                      -->
                    </xsl:otherwise>
                 </xsl:choose>
               </xsl:if>
            </xsl:for-each>
            
            </xsl:otherwise> <!--otherwise for @simple check -->
          </xsl:choose>

          <!-- attributes are common to simple types(when used in simple content) and other types -->
            <xsl:for-each select="property/@attribute">
              <xsl:if test="position()=1">
                 parent_element = (axiom_element_t *)axiom_node_get_data_element(parent, env);
                 attribute_hash = axiom_element_get_all_attributes(parent_element, env);
              </xsl:if>
            </xsl:for-each>

            <xsl:for-each select="property">
              <xsl:variable name="propertyType">
                 <xsl:choose>
                   <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
              <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

              <xsl:variable name="propertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="justPropertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:choose>
                <xsl:when test="@attribute">
                <!-- Just waiting for fix the axiom_element_get_attribute 
                  <xsl:choose>
                    <xsl:when test="@nsuri and @nsuri != ''">
                      qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>", NULL);
                    </xsl:when>
                    <xsl:otherwise>
                      qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                    </xsl:otherwise>
                  </xsl:choose>

                  parent_attri = axiom_element_get_attribute(parent_element, env, qname);
                  if(parent_attri != NULL)
                  {
                    attrib_text = axiom_attribute_get_value(parent_attri, env);
                  }
                  else
                  {
                    attrib_text = axiom_element_get_attribute_value_by_name(parent_element, env, "<xsl:value-of select="$propertyName"/>");
                  }
                  if(qname)
                  {
                     axutil_qname_free(qname, env);
                  } -->
                
                  parent_attri = NULL;
                  attrib_text = NULL;
                  if(attribute_hash)
                  {
                       axutil_hash_index_t *hi;
                       void *val;
                       const void *key;

                       for (hi = axutil_hash_first(attribute_hash, env); hi; hi = axutil_hash_next(env, hi)) 
                       {
                           axutil_hash_this(hi, &amp;key, NULL, &amp;val);
                           
                           <xsl:choose>
                             <xsl:when test="@nsuri and @nsuri != ''">
                               if(strstr((axis2_char_t*)key, "<xsl:value-of select="$propertyName"/>|<xsl:value-of select="@nsuri"/>"))
                             </xsl:when>
                             <xsl:otherwise>
                               if(!strcmp((axis2_char_t*)key, "<xsl:value-of select="$propertyName"/>"))
                             </xsl:otherwise>
                           </xsl:choose>
                               {
                                   parent_attri = (axiom_attribute_t*)val;
                                   break;
                               }
                       }
                  }

                  if(parent_attri)
                  {
                    attrib_text = axiom_attribute_get_value(parent_attri, env);
                  }
                  else
                  {
                    /* this is hoping that attribute is stored in "<xsl:value-of select="$propertyName"/>", this happnes when name is in default namespace */
                    attrib_text = axiom_element_get_attribute_value_by_name(parent_element, env, "<xsl:value-of select="$propertyName"/>");
                  }

                  if(attrib_text != NULL)
                  {
                      <!-- here only simple type possible -->
                      <xsl:choose>
                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, atoi(attrib_text));
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, (char)(*attrib_text));
                        </xsl:when>

                        <!-- add short s -->
                        <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, atoi(attrib_text));
                        </xsl:when>

                        <!-- add long s -->
                        <xsl:when test="$nativePropertyType='long' or $nativePropertyType='unsigned long'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, atol(attrib_text));
                        </xsl:when>

                        <!-- add float s -->
                        <xsl:when test="$nativePropertyType='float'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, atof(attrib_text));
                        </xsl:when>
                        <!-- add double s -->
                        <xsl:when test="$nativePropertyType='double'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, atof(attrib_text));
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, attrib_text);
                        </xsl:when>

                        <!-- add axutil_qname_t s -->
                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                          prefix_found = AXIS2_FALSE;
                          for(cp = attrib_text; *cp; cp ++)
                          {
                              if(*cp == ':')
                              {
                                  *cp = '\0';
                                  cp ++;
                                  prefix_found  = AXIS2_TRUE;
                                  break;
                              }
                          }
                       
                          if(prefix_found)
                          {
                              /* node value contain  the prefix */
                              qname_ns = axiom_element_find_namespace_uri(axiom_node_get_data_element(parent, env), env, attrib_text, parent);
                          }
                          else
                          {
                              /* Then it is the default namespace */
                              cp = attrib_text;
                              qname_ns = axiom_element_get_default_namespace(axiom_node_get_data_element(parent, env), env, parent);
                          }
                       
                          <!-- we are done extracting info, just set the extracted value to the qname -->
                       
                          <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env,
                                                    axutil_qname_create(
                                                          env, 
                                                          cp, /* cp contain the localname */
                                                          axiom_namespace_get_uri(qname_ns, env),
                                                          axiom_namespace_get_prefix(qname_ns, env)));
                        </xsl:when>

                        <!-- add axutil_uri_t s -->
                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, axutil_uri_parse_string(env, attrib_text));
                        </xsl:when>

                        <!-- add axutil_duration_t s -->
                        <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, axutil_duration_create_from_string(env, attrib_text));
                        </xsl:when>
                        <!-- add axis2_bool_t s -->
                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                           if (!axutil_strcmp(attrib_text, "TRUE") || !axutil_strcmp(attrib_text, "true"))
                           {
                               <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, AXIS2_TRUE);
                           }
                           else
                           {
                               <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, AXIS2_FALSE);
                           }
                        </xsl:when>
                        <xsl:when test="$nativePropertyType='axis2_byte_t'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, atoi(attrib_text));
                        </xsl:when>
                        <!-- add date_time_t* s -->
                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                           element = (void*)axutil_date_time_create(env);
                           axutil_date_time_deserialize_date_time((axutil_date_time_t*)element, env,
                                                                      attrib_text);
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, (<xsl:value-of select="$nativePropertyType"/>)element);
                        </xsl:when>
                        <!-- add hex_binary_t* s -->
                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                           element = (void*)axutil_base64_binary_create(env);
                           axutil_base64_binary_set_encoded_binary((<xsl:value-of select="$nativePropertyType"/>)element), env,
                                                                      attrib_text);
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, (<xsl:value-of select="$nativePropertyType"/>)element);
                        </xsl:when>
                        <xsl:when test="@ours">
                            element =  (void*)adb_<xsl:value-of select="@type"/>_create(env);
                            adb_<xsl:value-of select="@type"/>_deserialize_from_string((<xsl:value-of select="$nativePropertyType"/>)element, env, attrib_text, parent);
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, (<xsl:value-of select="$nativePropertyType"/>)element);
                        </xsl:when>
                        <xsl:otherwise>
                          <!--TODO: add new attributes types -->
                          /* can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
                        </xsl:otherwise>
                      </xsl:choose>
                    }
                    <xsl:if test="not(@optional)">
                    else
                    {
                        /* This is not a nillable attribute*/
                        AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "non optional attribute <xsl:value-of select="$propertyName"/> missing");
                        return AXIS2_FAILURE;
                    }
                    </xsl:if>
                </xsl:when>
                <xsl:when test="../@simple"></xsl:when> <!-- just to avoid preceeding code to be parsed in a simple type -->
                <xsl:otherwise> <!-- when it is an element not(@attribute) -->
                  <!-- handles arrays -->
                   <xsl:if test="@isarray">
                    /*
                     * building <xsl:value-of select="$CName"/> array
                     */
                       arr_list = axutil_array_list_create(env, 10);
                   </xsl:if>

                     <!-- for each non attribute properties there will always be an element-->
                     /*
                      * building <xsl:value-of select="$propertyName"/> element
                      */
                     <!-- array and non array build is so different so big choose, when is requried-->
                     <!-- the method of picking the element is depend on the ../@ordered -->
                     <xsl:choose>
                       <xsl:when test="not(@isarray)">  <!--not an array so continue normal -->
                           <xsl:choose>
                             <xsl:when test="$ordered or not($anon or $istype)"> <!-- since non-anon has just only one sub element-->
                               <xsl:choose>
                                 <xsl:when test="position()=1">
                                   current_node = first_node;
                                   <!-- Wait until AXIOM_ELEMENT -->
                                   <xsl:if test="not(@any)">
                                    while(current_node &amp;&amp; axiom_node_get_node_type(current_node, env) != AXIOM_ELEMENT)
                                    {
                                        current_node = axiom_node_get_next_sibling(current_node, env);
                                    }
                                    if(current_node != NULL)
                                    {
                                        current_element = (axiom_element_t *)axiom_node_get_data_element(current_node, env);
                                        qname = axiom_element_get_qname(current_element, env, current_node);
                                    }
                                   </xsl:if>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    /*
                                     * because elements are ordered this works fine
                                     */
                                  
                                   <!-- current node should contain the ordered value -->
                                   if(current_node != NULL &amp;&amp; is_early_node_valid)
                                   {
                                       current_node = axiom_node_get_next_sibling(current_node, env);
                                       <!-- Wait until AXIOM_ELEMENT -->
                                       <xsl:if test="not(@any)">
                                        while(current_node &amp;&amp; axiom_node_get_node_type(current_node, env) != AXIOM_ELEMENT)
                                        {
                                            current_node = axiom_node_get_next_sibling(current_node, env);
                                        }
                                        if(current_node != NULL)
                                        {
                                            current_element = (axiom_element_t *)axiom_node_get_data_element(current_node, env);
                                            qname = axiom_element_get_qname(current_element, env, current_node);
                                        }
                                       </xsl:if>
                                   }
                                   is_early_node_valid = AXIS2_FALSE;
                                 </xsl:otherwise>
                               </xsl:choose> <!-- close for position -1 -->

                               <xsl:choose>
                                 <xsl:when test="@any"></xsl:when>
                                 <xsl:when test="@nsuri and @nsuri != ''">
                                 element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>", NULL);
                                 </xsl:when>
                                 <xsl:otherwise>
                                 element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                                 </xsl:otherwise>
                               </xsl:choose>

                             </xsl:when>
                             <xsl:otherwise> <!-- otherwise for ($ordered), -->
                               /*
                                * because elements are not ordered we should surf all the sibling to pick the right one
                                */
                               for (current_node = first_node; current_node != NULL;
                                             current_node = axiom_node_get_next_sibling(current_node, env))
                               {
                                  if(axiom_node_get_node_type(current_node, env) != AXIOM_ELEMENT)
                                  {
                                     continue;
                                  }
                                  
                                  current_element = (axiom_element_t *)axiom_node_get_data_element(current_node, env);
                                  qname = axiom_element_get_qname(current_element, env, current_node);
                                <xsl:choose>
                                  <xsl:when test="@nsuri and @nsuri != ''">
                                  element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>", NULL);
                                  </xsl:when>
                                  <xsl:otherwise>
                                  element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                                  </xsl:otherwise>
                                </xsl:choose>
                                  if (axutil_qname_equals(element_qname, env, qname)<xsl:if test="not(@nsuri) or @nsuri=''"> || !axutil_strcmp("<xsl:value-of select="$propertyName"/>", axiom_element_get_localname(current_element, env))</xsl:if>)
                                  {
                                       /* found the requried element */
                                       break;
                                  }
                               }
                             </xsl:otherwise> <!-- close for ../@ordered or not($anon or $istype) -->
                           </xsl:choose>

                           if (<xsl:if test="@ours">adb_<xsl:value-of select="@type"/>_is_particle() || </xsl:if> <!-- is particle test should be done here -->
                                (current_node <xsl:if test="not(@any)">  &amp;&amp; current_element &amp;&amp; (axutil_qname_equals(element_qname, env, qname)<xsl:if test="not(@nsuri) or @nsuri=''"> || !axutil_strcmp("<xsl:value-of select="$propertyName"/>", axiom_element_get_localname(current_element, env))</xsl:if>)</xsl:if>))
                           {
                              is_early_node_valid = AXIS2_TRUE;
                              <!-- changes to following choose tag should be changed in another 2 places -->
                                 <xsl:choose>
                                    <xsl:when test="@ours">
                                      element = (void*)adb_<xsl:value-of select="@type"/>_create(env);

                                      status =  adb_<xsl:value-of select="@type"/>_deserialize((<xsl:value-of select="$nativePropertyType"/>)element,
                                                                            env, &amp;current_node);
                                      if(AXIS2_FAILURE == status)
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building adb object for element <xsl:value-of select="$propertyName"/>");
                                          return AXIS2_FAILURE;
                                      }
                                      status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   (<xsl:value-of select="$nativePropertyType"/>)element);
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axis2_char_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                               text_value);
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                            AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                            status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                          status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                             axutil_uri_parse_string(env, text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if> 
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                          status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                             axutil_duration_create_from_string(env, text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if> 
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            prefix_found = AXIS2_FALSE;
                                            for(cp = text_value; *cp; cp ++)
                                            {
                                                if(*cp == ':')
                                                {
                                                    *cp = '\0';
                                                    cp ++;
                                                    prefix_found  = AXIS2_TRUE;
                                                    break;
                                                }
                                            }
                                          
                                            if(prefix_found)
                                            {
                                                /* node value contain  the prefix */
                                                qname_ns = axiom_element_find_namespace_uri(current_element, env, text_value, current_node);
                                            }
                                            else
                                            {
                                                /* Then it is the default namespace */
                                                cp = text_value;
                                                qname_ns = axiom_element_get_default_namespace(current_element, env, current_node);
                                            }
                                          
                                            <!-- we are done extracting info, just set the extracted value to the qname -->
                                           
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env,
                                                    axutil_qname_create(
                                                          env, 
                                                          cp, /* cp contain the localname */
                                                          axiom_namespace_get_uri(qname_ns, env),
                                                          axiom_namespace_get_prefix(qname_ns, env)));
                                       }
                                       <xsl:if test="not(@nillable)">
                                         else
                                         {
                                             AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                             status = AXIS2_FAILURE;
                                         }
                                       </xsl:if>

                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                          status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                             (char)(*text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if> 

                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   atoi(text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if> 
                                   </xsl:when>
                                    <xsl:when test="$nativePropertyType='axis2_byte_t'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   atoi(text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if> 

                                   </xsl:when>
                                    <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   atoi(text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if> 

                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='float'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   atof(text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>

                                     </xsl:when>
                                    <xsl:when test="$nativePropertyType='double'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   atof(text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>

                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='long' or $nativePropertyType='unsigned long'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   atol(text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axiom_node_t*'">
                                      text_value = NULL; /* just to avoid warning */
                                      <xsl:choose>
                                        <xsl:when test="@any">
                                        {
                                          axiom_node_t *current_property_node = current_node;
                                          current_node = axiom_node_get_next_sibling(current_node, env);
                                          axiom_node_detach(current_property_node, env);
                                          status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                          current_property_node);
                                        }
                                        </xsl:when>
                                        <xsl:otherwise>
                                          if(axiom_node_get_first_child(current_node, env))
                                          {
                                              axiom_node_t *current_property_node = axiom_node_get_first_child(current_node, env);
                                              axiom_node_detach(current_property_node, env);
                                              status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                          current_property_node);
                                          }
                                          else
                                          {
                                              status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                          NULL);
                                          }
                                        </xsl:otherwise>
                                      </xsl:choose>

                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axis2_bool_t'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            if (!axutil_strcasecmp(text_value , "true"))
                                            {
                                                status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                 AXIS2_TRUE);
                                            }
                                            else
                                            {
                                                status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                      AXIS2_FALSE);
                                            }
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                          element = (void*)axutil_date_time_create(env);
                                          status = axutil_date_time_deserialize_date_time((axutil_date_time_t*)element, env,
                                                                          text_value);
                                          if(AXIS2_FAILURE ==  status)
                                          {
                                              if(element != NULL)
                                              {
                                                  axutil_date_time_free(element, env);
                                              }
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> ");
                                              return AXIS2_FAILURE;
                                          }
                                          status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                       (<xsl:value-of select="$nativePropertyType"/>)element);
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>

                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                          element = (void*)axutil_base64_binary_create(env);
                                          status = axutil_base64_binary_set_encoded_binary((axutil_base64_binary_t*)element, env,
                                                                          text_value);
                                          if(AXIS2_FAILURE ==  status)
                                          {
                                              if(element != NULL)
                                              {
                                                 axutil_base64_binary_free((axutil_base64_binary_t*)element, env);
                                              }
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> ");
                                              return AXIS2_FAILURE;
                                          }
                                          status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                       (<xsl:value-of select="$nativePropertyType"/>)element);
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>

                                    </xsl:when>
                                    <xsl:otherwise>
                                      <!-- TODO: add other types here -->
                                      /* Imposible to handle the request type - so please do it manually */
                                      text_value = NULL;
                                    </xsl:otherwise>
                                 </xsl:choose>
                                 if(AXIS2_FAILURE ==  status)
                                 {
                                     AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in setting the value for <xsl:value-of select="$propertyName"/> ");
                                     return AXIS2_FAILURE;
                                 }
                              }
                           <xsl:if test="not(@minOccurs=0)">
                              else
                              {
                                  if(element_qname)
                                  {
                                      axutil_qname_free(element_qname, env);
                                  }
                                  /* this is not a nillable element*/
                                  AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "non nillable or minOuccrs != 0 element <xsl:value-of select="$propertyName"/> missing");
                                  return AXIS2_FAILURE;
                              }
                           </xsl:if>
                        </xsl:when>
                        <xsl:otherwise> <!-- when it is all the way an array -->
                           <xsl:if test="@any">
                            /* 'any' arrays are not handling correctly when there are other elements mixed with the 'any' element. */
                           </xsl:if>
                           <xsl:choose>
                             <xsl:when test="../@ordered or not($anon or $istype)"> <!-- all the elements should follow this -->
                                <xsl:choose>
                                  <xsl:when test="@any"></xsl:when>
                                  <xsl:when test="@nsuri and @nsuri != ''">
                                    element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>", NULL);
                                  </xsl:when>
                                  <xsl:otherwise>
                                    element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                                  </xsl:otherwise>
                                </xsl:choose>
                               for (i = 0, sequence_broken = 0, tmp_node = current_node = <xsl:choose>
                                             <xsl:when test="position()=1">first_node</xsl:when>
                                             <xsl:otherwise>axiom_node_get_next_sibling(current_node, env)</xsl:otherwise></xsl:choose>; current_node != NULL; <xsl:if test="not(@any)">current_node = axiom_node_get_next_sibling(current_node, env)</xsl:if>) 
                                             <!-- We are not moving current_node to next sibling here if it an any type, because we already have done the move -->
                               {
                                  if(axiom_node_get_node_type(current_node, env) != AXIOM_ELEMENT)
                                  {
                                     continue;
                                  }
                                  <xsl:if test="not(@any)">
                                  current_element = (axiom_element_t *)axiom_node_get_data_element(current_node, env);
                                  qname = axiom_element_get_qname(current_element, env, current_node);

                                  if (axutil_qname_equals(element_qname, env, qname)<xsl:if test="not(@nsuri) or @nsuri=''"> || !axutil_strcmp("<xsl:value-of select="$propertyName"/>", axiom_element_get_localname(current_element, env))</xsl:if>)
                                  {
                                  </xsl:if>
                                      is_early_node_valid = AXIS2_TRUE;
                                      <xsl:if test="not(@any)">
                                      if (sequence_broken)
                                      {
                                        /* found element out of order */
                                        AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "elements found out of order for array<xsl:value-of select="$propertyName"/> missing");
                                        return AXIS2_FAILURE;
                                      }
                                      </xsl:if>
                                      tmp_node = current_node; /* always update the current node */
                                      element_found = 1;
                                      <!-- changes to following choose tag should be changed in another 2 places -->
                                     <xsl:choose>
                                        <xsl:when test="@ours">
                                          element = (void*)adb_<xsl:value-of select="@type"/>_create(env);
                                          
                                          status =  adb_<xsl:value-of select="@type"/>_deserialize((<xsl:value-of select="$nativePropertyType"/>)element, env,
                                                                                 &amp;current_node);
                                          
                                          if(AXIS2_FAILURE ==  status)
                                          {
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> ");
                                              return AXIS2_FAILURE;
                                          }
                                          axutil_array_list_add_at(arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              axutil_array_list_add_at(arr_list, env, i, (void*)text_value);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              prefix_found = AXIS2_FALSE;
                                              for(cp = text_value; *cp; cp ++)
                                              {
                                                  if(*cp == ':')
                                                  {
                                                      *cp = '\0';
                                                      cp ++;
                                                      prefix_found  = AXIS2_TRUE;
                                                      break;
                                                  }
                                              }
                                              
                                              if(prefix_found)
                                              {
                                                  /* node value contain  the prefix */
                                                  qname_ns = axiom_element_find_namespace_uri(current_element, env, text_value, current_node);
                                              }
                                              else
                                              {
                                                  /* Then it is the default namespace */
                                                  cp = text_value;
                                                  qname_ns = axiom_element_get_default_namespace(current_element, env, current_node);
                                              }
                                              
                                              <!-- we are done extracting info, just set the extracted value to the qname -->
                                              
                                              axutil_array_list_add_at(arr_list, env, i, (void*)
                                                      axutil_qname_create(
                                                            env, 
                                                            cp, /* cp contain the localname */
                                                            axiom_namespace_get_uri(qname_ns, env),
                                                            axiom_namespace_get_prefix(qname_ns, env)));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              axutil_array_list_add_at(arr_list, env, i, (void*)axutil_uri_parse_string(env, text_value));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                                axutil_array_list_add_at(arr_list, env, i, (void*)axutil_duration_create_from_string(env, text_value));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                               /* we keeps ints in arrays from their pointers */
                                               element = AXIS2_MALLOC(env-> allocator, 64);
                                               (*(<xsl:value-of select="$nativePropertyType"/>*)element) = (char)(*text_value);
                                               axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(int));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_byte_t'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(axis2_byte_t));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(short));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='float'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(float));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atof(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='double'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                               /* we keeps float in arrays from their pointers */
                                               element = AXIS2_MALLOC(env-> allocator, sizeof(double));
                                               (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atof(text_value);
                                               axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='long' or $nativePropertyType='unsigned long'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps long in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(long));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atol(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axiom_node_t*'">
                                          text_value = NULL; /* just to avoid warning */
                                          <xsl:choose>
                                            <xsl:when test="@any">
                                            {
                                              axiom_node_t *current_property_node = current_node;
                                              current_node = axiom_node_get_next_sibling(current_node, env);
                                              axiom_node_detach(current_property_node, env);
                                              axutil_array_list_add_at(arr_list, env, i, (void*)current_property_node);
                                            }
                                            </xsl:when>
                                            <xsl:otherwise>
                                              if(axiom_node_get_first_child(current_node, env))
                                              {
                                                  axiom_node_t *current_property_node = axiom_node_get_first_child(current_node, env);
                                                  axiom_node_detach(current_property_node, env);
                                                  axutil_array_list_add_at(arr_list, env, i, (void*)current_property_node);
                                              }
                                              else
                                              {
                                                  status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                              NULL);
                                              }
                                            </xsl:otherwise>
                                          </xsl:choose>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                               if (!axutil_strcasecmp (text_value , "true"))
                                               {
                                                  axutil_array_list_add_at(arr_list, env, i, (void*)AXIS2_TRUE);
                                               }
                                               else
                                               {
                                                  axutil_array_list_add_at(arr_list, env, i, (void*)AXIS2_FALSE);
                                               }
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              element = (void*)axutil_date_time_create(env);
                                              status = axutil_date_time_deserialize_date_time((axutil_date_time_t*)element, env,
                                                                              text_value);
                                              if(AXIS2_FAILURE ==  status)
                                              {
                                                  AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                      " %d :: %s", env->error->error_number,
                                                                      AXIS2_ERROR_GET_MESSAGE(env->error));
                                                  return AXIS2_FAILURE;
                                              }
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              element = (void*)axutil_base64_binary_create(env);
                                              status = axutil_base64_binary_set_encoded_binary((axutil_base64_binary_t*)element, env,
                                                                              text_value);
                                              if(AXIS2_FAILURE ==  status)
                                              {
                                                  AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                      " %d :: %s", env->error->error_number,
                                                                      AXIS2_ERROR_GET_MESSAGE(env->error));
                                                  return AXIS2_FAILURE;
                                              }
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:otherwise>
                                          <!-- TODO: add other types here -->
                                          /* imposible to handle the request type - so please do it manually */
                                          text_value = NULL;
                                        </xsl:otherwise>
                                     </xsl:choose>
                                     if(AXIS2_FAILURE ==  status)
                                     {
                                         AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in setting the value for <xsl:value-of select="$propertyName"/> "
                                                             " %d :: %s", env->error->error_number,
                                                             AXIS2_ERROR_GET_MESSAGE(env->error));
                                         return AXIS2_FAILURE;
                                     }

                                     i ++;
                                 <xsl:if test="not(@any)">
                                  }
                                  else
                                  {
                                      sequence_broken = 1;
                                  }
                                  </xsl:if>
                               }

                               current_node = tmp_node;
                               status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   arr_list);

                             </xsl:when>
                             <xsl:otherwise> <!-- otherwse for "../@ordered or not($anon or $istype)" -->
                                <xsl:choose>
                                  <xsl:when test="@any"></xsl:when>
                                  <xsl:when test="@nsuri and @nsuri != ''">
                                    element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>", NULL);
                                  </xsl:when>
                                  <xsl:otherwise>
                                    element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                                  </xsl:otherwise>
                                </xsl:choose>
                                /*
                                 * because elements are not ordered we should surf all the sibling to pick the right one
                                 */
                               for (i = 0, current_node = first_node; current_node != NULL; <xsl:if test="not(@any)">current_node = axiom_node_get_next_sibling(current_node, env)</xsl:if>)
                                             <!-- We are not moving current_node to next sibling here if it an any type, because we already have done the move -->
                               {
                                  if(axiom_node_get_node_type(current_node, env) != AXIOM_ELEMENT)
                                  {
                                     continue;
                                  }
                                  <xsl:if test="not(@any)">
                                  current_element = (axiom_element_t *)axiom_node_get_data_element(current_node, env);
                                  qname = axiom_element_get_qname(current_element, env, current_node);

                                  if (axutil_qname_equals(element_qname, env, qname)<xsl:if test="not(@nsuri) or @nsuri=''"> || !axutil_strcmp("<xsl:value-of select="$propertyName"/>", axiom_element_get_localname(current_element, env))</xsl:if>)
                                  {
                                  </xsl:if>
                                       /* found the requried element */
                                       element_found = 1;
                                      <!-- changes to following choose tag should be changed in another 2 places -->
                                     <xsl:choose>
                                        <xsl:when test="@ours">
                                          element = (void*)adb_<xsl:value-of select="@type"/>_create(env);
                                          
                                          status =  adb_<xsl:value-of select="@type"/>_deserialize((<xsl:value-of select="$nativePropertyType"/>)element, env,
                                                                                 &amp;current_node);
                                          if(AXIS2_FAILURE ==  status)
                                          {
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                  " %d :: %s", env->error->error_number,
                                                                  AXIS2_ERROR_GET_MESSAGE(env->error));
                                              return AXIS2_FAILURE;
                                          }
                                          axutil_array_list_add_at(arr_list, env, i, element);
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                                axutil_array_list_add_at(arr_list, env, i, (void*)text_value);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                            prefix_found = AXIS2_FALSE;
                                            for(cp = text_value; *cp; cp ++)
                                            {
                                                if(*cp == ':')
                                                {
                                                    *cp = '\0';
                                                    cp ++;
                                                    prefix_found  = AXIS2_TRUE;
                                                    break;
                                                }
                                            }
                                          
                                            if(prefix_found)
                                            {
                                                /* node value contain  the prefix */
                                                qname_ns = axiom_element_find_namespace_uri(current_element, env, text_value, current_node);
                                            }
                                            else
                                            {
                                                /* Then it is the default namespace */
                                                cp = text_value;
                                                qname_ns = axiom_element_get_default_namespace(current_element, env, current_node);
                                            }
                                          
                                            <!-- we are done extracting info, just set the extracted value to the qname -->
                                           
                                            axutil_array_list_add_at(arr_list, env, i, (void*)
                                                    axutil_qname_create(
                                                          env, 
                                                          cp, /* cp contain the localname */
                                                          axiom_namespace_get_uri(qname_ns, env),
                                                          axiom_namespace_get_prefix(qname_ns, env)));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                                axutil_array_list_add_at(arr_list, env, i, (void*)axutil_uri_parse_string(env, text_value));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                                axutil_array_list_add_at(arr_list, env, i, (void*)axutil_duration_create_from_string(env, text_value));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, 64);
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = (char)(*text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(int));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_byte_t'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(int));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                               /* we keeps ints in arrays from their pointers */
                                               element = AXIS2_MALLOC(env-> allocator, sizeof(short));
                                               (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                               axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='float'">
                                          /* we keeps float in arrays from their pointers */
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(float));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atof(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='double'">
                                          /* we keeps float in arrays from their pointers */
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(double));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atof(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='long' or $nativePropertyType='unsigned long'">
                                          /* we keeps long in arrays from their pointers */
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                               element = AXIS2_MALLOC(env-> allocator, sizeof(long));
                                               (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atol(text_value);
                                               axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axiom_node_t*'">
                                          text_value = NULL; /* just to avoid warning */
                                          <xsl:choose>
                                            <xsl:when test="@any">
                                            {
                                              axiom_node_t *current_property_node = current_node;
                                              current_node = axiom_node_get_next_sibling(current_node, env);
                                              axiom_node_detach(current_property_node, env);
                                              axutil_array_list_add_at(arr_list, env, i, (void*)current_property_node);
                                              status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                              current_property_node);
                                            }
                                            </xsl:when>
                                            <xsl:otherwise>
                                              if(axiom_node_get_first_child(current_node, env))
                                              {
                                                  axiom_node_t *current_property_node = axiom_node_get_first_child(current_node, env);
                                                  axiom_node_detach(current_property_node, env);
                                                  axutil_array_list_add_at(arr_list, env, i, (void*)current_property_node);
                                              }
                                            </xsl:otherwise>
                                          </xsl:choose>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              if (!strcmp (text_value , "true") || !strcmp (text_value, "TRUE"))
                                              {
                                                 axutil_array_list_add_at(arr_list, env, i, (void*)AXIS2_TRUE);
                                              }
                                              else
                                              {
                                                 axutil_array_list_add_at(arr_list, env, i, (void*)AXIS2_FALSE);
                                              }
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                                          element = (void*)axutil_date_time_create(env);
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              status = axutil_date_time_deserialize_date_time((axutil_date_time_t*)element, env,
                                                                              text_value);
                                              if(AXIS2_FAILURE ==  status)
                                              {
                                                  AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                      " %d :: %s", env->error->error_number,
                                                                      AXIS2_ERROR_GET_MESSAGE(env->error));
                                                  return AXIS2_FAILURE;
                                              }
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>

                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                                          element = (void*)axutil_base64_binary_create(env);
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              status = axutil_base64_binary_set_encoded_binary((axutil_base64_binary_t*)element, env,
                                                                              text_value);
                                              if(AXIS2_FAILURE ==  status)
                                              {
                                                  AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                      " %d :: %s", env->error->error_number,
                                                                      AXIS2_ERROR_GET_MESSAGE(env->error));
                                                  return AXIS2_FAILURE;
                                              }
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>


                                        </xsl:when>
                                        <xsl:otherwise>
                                          <!-- TODO: add other types here -->
                                          /* imposible to handle the request type - so please do it manually */
                                          text_value = NULL;
                                        </xsl:otherwise>
                                     </xsl:choose>
                                     if(AXIS2_FAILURE ==  status)
                                     {
                                         if(element_qname)
                                         {
                                             axutil_qname_free(element_qname, env);
                                         }
                                         AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in setting the value for <xsl:value-of select="$propertyName"/> "
                                                             " %d :: %s", env->error->error_number,
                                                             AXIS2_ERROR_GET_MESSAGE(env->error));
                                         return AXIS2_FAILURE;
                                     }

                                     i ++;
                                  <xsl:if test="not(@any)">
                                  }
                                  </xsl:if>
                               }
                               status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   arr_list);
                             </xsl:otherwise> <!--closing otherwise for "../@ordered  or not($anon or $istype)" -->
                           </xsl:choose> <!-- chooses for ordered or not @ordered or not($anon or $istype)-->
                        </xsl:otherwise> <!-- closing when it is all the way an array -->
                      </xsl:choose> <!-- check array or not -->
                   </xsl:otherwise> <!-- closing when it is an element not(@attribute) -->
                 </xsl:choose> <!--- chooosing for element or attribute -->
                 <xsl:if test="not(@simple)">
                  if(element_qname)
                  {
                     axutil_qname_free(element_qname, env);
                     element_qname = NULL;
                  }
                 </xsl:if>
              </xsl:for-each> <!-- closing for each property -->

            <xsl:if test="$particleClass">
                *dp_parent = current_node;
            </xsl:if>
          return status;
       }

          axis2_bool_t AXIS2_CALL
          <xsl:value-of select="$axis2_name"/>_is_particle()
          {
            <xsl:choose>
              <xsl:when test="$particleClass">
                 return AXIS2_TRUE;
              </xsl:when>
              <xsl:otherwise>
                 return AXIS2_FALSE;
              </xsl:otherwise>
            </xsl:choose>
          }


          void AXIS2_CALL
          <xsl:value-of select="$axis2_name"/>_declare_parent_namespaces(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env, axiom_element_t *parent_element,
                    axutil_hash_t *namespaces, int *next_ns_index)
          {
            <xsl:variable name="check_anything_to_declare">
                  <xsl:for-each select="property">
                    <xsl:if test="@type='axutil_qname_t*'">yes</xsl:if>
                  </xsl:for-each>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="contains($check_anything_to_declare, 'yes')">
                    axiom_namespace_t *element_ns = NULL;
                    axis2_char_t *qname_uri;
                    axis2_char_t *qname_prefix;
                </xsl:when>
                <xsl:otherwise>
                  /* Here this is an empty function, Nothing to declare */
                </xsl:otherwise>
            </xsl:choose>

                <xsl:for-each select="property">
                  <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                     <xsl:choose>
                       <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                       <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                       <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="CName"><xsl:value-of select="@cname"/></xsl:variable>
    
                  <xsl:variable name="propertyInstanceName"><!--these are used in arrays to take the native type-->
                     <xsl:choose>
                       <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                       <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:choose>
                    <!-- add axutil_qname_t namespaces -->
                    <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                      qname_uri = axutil_qname_get_uri(<xsl:value-of select="$propertyInstanceName"/>, env);
                      if(qname_uri &amp;&amp; !axutil_strcmp(qname_uri, ""))
                      {
                          if(!(qname_prefix = (axis2_char_t*)axutil_hash_get(namespaces, qname_uri, AXIS2_HASH_KEY_STRING)))
                          {
                              qname_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);

                              
                              sprintf(qname_prefix, "q%d", (*next_ns_index)++); <!-- just different prefix for the special case -->
                              axutil_hash_set(namespaces, qname_uri, AXIS2_HASH_KEY_STRING, qname_prefix);

                              if(parent_element)
                              {
                                    element_ns = axiom_namespace_create(env, qname_uri,
                                                                        qname_prefix);
                                    axiom_element_declare_namespace_assume_param_ownership(parent_element, env, element_ns);
                              }
                          }
                      }
                    </xsl:when>
                  </xsl:choose>
              </xsl:for-each> <!--closing the for-each select="property" -->
          }

        <xsl:if test="@simple">
            axis2_char_t* AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_serialize_to_string(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env, axutil_hash_t *namespaces)
            {
                axis2_char_t *text_value = NULL;
                axis2_char_t *qname_uri = NULL;
                axis2_char_t *qname_prefix = NULL;

                <xsl:for-each select="property">
                  <xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
                  <xsl:variable name="propertyType">
                     <xsl:choose>
                       <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                       <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                       <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                       <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                     <xsl:choose>
                       <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                       <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                       <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="propertyName"><xsl:value-of select="@originalName"/></xsl:variable>
                  <xsl:variable name="CName"><xsl:value-of select="@cname"/></xsl:variable>
    
                  <xsl:variable name="propertyInstanceName"><!--these are used in arrays to take the native type-->
                     <xsl:choose>
                       <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                       <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="justPropertyInstanceName"><!--these are used in arrays to take the native type-->
                     <xsl:choose>
                       <xsl:when test="@isarray">element</xsl:when>
                       <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>


                 <xsl:choose>
                    <!-- add int s -->
                    <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                       text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                       sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                    </xsl:when>
                    <!-- add axis2_byte_t s -->
                    <xsl:when test="$nativePropertyType='axis2_byte_t'">
                       text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                       sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                    </xsl:when>

                    <!-- add char s -->
                    <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                       text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                       sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                    </xsl:when>

                    <!-- add short s -->
                    <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                       text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                       sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                    </xsl:when>

                    <!-- add long s -->
                    <xsl:when test="$nativePropertyType='long' or $nativePropertyType='unsigned long'">
                       text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                       sprintf (text_value, "%d", (int)<xsl:value-of select="$propertyInstanceName"/>);
                    </xsl:when>

                    <!-- add float s -->
                    <xsl:when test="$nativePropertyType='float'">
                       text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                       sprintf (text_value, "%f", <xsl:value-of select="$propertyInstanceName"/>);
                    </xsl:when>

                    <!-- add double s -->
                    <xsl:when test="$nativePropertyType='double'">
                       text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                       sprintf (text_value, "%f", <xsl:value-of select="$propertyInstanceName"/>);
                    </xsl:when>

                    <!-- add axis2_char_t* s -->
                    <xsl:when test="$nativePropertyType='axis2_char_t*'">
                       text_value = (axis2_char_t*)axutil_strdup(env, <xsl:value-of select="$propertyInstanceName"/>);
                    </xsl:when>

                    <!-- add axutil_uri_t s -->
                    <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                       text_value = axutil_uri_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, AXIS2_URI_UNP_OMITUSERINFO);
                    </xsl:when>

                    <!-- add axutil_duration_t s -->
                    <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                       text_value = axutil_duration_serialize_duration(<xsl:value-of select="$propertyInstanceName"/>, env);
                    </xsl:when>

                    <!-- add axutil_qname_t s -->
                    <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                      <!-- namespaces are declared in _declare_parent_namespaces -->
                      qname_uri = axutil_qname_get_uri(<xsl:value-of select="$propertyInstanceName"/>, env);
                      if(qname_uri == NULL)
                      {
                            text_value = (axis2_char_t*)axutil_strdup(env, axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));
                      }
                      else
                      {
                        qname_prefix = (axis2_char_t*)axutil_hash_get(namespaces, qname_uri, AXIS2_HASH_STRING);
                        if(qname_prefix != NULL)
                        {
                            text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, 
                                        sizeof (axis2_char_t) * (ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                            axutil_strlen(axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env)) + 2));
                            sprintf(text_value, "%s:%s", qname_prefix,
                                                      axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));
                        }
                        else
                        {
                            AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Failed in serialize_to_string value for <xsl:value-of select="$propertyName"/>, "
                                                        "Prefix is not declared beofre using");
                            return NULL;
                        }
                      }
                    </xsl:when>

                    <!-- add axis2_bool_t s -->
                    <xsl:when test="$nativePropertyType='axis2_bool_t'">
                       <!--text_value = (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false";-->
                       text_value = (axis2_char_t*)axutil_strdup(env, (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false");
                    </xsl:when>
                    <!-- add axis2_date_time_t s -->
                    <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                       text_value =  axutil_date_time_serialize_date_time(<xsl:value-of select="$propertyInstanceName"/>, env);
                    </xsl:when>
                    <!-- add axis2_base64_binary_t s -->
                    <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                       text_value =  axutil_base64_binary_get_encoded_binary(<xsl:value-of select="$propertyInstanceName"/>, env);
                    </xsl:when>
                    <xsl:when test="@ours">
                        <!-- This should be in an unreachable path -->
                    </xsl:when>
                    <xsl:otherwise>
                      <!--TODO: add new property types -->
                      /* can not handle the property type <xsl:value-of select="$nativePropertyType"/>*/
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
                const axutil_env_t *env, axiom_node_t *parent, axiom_element_t *parent_element, int parent_tag_closed, axutil_hash_t *namespaces, int *next_ns_index)
        {
            <!-- first declaration part -->
            <xsl:for-each select="property/@attribute">
             <xsl:if test="position()=1">
               axiom_attribute_t *text_attri = NULL;
             </xsl:if>
            </xsl:for-each>
            <xsl:if test="@type or property/@attribute">
             axis2_char_t *string_to_stream;
            </xsl:if>
         
         axiom_node_t *current_node = NULL;

         <!--now distinguise the properties specific to simple types -->
         <xsl:choose>
           <xsl:when test="@simple">
            axiom_data_source_t *data_source = NULL;
            axutil_stream_t *stream = NULL;
            axis2_char_t *text_value;
             <xsl:for-each select="property/@attribute">
              <xsl:if test="position()=1">
               axiom_namespace_t *ns1 = NULL;
               axis2_char_t *p_prefix = NULL;
              </xsl:if>
             </xsl:for-each>
           </xsl:when>

           <!-- non simple types -->
           <xsl:otherwise>
             <xsl:for-each select="property">
              <xsl:if test="position()=1"> <!-- check for at least one element exists -->
                axiom_namespace_t *ns1 = NULL;

                axis2_char_t *qname_uri = NULL;
                axis2_char_t *qname_prefix = NULL;
                axis2_char_t *p_prefix = NULL;
                axis2_bool_t ns_already_defined;
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
                    <xsl:when test="not(@type) or (@ours='yes' and (@type='uri' or @type='qname' or @type='date_time' or @type='base64_binary' or @type='char')) or @type='char' or @type='axis2_char_t*' or @type='axutil_base64_binary_t*' or @type='axutil_date_time_t*' or @type='axiom_node_t*' or @type='axutil_duration_t*' or @type='axutil_uri_t*' or @type='axutil_qname_t*'">
                    axis2_char_t *text_value_<xsl:value-of select="$position"/>;
                    axis2_char_t *text_value_<xsl:value-of select="$position"/>_temp;
                    </xsl:when>
                    <xsl:otherwise>
                    axis2_char_t text_value_<xsl:value-of select="$position"/>[64];
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>

            <xsl:for-each select="property/@attribute">
             <xsl:if test="position()=1">
                axis2_char_t *text_value = NULL;
             </xsl:if>
            </xsl:for-each>
            <xsl:if test="property and (not(property/@attribute) or property/@attribute='' or property/@notattribute)">
               axis2_char_t *start_input_str = NULL;
               axis2_char_t *end_input_str = NULL;
               unsigned int start_input_str_len = 0;
               unsigned int end_input_str_len = 0;
            </xsl:if>
            <!-- Following is in special situatioin where no properties exist -->
               axiom_data_source_t *data_source = NULL;
               axutil_stream_t *stream = NULL;

            <xsl:if test="not(@type)"> <!-- So this is the root of the serialization call tree -->
                int next_ns_index_value = 0;
            </xsl:if>

            AXIS2_ENV_CHECK(env, NULL);
            AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, NULL);
            
            <xsl:if test="not(@type)"> <!-- So this is the root of the serialization call tree -->
              <xsl:for-each select="property">
                <xsl:if test="position()=1">
                    namespaces = axutil_hash_make(env);
                    next_ns_index = &amp;next_ns_index_value;
                    <xsl:choose>
                       <xsl:when test="$nsuri and $nsuri != ''"> 
                           ns1 = axiom_namespace_create (env,
                                             "<xsl:value-of select="$nsuri"/>",
                                             "n"); <!-- we are usinig "" instead of <xsl:value-of select="@child-nsuri"/>  -->
                           axutil_hash_set(namespaces, "<xsl:value-of select="$nsuri"/>", AXIS2_HASH_KEY_STRING, axutil_strdup(env, "n"));
                       </xsl:when>
                       <xsl:otherwise> 
                           ns1 = NULL; 
                       </xsl:otherwise>
                    </xsl:choose>
                    <!-- if not(@type) then no doubt the parent is NULL --> 
                    parent_element = axiom_element_create (env, NULL, "<xsl:value-of select="$originalName"/>", ns1 , &amp;parent);
                    
                    <!-- axiom_element_declare_default_namespace(parent_element, env, "<xsl:value-of select="$nsuri"/>"); -->
                    axiom_element_set_namespace(parent_element, env, ns1, parent);


               </xsl:if>
              </xsl:for-each>
            </xsl:if>
            </xsl:otherwise> <!--otherwise for @simple -->
            </xsl:choose>

                <xsl:if test="@type">
                    current_node = parent;
                    data_source = (axiom_data_source_t *)axiom_node_get_data_element(current_node, env);
                    if (!data_source)
                        return NULL;
                    stream = axiom_data_source_get_stream(data_source, env); /* assume parent is of type data source */
                    if (!stream)
                        return NULL;
                  </xsl:if>
                <xsl:if test="count(property)!=0">
                  <xsl:if test="not(@type)">
                    data_source = axiom_data_source_create(env, parent, &amp;current_node);
                    stream = axiom_data_source_get_stream(data_source, env);
                  </xsl:if>
                </xsl:if>


            
            <!--first write attributes tothe parent-->
            <xsl:if test="count(property[@attribute])!=0 or @type">
            if(!parent_tag_closed)
            {
            </xsl:if>
            <xsl:for-each select="property">
              <xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
              <xsl:variable name="propertyType">
                 <xsl:choose>
                   <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="propertyName"><xsl:value-of select="@originalName"/></xsl:variable>
              <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

              <xsl:variable name="propertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="justPropertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>

              <xsl:variable name="namespacePrefix">
                <xsl:choose>
                    <xsl:when test="$nsprefix"><xsl:value-of select="$nsprefix"/><xsl:text>:</xsl:text></xsl:when>
                    <xsl:otherwise><xsl:text></xsl:text></xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:variable name="qualifiedPropertyName">
                <xsl:value-of select="namespacePrefix"/><xsl:value-of select="$propertyName"/>
              </xsl:variable>

                <xsl:if test="@attribute">
                  if(<xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/>)
                  {
                    <xsl:choose>
                      <xsl:when test="@nsuri and @nsuri != ''">
                        if(!(p_prefix = (axis2_char_t*)axutil_hash_get(namespaces, "<xsl:value-of select="@nsuri"/>", AXIS2_HASH_KEY_STRING)))
                        {
                            p_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);
                            sprintf(p_prefix, "n%d", (*next_ns_index)++);
                            axutil_hash_set(namespaces, "<xsl:value-of select="@nsuri"/>", AXIS2_HASH_KEY_STRING, p_prefix);
                            axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                                     "<xsl:value-of select="@nsuri"/>",
                                                     p_prefix));
                        }
                      </xsl:when>
                      <xsl:otherwise>
                        p_prefix = NULL;
                      </xsl:otherwise>
                    </xsl:choose>

                      <!-- here only simple type possible -->
                      <!-- ADB_DEFAULT_DIGIT_LIMIT (64) bytes is used to the store the string representation of the number and the namespace prefix + ":" -->
                      <xsl:choose>
                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5 + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                                                            <!-- here axutil_strlen(":=\"\"") + 1(for NULL terminator) = 5 -->
                           sprintf(text_value, " %s%s%s=\"%d\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>
                        <!-- add axis2_byte_t s -->
                        <xsl:when test="$nativePropertyType='axis2_byte_t'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"%d\"", p_prefix?p_prefix:"", (p_prefix  &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"%d\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add short s -->
                        <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"%d\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add long s -->
                        <xsl:when test="$nativePropertyType='long' or $nativePropertyType='unsigned long'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"%d\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add float s -->
                        <xsl:when test="$nativePropertyType='float'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"%f\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add double s -->
                        <xsl:when test="$nativePropertyType='double'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"%f\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(<xsl:value-of select="$propertyInstanceName"/>) + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add axutil_uri_t s -->
                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                           text_value = axutil_uri_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, AXIS2_URI_UNP_OMITUSERINFO);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT) +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env-> allocator, string_to_stream);
                        </xsl:when>

                        <!-- add axutil_duration_t s -->
                        <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                           text_value = axutil_duration_serialize_duration(<xsl:value-of select="$propertyInstanceName"/>, env);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env-> allocator, string_to_stream);
                        </xsl:when>

                        <!-- add axutil_qname_t s -->
                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                           qname_uri =  axutil_qname_get_uri(<xsl:value-of select="$propertyInstanceName"/>, env);
                           if(qname_uri &amp;&amp; !axutil_strcmp(qname_uri, ""))
                           {
                               if(!(qname_prefix = (axis2_char_t*)axutil_hash_get(namespaces, qname_uri, AXIS2_HASH_KEY_STRING)))
                               {
                                   qname_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);

                                   sprintf(qname_prefix, "q%d", (*next_ns_index) ++); <!-- just different prefix for the special case -->
                                   axutil_hash_set(namespaces, qname_uri, AXIS2_HASH_KEY_STRING, qname_prefix);

                                   axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                                                                        qname_uri,
                                                                                        qname_prefix));
                               }
                               text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                                 (2 + axutil_strlen(qname_prefix) +
                                                                    axutil_strlen(axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env))));
                               sprintf(text_value, "%s%s%s", qname_prefix, (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                           axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));
                           }
                           else
                           {
                               text_value = axutil_strdup(env, axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));
                           }

                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);

                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env->allocator, string_to_stream);
                           AXIS2_FREE(env->allocator, text_value);
                        </xsl:when>

                        <!-- add axis2_bool_t s -->
                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                           <!--text_value = (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false";-->
                           text_value = (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false";
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env-> allocator, string_to_stream);
                        </xsl:when>
                        <!-- add axis2_date_time_t s -->
                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                           text_value =  axutil_date_time_serialize_date_time(<xsl:value-of select="$propertyInstanceName"/>, env);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env-> allocator, string_to_stream);
                        </xsl:when>
                        <!-- add axis2_base64 _binary_t s -->
                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                           text_value =  axutil_base64_binary_get_encoded_binary(<xsl:value-of select="$propertyInstanceName"/>, env);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env-> allocator, string_to_stream);
                        </xsl:when>
                        <xsl:when test="@ours">
        
                           adb_<xsl:value-of select="@type"/>_declare_parent_namespaces(<xsl:value-of select="$propertyInstanceName"/>,
                                                                                      env, parent_element, namespaces, next_ns_index);
                           text_value = adb_<xsl:value-of select="@type"/>_serialize_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, namespaces);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env-> allocator, string_to_stream);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>
                        <xsl:otherwise>
                          <!--TODO: add new attributes types -->
                          /* can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
                           text_value = NULL;
                        </xsl:otherwise>
                      </xsl:choose>
                   }
                   <xsl:if test="not(@optional)">
                   else
                   {
                      AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Nil value found in non-optional attribute <xsl:value-of select="$propertyName"/>");
                      return NULL;
                   }
                   </xsl:if>
                </xsl:if> <!-- if for attribute, -->
            </xsl:for-each>


            <xsl:if test="@type">
              string_to_stream = "&gt;"; <!-- The ending tag of the parent -->
              axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
            </xsl:if>

             <!-- end bracket for if(!parent_tag_closed)-->
            <xsl:if test="count(property[@attribute])!=0 or @type">
            }
            </xsl:if>

            <xsl:if test="@simple">
               <!-- how if this type is a qname :(, simply we are not handling that situation.. -->
               text_value = <xsl:value-of select="$axis2_name"/>_serialize_to_string(<xsl:value-of select="$name"/>, env, namespaces);
               axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
            </xsl:if>

            <xsl:for-each select="property">
              <xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
              <xsl:variable name="propertyType">
                 <xsl:choose>
                   <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="propertyName"><xsl:value-of select="@originalName"></xsl:value-of></xsl:variable>
              <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

              <xsl:variable name="propertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="justPropertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>

              <xsl:choose>
                <xsl:when test="@attribute">
                    <!-- here only simple type possible -->
                    if(parent_tag_closed)
                    {
                       if(<xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/>)
                       {
                       <xsl:choose>
                         <xsl:when test="@nsuri and @nsuri != ''">
                           if(!(p_prefix = (axis2_char_t*)axutil_hash_get(namespaces, "<xsl:value-of select="@nsuri"/>", AXIS2_HASH_KEY_STRING)))
                           {
                               p_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);
                               sprintf(p_prefix, "n%d", (*next_ns_index)++);
                               axutil_hash_set(namespaces, "<xsl:value-of select="@nsuri"/>", AXIS2_HASH_KEY_STRING, p_prefix);
                               
                               axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                                "<xsl:value-of select="@nsuri"/>",
                                                p_prefix));
                           }
                           ns1 = axiom_namespace_create (env,
                                                "<xsl:value-of select="@nsuri"/>",
                                                p_prefix);
                         </xsl:when>
                         <xsl:otherwise>
                           p_prefix = NULL;
                           ns1 = NULL;
                         </xsl:otherwise>
                       </xsl:choose> <!-- close for test nsuri and nsuri != "" -->

                       <xsl:choose>
                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           <!-- TODO: parent here can be data_source node, not element node should be fixed -->
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add axis2_byte_t s -->
                        <xsl:when test="$nativePropertyType='axis2_byte_t'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add short s -->
                        <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add long s -->
                        <xsl:when test="$nativePropertyType='long' or $nativePropertyType='unsigned long'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, "%d", (int)<xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add float s -->
                        <xsl:when test="$nativePropertyType='float'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, "%f", <xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add double s -->
                        <xsl:when test="$nativePropertyType='double'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, "%f", <xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                           text_value = <xsl:value-of select="$propertyInstanceName"/>;
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>

                        <!-- add axutil_uri_t s -->
                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                           text_value = axutil_uri_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, AXIS2_URI_UNP_OMITUSERINFO);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>

                        <!-- add axutil_duration_t s -->
                        <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                           text_value = axutil_duration_serialize_duration(<xsl:value-of select="$propertyInstanceName"/>, env);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>

                        <!-- add axutil_qname_t s -->
                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">

                           qname_uri =  axutil_qname_get_uri(<xsl:value-of select="$propertyInstanceName"/>, env);
                           if(qname_uri)
                           {
                               if(!(qname_prefix = (axis2_char_t*)axutil_hash_get(namespaces, qname_uri, AXIS2_HASH_KEY_STRING)))
                               {

                                   qname_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);

                                   sprintf(qname_prefix, "q%d", (*next_ns_index) ++ ); <!-- just different prefix for the special case -->

                                   axutil_hash_set(namespaces, qname_uri, AXIS2_HASH_KEY_STRING, qname_prefix);
                                   
                                   axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                                     qname_uri,
                                                     qname_prefix));
                               }
                           }

                           text_value = (axis2_char_t*) AXIS2_MALLOC(env-> allocator, 
                                         sizeof (axis2_char_t) * (ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env)) + 2));
                           sprintf(text_value, "%s%s%s", qname_uri?qname_prefix:"",
                                                        qname_uri?":":"",
                                                       axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));

                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env->allocator, text_value);
                        </xsl:when>

                        <!-- add axis2_bool_t s -->
                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                           <!--text_value = (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false";-->
                           text_value =  (<xsl:value-of select="$propertyInstanceName"/>)?axutil_strdup(env, "true"):axutil_strdup(env, "false");
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env->allocator, text_value);
                        </xsl:when>
                        <!-- add axis2_date_time_t s -->
                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                           text_value =  axutil_date_time_serialize_date_time(<xsl:value-of select="$propertyInstanceName"/>, env);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>
                       <!-- add axis2_base64_binary_t s -->
                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                           text_value =  axutil_base64_binary_get_encoded_binary(<xsl:value-of select="$propertyInstanceName"/>, env);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>
                        <xsl:when test="@ours">
                           adb_<xsl:value-of select="@type"/>_declare_parent_namespaces(<xsl:value-of select="$propertyInstanceName"/>,
                                                                                      env, parent_element, namespaces, next_ns_index);
                           text_value = adb_<xsl:value-of select="@type"/>_serialize_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, namespaces);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>
                        <xsl:otherwise>
                          <!--TODO: add new attributes types -->
                          /* Can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
                           text_value = NULL;
                           parent_element = NULL;
                           text_attri = NULL;
                        </xsl:otherwise>
                      </xsl:choose>
                      }
                      <xsl:if test="not(@optional)">
                      else
                      {
                         AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Nil value found in non-optional attribute <xsl:value-of select="$propertyName"/>");
                         return NULL;
                      }
                      </xsl:if> 
                  }<!-- End bracket for if(parent_tag_closed)-->
                </xsl:when>
                <xsl:when test="../@simple"></xsl:when> <!--Just to ignore parsing following code at simple types-->
                <xsl:otherwise>
 
                   <xsl:choose>
                     <xsl:when test="@nsuri and @nsuri != ''">
                       if(!(p_prefix = (axis2_char_t*)axutil_hash_get(namespaces, "<xsl:value-of select="@nsuri"/>", AXIS2_HASH_KEY_STRING)))
                       {
                           p_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);
                           sprintf(p_prefix, "n%d", (*next_ns_index)++);
                           axutil_hash_set(namespaces, "<xsl:value-of select="@nsuri"/>", AXIS2_HASH_KEY_STRING, p_prefix);
                           
                           axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                            "<xsl:value-of select="@nsuri"/>",
                                            p_prefix));
                       }
                     </xsl:when>
                     <xsl:otherwise>
                       p_prefix = NULL;
                     </xsl:otherwise>
                   </xsl:choose> <!-- close for test nsuri and nsuri != "" -->

                   if (!<xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/>)
                   {
                      <xsl:if test="@nillable">
                        <xsl:choose>
                          <xsl:when test="@minOccurs=0">
                           /* no need to complain for minoccurs=0 element */
                            <!-- just ignore the element.. -->
                          </xsl:when>
                          <xsl:otherwise>
                            <!-- just write a nil element -->
                            start_input_str = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof(axis2_char_t) *
                                        (5 + axutil_strlen(p_prefix) + 
                                         axutil_strlen("<xsl:value-of select="$propertyName"/>") + 
                                         axutil_strlen(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"1\"") + 
                                         ADB_DEFAULT_DIGIT_LIMIT)); 
                                        <!-- axutil_strlen("<:/>") + 1 = 5 -->
                            
                            sprintf(start_input_str, "&lt;%s%s<xsl:value-of select="$propertyName"/> xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"1\"/&gt;",
                                        p_prefix?p_prefix:"",
                                        (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"");
                                        
                            axutil_stream_write(stream, env, start_input_str, axutil_strlen(start_input_str));
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:if>

                      <xsl:if test="not(@nillable)">
                        <xsl:choose>
                          <xsl:when test="@minOccurs=0">
                           /* no need to complain for minoccurs=0 element */
                            <!-- just ignore the element.. -->
                          </xsl:when>
                          <xsl:otherwise>
                            <!-- just return an error -->
                            AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Nil value found in non-nillable property <xsl:value-of select="$propertyName"/>");
                            return NULL;
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:if>
                   }
                   else
                   {
                     start_input_str = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof(axis2_char_t) *
                                 (4 + axutil_strlen(p_prefix) + 
                                  axutil_strlen("<xsl:value-of select="$propertyName"/>") + 
                                  ADB_DEFAULT_DIGIT_LIMIT* 2)); 
                                 <!-- axutil_strlen("<:>") + 1 = 4 -->
                     end_input_str = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof(axis2_char_t) *
                                 (5 + ADB_DEFAULT_DIGIT_LIMIT + axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                                  <!-- axutil_strlen("</:>") + 1 = 5 -->
                     

                   <!-- handles arrays -->
                   <xsl:if test="@isarray">
                     /*
                      * Parsing <xsl:value-of select="$CName"/> array
                      */
                     if (<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> != NULL)
                     {
                        <xsl:choose>
                            <xsl:when test="@ours">

                            sprintf(start_input_str, "&lt;%s%s<xsl:value-of select="$propertyName"/>",
                                 p_prefix?p_prefix:"",
                                 (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"");
                            </xsl:when>
                            <xsl:otherwise>
                            sprintf(start_input_str, "&lt;%s%s<xsl:value-of select="$propertyName"/>&gt;",
                                 p_prefix?p_prefix:"",
                                 (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":""); 
                            </xsl:otherwise>
                        </xsl:choose>
                         start_input_str_len = axutil_strlen(start_input_str);

                         sprintf(end_input_str, "&lt;/%s%s<xsl:value-of select="$propertyName"/>&gt;",
                                 p_prefix?p_prefix:"",
                                 (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"");
                         end_input_str_len = axutil_strlen(end_input_str);

                         count = axutil_array_list_size(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
                         for(i = 0; i &lt; count; i ++)
                         {
                            element = axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);

                            if(NULL == element) <!--validty of individual -->
                            {
                                continue;
                            }
                    </xsl:if>
                     <!-- for each non attribute properties there will always be an element-->
                     /*
                      * parsing <xsl:value-of select="$propertyName"/> element
                      */

                    <!-- how to build all the ours things -->
                    <xsl:if test="not(@isarray)">
                        <xsl:choose>
                            <xsl:when test="@ours">
                            sprintf(start_input_str, "&lt;%s%s<xsl:value-of select="$propertyName"/>",
                                 p_prefix?p_prefix:"",
                                 (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":""); 
                            </xsl:when>
                            <xsl:otherwise>
                            sprintf(start_input_str, "&lt;%s%s<xsl:value-of select="$propertyName"/>&gt;",
                                 p_prefix?p_prefix:"",
                                 (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"");
                            </xsl:otherwise>
                        </xsl:choose>
                        start_input_str_len = axutil_strlen(start_input_str);
                        sprintf(end_input_str, "&lt;/%s%s<xsl:value-of select="$propertyName"/>&gt;",
                                 p_prefix?p_prefix:"",
                                 (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"");
                        end_input_str_len = axutil_strlen(end_input_str);
                    </xsl:if>


                      <xsl:choose>
                        <xsl:when test="@ours">
                            <xsl:if test="$anon or $istype"> <!-- As this shows, elements are not writing their tags here from stream.
                                                                 It is done using axiom manipualation above..-->
                            if(!adb_<xsl:value-of select="@type"/>_is_particle())
                            {
                                axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                            }
                            </xsl:if>
                            
                            <xsl:variable name="element_closed">
                                <xsl:choose>
                                    <xsl:when test="../@type">AXIS2_FALSE</xsl:when>
                                    <!-- this mean the anonymous header is writing -->
                                    <xsl:when test="$anon or $istype">AXIS2_FALSE</xsl:when>
                                    <xsl:otherwise>AXIS2_TRUE</xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            adb_<xsl:value-of select="@type"/>_serialize(<xsl:value-of select="$propertyInstanceName"/>, <!-- This will be either element (in array) or just the property -->
                                                                                 env, current_node, parent_element,
                                                                                 adb_<xsl:value-of select="@type"/>_is_particle() || <xsl:value-of select="$element_closed"/>, namespaces, next_ns_index);
                            <xsl:if test="$anon or $istype">
                            if(!adb_<xsl:value-of select="@type"/>_is_particle())
                            {
                                axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                            }
                            </xsl:if>
                        </xsl:when>

                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='int'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='unsigned int'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add char s -->
                        <xsl:when test="$nativePropertyType='char'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='unsigned char'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='axis2_byte_t'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add short s -->
                        <xsl:when test="$nativePropertyType='short'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='unsigned short'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%hu", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%hu", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>


                        <!-- NOTE: arrays for long, float, int are handled differently. they are stored in pointers -->
                        <!-- add long s -->
                        <xsl:when test="$nativePropertyType='long'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", (int)*((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", (int)<xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add long s -->
                        <xsl:when test="$nativePropertyType='unsigned long'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%lu", (int)*((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%lu", (int)<xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add float s -->
                        <xsl:when test="$nativePropertyType='float'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%f", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%f", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add double s -->
                        <xsl:when test="$nativePropertyType='double'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%f", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%f", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                           text_value_<xsl:value-of select="$position"/> = <xsl:value-of select="$propertyInstanceName"/>;
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                            
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
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add axutil_uri_t s -->
                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                           text_value_<xsl:value-of select="$position"/> = axutil_uri_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, AXIS2_URI_UNP_OMITUSERINFO);
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add axutil_duration_t s -->
                        <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                           text_value_<xsl:value-of select="$position"/> = axutil_duration_serialize_duration(<xsl:value-of select="$propertyInstanceName"/>, env);
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add axutil_qname_t s -->
                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                           <!-- Handled above -->
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>

                           <!-- TODO: Do this in single step -->

                           qname_uri =  axutil_qname_get_uri(<xsl:value-of select="$propertyInstanceName"/>, env);
                           if(qname_uri)
                           {
                               if(!(qname_prefix = (axis2_char_t*)axutil_hash_get(namespaces, qname_uri, AXIS2_HASH_KEY_STRING)))
                               {

                                   qname_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);

                                   sprintf(qname_prefix, "q%d", (*next_ns_index) ++ ); <!-- just different prefix for the special case -->

                                   axutil_hash_set(namespaces, qname_uri, AXIS2_HASH_KEY_STRING, qname_prefix);
                                   axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                            qname_uri,
                                            qname_prefix));
                               }
                           }

                           text_value_<xsl:value-of select="$position"/> = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, 
                                         sizeof (axis2_char_t) * (ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env)) + 2));

                           sprintf(text_value_<xsl:value-of select="$position"/>, "%s%s%s",
                                                       qname_uri?qname_prefix:"",
                                                       qname_uri?":":"",
                                                       axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));

                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           AXIS2_FREE(env-> allocator, text_value_<xsl:value-of select="$position"/>);
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                          <!-- add axis2_bool_t s -->
                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                           strcpy(text_value_<xsl:value-of select="$position"/>, (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false");
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add nodes -->
                        <xsl:when test="$nativePropertyType='axiom_node_t*'">
                           <xsl:choose>
                              <xsl:when test="$anon or $istype">
                                text_value_<xsl:value-of select="$position"/> = axiom_node_to_string(<xsl:value-of select="$propertyInstanceName"/>, env);
                                <xsl:if test="not(@any)">
                                axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                                </xsl:if>
                                axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                                <xsl:if test="not(@any)">
                                axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                                </xsl:if>
                              </xsl:when>
                              <xsl:otherwise>
                                text_value_<xsl:value-of select="$position"/> = NULL; /* just to bypass the warning unused variable */
                                axiom_node_add_child(parent, env, <xsl:value-of select="$propertyInstanceName"/>);
                              </xsl:otherwise>
                           </xsl:choose>
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                          text_value_<xsl:value-of select="$position"/> = axutil_date_time_serialize_date_time(<xsl:value-of select="$propertyInstanceName"/>, env);
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                          text_value_<xsl:value-of select="$position"/> =axutil_base64_binary_get_encoded_binary(<xsl:value-of select="$propertyInstanceName"/>, env);
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!--TODO: This should be extended for all the types that should be freed.. -->
                        <xsl:otherwise>
                          /* This is an unknown type or a primitive. handle this manually for unknown type */
                        </xsl:otherwise>
                      </xsl:choose>

                   <!-- close tags arrays -->
                   <xsl:if test="@isarray">
                         }
                     }
                   </xsl:if>
                 } <!-- else for non nillable -->
                </xsl:otherwise> <!-- othewise for non attributes -->
              </xsl:choose>
            </xsl:for-each>

            <xsl:if test="not(@type)"> <!-- So this is the root of the serialization call tree -->
              <xsl:for-each select="property">
                <xsl:if test="position()=1">
                   if(namespaces)
                   {
                       axutil_hash_index_t *hi;
                       void *val;
                       for (hi = axutil_hash_first(namespaces, env); hi; hi = axutil_hash_next(env, hi)) 
                       {
                           axutil_hash_this(hi, NULL, NULL, &amp;val);
                           AXIS2_FREE(env->allocator, val);
                       }
                       axutil_hash_free(namespaces, env);
                   }
                </xsl:if>
              </xsl:for-each>
            </xsl:if>

            return parent;
        }


        <xsl:for-each select="property">
            <xsl:variable name="propertyType">
               <xsl:choose>
                    <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                    <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                    <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                    <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
           
           <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>

              <!-- Just to identiy the pointer to arrays -->
              <!-- Simmilar to native property type except for shor, tint, float, double -->
              <xsl:variable name="PropertyTypeArrayParam"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@ours">adb_<xsl:value-of select="@type"/>_t*</xsl:when>
                   <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='unsigned long' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='long' or @type='axis2_byte_t' or @type='axis2_bool_t'">
                    <xsl:value-of select="@type"/><xsl:text>*</xsl:text>
                   </xsl:when>
                   <xsl:otherwise>
                    <xsl:value-of select="@type"/>
                   </xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>

              <xsl:variable name="propertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>

              <xsl:variable name="justPropertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>

            /**
             * getter for <xsl:value-of select="$propertyName"/>.
             */
            <xsl:value-of select="$propertyType"/> AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env)
             {
                <xsl:choose>
                  <xsl:when test="$propertyType='unsigned short' or $propertyType='unsigned char' or $propertyType='unsigned int' or $propertyType='unsigned long' or $propertyType='short' or $propertyType='axis2_byte_t' or $propertyType='axis2_bool_t' or $propertyType='char' or $propertyType='int' or $propertyType='float' or $propertyType='double' or $propertyType='long'">
                    AXIS2_ENV_CHECK(env, (<xsl:value-of select="$propertyType"/>)0);
                    AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, (<xsl:value-of select="$propertyType"/>)0);
                  </xsl:when>
                  <xsl:otherwise>
                    AXIS2_ENV_CHECK(env, NULL);
                    AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, NULL);
                  </xsl:otherwise>
                </xsl:choose>

                return <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>;
             }

            /**
             * setter for <xsl:value-of select="$propertyName"/>
             */
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env,
                    <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text> arg_<xsl:value-of select="$CName"/>)
             {
                <xsl:if test="@isarray">
                 int size = 0;
                 int i = 0;
                 axis2_bool_t non_nil_exists = AXIS2_FALSE;
                </xsl:if>

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);
                

                <xsl:if test="@isarray">
                 size = axutil_array_list_size(arg_<xsl:value-of select="$CName"/>, env);
                 <xsl:if test="not(@unbound)">
                     if (size &gt; <xsl:value-of select="@maxOccurs"/>)
                     {
                         AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "<xsl:value-of select="$propertyName"/> has exceed the maxOccurs(<xsl:value-of select="@maxOccurs"/>)");
                         return AXIS2_FAILURE;
                     }
                 </xsl:if>
                 if (size &lt; <xsl:value-of select="@minOccurs"/>)
                 {
                     AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "<xsl:value-of select="$propertyName"/> has less than minOccurs(<xsl:value-of select="@minOccurs"/>)");
                     return AXIS2_FAILURE;
                 }
                 for(i = 0; i &lt; size; i ++ )
                 {
                     if(NULL != axutil_array_list_get(arg_<xsl:value-of select="$CName"/>, env, i))
                     {
                         non_nil_exists = AXIS2_TRUE;
                         break;
                     }
                 }

                 <xsl:if test ="not(@nillabe) and not(@minOccurs='0')">
                    if(!non_nil_exists)
                    {
                        AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "All the elements in the array of <xsl:value-of select="$propertyName"/> is being set to NULL, but it is not a nullable or minOccurs=0 element");
                        return AXIS2_FAILURE;
                    }
                 </xsl:if>
                </xsl:if> <!-- close for the isarray -->

                <xsl:if test="not(@nillable) and not(@minOccurs='0') and (@ours or not($propertyType='unsigned char' or $propertyType='unsigned int' or $propertyType='unsigned short' or $propertyType='unsigned long' or $propertyType='char' or $propertyType='int' or $propertyType='short' or $propertyType='float' or $propertyType='axis2_byte_t' or $propertyType='double' or $propertyType='long' or $propertyType='axis2_bool_t'))">
                  if(NULL == arg_<xsl:value-of select="$CName"/>)
                  {
                      AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "<xsl:value-of select="$propertyName"/> is being set to NULL, but it is not a nullable element");
                      return AXIS2_FAILURE;
                  }
                </xsl:if>

                <!-- first reset whatever already in there -->
                <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env);

                <xsl:choose>
                    <xsl:when test="@isarray">
                        <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = arg_<xsl:value-of select="$CName"/>;
                        if(non_nil_exists)
                        {
                            <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;
                        }
                        <!-- else is_valid_* = AXIS2_FALSE is set by the above reset function -->
                    </xsl:when>
                    <xsl:when test="@type='axis2_char_t*' and not(@isarray)">
                        <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = (axis2_char_t *)axutil_strdup(env, arg_<xsl:value-of select="$CName"/>);
                        if(NULL == <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>)
                        {
                            AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Error allocating memeory for <xsl:value-of select="$propertyName"/>");
                            return AXIS2_FAILURE;
                        }
                        <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = arg_<xsl:value-of select="$CName"/>;
                        <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;
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

                <xsl:choose>
                  <xsl:when test="$nativePropertyType='unsigned short' or $nativePropertyType='unsigned char' or $nativePropertyType='unsigned int' or $nativePropertyType='unsigned long' or $nativePropertyType='short' or $nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_bool_t' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='long'">
                    AXIS2_ENV_CHECK(env, (<xsl:value-of select="$nativePropertyType"/>)0);
                    AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, (<xsl:value-of select="$nativePropertyType"/>)0);
                  </xsl:when>
                  <xsl:otherwise>
                    AXIS2_ENV_CHECK(env, NULL);
                    AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, NULL);
                  </xsl:otherwise>
                </xsl:choose>

                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    return (<xsl:value-of select="$nativePropertyType"/>)0;
                }
                ret_val = (<xsl:value-of select="$PropertyTypeArrayParam"/>)axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);
                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='unsigned long' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='long' or @type='axis2_byte_t' or @type='axis2_bool_t'">
                    return *ret_val;
                  </xsl:when>
                  <xsl:otherwise>
                    return ret_val;
                  </xsl:otherwise>
                </xsl:choose>
            }

            /**
             * Set the ith element of <xsl:value-of select="$propertyName"/>.
             */
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_at(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env, int i,
                    <xsl:value-of select="$nativePropertyType"/><xsl:text> arg_</xsl:text><xsl:value-of select="$CName"/>)
            {
                void *element = NULL;
                int size = 0;
                int j;
                axis2_bool_t non_nil_exists = AXIS2_FALSE;

                <xsl:if test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='unsigned long' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='long' or @type='axis2_bool_t' or @type='axis2_byte_t'">
                   <xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text> ptr_param_</xsl:text><xsl:value-of select="$CName"/>;
                </xsl:if>

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);
                
                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='unsigned long' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='long' or @type='axis2_bool_t' or @type='axis2_byte_t'">
                     non_nil_exists = AXIS2_TRUE;
                  </xsl:when>
                  <xsl:otherwise>
                    if(NULL == arg_<xsl:value-of select="$CName"/>)
                    {
                        if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> != NULL)
                        {
                            size = axutil_array_list_size(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
                            for(j = 0; j &lt; size; j ++ )
                            {
                                if(i == j) continue; <!-- should not count the ith element -->
                                if(NULL != axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i))
                                {
                                    non_nil_exists = AXIS2_TRUE;
                                    break;
                                }
                            }
                        }
                    }
                    else
                    {
                        non_nil_exists = AXIS2_TRUE;
                    }
                  </xsl:otherwise>
                </xsl:choose>

                <xsl:if test ="not(@nillabe) and not(@minOccurs='0')">
                   if(!non_nil_exists)
                   {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "All the elements in the array of <xsl:value-of select="$propertyName"/> is being set to NULL, but it is not a nullable or minOccurs=0 element");
                       return AXIS2_FAILURE;
                   }
                </xsl:if>

                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = axutil_array_list_create(env, 10);
                }
                
                /* check whether there already exist an element */
                element = axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);
                if(NULL != element)
                {
                  <!-- Some repeated code -->
                  <!-- For all inside the choose, propertyInstanceName = element -->
                  <xsl:choose>
                     <xsl:when test="@ours">
                        adb_<xsl:value-of select="@type"/>_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='unsigned short' or $nativePropertyType='unsigned char' or $nativePropertyType='unsigned int' or $nativePropertyType='unsigned long' or $nativePropertyType='short' or $nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_bool_t' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='long'">
                        <!-- free ints, longs, float in array-->
                        /* we keep primtives as pointers in arrasy, so need to free them */
                        AXIS2_FREE(env-> allocator, element);
                     </xsl:when>
            
                     <!-- free axis2_char_t s -->
                     <xsl:when test="$nativePropertyType='axis2_char_t*' and not(@isarray)">
                        AXIS2_FREE(env-> allocator, <xsl:value-of select="$propertyInstanceName"/>);
                     </xsl:when>
            
                     <!-- free nodes -->
                     <xsl:when test="$nativePropertyType='axiom_node_t*'">
                      axiom_node_free_tree (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                      axutil_qname_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                      axutil_uri_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                      axutil_duration_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                      axutil_date_time_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                      axutil_base64_binary_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
                       <xsl:when test="$propertyType='axutil_duration_t*'">
                      axutil_duration_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <!--TODO: This should be extended for all the types that should be freed.. -->
                     <xsl:otherwise>
                       /* This is an unknown type or a primitive. Please free this manually*/
                     </xsl:otherwise>
                  </xsl:choose>
                }

                <xsl:if test ="@nillabe or @minOccurs='0'">
                    if(!non_nil_exists)
                    {
                        <!-- No need to worry further -->
                        <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_FALSE;
                        axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, NULL);
                        return AXIS2_SUCCESS;
                    }
                </xsl:if>
                
                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='unsigned long' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='long' or @type='axis2_bool_t' or @type='axis2_byte_t'">
                   <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/> =  (<xsl:value-of select="$PropertyTypeArrayParam"/>)
                            AXIS2_MALLOC(env->allocator, sizeof(<xsl:value-of select="@type"/>));
                   if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                   {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Failed in allocatting memory for new value of <xsl:value-of select="$propertyName"/>");
                       return AXIS2_FAILURE;
                       
                   }
                   <xsl:text>*ptr_param_</xsl:text><xsl:value-of select="$CName"/> = <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>;
                   axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:when>
                  <xsl:otherwise>
                   axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:otherwise>
                </xsl:choose>
                <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;
                return AXIS2_SUCCESS;
            }

            /**
             * Add to <xsl:value-of select="$propertyName"/>.
             */
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env,
                    <xsl:value-of select="$nativePropertyType"/><xsl:text> arg_</xsl:text> <xsl:value-of select="$CName"/>)
             {
                <xsl:if test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='unsigned long' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='long' or @type='axis2_bool_t' or @type='axis2_byte_t'">
                   <xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text> ptr_param_</xsl:text><xsl:value-of select="$CName"/>;
                </xsl:if>

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);

                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='unsigned long' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='long' or @type='axis2_bool_t' or @type='axis2_byte_t'">
                  </xsl:when>
                  <xsl:otherwise>
                    if(NULL == arg_<xsl:value-of select="$CName"/>)
                    {
                      <xsl:choose>
                        <xsl:when test ="not(@nillabe) and not(@minOccurs='0')">
                           AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "All the elements in the array of <xsl:value-of select="$propertyName"/> is being set to NULL, but it is not a nullable or minOccurs=0 element");
                           return AXIS2_FAILURE;
                        </xsl:when>
                        <xsl:otherwise>
                           return AXIS2_SUCCESS; <!-- just no need to waist more time -->
                        </xsl:otherwise>
                      </xsl:choose>
                    }
                  </xsl:otherwise>
                </xsl:choose>

                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = axutil_array_list_create(env, 10);
                }
                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Failed in allocatting memory for <xsl:value-of select="$propertyName"/>");
                    return AXIS2_FAILURE;
                    
                }
                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='unsigned long' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='long' or @type='axis2_bool_t' or @type='axis2_byte_t'">
                   <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/> =  (<xsl:value-of select="$PropertyTypeArrayParam"/>)
                            AXIS2_MALLOC(env->allocator, sizeof(<xsl:value-of select="@type"/>));
                   if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                   {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Failed in allocatting memory for new value of <xsl:value-of select="$propertyName"/>");
                       return AXIS2_FAILURE;
                       
                   }
                   <xsl:text>*ptr_param_</xsl:text><xsl:value-of select="$CName"/> = <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>;
                   axutil_array_list_add(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:when>
                  <xsl:otherwise>
                   axutil_array_list_add(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:otherwise>
                </xsl:choose>
                <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;
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
                AXIS2_ENV_CHECK(env, -1);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, -1);
                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    return 0;
                }
                return axutil_array_list_size(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
            }
           </xsl:if> <!-- closes the isarray -->

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
               AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);
               

               <xsl:if test="@isarray or @ours or @type='axis2_char_t*' or @type='axutil_qname_t*' or @type='axutil_duration_t*' or @type='axutil_uri_t*' or @type='axutil_date_time_t*' or @type='axutil_base64_binary_t*'">
                <!-- handles arrays -->
                <xsl:if test="@isarray">
                  if (<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> != NULL)
                  {
                      count = axutil_array_list_size(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
                      for(i = 0; i &lt; count; i ++)
                      {
                         element = axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);
                </xsl:if>
            
                <!-- the following element can be inside array or exist independently-->
                if(<xsl:value-of select="$justPropertyInstanceName"/> != NULL)
                {
                   <!-- how to free all the ours things -->
                   <xsl:choose>
                     <xsl:when test="@ours">
                        adb_<xsl:value-of select="@type"/>_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='unsigned short' or $nativePropertyType='unsigned char' or $nativePropertyType='unsigned int' or $nativePropertyType='unsigned long' or $nativePropertyType='short' or $nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_bool_t' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='long'">
                       <xsl:if test="@isarray">
                        <!-- free ints, longs, float in array-->
                        /* we keep primtives as pointers in arrasy, so need to free them */
                        AXIS2_FREE(env-> allocator, element);
                       </xsl:if>
                     </xsl:when>
            
                     <!-- free axis2_char_t s -->
                     <xsl:when test="$nativePropertyType='axis2_char_t*'">
                        AXIS2_FREE(env-> allocator, <xsl:value-of select="$propertyInstanceName"/>);
                     </xsl:when>
            
                     <!-- free nodes -->
                     <xsl:when test="$nativePropertyType='axiom_node_t*'">
                      axiom_node_free_tree (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                      axutil_qname_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                      axutil_uri_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                      axutil_duration_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                      axutil_date_time_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                      axutil_base64_binary_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
                       <xsl:when test="$propertyType='axutil_duration_t*'">
                      axutil_duration_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <!--TODO: This should be extended for all the types that should be freed.. -->
                     <xsl:otherwise>
                       /* This is an unknown type or a primitive. Please free this manually*/
                     </xsl:otherwise>
                   </xsl:choose>
                   <xsl:value-of select="$justPropertyInstanceName"/> = NULL;
                }
            
                <!--/xsl:if-->
                <!-- close tags arrays -->
                <xsl:if test="@isarray">
                      }
                      axutil_array_list_free(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
                  }
                </xsl:if>
               </xsl:if> <!--close for test of primitive types -->
               <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_FALSE; 
               return AXIS2_SUCCESS;
           }

           /**
            * Check whether <xsl:value-of select="$propertyName"/> is nill
            */
           axis2_bool_t AXIS2_CALL
           <xsl:value-of select="$axis2_name"/>_is_<xsl:value-of select="$CName"/>_nil(
                   <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                   const axutil_env_t *env)
           {
               AXIS2_ENV_CHECK(env, AXIS2_TRUE);
               AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_TRUE);
               
               return !<xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/>;
           }

           /**
            * Set <xsl:value-of select="$propertyName"/> to nill (currently the same as reset)
            */
           axis2_status_t AXIS2_CALL
           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_nil(
                   <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                   const axutil_env_t *env)
           {
               return <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env);
           }

           <xsl:if test="@isarray">
           /**
            * Check whether <xsl:value-of select="$propertyName"/> is nill at i
            */
           axis2_bool_t AXIS2_CALL
           <xsl:value-of select="$axis2_name"/>_is_<xsl:value-of select="$CName"/>_nil_at(
                   <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                   const axutil_env_t *env, int i)
           {
               AXIS2_ENV_CHECK(env, AXIS2_TRUE);
               AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_TRUE);
               
               return (<xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> == AXIS2_FALSE ||
                        NULL == <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> || 
                        NULL == axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i));
           }

           /**
            * Set <xsl:value-of select="$propertyName"/> to nill at i
            */
           axis2_status_t AXIS2_CALL
           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_nil_at(
                   <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                   const axutil_env_t *env, int i)
           {
                void *element = NULL;
                int size = 0;
                int j;
                axis2_bool_t non_nil_exists = AXIS2_FALSE;

                int k = 0;

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);

                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL ||
                            <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> == AXIS2_FALSE)
                {
                    <!-- just assume it s null -->
                    non_nil_exists = AXIS2_FALSE;
                }
                else
                {
                    size = axutil_array_list_size(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
                    for(j = 0, k = 0; j &lt; size; j ++ )
                    {
                        if(i == j) continue; <!-- should not count the ith element -->
                        if(NULL != axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i))
                        {
                            k ++;
                            non_nil_exists = AXIS2_TRUE;
                            if( k >= <xsl:value-of select="@minOccurs"/>)
                            {
                                break;
                            }
                        }
                    }
                }
                <xsl:if test ="not(@nillabe) and not(@minOccurs='0')">
                   if(!non_nil_exists)
                   {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "All the elements in the array of <xsl:value-of select="$propertyName"/> is being set to NULL, but it is not a nullable or minOccurs=0 element");
                       return AXIS2_FAILURE;
                   }
                </xsl:if>

                if( k &lt; <xsl:value-of select="@minOccurs"/>)
                {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Size of the array of <xsl:value-of select="$propertyName"/> is beinng set to be smaller than the specificed number of minOccurs(<xsl:value-of select="@minOccurs"/>)");
                       return AXIS2_FAILURE;
                }
 
                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_FALSE;
                    <!-- just assume it s null -->
                    return AXIS2_SUCCESS;
                }

                /* check whether there already exist an element */
                element = axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);
                if(NULL != element)
                {
                  <!-- Some repeated code -->
                  <!-- For all inside the choose, propertyInstanceName = element -->
                  <xsl:choose>
                     <xsl:when test="@ours">
                        adb_<xsl:value-of select="@type"/>_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='unsigned short' or $nativePropertyType='unsigned char' or $nativePropertyType='unsigned int' or $nativePropertyType='unsigned long' or $nativePropertyType='short' or $nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_bool_t' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='long'">
                        <!-- free ints, longs, float in array-->
                        /* we keep primtives as pointers in arrasy, so need to free them */
                        AXIS2_FREE(env-> allocator, element);
                     </xsl:when>
            
                     <!-- free axis2_char_t s -->
                     <xsl:when test="$nativePropertyType='axis2_char_t*' and not(@isarray)">
                        AXIS2_FREE(env-> allocator, <xsl:value-of select="$propertyInstanceName"/>);
                     </xsl:when>
            
                     <!-- free nodes -->
                     <xsl:when test="$nativePropertyType='axiom_node_t*'">
                      axiom_node_free_tree (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                      axutil_qname_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                      axutil_uri_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                      axutil_duration_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                      axutil_date_time_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                      axutil_base64_binary_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
                       <xsl:when test="$propertyType='axutil_duration_t*'">
                      axutil_duration_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <!--TODO: This should be extended for all the types that should be freed.. -->
                     <xsl:otherwise>
                       /* This is an unknown type or a primitive. Please free this manually*/
                     </xsl:otherwise>
                  </xsl:choose>
                }

                <xsl:if test ="@nillabe or @minOccurs='0'">
                    if(!non_nil_exists)
                    {
                        <!-- No need to worry further -->
                        <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_FALSE;
                        axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, NULL);
                        return AXIS2_SUCCESS;
                    }
                </xsl:if>

                <!-- for all the other case just set the ith element NULL -->
                axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, NULL);
                
                return AXIS2_SUCCESS;

           }

           </xsl:if> <!-- end of checkiing is array -->
        </xsl:for-each>

    </xsl:template>

</xsl:stylesheet>
