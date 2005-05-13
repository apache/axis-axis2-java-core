<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
    package <xsl:value-of select="@package"/>;

    /**
     *  Auto generated bean class by the Axis code generator
     */

    public class <xsl:value-of select="@name"/> implements org.apache.axis.clientapi.Callback{

	<xsl:for-each select="method">
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>

         <xsl:variable name="inputtype"><xsl:value-of select="input/param/@type"></xsl:value-of></xsl:variable>  <!-- this needs to change-->
         <xsl:variable name="inputparam"><xsl:value-of select="input/param/@name"></xsl:value-of></xsl:variable>  <!-- this needs to change-->

        /**
         * Axis2 generated method signature 
         *<xsl:if test="$inputtype!=''">@param <xsl:value-of select="$inputparam"></xsl:value-of></xsl:if>
         */
        public  void <xsl:value-of select="@name"/>(<xsl:if test="$inputtype!=''"><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="$inputparam"></xsl:value-of></xsl:if>) throws java.rmi.RemoteException {
			//TODO Complete the implementation
			
        }
     </xsl:for-each>

     
    }
    </xsl:template>
 </xsl:stylesheet>