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

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.configuration.ConfigurationManager;
import org.apache.savan.configuration.Protocol;
import org.apache.savan.eventing.EventingConstants;
import org.apache.savan.eventing.EventingSubscriptionProcessor;
import org.apache.savan.eventing.subscribers.EventingLeafSubscriber;
import org.apache.savan.storage.DefaultSubscriberStore;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.subscription.ExpirationBean;
import org.apache.savan.util.CommonUtil;

import javax.xml.namespace.QName;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.TestCase;

public class EventingSubscripitonProcessorTest extends TestCase {

	private final String TEST_SAVAN_CONFIG = "savan-config-test.xml";
	private final String EVENTING_PROTOCOL_NAME = "eventing";
	
	public void testSubscriberExtraction () throws Exception {
		SavanMessageContext smc = getSubscriptionMessage();
		
//		Protocol protocol = new Protocol ();
//		protocol.setName("eventing");
//		protocol.setUtilFactory(new EventingUtilFactory ());
//		protocol.setDefaultSubscriber("org.apache.savan.eventing.subscribers.EventingLeafSubscriber");
		
		SubscriberStore store = new DefaultSubscriberStore ();
		
//		smc.setProtocol(protocol);
		
		smc.setSubscriberStore(store);
		
		EventingSubscriptionProcessor esp = new EventingSubscriptionProcessor ();
		EventingLeafSubscriber eventingSubscriber = (EventingLeafSubscriber) esp.getSubscriberFromMessage(smc);
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
		Date date = ConverterUtil.convertToDateTime("2004-06-26T21:07:00.000-08:00").getTime();
		assertEquals(eventingSubscriber.getSubscriptionEndingTime(),date);
	}
	
	public void testExpirationBeanExtraction () throws Exception {
		SavanMessageContext smc = getRenewMessage();
		EventingSubscriptionProcessor esp = new EventingSubscriptionProcessor ();
		ExpirationBean expirationBean = esp.getExpirationBean(smc);
		
		assertNotNull(expirationBean);
		assertNotNull(expirationBean.getSubscriberID());
		
		Date date = ConverterUtil.convertToDateTime("2004-06-26T21:07:00.000-08:00").getTime();
		assertEquals(expirationBean.getDateValue(),date);
	}
	
	private SavanMessageContext getSubscriptionMessage () throws IOException {
        File baseDir = new File("");
        String testRource = baseDir.getAbsolutePath() + File.separator + "test-resources";

		SOAPEnvelope envelope = CommonUtil.getTestEnvelopeFromFile(testRource,"eventing-subscription.xml");
		
		AxisConfiguration axisConfiguration = new AxisConfiguration ();
		ConfigurationContext configurationContext = new ConfigurationContext (axisConfiguration);
		
		MessageContext mc = new MessageContext ();
		SavanMessageContext smc = new SavanMessageContext (mc);
		mc.setEnvelope(envelope);
		
		mc.setConfigurationContext(configurationContext);
		
		Options options = new Options ();
		options.setTo(new EndpointReference ("http://DummyToAddress/"));
		
		EndpointReference replyToEPR = new EndpointReference ("http://DummyReplyToAddress/");
		replyToEPR.addReferenceParameter(new QName ("RefParam1"),"RefParamVal1");
		options.setTo(replyToEPR);
		
		//adding a dummy AxisService to avoid NullPointer Exceptions.
		mc.setAxisService(new AxisService ("DummyService"));
		
		options.setAction("urn:uuid:DummyAction");
		
		String savan_concig_file = testRource + File.separator + TEST_SAVAN_CONFIG;
		File file = new File (savan_concig_file);
		if (!file.exists())
			throw new IOException (TEST_SAVAN_CONFIG + " file is not available in test-resources.");
		
		ConfigurationManager configurationManager = new ConfigurationManager ();
		configurationManager.configure(file);
		
		configurationContext.setProperty(SavanConstants.CONFIGURATION_MANAGER,configurationManager);
		
		Protocol protocol = configurationManager.getProtocol(EVENTING_PROTOCOL_NAME);
		smc.setProtocol(protocol);
		
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
