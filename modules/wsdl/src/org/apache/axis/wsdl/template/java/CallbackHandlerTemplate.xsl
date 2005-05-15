<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/callback">
    package <xsl:value-of select="@package"/>;

    /**
     *  Auto generated Callback class by the Axis code generator
     */

    public class <xsl:value-of select="@name"/>{


	<xsl:for-each select="method">
         /**
         * auto generated Axis2 call back method
         *
         */
        public void receiveResult<xsl:value-of select="@name"/>(org.apache.axis.clientapi.AsyncResult result) {
			//Fill here with the code to handle the response
			
        }

         /**
         * auto generated Axis2 Error handler
         *
         */
        public void receiveError<xsl:value-of select="@name"/>(java.lang.Exception e) {
			//Fill here with the code to handle the exception

        }
     </xsl:for-each>

     
    }
    </xsl:template>
 </xsl:stylesheet>