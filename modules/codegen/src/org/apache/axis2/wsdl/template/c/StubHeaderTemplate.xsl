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
        #include &lt;axiom.h&gt;
        #include &lt;axis2_util.h&gt;
        #include &lt;axiom_soap.h&gt;
        #include &lt;axis2_client.h&gt;
        #include &lt;axis2_stub.h&gt;

       <xsl:for-each select="method">
        <xsl:variable name="outputours"><xsl:value-of select="output/param/@ours"></xsl:value-of></xsl:variable>
        <xsl:if test="$outputours and output/param/@type!='' and output/param/@type!='org.apache.axiom.om.OMElement'">
         <xsl:variable name="outputtype">axis2_<xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>
         #include  "<xsl:value-of select="$outputtype"/>.h"
        </xsl:if>
        <xsl:for-each select="input/param[@type!='' and @ours and @type!='org.apache.axiom.om.OMElement']">
         <xsl:variable name="inputtype">axis2_<xsl:value-of select="@type"></xsl:value-of></xsl:variable>
         #include "<xsl:value-of select="$inputtype"/>.h"
        </xsl:for-each>
       </xsl:for-each>
        /* function prototypes - for header file*/
        /**
         * axis2_<xsl:value-of select="$interfaceName"/>_stub_create
         * create and return the stub with services populated
         * params - env : environment ( mandatory)
         *        - client_home : Axis2/C home ( mandatory )
         *        - endpoint_uri : service endpoint uri( optional ) - if NULL default picked from wsdl used
         */
        axis2_stub_t*
        axis2_<xsl:value-of select="$interfaceName"/>_stub_create (const axis2_env_t *env,
                                        axis2_char_t *client_home,
                                        axis2_char_t *endpoint_uri);
        /**
         * axis2_populate_axis_service
         * populate the svc in stub with the service and operations
         */
        void axis2_populate_axis_service( axis2_stub_t* stub, const axis2_env_t* env);
        /**
         * axis2_get_endpoint_uri_from_wsdl
         * return the endpoint URI picked from wsdl
         */
        axis2_char_t* axis2_get_endpoint_uri_from_wsdl ( const axis2_env_t* env );

        <xsl:if test="$isSync='1'">
        <xsl:for-each select="method">
        <xsl:variable name="outputours"><xsl:value-of select="output/param/@ours"></xsl:value-of></xsl:variable>
        <xsl:variable name="outputtype"><xsl:if test="$outputours">axis2_</xsl:if><xsl:choose><xsl:when test="output/param/@type='org.apache.axiom.om.OMElement'">om_node</xsl:when><xsl:otherwise><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="$outputours">_t*</xsl:if></xsl:variable>
        <xsl:choose>
        <xsl:when test="not($outputtype) or $outputtype=''">axis2_status_t </xsl:when>
        <xsl:when test="$outputtype='axis2__t*'">void</xsl:when>
        <xsl:otherwise>
        <xsl:value-of select="$outputtype"/>
        </xsl:otherwise>
        </xsl:choose>
        axis2_<xsl:value-of select="@name"/>( axis2_stub_t* stub, const axis2_env_t* env <xsl:for-each select="input/param[@type!='']"> ,<xsl:variable name="paramtype"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">om_node</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if></xsl:variable>
                                                <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="$paramtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                                </xsl:for-each>);
        </xsl:for-each>
        </xsl:if>  <!--close for  test="$isSync='1'-->

        <!-- Async method prototype generation -->
        <xsl:if test="$isAsync='1'">
        <xsl:for-each select="method">
        <xsl:variable name="mep"><xsl:value-of select="@mep"/></xsl:variable>
        <xsl:if test="not($mep='10')">
        void axis2_start_<xsl:value-of select="@name"/>( axis2_stub_t* stub, const axis2_env_t* env, <xsl:for-each select="input/param[@type!='']"><xsl:variable name="paramtype"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">om_node</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if></xsl:variable>
                                                    <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="$paramtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                                    </xsl:for-each>,
                                                    axis2_status_t ( AXIS2_CALL *on_complete ) (struct axis2_callback *, const axis2_env_t* ) ,
                                                    axis2_status_t ( AXIS2_CALL *on_error ) (struct axis2_callback *, const axis2_env_t*, int ) );

        </xsl:if>  <!--close for  test="$mep='http://www.w3.org/2004/08/wsdl/in-out'"-->
        </xsl:for-each>
        </xsl:if>  <!--close for  test="$isAsync='1'-->
   </xsl:template>
</xsl:stylesheet>
