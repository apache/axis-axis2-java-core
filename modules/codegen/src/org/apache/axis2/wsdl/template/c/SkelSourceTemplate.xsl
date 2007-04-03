<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/interface">
    <xsl:variable name="svc_name"><xsl:value-of select="@name"/></xsl:variable>
    <xsl:variable name="method-prefix"><xsl:value-of select="@prefix"/></xsl:variable>
    <xsl:variable name="qname"><xsl:value-of select="@qname"/></xsl:variable>

    /**
     * <xsl:value-of select="@name"/>.c
     *
     * This file was auto-generated from WSDL for "<xsl:value-of select="$qname"/>" service
     * by the Apache Axis2/C version: #axisVersion# #today#
     * <xsl:value-of select="@name"/> Axis2/C skeleton for the axisService
     */

     #include "<xsl:value-of select="@name"/>.h"

     <xsl:for-each select="method">
         <xsl:variable name="outputours"><xsl:value-of select="output/param/@ours"></xsl:value-of></xsl:variable>
         <xsl:variable name="count"><xsl:value-of select="count(output/param)"/></xsl:variable>
         <xsl:variable name="outputtype">
           <xsl:choose>
             <xsl:when test="output/param/@ours">axis2_<xsl:value-of select="output/param/@type"></xsl:value-of>_t*</xsl:when>
             <xsl:otherwise><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:otherwise>
           </xsl:choose>
         </xsl:variable>

		 <!-- regardless of the sync or async status, the generated method signature would be just a usual
	           c function-->
        /**
         * auto generated function definition signature
         * for "<xsl:value-of select="@qname"/>" operation.
         <!--  select only the body parameters  -->
         <xsl:for-each select="input/param[@type!='']">* @param <xsl:value-of select="@name"></xsl:value-of></xsl:for-each>
         */
        <xsl:choose>
        <xsl:when test="$outputtype=''">axis2_status_t </xsl:when>
        <xsl:when test="$outputtype!=''"><xsl:value-of select="$outputtype"/></xsl:when>
        </xsl:choose>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$method-prefix"/>_<xsl:value-of select="@name"/> (const axutil_env_t *env <xsl:for-each select="input/param[@type!='']"> ,
                                              <xsl:variable name="inputtype">
                                                  <xsl:if test="@ours">axis2_</xsl:if><xsl:value-of select="@type"/><xsl:if test="@ours">_t*</xsl:if>
                                              </xsl:variable>
                                              <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                          </xsl:for-each> )
        {
          /* TODO fill this with the necessary business logic */
          <xsl:if test="$outputtype!=''">return NULL;</xsl:if>
          <xsl:if test="$outputtype=''">return AXIS2_SUCCESS;</xsl:if>
        }
     </xsl:for-each>

    </xsl:template>
 </xsl:stylesheet>
