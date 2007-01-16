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

package org.apache.axis2.savan;

import junit.framework.TestCase;

import org.apache.axis2.context.MessageContext;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.publication.PublicationReport;
import org.apache.savan.subscribers.CompositeSubscriber;
import org.apache.savan.subscribers.LeafSubscriber;

public class CompositeSubscriberTest extends TestCase {

	public void testSubscribers () throws SavanException {
		
		MessageContext mc = new MessageContext ();
		SavanMessageContext smc = new SavanMessageContext (mc);
		
		LeafSubscriberImpl leafSubscriber1 = new LeafSubscriberImpl ();
		LeafSubscriberImpl leafSubscriber2 = new LeafSubscriberImpl ();
		CompositeSubscriber compositeSubscriber = new CompositeSubscriber ();
		
		compositeSubscriber.addSubscriber(leafSubscriber1);
		compositeSubscriber.addSubscriber(leafSubscriber2);
		
		PublicationReport report = new PublicationReport ();
		compositeSubscriber.sendPublication(smc,report);
		assertTrue(leafSubscriber1.isNotified());
		assertTrue(leafSubscriber2.isNotified());
	}
	
	class LeafSubscriberImpl extends LeafSubscriber {
		
		boolean notified = false;
		
		public void doProtocolSpecificPublication(SavanMessageContext notificationMessage) {
			notified = true;
		}
		
		public boolean isNotified () {
			return notified;
		}
	}
	
}
