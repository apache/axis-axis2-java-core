<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <xsl:template match="/class">
        <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
        <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
        <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
        <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
        <xsl:variable name="soapVersion"><xsl:value-of select="@soap-version"/></xsl:variable>
        <xsl:variable name="method-prefix"><xsl:value-of select="@prefix"/></xsl:variable>
        <xsl:variable name="qname"><xsl:value-of select="@qname"/></xsl:variable>

        /**
        * <xsl:value-of select="@name"/>.h
        *
        * This file was auto-generated from WSDL for "<xsl:value-of select="$qname"/>" service
        * by the Apache Axis2/C version: #axisVersion# #today#
        */

        #include &lt;stdio.h&gt;
        #include &lt;axiom.h&gt;
        #include &lt;axutil_utils.h&gt;
        #include &lt;axiom_soap.h&gt;
        #include &lt;axis2_client.h&gt;
        #include &lt;axis2_stub.h&gt;

       <xsl:for-each select="method">
        <xsl:if test="output/param[@ours and @type!='']">
         <xsl:variable name="outputtype">adb_<xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>
         #include "<xsl:value-of select="$outputtype"/>.h"
        </xsl:if>
        <xsl:for-each select="input/param[@type!='' and @ours ]">
         <xsl:variable name="inputtype">adb_<xsl:value-of select="@type"></xsl:value-of></xsl:variable>
         #include "<xsl:value-of select="$inputtype"/>.h"
        </xsl:for-each>
       </xsl:for-each>
        /* function prototypes - for header file*/
        /**
         * <xsl:value-of select="$method-prefix"/>_create
         * create and return the stub with services populated
         * params - env : environment ( mandatory)
         *        - client_home : Axis2/C home ( mandatory )
         *        - endpoint_uri : service endpoint uri( optional ) - if NULL default picked from wsdl used
         */
        axis2_stub_t*
        <xsl:value-of select="$method-prefix"/>_create (const axutil_env_t *env,
                                        axis2_char_t *client_home,
                                        axis2_char_t *endpoint_uri);
        /**
         * <xsl:value-of select="$method-prefix"/>_populate_services
         * populate the svc in stub with the service and operations
         */
        void <xsl:value-of select="$method-prefix"/>_populate_services( axis2_stub_t *stub, const axutil_env_t *env);
        /**
         * <xsl:value-of select="$method-prefix"/>_get_endpoint_uri_from_wsdl
         * return the endpoint URI picked from wsdl
         */
        axis2_char_t *<xsl:value-of select="$method-prefix"/>_get_endpoint_uri_from_wsdl ( const axutil_env_t *env );

        <xsl:if test="$isSync='1'">
        <xsl:for-each select="method">
        /**
         * auto generated function declaration
         * for "<xsl:value-of select="@qname"/>" operation.
         <!--  select only the body parameters  -->
         <xsl:for-each select="input/param[@type!='']">* @param <xsl:value-of select="@name"></xsl:value-of></xsl:for-each>
         */

        <xsl:variable name="outputours"><xsl:value-of select="output/param/@ours"></xsl:value-of></xsl:variable>
        <xsl:variable name="outputtype">
            <xsl:choose>
                <xsl:when test="output/param/@ours">adb_<xsl:value-of select="output/param/@type"></xsl:value-of>_t*</xsl:when>
                <xsl:otherwise><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
        <xsl:when test="$outputtype=''">axis2_status_t</xsl:when>
        <xsl:when test="$outputtype!=''"><xsl:value-of select="$outputtype"/></xsl:when>
        </xsl:choose>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$method-prefix"/>_<xsl:value-of select="@name"/>( axis2_stub_t *stub, const axutil_env_t *env<xsl:for-each select="input/param[@type!='']">,
                                                    <xsl:variable name="inputtype">
                                                        <xsl:if test="@ours">adb_</xsl:if><xsl:value-of select="@type"/><xsl:if test="@ours">_t*</xsl:if>
                                                    </xsl:variable>
                                                    <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                                </xsl:for-each>);
        </xsl:for-each>
        </xsl:if>  <!--close for  test="$isSync='1'-->

        <!-- Async method prototype generation -->
        <xsl:if test="$isAsync='1'">
        <xsl:for-each select="method">
        /**
         * auto generated function declaration
         * for "<xsl:value-of select="@qname"/>" operation.
         <!--  select only the body parameters  -->
         <xsl:for-each select="input/param[@type!='']">* @param <xsl:value-of select="@name"></xsl:value-of></xsl:for-each>
         * @param on_complete callback to handle on complete
         * @param on_error callback to handle on error
         */

        <xsl:variable name="mep"><xsl:value-of select="@mep"/></xsl:variable>
        <xsl:if test="$mep='12'">
        void <xsl:value-of select="$method-prefix"/>_<xsl:value-of select="@name"/>_start( axis2_stub_t *stub, const axutil_env_t *env<xsl:for-each select="input/param[@type!='']">,
                                                        <xsl:variable name="inputtype">
                                                            <xsl:if test="@ours">adb_</xsl:if><xsl:value-of select="@type"/><xsl:if test="@ours">_t*</xsl:if>
                                                        </xsl:variable>
                                                        <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                                    </xsl:for-each>,
                                                        axis2_status_t ( AXIS2_CALL *on_complete ) (struct axis2_callback *, const axutil_env_t* ) ,
                                                        axis2_status_t ( AXIS2_CALL *on_error ) (struct axis2_callback *, const axutil_env_t*, int ) );

        </xsl:if>  <!--close for  test="$mep='http://www.w3.org/2004/08/wsdl/in-out'"-->
        </xsl:for-each>
        </xsl:if>  <!--close for  test="$isAsync='1'-->
   </xsl:template>
</xsl:stylesheet>
