/*
 * Created on Sep 26, 2004
 * Copyright  2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.axis.om;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.apache.axis.om.*;

/**
 * @author Dasarath Weeratunge
 *
 */
public class Tester extends TestCase {
    private static final String IN_FILE_NAME = "src/test-resources/soapmessage.xml";
    private static final String OUT_FILE_NAME = "src/test-resources/tester-out.xml";

    public void test1() throws Exception {
		System.out.println("\n+++");
		OMElement root= getOMBuilder().getDocument();
		System.out.println(root.isComplete());
		root.print(System.out);
	}

	public void test2() throws Exception {
		System.out.println("\n+++");
		OMElement root= getOMBuilder().getDocument();

		System.out.println(root.isComplete());
		root.getFirstChild().detach();

		System.out.println("---");
		navigate(root);

		OMElement header= (OMElement)root.getFirstChild();
		//	we read the header completely but do not cache it
		header.detach();

		System.out.println("---");
		navigate(root);

		System.out.println("---");
		root.print(System.out);
	}

	public void test3() throws Exception {
		System.out.println("\n+++");
		OMElement root= getOMBuilder().getDocument();

		OMNamespace soapenv= root.resolveNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
		OMNamespace wsa= root.resolveNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", "wsa");

		OMElement relatesTo= new OMElement("RelatesTo", wsa);
		relatesTo.insertAttribute(new OMAttribute("RelationshipType", null, "wsa:Reply"));
		relatesTo.insertAttribute(new OMAttribute("mustUnderstand", soapenv, "0"));
		relatesTo.insertChild(new OMText("uuid:3821F4F0-D020-11D8-A10A-E4EE6425FCB0"));

		System.out.println(root.isComplete());
		root.insertChild(relatesTo);

		OMNavigator navigator= new OMNavigator(root);
		OMNode node= navigator.next();
		do {
			if (node instanceof OMElement) {
				OMElement el= (OMElement)node;
				System.out.println("OMElement= " + el.getLocalName());
			}
			else
				System.out.println("OMText= " + node.getValue());
			node= navigator.next();
		}
		while (node != null);

		root.print(System.out);
	}

	public void test4() throws Exception {
		System.out.println("\n+++");
		OMElement root= new OMElement("Envelope", null);
		OMNamespace soapenv= root.createNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
		root.setNamespace(soapenv);
		OMElement header= new OMElement("Header", soapenv);
		root.insertChild(header);
		OMNamespace xsd= root.createNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
		OMNamespace xsi= root.createNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
		OMNamespace wsa= root.createNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", "wsa");

		OMElement messageID= new OMElement("MessageID", wsa);
		messageID.insertChild(new OMText("uuid:920C5190-0B8F-11D9-8CED-F22EDEEBF7E5"));
		messageID.insertAttribute(new OMAttribute("mustUnderstand", soapenv, "0"));

		OMElement from= new OMElement("From", wsa);
		OMElement address= new OMElement("Address", null);
		address.createNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", null);
		address.setNamespace(
			address.resolveNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", null));
		address.insertChild(new OMText("http://schemas.xmlsoap.org/ws/2004/03/addressing/role/anonymous"));
		from.insertChild(address);
		//	FIXME:	cannot reuse the attribute object created earlier. linked list--
		from.insertAttribute(new OMAttribute("mustUnderstand", soapenv, "0"));

		OMElement to= new OMElement("To", wsa);
		to.insertChild(new OMText("http://localhost:8081/axis/services/BankPort"));
		to.insertAttribute(new OMAttribute("mustUnderstand", soapenv, "0"));

		OMElement relatesTo= new OMElement("RelatesTo", wsa);
		relatesTo.insertAttribute(new OMAttribute("RelationshipType", null, "wsa:Reply"));
		relatesTo.insertAttribute(new OMAttribute("mustUnderstand", soapenv, "0"));
		relatesTo.insertChild(new OMText("uuid:3821F4F0-D020-11D8-A10A-E4EE6425FCB0"));

		header.insertChild(from);
		from.insertSiblingAfter(relatesTo);
		from.insertSiblingBefore(to);
		to.insertSiblingBefore(messageID);

		root.print(new PrintStream(new FileOutputStream(OUT_FILE_NAME)));
	}
	
	public void test5() throws Exception {
			System.out.println("\n+++");
			OMXmlPullParserWrapper omBuilder= getOMBuilder();
			OMElement root= omBuilder.getDocument();
			root.getFirstChild().detach();
			OMElement header= (OMElement)root.getFirstChild();
			while (!header.isComplete())
				omBuilder.next();
			navigate(root);	
			System.out.println("---");
			omBuilder.reset(header);
			int event;
			do {
				event= omBuilder.next();
				System.out.print(getFieldName(event)+"= ");
				if (event == XmlPullParser.TEXT)
					System.out.println(omBuilder.getText());
				else {
					System.out.println("{"+omBuilder.getNamespace()+"}"+omBuilder.getName());
				}
			}while (event != XmlPullParser.END_DOCUMENT);
			System.out.println("---");
			root.print(System.out);
		}

	private void navigate(OMNode node) throws Exception {
		OMNavigator navigator= new OMNavigator(node);
		node= navigator.next();
		do {
			if (node instanceof OMElement) {
				OMElement el= (OMElement)node;
				System.out.print("OMElement= " + el.getLocalName());
			}
			else
				System.out.print("OMText= " + node.getValue());
			System.out.println(" isComplete= " + node.isComplete());
			node= navigator.next();
		}
		while (node != null);
	}

	private OMXmlPullParserWrapper getOMBuilder() throws Exception {
		XmlPullParser parser= XmlPullParserFactory.newInstance().newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
		parser.setInput(new FileReader(IN_FILE_NAME));
		return new OMXmlPullParserWrapper(parser);
	}
	
	private static Field[] flds= XmlPullParser.class.getDeclaredFields();

	public static String getFieldName(int field) throws Exception {
		for (int i= 1; i < flds.length + 1; i++) {
			if (flds[i].getInt(null) == field)
				return flds[i].getName();
		}
		return null;
	}
}
