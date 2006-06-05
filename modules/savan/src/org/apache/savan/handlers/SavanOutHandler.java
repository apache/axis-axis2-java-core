package org.apache.savan.handlers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.publication.PublicationErrorReport;
import org.apache.savan.publication.PublicationProcessor;
import org.apache.savan.util.AbstractSavanUtilFactory;
import org.apache.savan.util.ProtocolManager;
import org.apache.savan.util.SavanUtilFactory;

public class SavanOutHandler extends AbstractHandler {

	public void invoke(MessageContext msgContext) throws AxisFault {

		int protocolVersion = ProtocolManager.getMessageProtocol (msgContext);
		SavanUtilFactory utilFactory = AbstractSavanUtilFactory.getUtilFactory(protocolVersion);
		
		SavanMessageContext smc = utilFactory.createSavanMessageContext (msgContext); 
		smc.setProtocolVersion(protocolVersion);
		
		PublicationProcessor processor = utilFactory.createPublicationProcessor();

		String publicationProperty = (String) smc.getProperty(SavanConstants.PUBLICATION_MESSAGE);
		boolean publication = false;
		if (publicationProperty!=null && SavanConstants.VALUE_TRUE.equalsIgnoreCase(publicationProperty))
			publication = true;
		
		int messagetype = smc.getMessageType();		
		
		if (messagetype==SavanConstants.MessageTypes.UNKNOWN) {
			PublicationErrorReport report = processor.notifyListners (smc);
			
			//TODO use the report to inform abt the errors.
			
			//stopping the message from going further.
			msgContext.pause();
		}
		

	}

}
