<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/interface">
    <xsl:variable name="svc_name"><xsl:value-of select="@name"/></xsl:variable>
    /**
     * <xsl:value-of select="@name"/>.h
     *
     * This file was auto-generated from WSDL 
     * by the Apache Axis2/Java version: #axisVersion# #today#
     * <xsl:value-of select="@name"/> Axis2/C skeleton for the axisService- Header file
     */

	
	#include &lt;axis2_svc_skeleton.h&gt;
	#include &lt;axis2_log_default.h&gt;
	#include &lt;axis2_error_default.h&gt;
	#include &lt;axiom_text.h&gt;
	#include &lt;axiom_node.h&gt;
	#include &lt;axiom_element.h&gt;
    #include &lt;stdio.h&gt;


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


     <xsl:for-each select="method">
        <xsl:variable name="outputours"><xsl:value-of select="output/param/@ours"></xsl:value-of></xsl:variable>
         <xsl:variable name="count"><xsl:value-of select="count(output/param)"/></xsl:variable>
         <xsl:variable name="outputtype"><xsl:if test="$outputours">axis2_</xsl:if><xsl:choose><xsl:when test="output/param/@type='org.apache.axiom.om.OMElement'">om_node</xsl:when><xsl:otherwise><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="$outputours">_t*</xsl:if></xsl:variable>

		 <!-- regardless of the sync or async status, the generated method signature would be just a usual
	           c function-->
        /**
         * Auto generated function declaration
         <!--  select only the body parameters  -->
          <xsl:for-each select="input/param[@location='body']">
            <xsl:if test="@type!=''">* @param <xsl:value-of select="@name"></xsl:value-of><xsl:text>
         </xsl:text></xsl:if></xsl:for-each>
         */
        <xsl:choose>
        <xsl:when test="$count=0">axis2_status_t </xsl:when>
        <xsl:when test="$outputtype=''">axis2_status_t </xsl:when>
        <xsl:when test="$outputtype='axis2__t*'">void</xsl:when>
        <xsl:otherwise>
        <xsl:value-of select="$outputtype"/>
        </xsl:otherwise>
        </xsl:choose>
        <xsl:value-of select="$svc_name"/>_<xsl:value-of select="@name"/> (const axis2_env_t* env <xsl:for-each select="input/param[@location='body']"> ,<xsl:variable name="paramtype"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if></xsl:variable>
                                          <xsl:value-of select="$paramtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                          </xsl:for-each> );
     </xsl:for-each>

    </xsl:template>
 </xsl:stylesheet>
