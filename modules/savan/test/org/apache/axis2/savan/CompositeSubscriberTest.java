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
