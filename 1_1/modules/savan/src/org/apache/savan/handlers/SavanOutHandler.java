/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.savan.handlers;

import java.util.Iterator;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.publication.PublicationReport;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.subscribers.AbstractSubscriber;
import org.apache.savan.util.CommonUtil;

/**
 * Handler of Savan in the outFlow.
 * Notification messages should go through this handler and this will sent them to
 * each subscriber based on their filter.
 * 
 */
public class SavanOutHandler extends AbstractHandler {

	Log log = LogFactory.getLog(SavanOutHandler.class);
	
	public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

		SavanMessageContext smc = new SavanMessageContext (msgContext);
		int messagetype = smc.getMessageType();
	
		//TODO hv a better method to identify publication messages
		if (messagetype == SavanConstants.MessageTypes.UNKNOWN) {
			SavanMessageContext publication = new SavanMessageContext(msgContext);
			SubscriberStore store = CommonUtil.getSubscriberStore(msgContext.getAxisService());
			if (store != null) {
				
				//building the publication envelope
				msgContext.getEnvelope().build();
				
				//this tell addressing to polulate the SOAP envelope with the new values set in the options object
				//(i.e. by removing old headers) every time the message sent through it.
				msgContext.setProperty(AddressingConstants.REPLACE_ADDRESSING_HEADERS, Boolean.TRUE);
				
				PublicationReport report = new PublicationReport();
				Iterator iterator = store.retrieveAll();
				while (iterator.hasNext()) {
					AbstractSubscriber subscriber = (AbstractSubscriber) iterator.next();
					try {
						subscriber.processPublication (publication, report);
					} catch (SavanException e) {
						report.addErrorReportEntry(subscriber.getId(),e);
						e.printStackTrace();
					}
					
					//TODO do something with the report.
				}
			} else {
				String message = "Couldnt send the message since the subscriber storage was not found";
				log.debug(message);
			}
			
            msgContext.pause();
            return InvocationResponse.SUSPEND;        
		}
        return InvocationResponse.CONTINUE;        

	}

}
