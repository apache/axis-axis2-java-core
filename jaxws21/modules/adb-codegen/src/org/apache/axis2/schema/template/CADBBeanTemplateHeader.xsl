<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

     <!-- cater for the multiple classes - wrappped mode -->
    <xsl:template match="/classs">
        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="axis2_name">axi2_<xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="caps_axis2_name">AXIS2_<xsl:value-of select="@caps-name"/></xsl:variable>
        #ifndef <xsl:value-of select="$caps_axis2_name"/>_H
        #define <xsl:value-of select="$caps_axis2_name"/>_H

        /**
        * <xsl:value-of select="$axis2_name"/>.h
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2 Java version: #axisVersion# #today#
        */
        <xsl:for-each select="property">
          <xsl:if test="@ours">
          <xsl:variable name="propertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:value-of select="@type"></xsl:value-of></xsl:variable>
          #include "<xsl:value-of select="$propertyType"/>.h"
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
        <xsl:variable name="axis2_name">axis2_<xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="caps_axis2_name">AXIS2_<xsl:value-of select="@caps-name"/></xsl:variable>

        #ifndef <xsl:value-of select="$caps_axis2_name"/>_H
        #define <xsl:value-of select="$caps_axis2_name"/>_H

        /**
        * <xsl:value-of select="$axis2_name"/>.h
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2 version: #axisVersion# #today#
        */

        <xsl:for-each select="property">
          <xsl:if test="@ours">
          <xsl:variable name="propertyType"><xsl:if test="@ours">axis2_</xsl:if><xsl:value-of select="@type"></xsl:value-of></xsl:variable>
          #include "<xsl:value-of select="$propertyType"/>.h"
          </xsl:if>
        </xsl:for-each>
        <!--include special headers-->
        <xsl:if test="property/@type='axutil_date_time_t*'">
          #include &lt;axutil_date_time.h&gt;
        </xsl:if>
        <xsl:if test="property/@type='axutil_base64_binary_t*'">
          #include &lt;axutil_base64_binary.h&gt;
        </xsl:if>

        #include &lt;stdio.h&gt;
        #include &lt;axiom.h&gt;
        #include &lt;axutil_utils.h&gt;
        #include &lt;axiom_soap.h&gt;
        #include &lt;axis2_client.h&gt;

        #ifdef __cplusplus
        extern "C"
        {
        #endif

        #define AXIS2_DEFAULT_DIGIT_LIMIT 64
        /**
        *  <xsl:value-of select="$axis2_name"/> class class
        */
        typedef struct <xsl:value-of select="$axis2_name"/><xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t;

        AXIS2_EXTERN <xsl:value-of select="$axis2_name"/>_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_create(
            const axutil_env_t *env );

        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_free (
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

        <xsl:if test="not(@type)">
        axutil_qname_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_qname (
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);
        </xsl:if>

        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_serialize(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env,
            axiom_node_t* <xsl:value-of select="$name"/>_om_node, int has_parent);

        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_deserialize(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env, axiom_node_t* parent);

        <xsl:for-each select="property">
            <xsl:variable name="propertyType">
            <xsl:choose>
                <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                <xsl:when test="@ours">axis2_<xsl:value-of select="@type"/>_t*</xsl:when>
                <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
            </xsl:choose>
            </xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

        /**
         * getter for <xsl:value-of select="$propertyName"/>.
         */
        <xsl:value-of select="$propertyType"/> AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

        /**
         * setter for <xsl:value-of select="$propertyName"/>
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env,
            <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text> param_<xsl:value-of select="$CName"/>);

        <xsl:if test="@isarray">
        /**
        * resetter for <xsl:value-of select="$propertyName"/>
        */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);
        </xsl:if>
        </xsl:for-each>

     #ifdef __cplusplus
     }
     #endif

     #endif /* <xsl:value-of select="$caps_axis2_name"/>_H */
    </xsl:template>
</xsl:stylesheet>
