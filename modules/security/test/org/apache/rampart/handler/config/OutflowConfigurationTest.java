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

package org.apache.rampart.handler.config;

import junit.framework.TestCase;
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;

/**
 * Tests the org.apache.axis2.security.handler.config.OutflowConfiguration
 */
public class OutflowConfigurationTest extends TestCase {

	public OutflowConfigurationTest() {
		super();
	}

	public OutflowConfigurationTest(String name) {
		super(name);
	}

	/**
	 * This sets all the possible properties that can be set with 
	 * the outflow configuration
	 */
	public void testGetProperty() {

		OutflowConfiguration ofc = new OutflowConfiguration();

		String actionItems = "Timestamp Signature Encrypt";
		String user = "alice";
		String pwcb = "org.apache.axis2.security.PWCallback";
		String sigKeyId = "interop.properties";
		String sigParts = "{Element}{http://schemas.xmlsoap.org/ws/2004/08/"
				+ "addressing}MessageID;{Element}{http://docs.oasis-open.org/wss/"
				+ "2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp";
		String optimizeParts = "//xenc:EncryptedData/xenc:CipherData/xenc:CipherValue";
		String embeddedKeyCallbackClass = "org.apache.axis2.security.PWCallback";
		String encrUser = "bob";
		String samlPropFile = "saml.properties";
		String sigPropFile = "sig.properties";
		String encPropFile = "enc.properties";

		// Setting the properties in the ofc
		ofc.setActionItems(actionItems);
		ofc.setUser(user);
		ofc.setPasswordCallbackClass(pwcb);
		ofc.setSignatureKeyIdentifier(sigKeyId);
		ofc.setEncryptionKeyIdentifier(WSSHandlerConstants.SKI_KEY_IDENTIFIER);
		ofc.setSignatureParts(sigParts);
		ofc.setOptimizeParts(optimizeParts);
		ofc.setEmbeddedKeyCallbackClass(embeddedKeyCallbackClass);
		ofc.setEncryptionKeyTransportAlgorithm(WSConstants.KEYTRANSPORT_RSA15);
		ofc.setEncryptionSymAlgorithm(WSConstants.AES_128);
		ofc.setEncryptionUser(encrUser);
		ofc.setPasswordType(WSConstants.PW_DIGEST);
		ofc.setSamlPropFile(samlPropFile);
		ofc.setSignaturePropFile(sigPropFile);
		ofc.setEncryptionPropFile(encPropFile);

		// Check whether the props are there
		assertTrue("Action items missing", -1 < ofc.getProperty().getParameterElement().toString()
				.indexOf(
						"<" + WSSHandlerConstants.ACTION_ITEMS + ">"
								+ actionItems + "</"
								+ WSSHandlerConstants.ACTION_ITEMS + ">"));

		assertTrue("User missing", -1 < ofc.getProperty().getParameterElement().toString().indexOf(
				"<" + WSHandlerConstants.USER + ">" + user + "</"
						+ WSHandlerConstants.USER + ">"));

		assertTrue("passwordCallbackClass missing", -1 < ofc.getProperty().getParameterElement()
				.toString().indexOf(
						"<" + WSHandlerConstants.PW_CALLBACK_CLASS + ">" + pwcb
								+ "</" + WSHandlerConstants.PW_CALLBACK_CLASS
								+ ">"));

		assertTrue("sigKeyId missing", -1 < ofc.getProperty().getParameterElement().toString()
				.indexOf(
						"<" + WSHandlerConstants.SIG_KEY_ID + ">" + sigKeyId
								+ "</" + WSHandlerConstants.SIG_KEY_ID + ">"));

		assertTrue("encKeyId missing", -1 < ofc.getProperty().getParameterElement().toString()
				.indexOf(
						"<" + WSHandlerConstants.ENC_KEY_ID + ">"
								+ WSSHandlerConstants.SKI_KEY_IDENTIFIER + "</"
								+ WSHandlerConstants.ENC_KEY_ID + ">"));

		assertTrue("signature parts missing", -1 < ofc.getProperty().getParameterElement().toString()
				.indexOf(
						"<" + WSHandlerConstants.SIGNATURE_PARTS + ">"
								+ sigParts + "</"
								+ WSHandlerConstants.SIGNATURE_PARTS + ">"));

		assertTrue("optimize parts missing", -1 < ofc.getProperty().getParameterElement().toString()
				.indexOf(
						"<" + WSSHandlerConstants.OPTIMIZE_PARTS + ">"
								+ optimizeParts + "</"
								+ WSSHandlerConstants.OPTIMIZE_PARTS + ">"));

		assertTrue("EmbeddedKeyCallbackClass missing", -1 < ofc.getProperty().getParameterElement().toString()
				.indexOf(
						"<" + WSHandlerConstants.ENC_CALLBACK_CLASS + ">"
								+ embeddedKeyCallbackClass + "</"
								+ WSHandlerConstants.ENC_CALLBACK_CLASS + ">"));

		assertTrue("encryptionKeyTransportAlgorithm missing", -1 < ofc.getProperty().getParameterElement().toString()
				.indexOf(
						"<" + WSHandlerConstants.ENC_KEY_TRANSPORT + ">"
								+ WSConstants.KEYTRANSPORT_RSA15 + "</"
								+ WSHandlerConstants.ENC_KEY_TRANSPORT + ">"));

		assertTrue("encryptionSymAlgorithm missing", -1 < ofc.getProperty().getParameterElement().toString()
				.indexOf(
						"<" + WSHandlerConstants.ENC_SYM_ALGO + ">"
								+ WSConstants.AES_128 + "</"
								+ WSHandlerConstants.ENC_SYM_ALGO + ">"));

		assertTrue("encrUser missing", -1 < ofc.getProperty().getParameterElement().toString()
				.indexOf(
						"<" + WSHandlerConstants.ENCRYPTION_USER + ">"
								+ encrUser + "</"
								+ WSHandlerConstants.ENCRYPTION_USER + ">"));

		assertTrue("passwordType missing", -1 < ofc.getProperty().getParameterElement().toString()
				.indexOf(
						"<" + WSHandlerConstants.PASSWORD_TYPE + ">"
								+ WSConstants.PW_DIGEST + "</"
								+ WSHandlerConstants.PASSWORD_TYPE + ">"));

		assertTrue("samlPropFile missing", -1 < ofc.getProperty().getParameterElement().toString()
				.indexOf(
						"<" + WSHandlerConstants.SAML_PROP_FILE + ">"
								+ samlPropFile + "</"
								+ WSHandlerConstants.SAML_PROP_FILE + ">"));

		assertTrue("sigPropFile missing", -1 < ofc.getProperty().getParameterElement().toString()
				.indexOf(
						"<" + WSHandlerConstants.SIG_PROP_FILE + ">"
								+ sigPropFile + "</"
								+ WSHandlerConstants.SIG_PROP_FILE + ">"));
		assertTrue("encPropFile missing", -1 < ofc.getProperty().getParameterElement().toString()
				.indexOf(
						"<" + WSHandlerConstants.ENC_PROP_FILE + ">"
								+ encPropFile + "</"
								+ WSHandlerConstants.ENC_PROP_FILE + ">"));
	}
	
	/**
	 * This tests multiple action configurations
	 */
	public void testMultipleActions() {
		OutflowConfiguration ofc = new OutflowConfiguration(2);

		String actionItems1 = "Timestamp Signature Encrypt";
		String user1 = "alice";

		String actionItems2 = "Signature Encrypt Timestamp";
		String user2 = "alice2";
		
		ofc.setActionItems(actionItems1);
		ofc.setUser(user1);
		
		ofc.nextAction();
		
		ofc.setActionItems(actionItems2);
		ofc.setUser(user2);

		assertEquals("Action items mismatch", actionItems2, ofc.getActionItems());
		assertEquals("Action items mismatch", user2, ofc.getUser());
		
		ofc.previousAction();
		
		assertEquals("Action items mismatch", actionItems1, ofc.getActionItems());
		assertEquals("Action items mismatch", user1, ofc.getUser());
		
	}

}
