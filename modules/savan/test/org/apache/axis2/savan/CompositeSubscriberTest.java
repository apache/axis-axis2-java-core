package org.apache.axis2.savan;

import org.apache.axis2.context.MessageContext;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.subscribers.CompositeSubscriber;
import org.apache.savan.subscribers.LeafSubscriber;

import junit.framework.TestCase;

public class CompositeSubscriberTest extends TestCase {

	public void testSubscribers () throws SavanException {
		
		MessageContext mc = new MessageContext ();
		SavanMessageContext smc = new SavanMessageContext (mc);
		
		LeafSubscriberImpl leafSubscriber1 = new LeafSubscriberImpl ();
		LeafSubscriberImpl leafSubscriber2 = new LeafSubscriberImpl ();
		CompositeSubscriber compositeSubscriber = new CompositeSubscriber ();
		
		compositeSubscriber.addSubscriber(leafSubscriber1);
		compositeSubscriber.addSubscriber(leafSubscriber2);
		
		compositeSubscriber.sendNotification(smc);
		assertTrue(leafSubscriber1.isNotified());
		assertTrue(leafSubscriber2.isNotified());
	}
	
	class LeafSubscriberImpl extends LeafSubscriber {
		
		boolean notified = false;
		
		public void doProtocolSpecificNotification(SavanMessageContext notificationMessage) {
			notified = true;
		}
		
		public boolean isNotified () {
			return notified;
		}
	}
	
}
