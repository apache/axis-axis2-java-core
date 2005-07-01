<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/callback">
    package <xsl:value-of select="@package"/>;

    /**
     *  Auto generated Callback class by the Axis code generator
     */

    public class <xsl:value-of select="@name"/>{
    
    
    
	private Object clientData;
		
		
	/**
	* User can pass in any object that needs to be accessed once the NonBlocking 
	* Web service call is finished and appropreate method of this CallBack is called.
	* @param clientData Object mechanism by which the user can pass in user data
	* that will be avilable at the time this callback is called.
	*/
	public <xsl:value-of select="@name"/>(Object clientData){
		this.clientData = clientData;
	}


	<xsl:for-each select="method">
         /**
         * auto generated Axis2 call back method for <xsl:value-of select="@name"/> method
         *
         */
        public void receiveResult<xsl:value-of select="@name"/>(org.apache.axis2.clientapi.AsyncResult result) {
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