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

import junit.framework.TestCase;
import org.apache.axis.om.impl.OMAttributeImpl;
import org.apache.axis.om.impl.OMElementImpl;
import org.apache.axis.om.impl.OMNavigator;
import org.apache.axis.om.impl.OMTextImpl;
import org.apache.axis.om.impl.serialize.SimpleOMSerializer;
import org.apache.axis.om.impl.streamwrapper.OMStAXBuilder;
import org.xmlpull.v1.XmlPullParser;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;


/**
 * @author Dasarath Weeratunge
 *
 */
public class Tester extends TestCase {
    private static final String IN_FILE_NAME = "resources/soapmessage.xml";
    private static final String OUT_FILE_NAME = "resources/tester-out.xml";

    private SimpleOMSerializer serializer = null;

    protected void setUp() throws Exception {
        super.setUp();
        serializer = new SimpleOMSerializer();
    }

    public void test1() throws Exception {
		System.out.println("\n+++");
		OMElement root= getOMBuilder().getSOAPMessage().getEnvelope();
		System.out.println(root.isComplete());
		print(root,System.out);
	}

	public void test2() throws Exception {
		System.out.println("\n+++");
		OMElement root= getOMBuilder().getSOAPMessage().getEnvelope();

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
		print(root,System.out);
	}

	public void test3() throws Exception {
		System.out.println("\n+++");
		OMElement root= getOMBuilder().getSOAPMessage().getEnvelope();

		OMNamespace soapenv= root.resolveNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
		OMNamespace wsa= root.resolveNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", "wsa");

		OMElement relatesTo= new OMElementImpl("RelatesTo", wsa);
		relatesTo.insertAttribute(new OMAttributeImpl("RelationshipType", null, "wsa:Reply", relatesTo));
		relatesTo.insertAttribute(new OMAttributeImpl("mustUnderstand", soapenv, "0", relatesTo));
		relatesTo.addChild(new OMTextImpl(relatesTo, "uuid:3821F4F0-D020-11D8-A10A-E4EE6425FCB0"));

		System.out.println(root.isComplete());
		root.addChild(relatesTo);

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

		print(root,System.out);
	}

	public void test4() throws Exception {
		System.out.println("\n+++");
		OMElementImpl root= new OMElementImpl("Envelope", null);
		OMNamespace soapenv= root.createNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
		root.setNamespace(soapenv);
		OMElement header= new OMElementImpl("Header", soapenv);
		root.addChild(header);
		OMNamespace xsd= root.createNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
		OMNamespace xsi= root.createNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
		OMNamespace wsa= root.createNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", "wsa");

		OMElement messageID= new OMElementImpl("MessageID", wsa);
		messageID.addChild(new OMTextImpl("uuid:920C5190-0B8F-11D9-8CED-F22EDEEBF7E5"));
		messageID.insertAttribute(new OMAttributeImpl("mustUnderstand", soapenv, "0"));

		OMElement from= new OMElementImpl("From", wsa);
		OMElement address= new OMElementImpl("Address", null);
		address.createNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", null);
		address.setNamespace(
			address.resolveNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", null));
		address.addChild(new OMTextImpl("http://schemas.xmlsoap.org/ws/2004/03/addressing/role/anonymous"));
		from.addChild(address);
		//	FIXME:	cannot reuse the attribute object created earlier. linked list--
		from.insertAttribute(new OMAttributeImpl("mustUnderstand", soapenv, "0"));

		OMElement to= new OMElementImpl("To", wsa);
		to.addChild(new OMTextImpl("http://localhost:8081/axis/services/BankPort"));
		to.insertAttribute(new OMAttributeImpl("mustUnderstand", soapenv, "0"));

		OMElement relatesTo= new OMElementImpl("RelatesTo", wsa);
		relatesTo.insertAttribute(new OMAttributeImpl("RelationshipType", null, "wsa:Reply"));
		relatesTo.insertAttribute(new OMAttributeImpl("mustUnderstand", soapenv, "0"));
		relatesTo.addChild(new OMTextImpl("uuid:3821F4F0-D020-11D8-A10A-E4EE6425FCB0"));

		header.addChild(from);
		from.insertSiblingAfter(relatesTo);
		from.insertSiblingBefore(to);
		to.insertSiblingBefore(messageID);

		root.print(new PrintStream(new FileOutputStream(OUT_FILE_NAME)));
	}
	
//	public void test5() throws Exception {
//			System.out.println("\n+++");
//			OMXMLParserWrapper omBuilder= getOMBuilder();
//			OMElement root= omBuilder.getSOAPMessage().getEnvelope();
//
//        System.out.println("root.isComplete() = " + root.isComplete());
//			root.getFirstChild().detach();
//			OMElement header= (OMElement)root.getFirstChild();
//			while (!header.isComplete())
//				omBuilder.next();
//			navigate(root);
//			System.out.println("---");
//			omBuilder.reset(header);
//			int event;
//			do {
//				event= omBuilder.next();
//				System.out.print(getFieldName(event)+"= ");
//				if (event == XmlPullParser.TEXT)
//					System.out.println(omBuilder.getText());
//				else {
//					System.out.println("{"+omBuilder.getNamespace()+"}"+omBuilder.getName());
//				}
//			}while (event != XmlPullParser.END_DOCUMENT);
//			System.out.println("---");
//			root.print(System.out);
//		}

	private void navigate(OMElement node) throws Exception {
		OMNavigator navigator= new OMNavigator(node);
		OMNode tempNode= navigator.next();
		do {
			if (tempNode instanceof OMElement) {
				OMElement el= (OMElement)tempNode;
				System.out.print("OMElement= " + el.getLocalName());
			}
			else
				System.out.print("OMText= " + tempNode.getValue());
			System.out.println(" isComplete= " + tempNode.isComplete());
			tempNode= navigator.next();
		}
		while (tempNode != null);
	}

	private OMXMLParserWrapper getOMBuilder() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(IN_FILE_NAME));
		return new OMStAXBuilder(reader);
	}
	
	private static Field[] flds= XmlPullParser.class.getDeclaredFields();

	public static String getFieldName(int field) throws Exception {
		for (int i= 1; i < flds.length + 1; i++) {
			if (flds[i].getInt(null) == field)
				return flds[i].getName();
		}
		return null;
	}

    private void print(OMNode node,OutputStream stream){
        this.serializer.serialize(node,stream);
    }
}
