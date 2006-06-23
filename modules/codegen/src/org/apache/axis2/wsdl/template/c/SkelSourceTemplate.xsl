<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/interface">
    <xsl:variable name="svc_name"><xsl:value-of select="@name"/></xsl:variable>
    /**
     * <xsl:value-of select="@name"/>.c 
     *
     * This file was auto-generated from WSDL 
     * by the Apache Axis2/Java version: #axisVersion# #today#
     * <xsl:value-of select="@name"/> Axis2/C skeleton for the axisService
     */

     #include "<xsl:value-of select="@name"/>.h"

     <xsl:for-each select="method">
         <xsl:variable name="outputours"><xsl:value-of select="output/param/@ours"></xsl:value-of></xsl:variable>
         <xsl:variable name="count"><xsl:value-of select="count(output/param)"/></xsl:variable>
         <xsl:variable name="outputtype"><xsl:choose><xsl:when test="output/param/@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:if test="output/param/@ours">axis2_</xsl:if><xsl:value-of select="output/param/@type"></xsl:value-of><xsl:if test="output/param/@ours">_t*</xsl:if></xsl:otherwise></xsl:choose></xsl:variable>

		 <!-- regardless of the sync or async status, the generated method signature would be just a usual
	           c function-->
        /**
         * Auto generated function definition
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
        <xsl:value-of select="$svc_name"/>_<xsl:value-of select="@name"/> (const axis2_env_t* env <xsl:for-each select="input/param[@location='body']"> ,<xsl:variable name="paramtype"><xsl:if test="@ours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="@ours">_t*</xsl:if></xsl:variable>
                                          <xsl:value-of select="$paramtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                          </xsl:for-each> )

        {
          /* Todo fill this with the necessary business logic */
          <xsl:if test="$outputtype!=''">return NULL;</xsl:if>
          <xsl:if test="$outputtype=''">return AXIS2_SUCCESS;</xsl:if>
        }
     </xsl:for-each>

    </xsl:template>
 </xsl:stylesheet>
