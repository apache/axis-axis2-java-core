<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text"/>
	<xsl:template match="/interface">
		<xsl:variable name="skeletonname">
			<xsl:value-of select="@skeletonname"/>
		</xsl:variable>
		<xsl:variable name="dbsupportpackage">
			<xsl:value-of select="@dbsupportpackage"/>
		</xsl:variable>
	
    package <xsl:value-of select="@package"/>;

    /**
     *  Auto generated message receiver
     */

    public class <xsl:value-of select="@name"/> extends <xsl:value-of select="@basereceiver"/>{
    
		public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
		throws org.apache.axis2.AxisFault{
    
     try {

            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(msgContext);
           
            <xsl:value-of select="$skeletonname"/> skel = (<xsl:value-of select="$skeletonname"/>)obj;
            //Out Envelop
             org.apache.axis2.soap.SOAPEnvelope envelope = null;
             //Find the operation that has been set by the Dispatch phase.
            org.apache.axis2.description.OperationDescription op = msgContext.getOperationContext().getAxisOperation();
            if (op == null) {
                throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
            }
            
            String methodName;
            if(op.getName() != null &amp; (methodName = op.getName().getLocalPart()) != null){
            
				<xsl:for-each select="method">
				
			<xsl:variable name="returntype"><xsl:value-of select="output/param/@type"/></xsl:variable>
			<xsl:variable name="returnvariable"><xsl:value-of select="output/param/@name"/></xsl:variable>
			<xsl:variable name="namespace"><xsl:value-of select="@namespace"/></xsl:variable>
			<xsl:variable name="dbsupportname"><xsl:value-of select="@dbsupportname"/></xsl:variable>
			
			<xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
			<xsl:variable name="style"><xsl:value-of select="@style"/></xsl:variable>
					
					
					if(methodName.equals("<xsl:value-of select="@name"/>")){
											
				
			<xsl:if test="$returntype!=''">
				<xsl:value-of select="$returntype"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="$returnvariable"/> = null;
						</xsl:if>
						
						
			<xsl:choose>			
				<xsl:when test="$style='rpc'">
					//rpc style				
					<xsl:variable name="inputparamcount"><xsl:value-of select="count(input/param)"/></xsl:variable>
				<xsl:for-each select="input/param">
					<xsl:if test="@type!=''">
		
					org.apache.axis2.om.OMElement firstChild = (org.apache.axis2.om.OMElement)msgContext.getEnvelope().getBody().getFirstChild();
					if(null == firstChild)
						throw new org.apache.axis2.AxisFault("Wrapper Element Not Found for the operation of RPC style");
					java.util.Iterator children = firstChild.getChildren();
					org.apache.xmlbeans.XmlObject[] params = new org.apache.xmlbeans.XmlObject[<xsl:value-of select="$inputparamcount"/>];
					int count = 0;
					while(children.hasNext() &amp;&amp; count &lt; <xsl:value-of select="$inputparamcount"/>){
						params[count] = org.soapinterop.databinding.echoStringDatabindingSupporter.fromOM((org.apache.axis2.om.OMElement)children.next(), <xsl:value-of select="@type"/>.class);
						count++;
					}
				if(count!= <xsl:value-of select="$inputparamcount"/>)
					throw new org.apache.axis2.AxisFault("Parts mismatch in the message");
						 
					</xsl:if>
				</xsl:for-each>
				
					<xsl:if test="$returntype!=''">
						<xsl:value-of select="$returnvariable"/> =</xsl:if> skel.<xsl:value-of select="@name"/>(
								<xsl:for-each select="input/param">
									<xsl:if test="@type!=''">
										(<xsl:value-of select="@type"/>)params[<xsl:value-of select="position()-1"/>]<xsl:if test="position()!=$inputparamcount">,</xsl:if>
									</xsl:if>
								</xsl:for-each>);
							//Create a default envelop
								envelope = getSOAPFactory().getDefaultEnvelope();
								org.apache.axis2.om.OMNamespace ns = getSOAPFactory().createOMNamespace("<xsl:value-of select="$namespace"/>", "<xsl:value-of select="$name"/>Responce");
								org.apache.axis2.om.OMElement responseMethodName = getSOAPFactory().createOMElement(methodName + "Response", ns);
								//Create a Omelement of the result if a result exist
								<xsl:if test="$returntype!=''">
									responseMethodName.setFirstChild(<xsl:value-of select="$dbsupportpackage"/>.<xsl:value-of select="$dbsupportname"/>.toOM(<xsl:value-of select="$returnvariable"/>));		
								</xsl:if>
											
								envelope.getBody().setFirstChild(responseMethodName);
				</xsl:when>
							
							
							
							
							
				<xsl:when test="$style='doc'">
				//doc style
					<xsl:if test="$returntype!=''">
						<xsl:value-of select="$returnvariable"/> =</xsl:if> skel.<xsl:value-of select="@name"/>(<xsl:if test="input/param/@type!=''">(<xsl:value-of select="input/param/@type"/>)<xsl:value-of select="$dbsupportpackage"/>.<xsl:value-of select="$dbsupportname"/>.fromOM((org.apache.axis2.om.OMElement)msgContext.getEnvelope().getBody().getFirstChild().detach(), <xsl:value-of select="input/param/@type"/>.class)</xsl:if>);
						
					//Create a default envelop
					envelope = getSOAPFactory().getDefaultEnvelope();
					//Create a Omelement of the result if a result exist
					
					<xsl:if test="$returntype!=''">envelope.getBody().setFirstChild(<xsl:value-of select="$dbsupportpackage"/>.<xsl:value-of select="$dbsupportname"/>.toOM(<xsl:value-of select="$returnvariable"/>));		
					</xsl:if>											
				</xsl:when>
							
							
				<xsl:otherwise>
									//Unknown style!! No code is generated
								  throw UnsupportedOperationException("Unknown Style");
							</xsl:otherwise>
			</xsl:choose>
						
						
						
					}
			   </xsl:for-each>
			   
			   newMsgContext.setEnvelope(envelope);
            }
           
            

        } catch (Exception e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
     <xsl:for-each select="method"/>
		 }
	
    }
    </xsl:template>
</xsl:stylesheet>
