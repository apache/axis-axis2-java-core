/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.sample.headershandler;

import java.io.ByteArrayOutputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.LogicalMessage;
import jakarta.xml.ws.ProtocolException;
import jakarta.xml.ws.handler.MessageContext;

import org.apache.axis2.Constants;
import org.apache.axis2.jaxws.handler.LogicalMessageContext;

public class HeadersServerLogicalHandler implements
        jakarta.xml.ws.handler.LogicalHandler<LogicalMessageContext> {

	private HandlerTracker tracker = new HandlerTracker(HeadersServerLogicalHandler.class.getSimpleName());
	private TestHeaders headerUtil = new TestHeaders(this.getClass());
	
    public void close(MessageContext messagecontext) {
    	tracker.close();
    }

    public boolean handleFault(LogicalMessageContext messagecontext) {
    	Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    	tracker.handleFault(outbound);
        return true;
    }

    public boolean handleMessage(LogicalMessageContext messagecontext) {
        Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        tracker.handleMessage(outbound);
        if (outbound) { // outbound response on the server

        	// this is the first server outbound handler hit
        	
            // turn off special property that logs ability to use
            // both SOAPHeadersAdapter and SAAJ in a single handler method:
            messagecontext.put(org.apache.axis2.jaxws.handler.Constants.JAXWS_HANDLER_TRACKER, false);
            
        	Map<QName, List<String>> requestHeaders = (Map<QName, List<String>>)messagecontext.get(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);
        	
        	// add a header
        	String acoh1 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL1);
        	List<String> acoh1list = new ArrayList<String>();
        	acoh1list.add(acoh1);
        	requestHeaders.put(TestHeaders.ACOH1_HEADER_QNAME, acoh1list);
        	messagecontext.put(Constants.JAXWS_INBOUND_SOAP_HEADERS, requestHeaders);
        	tracker.addHeader(acoh1.toString());
        	
        	// manipulate the payload just to be thorough (the response element happens
        	// to be in the same place as arg0, so using the same methods as inbound)

        	LogicalMessage msg = messagecontext.getMessage();
        	String st = getStringFromSourcePayload(msg.getPayload());
        	String txt = String.valueOf(Integer.valueOf(getFirstArg(st)) - 1);
        	st = replaceFirstArg(st, txt);
        	msg.setPayload(new StreamSource(new StringBufferInputStream(st)));

        } else {  // inbound request on the server
        	
        	// this is the second server inbound hit
            
            // turn off special property that logs ability to use
            // both SOAPHeadersAdapter and SAAJ in a single handler method:
            messagecontext.put(org.apache.axis2.jaxws.handler.Constants.JAXWS_HANDLER_TRACKER, false);
        	
        	// let's check for all the headers we expect, remove them, and manipulate the message payload
        	
        	Map<QName, List<String>> requestHeaders = (Map<QName, List<String>>)messagecontext.get(Constants.JAXWS_INBOUND_SOAP_HEADERS);
        	headerUtil.confirmHeadersAdapterList(Constants.JAXWS_INBOUND_SOAP_HEADERS, requestHeaders, 2);

        	// expecting two header elements under two different QNames
        	
        	List<String> list1 = requestHeaders.get(TestHeaders.ACOH3_HEADER_QNAME);
        	headerUtil.confirmList(TestHeaders.ACOH3_HEADER_QNAME, list1, 1);
        	String acoh1 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH3_HEADER_QNAME, TestHeaders.CONTENT_LARGE);
        	headerUtil.compareHeaderStrings(acoh1, list1.get(0));
        	tracker.checkHeader(list1.get(0));

        	List<String> list2 = requestHeaders.get(TestHeaders.ACOH4_HEADER_QNAME);
        	headerUtil.confirmList(TestHeaders.ACOH4_HEADER_QNAME, list2, 1);
        	String acoh4 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH4_HEADER_QNAME, TestHeaders.CONTENT_SMALL4);
        	headerUtil.compareHeaderStrings(acoh4, list2.get(0));
        	tracker.checkHeader(list2.get(0));

        	// remove the headers before continuing
        	tracker.removedHeader(acoh1);
        	requestHeaders.remove(TestHeaders.ACOH3_HEADER_QNAME);
        	
        	// manipulate the payload between header removals just to be thorough
        	
        	/*
        	 * NOTE: This is an important test!  The get of a header from the SOAPHeadersAdapter triggers a
        	 * parse of the header OM element, which previously marked the parser as 'done'.  This caused the
        	 * below getPayload() to throw an OMException.  This only occurred on server inbound in this
        	 * scenario, so this implementation MUST REMAIN.  See top of XMLStringBlockImpl._getBOFromReader
        	 * for the fix.
        	 */
        	
        	LogicalMessage msg = messagecontext.getMessage();
        	String st = getStringFromSourcePayload(msg.getPayload());
        	int firstArg = Integer.valueOf(getFirstArg(st));
        	// FIXED INTERMITTENT TEST FAILURE: Extract second parameter value to check precisely
        	int secondArg = Integer.valueOf(getSecondArg(st));

        	String txt = String.valueOf(firstArg - 1);
        	st = replaceFirstArg(st, txt);
        	msg.setPayload(new StreamSource(new StringBufferInputStream(st)));

        	tracker.removedHeader(acoh4);
        	requestHeaders.remove(TestHeaders.ACOH4_HEADER_QNAME);

        	// FIXED INTERMITTENT TEST FAILURE: Previously used unreliable st.contains("66")/st.contains("33")
        	// checks on entire XML payload string, which could match XML structure/namespaces causing false positives.
        	// Now check the actual parsed parameter values to avoid intermittent failures.
        	if (secondArg == 66) {
        	    // test flow reversal and handleFault method ability to access/set headers
        	    throw new ProtocolException("I don't like 66");
        	} else if (secondArg == 33) {
        	    // test flow reversal, without handleFault flow
        	    return false;
        	}
        	
        }
        return true;
    }

    private static String getFirstArg(String payloadString) {
        StringTokenizer st = new StringTokenizer(payloadString, ">");
        st.nextToken(); // skip first token.
        st.nextToken(); // skip second
        String tempString = st.nextToken();
        String returnString = new StringTokenizer(tempString, "<").nextToken();
        return returnString;
    }

    /**
     * Extract the second argument from the XML payload string.
     * Added to fix intermittent test failure - enables precise parameter value checking
     * instead of unreliable substring matching on entire XML payload.
     */
    private static String getSecondArg(String payloadString) {
        StringTokenizer st = new StringTokenizer(payloadString, ">");
        st.nextToken(); // skip first token.
        st.nextToken(); // skip second
        st.nextToken(); // skip third (first arg)
        st.nextToken(); // skip fourth
        String tempString = st.nextToken();
        String returnString = new StringTokenizer(tempString, "<").nextToken();
        return returnString;
    }

    private static String replaceFirstArg(String payloadString, String newArg) {
        String firstArg = getFirstArg(payloadString);
        payloadString = payloadString.replaceFirst(firstArg, newArg);
        return payloadString;
    }
    
    private static String getStringFromSourcePayload(Source payload) {
        try {

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer trans = factory.newTransformer();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(baos);

            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.transform(payload, result);

            HandlerTracker tracker = new HandlerTracker(HeadersServerLogicalHandler.class.getSimpleName());
            
            return new String(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
