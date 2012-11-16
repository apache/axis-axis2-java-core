package org.apache.axis2.transport.xmpp.sample;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.xmpp.util.XMPPConstants;
import org.apache.axis2.util.XMLPrettyPrinter;

public class XMPPSampleClient {
    public static void main(String[] args) {
		invokeTimeService();
	}

	private static void invokeTimeService() {
		String endPointUrl = "xmpp://synapse.demo.0@gmail.com/" + "TimeService";

		EndpointReference targetEPR = new EndpointReference(endPointUrl);
		try {
            ConfigurationContext ctx =
            ConfigurationContextFactory.createConfigurationContextFromURIs(
                    XMPPSampleClient.class.getResource("axis2.xml"), null);

			OMElement payload = getPayload();
			Options options = new Options();
			options.setProperty(XMPPConstants.XMPP_SERVER_TYPE, XMPPConstants.XMPP_SERVER_TYPE_GOOGLETALK);
			options.setProperty(XMPPConstants.XMPP_SERVER_URL, XMPPConstants.GOOGLETALK_URL);
			options.setProperty(XMPPConstants.XMPP_SERVER_USERNAME, "synapse.demo.0");
			options.setProperty(XMPPConstants.XMPP_SERVER_PASSWORD, "mailpassword");

			options.setTo(targetEPR);
			options.setAction("urn:getServerTime");
			ServiceClient sender = new ServiceClient(ctx,null);

			sender.setOptions(options);
			OMElement result = sender.sendReceive(payload);
			XMLPrettyPrinter.prettify(result, System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static OMElement getPayload() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace(
				"http://example.xmpp.transports.axis2.org/example1", "example1");
        return fac.createOMElement("getServerTime", omNs);
	}
}
