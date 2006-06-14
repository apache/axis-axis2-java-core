<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

     <!-- cater for the multiple classes - wrappped mode -->
    <xsl:template match="/beans">
        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="axis2_name">axis2_<xsl:value-of select="@name"/></xsl:variable>
        /**
        * <xsl:value-of select="$axis2_name"/>.c
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2 Java version: #axisVersion# #today#
        */

        /**
        *  <xsl:value-of select="$axis2_name"/> wrapped bean classes ( C Implementation )
        */
        #include "<xsl:value-of select="$axis2_name"/>.h"

        <xsl:apply-templates/>

    </xsl:template>

    <!--cater for the multiple classes - unwrappped mode -->
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>


    <xsl:template match="bean">
        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="axis2_name">axis2_<xsl:value-of select="@name"/></xsl:variable>

        <xsl:variable name="nsuri"><xsl:value-of select="@nsuri"/></xsl:variable>
        <xsl:variable name="nsprefix"><xsl:value-of select="@nsprefix"/></xsl:variable>
        /**
        * <xsl:value-of select="$axis2_name"/>.h
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2 version: #axisVersion# #today#
        */

        #include "<xsl:value-of select="$axis2_name"/>.h"
        /**
        *  <xsl:value-of select="$axis2_name"/> stucture
        */
        typedef struct <xsl:value-of select="$axis2_name"/><xsl:text>_impl </xsl:text><xsl:value-of select="$axis2_name"/>_impl_t;

        struct <xsl:value-of select="$axis2_name"/>_impl
        {
            <xsl:value-of select="$axis2_name"/>_t <xsl:value-of select="$name"/>;

            axis2_qname_t* qname;

            <xsl:for-each select="property">
                <xsl:variable name="propertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if><xsl:if test="@isarray">*</xsl:if> </xsl:variable>
                <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
                <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>

                <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text>attr_<xsl:value-of select="$javaName"/>;
                <xsl:if test="@isarray">
                  int  attr_<xsl:value-of select="$javaName"/>_length;
                </xsl:if>
            </xsl:for-each>
        };

        #define AXIS2_INTF_TO_IMPL(<xsl:value-of select="$name"/>) \
            ((<xsl:value-of select="$axis2_name"/>_impl_t *) <xsl:value-of select="$name"/>)

        /************************* Function prototypes ********************************/

        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_free (
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axis2_env_t *env);

        axis2_qname_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_qname (
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axis2_env_t *env);

        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_parse_om (
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axis2_env_t *env,
                axiom_node_t* <xsl:value-of select="$name"/>_om_node);

        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_build_om (
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axis2_env_t *env, axiom_node_t* parent,
                axiom_namespace_t* xsi, axiom_namespace_t* xsd);

        <xsl:for-each select="property">
            <xsl:variable name="propertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if><xsl:if test="@isarray">*</xsl:if> </xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>


            <xsl:value-of select="$propertyType"/> AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$propertyName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axis2_env_t *env<xsl:if test="@isarray">, int* length</xsl:if>);

            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$propertyName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axis2_env_t *env,
                    <xsl:value-of select="$propertyType"/> param<xsl:if test="@isarray">,int length</xsl:if>);

        </xsl:for-each>

       /************************* Function Implmentations ********************************/
        AXIS2_EXTERN <xsl:value-of select="$axis2_name"/>_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_create(
            const axis2_env_t *env )
        {
            <xsl:value-of select="$axis2_name"/>_impl_t* <xsl:value-of select="$name"/>_impl = NULL;
            axis2_qname_t* qname = NULL;
            AXIS2_ENV_CHECK(env, NULL);

            <xsl:value-of select="$name"/>_impl = (<xsl:value-of select="$axis2_name"/>_impl_t *) AXIS2_MALLOC(env->
                allocator, sizeof(<xsl:value-of select="$axis2_name"/>_impl_t));

            if(NULL == <xsl:value-of select="$name"/>_impl)
            {
                AXIS2_ERROR_SET(env->error, AXIS2_ERROR_NO_MEMORY, AXIS2_FAILURE);
                return NULL;
            }

            <xsl:value-of select="$name"/>_impl-> <xsl:value-of select="$name"/>.ops =
                            AXIS2_MALLOC (env->allocator, sizeof(<xsl:value-of select="$axis2_name"/>_ops_t));
            if(NULL == <xsl:value-of select="$name"/>_impl-> <xsl:value-of select="$name"/>.ops)
            {
                axis2_<xsl:value-of select="$name"/>_free(&amp;(<xsl:value-of select="$name"/>_impl-><xsl:value-of select="$name"/>), env);
                AXIS2_ERROR_SET(env->error, AXIS2_ERROR_NO_MEMORY, AXIS2_FAILURE);
                return NULL;
            }

        <xsl:for-each select="property">
            <xsl:variable name="propertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if><xsl:if test="@isarray">*</xsl:if> </xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
            <xsl:if test="@ours">
                <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/>  = NULL;
            </xsl:if>
        </xsl:for-each>

            <xsl:value-of select="$name"/>_impl-><xsl:value-of select="$name"/>.ops->free = <xsl:value-of select="$axis2_name"/>_free;
            <xsl:value-of select="$name"/>_impl-><xsl:value-of select="$name"/>.ops->get_qname = <xsl:value-of select="$axis2_name"/>_get_qname;
            <xsl:value-of select="$name"/>_impl-><xsl:value-of select="$name"/>.ops->parse_om = <xsl:value-of select="$axis2_name"/>_parse_om;
            <xsl:value-of select="$name"/>_impl-><xsl:value-of select="$name"/>.ops->build_om = <xsl:value-of select="$axis2_name"/>_build_om;
            <xsl:for-each select="property">
                <xsl:variable name="propertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if><xsl:if test="@isarray">*</xsl:if> </xsl:variable>
                <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
                <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
                <xsl:value-of select="$name"/>_impl-><xsl:value-of select="$name"/>.ops->get_<xsl:value-of select="$propertyName"/> = <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$propertyName"/>;
                <xsl:value-of select="$name"/>_impl-><xsl:value-of select="$name"/>.ops->set_<xsl:value-of select="$propertyName"/> = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$propertyName"/>;
            </xsl:for-each>
            <xsl:choose>
                <xsl:when test="@type">/* This type was generated from the piece of schema that had
                    name = <xsl:value-of select="@originalName"/>
                    Namespace URI = <xsl:value-of select="@nsuri"/>
                    Namespace Prefix = <xsl:value-of select="@nsprefix"/>
                    */
                    qname  = NULL;
                </xsl:when>
                <xsl:otherwise>
                    qname =  axis2_qname_create (env,
                                    "<xsl:value-of select="@originalName"/>",
                                    "<xsl:value-of select="@nsuri"/>",
                                    "<xsl:value-of select="@nsprefix"/>");

                </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="$name"/>_impl->qname = qname;

            return &amp;(<xsl:value-of select="$name"/>_impl-><xsl:value-of select="$name"/>);
         }

        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_free (
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axis2_env_t *env)
        {
            <xsl:value-of select="$axis2_name"/>_impl_t *<xsl:value-of select="$name"/>_impl = NULL;

            AXIS2_ENV_CHECK(env, AXIS2_FAILURE);

            <xsl:value-of select="$name"/>_impl = AXIS2_INTF_TO_IMPL(<xsl:value-of select="$name"/>);

            <xsl:for-each select="property">
            <xsl:variable name="propertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if><xsl:if test="@isarray">*</xsl:if> </xsl:variable>
            <xsl:variable name="singlepropertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if> </xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
            <xsl:variable name="capspropertyType"><xsl:if test="@ours">AXIS2_</xsl:if><xsl:value-of select="@caps-type"></xsl:value-of></xsl:variable>

              <xsl:if test="@ours">

                if ( <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> )
                {
                    <xsl:if test="@isarray">
                    while (<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length > 0 )
                    {
                       <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length --;
                       <xsl:value-of select="$capspropertyType"/>_FREE (
                        <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>[<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length], env );
                       <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>[<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length] = NULL;
                    }
                    free ( <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> );
                    </xsl:if>
                    <xsl:if test="not(@isarray)">
                     <xsl:value-of select="$capspropertyType"/>_FREE (
                        <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>, env );
                    </xsl:if>
                    <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> = NULL;
                }
              </xsl:if>
              <xsl:if test="$singlepropertyType='axis2_char_t*'">


                if ( <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> )
                {
                    <xsl:if test="@isarray">
                    while (<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length > 0 )
                    {
                       <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length --;
                       free (
                        <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>[<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length] );
                       <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>[<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length] = NULL;
                    }
                    </xsl:if>
                    free ( <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> );
                    <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> = NULL;
                }
              </xsl:if>
              <xsl:if test="$singlepropertyType='axiom_node_t*'">

                if ( <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> )
                {

                    <xsl:if test="@isarray">
                    while (<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length > 0 )
                    {
                       <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length --;
                       AXIOM_NODE_FREE_TREE (
                        <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>[<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length],env );
                       <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>[<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length] = NULL;
                    }
                    free ( <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> );
                    </xsl:if>
                    <xsl:if test="not(@isarray)">
                    AXIOM_NODE_FREE_TREE ( <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>, env );
                    </xsl:if>
                    <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> = NULL;
                }
              </xsl:if>
              <xsl:if test="$singlepropertyType='axis2_date_time_t*'">

                if ( <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> )
                {

                    <xsl:if test="@isarray">
                    while (<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length > 0 )
                    {
                       <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length --;
                       AXIS2_DATE_TIME_FREE(
                        <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>[<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length],env );
                       <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>[<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length] = NULL;
                    }
                    free ( <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> );
                    </xsl:if>
                    <xsl:if test="not(@isarray)">
                    AXIS2_DATE_TIME_FREE ( <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>, env );
                    </xsl:if>
                    <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> = NULL;
                }
              </xsl:if>
              <xsl:if test="$singlepropertyType='axis2_base64_binary_t*'">

                if ( <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> )
                {

                    <xsl:if test="@isarray">
                    while (<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length > 0 )
                    {
                       <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length --;
                       AXIS2_BASE64_BINARY_FREE (
                        <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>[<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length],env );
                       <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>[<xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length] = NULL;
                    }
                    free ( <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> );
                    </xsl:if>
                    <xsl:if test="not(@isarray)">
                    AXIS2_BASE64_BINARY_FREE ( <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>, env );
                    </xsl:if>
                    <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/> = NULL;
                }
              </xsl:if>
            </xsl:for-each>

            if(<xsl:value-of select="$name"/>->ops)
            {
                AXIS2_FREE(env->allocator, <xsl:value-of select="$name"/>->ops);
                <xsl:value-of select="$name"/>->ops = NULL;
            }

            if(<xsl:value-of select="$name"/>_impl->qname )
            {
                AXIS2_QNAME_FREE (<xsl:value-of select="$name"/>_impl->qname, env);
                <xsl:value-of select="$name"/>_impl->qname = NULL;
            }

            if(<xsl:value-of select="$name"/>_impl)
            {
                AXIS2_FREE( env->allocator, <xsl:value-of select="$name"/>_impl);
                <xsl:value-of select="$name"/>_impl = NULL;
            }
            return AXIS2_SUCCESS;
        }

        axis2_qname_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_qname (
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axis2_env_t *env)
        {
            <xsl:value-of select="$axis2_name"/>_impl_t *<xsl:value-of select="$name"/>_impl = NULL;

            AXIS2_ENV_CHECK(env, AXIS2_FAILURE);

            <xsl:value-of select="$name"/>_impl = AXIS2_INTF_TO_IMPL(<xsl:value-of select="$name"/>);

            return <xsl:value-of select="$name"/>_impl-> qname;
        }

        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_parse_om (
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axis2_env_t *env,
                axiom_node_t* <xsl:value-of select="$name"/>_om_node)
        {
            <xsl:value-of select="$axis2_name"/>_impl_t *<xsl:value-of select="$name"/>_impl = NULL;
            axiom_node_t* current_node = NULL;

            <xsl:for-each select="property">
             <xsl:if test="position()=1">
              <xsl:if test="not(@ours)">
               axiom_element_t* text_element = NULL;
               axis2_char_t* text_result = NULL;
              </xsl:if>
              <xsl:if test="@isarray">
               int index = 0;
              </xsl:if>
              <xsl:if test="@ours">
                axiom_node_t* struct_node = NULL;
              </xsl:if>
             </xsl:if>
            </xsl:for-each>


            AXIS2_ENV_CHECK(env, AXIS2_FAILURE);

            <xsl:value-of select="$name"/>_impl = AXIS2_INTF_TO_IMPL(<xsl:value-of select="$name"/>);

            <xsl:for-each select="property">
              <xsl:variable name="propertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if><xsl:if test="@isarray">*</xsl:if> </xsl:variable>
              <xsl:variable name="singlepropertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if> </xsl:variable>
              <xsl:variable name="prefixpropertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:value-of select="@type"></xsl:value-of><xsl:if test="@ours"></xsl:if></xsl:variable>
              <xsl:variable name="capspropertyType"><xsl:if test="@ours">AXIS2_</xsl:if><xsl:value-of select="@caps-type"></xsl:value-of></xsl:variable>
              <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
              <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
              <xsl:variable name="ours"><xsl:value-of select="@ours"></xsl:value-of></xsl:variable>

               <xsl:choose>
                <xsl:when test="position()=1">
                  <xsl:if test="not(@isarray)">
                  current_node = <xsl:value-of select="$name"/>_om_node;
                  </xsl:if>
                  <xsl:if test="@isarray">
                  current_node = AXIOM_NODE_GET_FIRST_CHILD (<xsl:value-of select="$name"/>_om_node, env );
                  </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                  current_node = AXIOM_NODE_GET_NEXT_SIBLING ( current_node, env );
                </xsl:otherwise>
              </xsl:choose>
              <xsl:if test="@isarray">
               for ( index = 0; current_node ; index ++ )
               {
                  if ( 0 ==index )
                  {
                     <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/> =
                         (<xsl:value-of select="$propertyType"/>)malloc ( sizeof ( <xsl:value-of select="$singlepropertyType"/> ) );
                  }
                  else
                  {
                     <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/> =
                         (<xsl:value-of select="$propertyType"/>)realloc ( <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/>, sizeof ( <xsl:value-of select="$singlepropertyType"/> )* (index+1) );
                  }
              </xsl:if>
              <xsl:choose>
                <xsl:when test="@ours">
                  struct_node = AXIOM_NODE_GET_FIRST_CHILD ( current_node, env );
                  <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if> = <xsl:value-of select="$prefixpropertyType"></xsl:value-of>_create (env );

                  <xsl:value-of select="$capspropertyType"/>_PARSE_OM ( <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if>, env, struct_node );
                </xsl:when>
                <xsl:otherwise>
                    text_element = (axiom_element_t*)AXIOM_NODE_GET_DATA_ELEMENT(current_node, env);
                    text_result = AXIOM_ELEMENT_GET_TEXT(text_element, env, current_node );
                    <xsl:choose>
                      <xsl:when test="$singlepropertyType='axis2_char_t*'">
                        <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if> =strdup(text_result);
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='int'">
                        <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if> =atoi (text_result);
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='float'">
                        <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if> =atof (text_result);
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='long'">
                        <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if> =atol (text_result);
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='axiom_node_t*'">
                        <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if> =current_node;
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='axis2_bool_t'">
                        if ( !strcmp ( text_result, "true" ) || !strcmp ( text_result, "TRUE") )
                        {
                          <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if> =AXIS2_TRUE;
                        }
                        else
                        {
                          <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if> =AXIS2_FALSE;
                        }
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='axis2_date_time_t*'">
                        <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if> =
                                                                 axis2_date_time_create (env );
                        AXIS2_DATE_TIME_DESERIALIZE_DATE_TIME( <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if>,
                                                               env, text_result );
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='axis2_base64_binary_t*'">
                        <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if> =
                                                                 axis2_base64_binary_create (env );
                        AXIS2_BASE64_BINARY_SET_ENCODED_BINARY( <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if>,
                                                               env, text_result );
                      </xsl:when>
                      <xsl:otherwise>
                        /** imposible to handle the request type - so please do it manually*/
                      </xsl:otherwise>
                    </xsl:choose>
                 </xsl:otherwise>
               </xsl:choose>
              <xsl:if test="@isarray">
                  current_node = AXIOM_NODE_GET_NEXT_SIBLING ( current_node, env );
               }
               <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length = index ;
              </xsl:if>
           </xsl:for-each>

           return AXIS2_SUCCESS;
        }
        
        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_build_om (
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axis2_env_t *env, axiom_node_t* parent ,
                axiom_namespace_t* xsi, axiom_namespace_t* xsd)
        {
            <xsl:value-of select="$axis2_name"/>_impl_t *<xsl:value-of select="$name"/>_impl = NULL;
            axiom_node_t* om_node = NULL;
            axiom_node_t* current_node = NULL;
            axiom_element_t* current_element = NULL;
            axiom_attribute_t* text_attri = NULL;
            axiom_namespace_t* ns1 = NULL;
            <xsl:for-each select="property">
             <xsl:if test="position()=1">
               <xsl:choose>
                <xsl:when test="@ours">

                </xsl:when>
                <xsl:otherwise>
                 axis2_char_t* text_value = NULL;
                </xsl:otherwise>
               </xsl:choose>
               <xsl:if test="@isarray">
                int index = 0;
                axiom_node_t* array_node = NULL;
               </xsl:if>
              </xsl:if>
            </xsl:for-each>
            AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
            <xsl:value-of select="$name"/>_impl = AXIS2_INTF_TO_IMPL(<xsl:value-of select="$name"/>);
            <xsl:for-each select="property">
              <xsl:variable name="propertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if><xsl:if test="@isarray">*</xsl:if> </xsl:variable>
              <xsl:variable name="singlepropertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if> </xsl:variable>
              <xsl:variable name="capspropertyType"><xsl:if test="@ours">AXIS2_</xsl:if><xsl:value-of select="@caps-type"></xsl:value-of></xsl:variable>
              <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
              <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
              <xsl:variable name="ours"><xsl:value-of select="@ours"></xsl:value-of></xsl:variable>
              <xsl:variable name="arrayele"><xsl:value-of select="@arrayele"></xsl:value-of></xsl:variable>



              <xsl:if test="@isarray">
               <xsl:if test="position()=1">

                 if ( NULL == xsi &amp;&amp; xsd != NULL)
                 {
                    ns1 = axiom_namespace_create (env,
                                                  "<xsl:value-of select="$nsuri"/>",
                                                  "<xsl:value-of select="$nsprefix"/>");
                    current_element = axiom_element_create (env, parent, "<xsl:value-of select="$propertyName"/>", ns1 , &amp;array_node);
                 }
                 else
                 {
                    current_element = axiom_element_create (env, parent, "<xsl:value-of select="$propertyName"/>", NULL , &amp;array_node);
                 }
                 parent= array_node;
              </xsl:if>
              for ( index = 0; index &lt; <xsl:value-of select="$name"/>_impl->attr_<xsl:value-of select="$javaName"/>_length ; index ++ )
              {
              </xsl:if>

              <xsl:choose>
                <xsl:when test="@ours">
                 current_element = axiom_element_create (env, parent, "<xsl:if test="not(@isarray)"><xsl:value-of select="$name"/></xsl:if><xsl:if test="@isarray"><xsl:value-of select="$arrayele"/>element</xsl:if>", NULL , &amp;current_node);
                </xsl:when>
                <xsl:otherwise>
                 if ( NULL == xsi &amp;&amp; xsd != NULL)
                 {
                    ns1 = axiom_namespace_create (env,
                                                  "<xsl:value-of select="$nsuri"/>",
                                                  "<xsl:value-of select="$nsprefix"/>");
                    current_element = axiom_element_create (env, parent, "<xsl:value-of select="$propertyName"/>", ns1 , &amp;current_node);
                 }
                 else
                 {
                    current_element = axiom_element_create (env, parent, "<xsl:value-of select="$propertyName"/>", NULL , &amp;current_node);
                  }
                </xsl:otherwise>
              </xsl:choose>

              <xsl:choose>
                <xsl:when test="@ours">
                  <xsl:value-of select="$capspropertyType"/>_BUILD_OM ( <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[index ]</xsl:if>, env, current_node, xsi, xsd );
                  text_attri = axiom_attribute_create (env, "type", "<xsl:value-of select="$nsprefix"/>:<xsl:value-of select="@type"/>", xsi);
                  AXIOM_ELEMENT_ADD_ATTRIBUTE (current_element, env, text_attri, current_node);
                  <xsl:if test="not(@isarray)">
                  ns1 = axiom_namespace_create (env,
                                                  "<xsl:value-of select="$nsuri"/>",
                                                  "<xsl:value-of select="$nsprefix"/>");
                  AXIOM_ELEMENT_DECLARE_NAMESPACE (current_element, env,
                                                      current_node, ns1);
                  </xsl:if>
                </xsl:when>
                <xsl:otherwise>

                    <xsl:choose>
                      <xsl:when test="$singlepropertyType='axis2_char_t*'">
                        text_value = strdup (<xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[index ]</xsl:if>) ;

                        if ( xsi != NULL &amp;&amp; xsd != NULL)
                        {
                            text_attri = axiom_attribute_create (env, "type", "xsd:string", xsi);
                            AXIOM_ELEMENT_ADD_ATTRIBUTE (current_element, env, text_attri, current_node);
                        }
                        AXIOM_ELEMENT_SET_TEXT(current_element, env, text_value, current_node);
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='int'">
                        text_value = (axis2_char_t*) malloc ( sizeof ( axis2_char_t) * AXIS2_DEFAULT_DIGIT_LIMIT );
                        sprintf ( text_value, "%d", <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[index ]</xsl:if> );

                        if ( xsi != NULL &amp;&amp; xsd != NULL)
                        {
                            text_attri = axiom_attribute_create (env, "type", "xsd:integer", xsi);
                            AXIOM_ELEMENT_ADD_ATTRIBUTE (current_element, env, text_attri, current_node);
                        }
                        AXIOM_ELEMENT_SET_TEXT(current_element, env, text_value, current_node);
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='float'">
                        text_value = (axis2_char_t*) malloc ( sizeof ( axis2_char_t) * AXIS2_DEFAULT_DIGIT_LIMIT );
                        sprintf ( text_value, "%f", <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[index ]</xsl:if> );

                        if ( xsi != NULL &amp;&amp; xsd != NULL)
                        {
                            text_attri = axiom_attribute_create (env, "type", "xsd:float", xsi);
                            AXIOM_ELEMENT_ADD_ATTRIBUTE (current_element, env, text_attri, current_node);
                        }
                        AXIOM_ELEMENT_SET_TEXT(current_element, env, text_value, current_node);
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='long'">
                        text_value = (axis2_char_t*) malloc ( sizeof ( axis2_char_t) * AXIS2_DEFAULT_DIGIT_LIMIT );
                        sprintf ( text_value, "%d", <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[index ]</xsl:if> );

                        if ( xsi != NULL &amp;&amp; xsd != NULL)
                        {
                            text_attri = axiom_attribute_create (env, "type", "xsd:long", xsi);
                            AXIOM_ELEMENT_ADD_ATTRIBUTE (current_element, env, text_attri, current_node);
                        }
                        AXIOM_ELEMENT_SET_TEXT(current_element, env, text_value, current_node);
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='axis2_bool_t'">
                        text_value = (axis2_char_t*) malloc ( sizeof ( axis2_char_t) * AXIS2_DEFAULT_DIGIT_LIMIT );
                        sprintf ( text_value, "%s", <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[index ]</xsl:if>?"TRUE":"FALSE" );
                        if ( xsi != NULL &amp;&amp; xsd != NULL)
                        {
                            text_attri = axiom_attribute_create (env, "type", "xsd:boolean", xsi);
                            AXIOM_ELEMENT_ADD_ATTRIBUTE (current_element, env, text_attri, current_node);
                        }
                        AXIOM_ELEMENT_SET_TEXT(current_element, env, text_value, current_node);
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='axiom_node_t*'">
                        if ( xsi != NULL &amp;&amp; xsd != NULL)
                        {
                            text_attri = axiom_attribute_create (env, "type", "xsd:any", xsi);
                            AXIOM_ELEMENT_ADD_ATTRIBUTE (current_element, env, text_attri, current_node);
                        }
                        AXIOM_NODE_ADD_CHILD( current_node, env, <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[index ]</xsl:if>);
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='axis2_date_time_t*'">
                        if ( xsi != NULL &amp;&amp; xsd != NULL)
                        {
                            text_attri = axiom_attribute_create (env, "type", "xsd:dateTime", xsi);
                            AXIOM_ELEMENT_ADD_ATTRIBUTE (current_element, env, text_attri, current_node);
                        }
                        text_value = AXIS2_DATE_TIME_SERIALIZE_DATE_TIME( <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if>, env);
                        AXIOM_ELEMENT_SET_TEXT(current_element, env, text_value, current_node);
                      </xsl:when>
                      <xsl:when test="$singlepropertyType='axis2_base64_binary_t*'">
                        if ( xsi != NULL &amp;&amp; xsd != NULL)
                        {
                            text_attri = axiom_attribute_create (env, "type", "xsd:base64", xsi);
                            AXIOM_ELEMENT_ADD_ATTRIBUTE (current_element, env, text_attri, current_node);
                        }
                        text_value = AXIS2_BASE64_BINARY_GET_ENCODED_BINARY(<xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/><xsl:if test="@isarray">[ index]</xsl:if>, env);
                        AXIOM_ELEMENT_SET_TEXT(current_element, env, text_value, current_node);
                      </xsl:when>
                      <xsl:otherwise>
                        /** imposible to handle the request type - so please do it manually*/
                      </xsl:otherwise>
                    </xsl:choose>
                 </xsl:otherwise>
               </xsl:choose>
               <xsl:if test="position()=1">
                <xsl:if test="not(@isarray)">
                 om_node = current_node;
                </xsl:if>
               </xsl:if>
               <xsl:if test="@isarray">
               }
               om_node = array_node;
               </xsl:if>
           </xsl:for-each>

           return om_node;         
        }


        <xsl:for-each select="property">
            <xsl:variable name="propertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if><xsl:if test="@isarray">*</xsl:if></xsl:variable>
            <xsl:variable name="capspropertyType"><xsl:if test="@ours">AXIS2_</xsl:if><xsl:value-of select="@caps-type"></xsl:value-of></xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>


            <xsl:value-of select="$propertyType"/> AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$propertyName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axis2_env_t *env<xsl:if test="@isarray">,int* length</xsl:if>)
            {
                <xsl:value-of select="$axis2_name"/>_impl_t *<xsl:value-of select="$name"/>_impl = NULL;

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);

                <xsl:value-of select="$name"/>_impl = AXIS2_INTF_TO_IMPL(<xsl:value-of select="$name"/>);

                <xsl:if test="@isarray">*length = <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/>_length;</xsl:if>
                return <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/>;
            }

            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$propertyName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axis2_env_t *env,
                    <xsl:value-of select="$propertyType"/> param<xsl:if test="@isarray">,int length</xsl:if>)
            {
                <xsl:value-of select="$axis2_name"/>_impl_t *<xsl:value-of select="$name"/>_impl = NULL;

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);

                <xsl:value-of select="$name"/>_impl = AXIS2_INTF_TO_IMPL(<xsl:value-of select="$name"/>);

                <xsl:if test="@isarray"><xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/>_length = length;
                </xsl:if>
                <xsl:value-of select="$name"/>_impl-> attr_<xsl:value-of select="$javaName"/> = param;
                return AXIS2_SUCCESS;
            }

        </xsl:for-each>

    </xsl:template>

</xsl:stylesheet>
