<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <!-- import the databinding template-->
    <xsl:include href="databindsupporter"/>
    <!-- import the other templates for databinding
         Note  -  these names would be handled by a special
         URI resolver during the xslt transformations
     -->
    <xsl:include href="externalTemplate"/>
    
    
    <xsl:include href="policyExtensionTemplate"/>

    <xsl:template match="/class">
        <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
        <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>
        <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
        <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
        <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
        <xsl:variable name="soapVersion"><xsl:value-of select="@soap-version"/></xsl:variable>
        /**
        * <xsl:value-of select="@name"/>.h
        *
        * This file was auto-generated from WSDL for Axis2/C - stub code
        * by the Apache Axis2/Java version: #axisVersion# #today#
        */

        #include &lt;stdio.h&gt;
        #include &lt;axis2_om.h&gt;
        #include &lt;axis2_util.h&gt;
        #include &lt;axis2_soap.h&gt;
        #include &lt;axis2_client.h&gt;
        #include &lt;axis2_stub.h&gt;

        /* function prototypes - for header file*/
        /**
         * axis2_create_<xsl:value-of select="$interfaceName"/>_stub 
         * create and return the stub with services populated
         * params - env : environment ( mandatory)
         *        - client_home : Axis2/C home ( mandatory )
         *        - endpoint_ref : service endpoint ( optional ) - if NULL default picked from wsdl used
         */
        axis2_stub_t*
        axis2_create_<xsl:value-of select="$interfaceName"/>_stub (axis2_env_t **env,
                                        axis2_char_t *client_home,
                                        axis2_endpoint_ref_t *endpoint_ref);
        /**
         * axis2_populate_axis_service
         * populate the svc in stub with the service and operations
         */
        void axis2_populate_axis_service( axis2_stub_t* stub, axis2_env_t** env);
        /**
         * axis2_get_endpoint_ref_from_wsdl
         * return the endpoint reference picked from wsdl
         */
        axis2_endpoint_ref_t* axis2_get_endpoint_ref_from_wsdl ( axis2_env_t** env );

        <xsl:if test="$isSync='1'">
        <xsl:for-each select="method">
        <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>
        <xsl:choose>
        <xsl:when test="$outputtype=''">axis2_status_t </xsl:when>
        <xsl:otherwise>
        axis2_om_node_t*
        </xsl:otherwise>
        </xsl:choose>
        axis2_<xsl:value-of select="@name"/>( axis2_stub_t* stub, axis2_env_t** env <xsl:for-each select="input/param[@type!='']"> ,
                                          axis2_om_node_t*<xsl:text> </xsl:text>payload<!--<xsl:value-of select="@name"/>-->
                                          </xsl:for-each> );
        </xsl:for-each>
        </xsl:if>  <!--close for  test="$isSync='1'-->

        <!-- Async method prototype generation -->
        <xsl:if test="$isAsync='1'">
        <xsl:for-each select="method">
        <xsl:variable name="mep"><xsl:value-of select="@mep"/></xsl:variable>
        <xsl:if test="$mep='http://www.w3.org/2004/08/wsdl/in-out'">
        void axis2_start_<xsl:value-of select="@name"/>( axis2_stub_t* stub, axis2_env_t** env, <xsl:for-each select="input/param[@type!='']">
                                                    axis2_om_node_t*<xsl:text> </xsl:text>payload<!--<xsl:value-of select="@name"></xsl:value-of>--> ,
                                                    </xsl:for-each>
                                                    axis2_status_t ( AXIS2_CALL *on_complete ) (struct axis2_callback *, axis2_env_t** ) ,
                                                    axis2_status_t ( AXIS2_CALL *on_error ) (struct axis2_callback *, axis2_env_t**, int ) );

        </xsl:if>  <!--close for  test="$mep='http://www.w3.org/2004/08/wsdl/in-out'"-->
        </xsl:for-each>
        </xsl:if>  <!--close for  test="$isAsync='1'-->
   </xsl:template>
</xsl:stylesheet>
