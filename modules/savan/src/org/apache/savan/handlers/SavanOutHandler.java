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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.publication.PublicationReport;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.subscribers.Subscriber;
import org.apache.savan.util.CommonUtil;

/**
 * Handler of Savan in the outFlow.
 * Notification messages should go through this handler and this will sent them to
 * each subscriber based on their filter.
 * 
 */
public class SavanOutHandler extends AbstractHandler {

	Log log = LogFactory.getLog(SavanOutHandler.class);
	
	public void invoke(MessageContext msgContext) throws AxisFault {

		SavanMessageContext smc = new SavanMessageContext (msgContext);
		int messagetype = smc.getMessageType();
	
		//TODO hv a better method to identify publication messages
		if (messagetype == SavanConstants.MessageTypes.UNKNOWN) {
			SavanMessageContext publication = new SavanMessageContext(msgContext);
			SubscriberStore store = (SubscriberStore) CommonUtil.getSubscriberStore(msgContext.getAxisService());
			if (store != null) {
				PublicationReport report = new PublicationReport();
				Iterator iterator = store.retrieveAll();
				while (iterator.hasNext()) {
					Subscriber subscriber = (Subscriber) iterator.next();
					try {
						subscriber.processPublication (publication, report);
					} catch (SavanException e) {
						report.addErrorReportEntry(subscriber.getId(),e);
					}
					
					//TODO do something with the report.
				}
			} else {
				String message = "Couldnt send the message since the subscriber storage was not found";
				log.debug(message);
				throw new SavanException (message);
			}
			
			msgContext.pause();
		}

	}

}
