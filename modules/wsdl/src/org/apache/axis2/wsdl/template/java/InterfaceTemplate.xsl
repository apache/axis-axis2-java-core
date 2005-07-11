<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/interface">
        <xsl:variable name="isSync">
            <xsl:value-of select="@isSync"/>
        </xsl:variable>
        <xsl:variable name="isAsync">
            <xsl:value-of select="@isAsync"/>
        </xsl:variable>
        <xsl:variable name="callbackname">
            <xsl:value-of select="@callbackname"/>
        </xsl:variable>
        <xsl:variable name="package">
            <xsl:value-of select="@package"/>
        </xsl:variable>

    package
        <xsl:value-of select="$package"/>;

    /*
     *  Auto generated java interface by the Axis code generator
     */

    public interface
        <xsl:value-of select="@name"></xsl:value-of> {
        <xsl:for-each select="method">
            <xsl:variable name="outputtype">
                <xsl:value-of select="output/param/@type"></xsl:value-of>
            </xsl:variable>

            <xsl:variable name="inputtype">
                <xsl:value-of select="input/param/@type"></xsl:value-of>
            </xsl:variable>  <!-- this needs to change-->
            <xsl:variable name="inputparam">
                <xsl:value-of select="input/param/@name"></xsl:value-of>
            </xsl:variable>  <!-- this needs to change-->
            <xsl:if test="$isSync='1'">
        /**
         * Auto generated method signature 
         *
                <xsl:if test="$inputtype!=''">@param
                    <xsl:value-of select="$inputparam"></xsl:value-of>
                </xsl:if>
         */
        public
                <xsl:if test="$outputtype=''">void</xsl:if>
                <xsl:if test="$outputtype!=''">
                    <xsl:value-of select="$outputtype"/>
                </xsl:if>
                <xsl:text> </xsl:text>
                <xsl:value-of select="@name"/>(
                <xsl:if test="$inputtype!=''">
                    <xsl:value-of select="$inputtype"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$inputparam"></xsl:value-of>
                </xsl:if>) throws java.rmi.RemoteException;
            </xsl:if>
            <xsl:if test="$isAsync='1'">
         /**
         * Auto generated method signature
         *
                <xsl:if test="$inputtype!=''">@param
                    <xsl:value-of select="$inputparam"></xsl:value-of>
                </xsl:if>
         */
        public  void start
                <xsl:value-of select="@name"/>(
                <xsl:if test="$inputtype!=''">
                    <xsl:value-of select="$inputtype"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$inputparam"></xsl:value-of>,
                </xsl:if>final
                <xsl:value-of select="$package"/>.
                <xsl:value-of select="$callbackname"/> callback) throws java.rmi.RemoteException;
            </xsl:if>
        </xsl:for-each>
    }
    </xsl:template>
</xsl:stylesheet>