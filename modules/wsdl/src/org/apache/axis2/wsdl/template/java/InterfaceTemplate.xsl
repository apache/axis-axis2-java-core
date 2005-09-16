<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/interface">
    <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
    <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
    <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
    <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>

    package <xsl:value-of select="$package"/>;

    /*
     *  Auto generated java interface by the Axis code generator
     */

    public interface <xsl:value-of select="@name"></xsl:value-of> {
 		 <xsl:for-each select="method">
		    <!-- Code for in-out mep -->
         <xsl:if test="@mep='http://www.w3.org/2004/08/wsdl/in-out'">
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>

        <!-- start of the sync block -->                                          
         <xsl:if test="$isSync='1'">
        /**
         * Auto generated method signatures
         <xsl:for-each select="input/param[@type!='']">* @param <xsl:value-of select="@name"></xsl:value-of></xsl:for-each>
         */
         public <xsl:choose><xsl:when test="$outputtype=''">void</xsl:when><xsl:otherwise><xsl:value-of select="$outputtype"/></xsl:otherwise></xsl:choose>
        <xsl:text> </xsl:text><xsl:value-of select="@name"/>(
         <xsl:for-each select="input/param[@type!='']">
            <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
          </xsl:for-each>) throws java.rmi.RemoteException;
        <!-- end of the sync block -->
        </xsl:if>

       <!-- start of the async block -->
        <xsl:if test="$isAsync='1'">
         /**
          * Auto generated method signature
          <xsl:for-each select="input/param"><xsl:if test="@type!=''">* @param <xsl:value-of select="@name"></xsl:value-of></xsl:if></xsl:for-each>
          */

        public void start<xsl:value-of select="@name"/>(
         <xsl:variable name="paramCount"><xsl:value-of select="count(input/param[@type!=''])"></xsl:value-of></xsl:variable>
               <xsl:for-each select="input/param">
            <xsl:if test="@type!=''"><xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"></xsl:value-of></xsl:if></xsl:for-each>
           <xsl:if test="$paramCount>0">,</xsl:if>final <xsl:value-of select="$package"/>.<xsl:value-of select="$callbackname"/> callback) throws java.rmi.RemoteException;
        </xsl:if>
<!-- end of async block-->

     </xsl:if>
        <!-- Code for in-only mep -->
       <xsl:if test="@mep='http://www.w3.org/2004/08/wsdl/in-only'">

       <!-- For in-only meps there would not be any asynchronous methods since there is no output -->
         /**
         * Auto generated method signature
         <xsl:for-each select="input/param">
         <xsl:if test="@type!=''">*@param <xsl:value-of select="@name"></xsl:value-of><xsl:text>
         </xsl:text></xsl:if></xsl:for-each>
         */
         public  void
        <xsl:text> </xsl:text><xsl:value-of select="@name"/>(
         <xsl:for-each select="input/param">
            <xsl:if test="@type!=''"><xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
            </xsl:if>
         </xsl:for-each>) throws java.rmi.RemoteException;

        </xsl:if>
       </xsl:for-each>
       }
    </xsl:template>
   </xsl:stylesheet>