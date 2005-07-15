<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/bean">
    package
        <xsl:value-of select="@package"/>;

    /**
     *  Auto generated bean class by the Axis code generator
     */

    public class
        <xsl:value-of select="@name"/> {


        <xsl:for-each select="property">
            <xsl:variable name="propertyType">
                <xsl:value-of select="@type"/>
            </xsl:variable>
            <xsl:variable name="propertyName">
                <xsl:value-of select="@name"/>
            </xsl:variable>
        /**
         * field for
            <xsl:value-of select="$propertyName"/>
         */
         private
            <xsl:value-of select="$propertyType"/> local
            <xsl:value-of select="$propertyName"/>;

        /**
         * Auto generated getter method
         * @return
            <xsl:value-of select="$propertyType"/>
         */
        public
            <xsl:value-of select="$propertyType"/>
            <xsl:text> </xsl:text>get
            <xsl:value-of select="$propertyName"/>(){
             return local
            <xsl:value-of select="$propertyName"/>;
        }

        /**
         * Auto generated setter method
         * @param param
            <xsl:value-of select="$propertyName"/>
         */
        public void set
            <xsl:value-of select="$propertyName"/>(
            <xsl:value-of select="$propertyType"/> param
            <xsl:value-of select="$propertyName"/>){
             this.local
            <xsl:value-of select="$propertyName"/>=param
            <xsl:value-of select="$propertyName"/>;
        }
        </xsl:for-each>
    }
    </xsl:template>
</xsl:stylesheet>