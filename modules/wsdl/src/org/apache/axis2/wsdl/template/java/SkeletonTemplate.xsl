<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/interface">

    package
        <xsl:value-of select="@package"/>;

    /**
     *  Auto generated java skeleton for the service by the Axis code generator
     */

    public class
        <xsl:value-of select="@name"/> {
        <xsl:for-each select="method">
            <xsl:variable name="outputtype">
                <xsl:value-of select="output/param/@type"/>
            </xsl:variable>

            <xsl:variable name="inputtype">
                <xsl:value-of select="input/param/@type"/>
            </xsl:variable>  <!-- this needs to change-->
            <xsl:variable name="inputparam">
                <xsl:value-of select="input/param/@name"/>
            </xsl:variable>  <!-- this needs to change-->

        /**
         * Auto generated method signature
         *
            <xsl:if test="$inputtype!=''">@param
                <xsl:value-of select="$inputparam"/>
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
                <xsl:value-of select="$inputparam"/>
            </xsl:if>){
                //Todo fill this with the necessary business logic
            <xsl:if test="$outputtype!=''">return null;</xsl:if>
        }


        </xsl:for-each>
    }
    </xsl:template>
</xsl:stylesheet>