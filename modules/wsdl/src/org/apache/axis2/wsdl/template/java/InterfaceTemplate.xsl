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
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>
         <xsl:if test="$isSync='1'">
        /**
         * Auto generated method signature
         <xsl:for-each select="input/param">
         * @param <xsl:value-of select="@name"/>
         </xsl:for-each>
         */
        public <xsl:if test="$outputtype=''">void</xsl:if><xsl:if test="$outputtype!=''"><xsl:value-of select="$outputtype"/></xsl:if><xsl:text> </xsl:text><xsl:value-of select="@name"/>(<xsl:for-each select="input/param"><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"></xsl:value-of><xsl:if test="not(position()=last())">, </xsl:if></xsl:for-each>) throws java.rmi.RemoteException;
        </xsl:if>
        <xsl:if test="$isAsync='1'">
         /**
         * Auto generated method signature
         <xsl:for-each select="input/param">
         * @param <xsl:value-of select="@name"/>
         </xsl:for-each>
         */
        public void start<xsl:value-of select="@name"/>(<xsl:for-each select="input/param"><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"></xsl:value-of>, </xsl:for-each>final <xsl:value-of select="$package"/>.<xsl:value-of select="$callbackname"/> callback) throws java.rmi.RemoteException;
        </xsl:if>
     </xsl:for-each>
    }
    </xsl:template>
 </xsl:stylesheet>