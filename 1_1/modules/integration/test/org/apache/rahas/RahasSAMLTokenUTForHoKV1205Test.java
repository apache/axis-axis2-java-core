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

package org.apache.rahas;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.util.Base64;
import org.apache.neethi.Policy;
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.util.WSSecurityUtil;
import org.opensaml.XML;

import javax.xml.namespace.QName;

public class RahasSAMLTokenUTForHoKV1205Test extends TestClient {

    byte[] clientEntr;
    
    /**
     * @param name
     */
    public RahasSAMLTokenUTForHoKV1205Test(String name) {
        super(name);
    }

    public OMElement getRequest() {
        try {
            OMElement rstElem = TrustUtil.createRequestSecurityTokenElement(RahasConstants.VERSION_05_12);
            TrustUtil.createRequestTypeElement(RahasConstants.VERSION_05_12, rstElem, RahasConstants.REQ_TYPE_ISSUE);
            OMElement tokenTypeElem = TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_12, rstElem);
            tokenTypeElem.setText(RahasConstants.TOK_TYPE_SAML_10);
            
            TrustUtil.createAppliesToElement(rstElem,
//                    "https://207.200.37.116/Ping/Scenario1", this.getWSANamespace());
                    "http://localhost:5555/axis2/services/SecureService", this.getWSANamespace());
            TrustUtil.createKeyTypeElement(RahasConstants.VERSION_05_12,
                    rstElem, RahasConstants.KEY_TYPE_SYMM_KEY);
            TrustUtil.createKeySizeElement(RahasConstants.VERSION_05_12, rstElem, 256);
            
            byte[] nonce = WSSecurityUtil.generateNonce(16);
            clientEntr = nonce;
            OMElement entrElem = TrustUtil.createEntropyElement(RahasConstants.VERSION_05_12, rstElem);
            TrustUtil.createBinarySecretElement(RahasConstants.VERSION_05_12, entrElem, RahasConstants.BIN_SEC_TYPE_NONCE).setText(Base64.encode(nonce));
            TrustUtil.createComputedKeyAlgorithm(RahasConstants.VERSION_05_12,rstElem, RahasConstants.COMPUTED_KEY_PSHA1);
            
            return rstElem;
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OutflowConfiguration getClientOutflowConfiguration() {
        OutflowConfiguration ofc = new OutflowConfiguration();

        ofc.setActionItems("UsernameToken Timestamp");
        ofc.setUser("joe");
        ofc.setPasswordType(WSConstants.PW_TEXT);
        ofc.setPasswordCallbackClass(PWCallback.class.getName());
        return ofc;
    }

    public InflowConfiguration getClientInflowConfiguration() {
        InflowConfiguration ifc = new InflowConfiguration();

        ifc.setActionItems("Timestamp");
        
        return ifc;
    }

    public String getServiceRepo() {
        return "rahas_service_repo_3";
    }

    public String getRequestAction() throws TrustException {
        return TrustUtil.getActionValue(RahasConstants.VERSION_05_12, RahasConstants.RST_ACTION_ISSUE);
    }

    public void validateRsponse(OMElement resp) {
        OMElement rstr = resp.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_12,
                                                              RahasConstants.LocalNames.
                                                                      REQUEST_SECURITY_TOKEN_RESPONSE));
        assertNotNull("RequestedSecurityTokenResponse missing", rstr);
        OMElement rst = rstr.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_12,
                                                             RahasConstants.IssuanceBindingLocalNames.
                                                                     REQUESTED_SECURITY_TOKEN));
        assertNotNull("RequestedSecurityToken missing", rst);
        
        OMElement elem = rst.getFirstChildWithName(new QName(XML.SAML_NS, "Assertion"));
        assertNotNull("Missing SAML Assertoin", elem);
        
        //Uncomment for inteorp - START
//        String respEntrB64 = rstr.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_12, RahasConstants.ENTROPY_LN)).getFirstChildWithName(new QName(RahasConstants.WST_NS_05_12, RahasConstants.BINARY_SECRET_LN)).getText().trim();
//
//        
//        
//        OMElement attrStmtElem = elem.getFirstChildWithName(new QName(XML.SAML_NS, "AttributeStatement"));
//        OMElement kiElem = attrStmtElem.getFirstChildWithName(new QName(XML.SAML_NS,"Subject")).getFirstChildWithName(new QName(XML.SAML_NS,"SubjectConfirmation")).getFirstChildWithName(new QName("http://www.w3.org/2000/09/xmldsig#", "KeyInfo"));
//        OMElement encrKey = kiElem.getFirstChildWithName(new QName("http://www.w3.org/2001/04/xmlenc#", "EncryptedKey"));
//        
//        
//        String cipherValue = encrKey.getFirstChildWithName(new QName("http://www.w3.org/2001/04/xmlenc#", "CipherData")).getFirstChildWithName(new QName("http://www.w3.org/2001/04/xmlenc#", "CipherValue")).getText();
//        
//        byte[] serviceEntr = Base64.decode(respEntrB64);
        
//      try {
//          this.requestService(elem, clientEntr, serviceEntr);
//      } catch (Exception e) {
//          e.printStackTrace();
//      }

        //Uncomment for inteorp - END
        
        

    }

    public Policy getServicePolicy() throws Exception {
        return this.getPolicy("test-resources/rahas/policy/service-policy-transport-binding.xml");
    }

    public Policy getSTSPolicy() throws Exception {
        return this.getPolicy("test-resources/rahas/policy/sts-policy-transport-binding.xml");
    }
    

    /* (non-Javadoc)
     * @see org.apache.rahas.TestClient#getRSTTemplate()
     */
    public OMElement getRSTTemplate() throws TrustException {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement elem = factory.createOMElement(Constants.RST_TEMPLATE.getLocalPart(), factory.createOMNamespace(Constants.RST_TEMPLATE.getNamespaceURI(),"wsp"));
        
        TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_12, elem).setText(RahasConstants.TOK_TYPE_SAML_10);
        TrustUtil.createKeyTypeElement(RahasConstants.VERSION_05_12, elem, RahasConstants.KEY_TYPE_SYMM_KEY);
        TrustUtil.createKeySizeElement(RahasConstants.VERSION_05_12, elem, 256);
        
        return elem;
    }
    
    public int getTrstVersion() {
        return RahasConstants.VERSION_05_12;
    }
    
//    private void requestService(OMElement assertion, byte[] reqEnt, byte[] respEnt) throws Exception {
//        
//        StAXOMBuilder builder = new StAXOMBuilder(new OMDOMFactory(), assertion.getXMLStreamReader());
//        Element domAssertionElem = (Element)builder.getDocumentElement();
//
//        DocumentBuilderFactoryImpl.setDOOMRequired(true);
//        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//        
//        SOAPFactory fac = new SOAP11Factory((DocumentImpl)doc);
//        SOAPEnvelope envelope = fac.getDefaultEnvelope();
//        this.addPayload(envelope);
//        
//        WSSecHeader secHeader = new WSSecHeader();
//        secHeader.insertSecurityHeader(doc);
//        
//        WSSecTimestamp ts = new WSSecTimestamp();
//        ts.prepare(doc);
//        ts.prependToHeader(secHeader);
//        
//        WSSecDKSign sig = new WSSecDKSign();
//        sig.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA1);
//        P_SHA1 p_sha1 = new P_SHA1();
//        SecurityTokenReference ref = new SecurityTokenReference(doc);
//        ref.setSAMLKeyIdentifier(assertion.getAttributeValue(new QName("AssertionID")));
//        
//        System.out.println("\nRequest Entropy: " + Base64.encode(reqEnt));
//        System.out.println("Response Entropy: " + Base64.encode(respEnt));
//        
//        byte[] ephmeralKey = p_sha1.createKey(reqEnt, respEnt, 0, 32);
//        
//        System.out.println( ephmeralKey.length * 8 + " bit Key: " + Base64.encode(ephmeralKey));
//        
//        sig.setExternalKey(ephmeralKey, ref.getElement());
//
//        WSEncryptionPart part = new WSEncryptionPart(WSConstants.TIMESTAMP_TOKEN_LN, WSConstants.WSU_NS, "Element");
//        Vector partsVector = new Vector();
//        partsVector.add(part);
//        sig.setParts(partsVector);
//        
//        sig.prepare(doc, secHeader);
//        sig.addReferencesToSign(partsVector, secHeader);
//        sig.computeSignature();
//        
//        Element importedAssertionElement = (Element) doc.importNode(domAssertionElem, true);
//        WSSecurityUtil.appendChildElement(doc, secHeader.getSecurityHeader(), importedAssertionElement);
//        sig.appendDKElementToHeader(secHeader);
//        sig.appendSigToHeader(secHeader);
//
//        
//        System.out.println(envelope);
//        
//        
//        //Create a service client and send the request
//        AxisService service = new AxisService("ping");
//        AxisOperation op = new OutInAxisOperation(new QName("Ping"));
//        service.addChild(op);
//        
//        ServiceClient client = new ServiceClient(ConfigurationContextFactory.createConfigurationContextFromFileSystem(Constants.TESTING_PATH + "rahas_client_repo", null), service);
//
//        
//        OperationClient opClient = client.createClient(new QName("Ping"));
//        MessageContext mc = new MessageContext();
//        mc.setEnvelope(envelope);
//        
//        client.engageModule(new QName("addressing"));
//        client.engageModule(new QName("rampart"));
//        
//        opClient.addMessageContext(mc);
////        opClient.getOptions().setTo(new EndpointReference("https://131.107.72.15/PingService/OasisScenario1"));
//        opClient.getOptions().setTo(new EndpointReference("https://207.200.37.116/Ping/Scenario1"));
//        
//        opClient.getOptions().setAction("http://example.org/Ping");
////        opClient.getOptions().setProperty(AddressingConstants.WS_ADDRESSING_VERSION, AddressingConstants.Submission.WSA_NAMESPACE);
//        
//        opClient.execute(true);
//        MessageContext response = opClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
//        System.out.println("------------------------------RESPONSE------------------------------\n" + response.getEnvelope());
//        
//    }
//    
//    private void addPayload(SOAPEnvelope env) {
//        //<Ping xmlns="http://example.org/Ping">Ping</Ping>
//        OMNamespace ns = env.getOMFactory().createOMNamespace("http://example.org/Ping", "");
//        OMElement elem = env.getOMFactory().createOMElement("Ping", ns);
//        elem.setText("Ping");
//        
//        env.getBody().addChild(elem);
//    }
    

}
