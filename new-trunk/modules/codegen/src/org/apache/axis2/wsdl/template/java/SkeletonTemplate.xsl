<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/interface">
    /**
     * <xsl:value-of select="@name"/>.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: #axisVersion# #today#
     */
    package <xsl:value-of select="@package"/>;
    /**
     *  <xsl:value-of select="@name"/> java skeleton for the axisService
     */
    public class <xsl:value-of select="@name"></xsl:value-of>
        <xsl:if test="@skeletonInterfaceName"> implements <xsl:value-of select="@skeletonInterfaceName"/></xsl:if>{
     <xsl:for-each select="method">
         <xsl:variable name="count"><xsl:value-of select="count(output/param)"/></xsl:variable>
         <xsl:variable name="outputtype" select="output/param/@type"/>
         <!-- regardless of the sync or async status, the generated method signature would be just a usual
               java method -->
        /**
         * Auto generated method signature
         <!--  select only the body parameters  -->
          <xsl:for-each select="input/param[@location='body']">
            <xsl:if test="@type!=''">* @param <xsl:value-of select="@name"></xsl:value-of><xsl:text>
         </xsl:text></xsl:if></xsl:for-each>
         */
        public  <xsl:if test="$count=0 or $outputtype=''">void</xsl:if><xsl:if test="$outputtype!=''"><xsl:value-of select="$outputtype"/></xsl:if><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                  (
          <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                    <xsl:choose>
                        <xsl:when test="$inputcount=1">
                            <!-- Even when the parameters are 1 we have to see whether we have the
                                 wrapped parameters -->
                            <xsl:variable name="inputWrappedCount" select="count(input/param[@location='body' and @type!='']/param)"/>
                            <xsl:choose>
                                <xsl:when test="$inputWrappedCount &gt; 0">
                                   <xsl:for-each select="input/param[@location='body' and @type!='']/param">
                                        <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="input/param[@location='body' and @type!='']/@type"/><xsl:text> </xsl:text><xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise><!-- Just leave it - nothing we can do here --></xsl:otherwise>
                    </xsl:choose>
          )
         <!--add the faults-->
           <xsl:for-each select="fault/param[@type!='']">
               <xsl:if test="position()=1">throws </xsl:if>
               <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@name"/>
           </xsl:for-each>{
                //Todo fill this with the necessary business logic
                <xsl:if test="string-length(normalize-space($outputtype)) &gt; 0">throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#<xsl:value-of select="@name"/>");</xsl:if>
        }
     </xsl:for-each>
    }
    </xsl:template>
 </xsl:stylesheet>