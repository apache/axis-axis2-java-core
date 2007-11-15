<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

     <!-- cater for the multiple classes - wrappped mode -->
    <xsl:template match="/beans">
        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="axis2_name">adb_<xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="caps_axis2_name">ADB_<xsl:value-of select="@caps-name"/></xsl:variable>
        #ifndef <xsl:value-of select="$caps_axis2_name"/>_H
        #define <xsl:value-of select="$caps_axis2_name"/>_H

        /**
        * <xsl:value-of select="$axis2_name"/>.h
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2/Java version: #axisVersion# #today#
        */

        #include &lt;stdio.h&gt;
        #include &lt;axiom.h&gt;
        #include &lt;axis2_util.h&gt;
        #include &lt;axiom_soap.h&gt;
        #include &lt;axis2_client.h&gt;

        #ifdef __cplusplus
        extern "C"
        {
        #endif

        #define AXIS2_DEFAULT_DIGIT_LIMIT 128

        /**
        *  <xsl:value-of select="$axis2_name"/> wrapped class classes ( structure for C )
        */

        <xsl:apply-templates/>


        #ifdef __cplusplus
        }
        #endif

        #endif /* <xsl:value-of select="$caps_axis2_name"/>_H */
    </xsl:template>

    <!--cater for the multiple classes - unwrappped mode -->
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>


    <xsl:template match="class">
        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="axis2_name">adb_<xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="caps_axis2_name">ADB_<xsl:value-of select="@caps-name"/></xsl:variable>

        #ifndef <xsl:value-of select="$caps_axis2_name"/>_H
        #define <xsl:value-of select="$caps_axis2_name"/>_H

       /**
        * <xsl:value-of select="$axis2_name"/>.h
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2/Java version: #axisVersion# #today#
        */

       /**
        *  <xsl:value-of select="$axis2_name"/> class
        */
        typedef struct <xsl:value-of select="$axis2_name"/><xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t;

        <xsl:for-each select="property">
          <xsl:if test="@ours">
          <xsl:variable name="propertyType"><xsl:if test="@ours">adb_</xsl:if><xsl:value-of select="@type"></xsl:value-of></xsl:variable>
          #include "<xsl:value-of select="$propertyType"/>.h"
          </xsl:if>
        </xsl:for-each>
        <!--include special headers-->
        <xsl:for-each select="property[@type='axutil_date_time_t*']">
          <xsl:if test="position()=1">
            #include &lt;axutil_date_time.h&gt;
          </xsl:if>
        </xsl:for-each>
        <xsl:for-each select="property[@type='axutil_base64_binary_t*']">
          <xsl:if test="position()=1">
            #include &lt;axutil_base64_binary.h&gt;
          </xsl:if>
        </xsl:for-each>
        <xsl:for-each select="property[@type='axutil_duration_t*']">
          <xsl:if test="position()=1">
            #include &lt;axutil_duration.h&gt;
          </xsl:if>
        </xsl:for-each>

        #include &lt;stdio.h&gt;
        #include &lt;axiom.h&gt;
        #include &lt;axis2_util.h&gt;
        #include &lt;axiom_soap.h&gt;
        #include &lt;axis2_client.h&gt;

        #ifdef __cplusplus
        extern "C"
        {
        #endif

        #define AXIS2_DEFAULT_DIGIT_LIMIT 64

        /**
         * Constructor for creating <xsl:value-of select="$axis2_name"/>_t
         * @param env pointer to environment struct
         * @return newly created <xsl:value-of select="$axis2_name"/>_t object
         */
        AXIS2_EXTERN <xsl:value-of select="$axis2_name"/>_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_create(
            const axutil_env_t *env );

        /**
         * Free <xsl:value-of select="$axis2_name"/>_t object
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object to free
         * @param env pointer to environment struct
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_free (
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

        <xsl:if test="@simple">
            /**
             * Deserialize the content from a string to adb objects
             * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/><xsl:text> </xsl:text> <xsl:value-of select="$axis2_name"/>_t object
             * @param env pointer to environment struct
             * @param node_value to deserialize
             * @param parent_element The parent element if it is an element, NULL otherwise
             * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
             */
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_deserialize_from_string(
                            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                                            const axutil_env_t *env,
                                            axis2_char_t *node_value,
                                            axiom_node_t *parent);
        </xsl:if>

        /**
         * Deserialize an XML to adb objects
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param parent double pointer to the parent node to deserialize
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_deserialize(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env, axiom_node_t** parent);
            
            <!-- Here the double pointer is used to change the parent pointer - This can be happned when deserialize is called in a particle class -->

       /**
         * Declare namespace in the parent node (Pass either the parent element or the stream and set the other NULL)
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/><xsl:text> </xsl:text> <xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param stream if parent is not an element and is a stream, pass the stream, NULL otherwise
         */
       void AXIS2_CALL
       <xsl:value-of select="$axis2_name"/>_declare_parent_namespaces(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env, axiom_element_t *parent_element, axutil_stream_t *stream);

        <xsl:if test="@simple">
        /**
         * Serialize to a String from the adb objects
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return serialized string
         */
            axis2_char_t* AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_serialize_to_string(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env);
        </xsl:if>

        /**
         * Serialize to an XML from the adb objects
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param <xsl:value-of select="$name"/>_om_node node to serialize from
         * @param tag_closed whether the parent tag is closed or not
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_serialize(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env,
            axiom_node_t* <xsl:value-of select="$name"/>_om_node, int tag_closed);


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
        /**
         * Getter for <xsl:value-of select="$propertyName"/>.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return <xsl:value-of select="$paramComment"/>
         */
        <xsl:value-of select="$propertyType"/> AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

        /**
         * Setter for <xsl:value-of select="$propertyName"/>.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param param_<xsl:value-of select="$CName"/><xsl:text> </xsl:text> <xsl:value-of select="$paramComment"/>
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env,
            <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text> param_<xsl:value-of select="$CName"/>);

        <xsl:if test="@isarray">
        /**
         * Resetter for <xsl:value-of select="$propertyName"/>
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

        /**
         * Get ith element of <xsl:value-of select="$propertyName"/>.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param i index of the item to return
         * @return ith <xsl:value-of select="$nativePropertyType"/> of the array
         */
        <xsl:value-of select="$nativePropertyType"/> AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>_at(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, int i);

        /**
         * Add to <xsl:value-of select="$propertyName"/>.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param <xsl:text>param_</xsl:text> <xsl:value-of select="$CName"/> element to add <xsl:value-of select="$nativePropertyType"/> to the array
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env,
                <xsl:value-of select="$nativePropertyType"/><xsl:text> param_</xsl:text> <xsl:value-of select="$CName"/>);

        /**
         * Get the size of the <xsl:value-of select="$propertyName"/> array.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct.
         * @return the size of the <xsl:value-of select="$propertyName"/> array.
         */
        int AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_sizeof_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env);
        </xsl:if> <!-- closes isarray -->

        /**
         * Check whether the <xsl:value-of select="$propertyName"/> is a particle class (E.g. A group)
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object.
         * @param env pointer to environment struct.
         * @return whether this is a particle class.
         */
        axis2_bool_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_is_particle(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env);


       </xsl:for-each>

     #ifdef __cplusplus
     }
     #endif

     #endif /* <xsl:value-of select="$caps_axis2_name"/>_H */
    </xsl:template>
</xsl:stylesheet>
