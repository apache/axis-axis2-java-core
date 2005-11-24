/**
 * 
 */
package org.apache.axis2.jaxws.server;

import org.apache.axis2.receivers.*;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.impl.llom.OMTextImpl;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.MessageReceiver;
/**
 * @author sunja07
 *
 */
public class JAXRPCInOutMessageReceiver extends AbstractInOutSyncMessageReceiver 
		implements MessageReceiver {

	/**
	 * 
	 */
	public JAXRPCInOutMessageReceiver() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver#invokeBusinessLogic(org.apache.axis2.context.MessageContext, org.apache.axis2.context.MessageContext)
	 */
	@Override
	public void invokeBusinessLogic(MessageContext oldMsgCntxt, MessageContext newMsgCntxt) throws AxisFault {
		System.out.println("[Testing] JAXRPCInOutMessageReceiver got picked up!");
		
		SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
		
		OMElement bodyContent = OMAbstractFactory.getSOAP11Factory().
			createOMElement("result","http://serverTestURL.org","res");
		bodyContent.setText("This is just to show that on " +
				"the server side the JAXRPCInOutMessageReceiver is chosen by " +
				"Axis!");
		envelope.getBody().addChild(bodyContent);
		
		newMsgCntxt.setEnvelope(envelope);
	}

}
