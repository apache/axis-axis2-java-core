package org.apache.axis2.transport.jms;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.util.MessageContextBuilder;

import junit.framework.TestCase;

/**
 * A simple test added to check the copy of correlation
 * id from the inMessageContext to the outMessageContext.
 * 
 * This simple test will serve as check in case the copying is
 * broken during modifications.
 */
public class CorrelationIdPropagationTest extends TestCase {

	public CorrelationIdPropagationTest(String arg0){
		super(arg0);
	}
	
	public void testCorrelationIdPropagation()throws Exception{
		MessageContext inMsgCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null).createMessageContext();
		inMsgCtx.setIncomingTransportName(Constants.TRANSPORT_JMS);
		String correlationId = "123Test";
		inMsgCtx.setProperty(JMSConstants.JMS_COORELATION_ID, correlationId);
		
		MessageContext outMsgCtx = MessageContextBuilder.createOutMessageContext(inMsgCtx);
		String corrId = (String)outMsgCtx.getProperty(JMSConstants.JMS_COORELATION_ID);
		assertEquals("Correlation Id is not copied from in message ctx to out message ctx",correlationId,corrId);
		
		MessageContext faultMsgCtx = MessageContextBuilder.createFaultMessageContext(inMsgCtx, new Exception());
		corrId = (String)faultMsgCtx.getProperty(JMSConstants.JMS_COORELATION_ID);
		assertEquals("Correlation Id is not copied properly to fault message ctx",correlationId,corrId);
	}
	
	public static void main(String[] args)
	 {
		 CorrelationIdPropagationTest t = new CorrelationIdPropagationTest("S");
		 try{
		 	t.testCorrelationIdPropagation();
		 }catch(Exception e)
		 {
			 e.printStackTrace();
		 }
	 }
}
