package org.apache.axis2.savan;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.eventing.EventingConstants;
import org.apache.savan.eventing.EventingSubscriber;
import org.apache.savan.eventing.EventingSubscriptionProcessor;
import org.apache.savan.subscription.ExpirationBean;
import org.apache.savan.util.CommonUtil;

public class EventingSubscripitonProcessorTest extends TestCase {

	public void testSubscriberExtraction () throws Exception {
		SavanMessageContext smc = getSubscriptionMessage();
		EventingSubscriptionProcessor esp = new EventingSubscriptionProcessor ();
		EventingSubscriber eventingSubscriber = (EventingSubscriber) esp.getSubscriberFromMessage(smc);
		assertNotNull(eventingSubscriber);
		
		assertNotNull(eventingSubscriber.getDelivery());
		assertNotNull(eventingSubscriber.getDelivery().getDeliveryEPR());
		assertNotNull(eventingSubscriber.getFilter());
		assertNotNull(eventingSubscriber.getEndToEPr());
		assertNotNull(eventingSubscriber.getId());
		assertNotNull(eventingSubscriber.getSubscriptionEndingTime());
		
		assertEquals(eventingSubscriber.getDelivery().getDeliveryMode(),EventingConstants.DEFAULT_DELIVERY_MODE);
		
		assertEquals(eventingSubscriber.getDelivery().getDeliveryEPR().getAddress() ,"http://www.other.example.com/OnStormWarning");
		assertEquals(eventingSubscriber.getEndToEPr().getAddress(),"http://www.example.com/MyEventSink");
		assertEquals(eventingSubscriber.getFilter().getFilterType(),"http://www.example.org/topicFilter");
		assertEquals(((OMText) eventingSubscriber.getFilter().getFilter()).getText().trim(),"weather.storms");
		
		Date date = ConverterUtil.convertTodateTime("2004-06-26T21:07:00.000-08:00").getTime();
		assertEquals(eventingSubscriber.getSubscriptionEndingTime(),date);
	}
	
	public void testExpirationBeanExtraction () throws Exception {
		SavanMessageContext smc = getRenewMessage();
		EventingSubscriptionProcessor esp = new EventingSubscriptionProcessor ();
		ExpirationBean expirationBean = esp.getExpirationBean(smc);
		
		assertNotNull(expirationBean);
		assertNotNull(expirationBean.getSubscriberID());
		
		Date date = ConverterUtil.convertTodateTime("2004-06-26T21:07:00.000-08:00").getTime();
		assertEquals(expirationBean.getDateValue(),date);
	}
	
	private SavanMessageContext getSubscriptionMessage () throws IOException {
        File baseDir = new File("");
        String testRource = baseDir.getAbsolutePath() + File.separator + "test-resources";

		SOAPEnvelope envelope = CommonUtil.getTestEnvelopeFromFile(testRource,"eventing-subscription.xml");
		
		MessageContext mc = new MessageContext ();
		SavanMessageContext smc = new SavanMessageContext (mc);
		mc.setEnvelope(envelope);
		
		Options options = new Options ();
		options.setTo(new EndpointReference ("http://DummyToAddress/"));
		
		EndpointReference replyToEPR = new EndpointReference ("http://DummyReplyToAddress/");
		replyToEPR.addReferenceParameter(new QName ("RefParam1"),"RefParamVal1");
		options.setTo(replyToEPR);
		
		options.setAction("urn:uuid:DummyAction");
		
		return smc;
	}
	
	private SavanMessageContext getRenewMessage () throws IOException {
        File baseDir = new File("");
        String testRource = baseDir.getAbsolutePath() + File.separator + "test-resources";

		SOAPEnvelope envelope = CommonUtil.getTestEnvelopeFromFile(testRource,"eventing-renew-datetime.xml");
		
		MessageContext mc = new MessageContext ();
		SavanMessageContext smc = new SavanMessageContext (mc);
		mc.setEnvelope(envelope);
		
		Options options = new Options ();
		options.setTo(new EndpointReference ("http://DummyToAddress/"));
		
		EndpointReference replyToEPR = new EndpointReference ("http://DummyReplyToAddress/");
		replyToEPR.addReferenceParameter(new QName ("RefParam1"),"RefParamVal1");
		options.setTo(replyToEPR);
		
		options.setAction("urn:uuid:DummyAction");
		
		return smc;
	}
}
