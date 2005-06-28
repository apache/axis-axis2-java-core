<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/interface">

	<xsl:variable name="skeletonname"><xsl:value-of select="@skeletonname"/></xsl:variable>
    package <xsl:value-of select="@package"/>;

    /**
     *  Auto generated message receiver
     */

    public class <xsl:value-of select="@name"></xsl:value-of> extends <xsl:value-of select="@basereceiver"/>{
    
		public void invokeBusinessLogic(org.apache.axis.context.MessageContext msgContext, org.apache.axis.context.MessageContext newMsgContext) 
		throws org.apache.axis.engine.AxisFault{
    
     try {

            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(msgContext);
           
            <xsl:value-of select="$skeletonname"></xsl:value-of> skel = (<xsl:value-of select="$skeletonname"></xsl:value-of>)obj;
            //Out Envelop
             org.apache.axis.soap.SOAPEnvelope envelope = null;
             //Find the operation that has been set by the Dispatch phase.
            org.apache.axis.description.OperationDescription op = msgContext.getOperationContext().getAxisOperation();
            if (op == null) {
                throw new org.apache.axis.engine.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
            }
            
            String methodName;
            if(op.getName() != null &amp; (methodName = op.getName().getLocalPart()) != null){
            
				<xsl:for-each select="method">

					<xsl:variable name="returntype"><xsl:value-of select="output/param/@type"/></xsl:variable>
					<xsl:variable name="returnvariable"><xsl:value-of select="output/param/@name"/></xsl:variable>
					
					if(methodName.equals("<xsl:value-of select="@name"></xsl:value-of> ")){
						<xsl:for-each select="input/param">
							<xsl:if test="@type!=''">
								<xsl:value-of select="@type"/> <xsl:text> </xsl:text><xsl:value-of select="@name"/> = null;
							</xsl:if>
						</xsl:for-each>
						<xsl:if test="$returntype!=''">
								<xsl:value-of select="$returntype"/> <xsl:text> </xsl:text><xsl:value-of select="$returnvariable"/> = null;
						</xsl:if>
						<xsl:if test="count(input/param)='1'">
							<xsl:if test="$returntype!=''"><xsl:value-of select="$returnvariable"/> =</xsl:if> skel.<xsl:value-of select="@name"></xsl:value-of>(
							<xsl:for-each select="input/param">
								<xsl:if test="@type!=''">
									<xsl:value-of select="@name"/>
								</xsl:if>
							</xsl:for-each>);
						</xsl:if>
					}
			   </xsl:for-each>
            }
           
            

        } catch (Exception e) {
            throw org.apache.axis.engine.AxisFault.makeFault(e);
        }
     <xsl:for-each select="method"></xsl:for-each>
		 }
	
    }
    </xsl:template>
 </xsl:stylesheet>