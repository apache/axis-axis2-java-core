
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
		
		<xsl:variable name="optimized">
			<xsl:value-of select="//optimizeContent"/>
		</xsl:variable>

       <xsl:choose>
           <xsl:when test="$optimized">
            private void setOpNameArray(){
            opNameArray = new javax.xml.namespace.QName[] {
			<xsl:for-each select="optimizeContent/opName">
				<xsl:if test="position()>1">,
				</xsl:if>new javax.xml.namespace.QName("<xsl:value-of select="@ns-url"/>","<xsl:value-of select="@localName"/>")
			</xsl:for-each>
			};
           }
           </xsl:when>
           <xsl:otherwise>
            private void setOpNameArray(){
            opNameArray = null;
            }
           </xsl:otherwise>
       </xsl:choose>
</xsl:template>
</xsl:stylesheet>