
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="stubMethods">
	
		<xsl:if test="//createSequence">
		/**
		 * Starts a reliabel message sequence
		 */
		public void startSequence() {
			_getServiceClient().getOptions().setProperty("START_RM_SEQUENCE", "true");			
		}
		</xsl:if>
		
		<xsl:if test="//setLastMessage">
		/**
		 * Marks the last message for the sequence
		 */
		 public void setLastMessage() {
		 	_getServiceClient().getOptions().setProperty("Sandesha2ClientAPIPropertyWSRMLastMessage", "true");
		 }
		</xsl:if>
		
		<xsl:if test="//endSequence">
		/**
		 * Terminates the reliabel message sequence
		 */
		public void endSequence() {
			_getServiceClient().getOptions().setProperty("END_RM_SEQUENCE", "true");
		}
		</xsl:if>
		
</xsl:template>
</xsl:stylesheet>