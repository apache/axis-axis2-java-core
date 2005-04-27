package interop.doclit;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMText;
import org.apache.axis.om.SOAPBody;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.testUtils.SimpleTypeEncodingUtils;

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
public class InteropStub {

    private static final String INTEROP_NS_URI = "http://soapinterop.org/xsd";
    private static final String INTEROP_PREFIX = "itop";

    private OMFactory factory ;
    //private XMLOutputFactory outputFactory;
    private OMNamespace interopNS;
    private String endpointURL;
    private String SOAPAction;

    //these are special attributes to provide the input and output SOAP envelopes
    private SOAPEnvelope sentEnvelope;
    private SOAPEnvelope recvdEnvelope;

    public SOAPEnvelope getSentEnvelope() {
        return sentEnvelope;
    }


    public SOAPEnvelope getRecvdEnvelope() {
        return recvdEnvelope;
    }



    public String getSOAPAction() {
        return SOAPAction;
    }

    public void setSOAPAction(String SOAPAction) {
        this.SOAPAction = SOAPAction;
    }


    public InteropStub(String endpointURL) {
        this.endpointURL = endpointURL;
        factory= OMFactory.newInstance();

        //outputFactory = XMLOutputFactory.newInstance();
        this.interopNS = this.factory.createOMNamespace(INTEROP_NS_URI,INTEROP_PREFIX);

    }

    private SOAPEnvelope getEmptyEnvelop(){
        return factory.getDefaultEnvelope();
    }

    public SOAPStruct echoStruct(SOAPStruct inputEchoStruct) throws Exception{


            OMElement echoStructElementNode = factory.createOMElement("echoStructParam",interopNS);

            OMElement echoStructVarFloatElement = factory.createOMElement("varFloat",interopNS);
            echoStructVarFloatElement.addChild(factory.createText(inputEchoStruct.getVarFloat() +""));
            echoStructElementNode.addChild(echoStructVarFloatElement);

            OMElement echoStructVarIntElementNode = factory.createOMElement("varInt",interopNS);
            echoStructVarIntElementNode.addChild(factory.createText(inputEchoStruct.getVarInt() +""));
            echoStructElementNode.addChild(echoStructVarIntElementNode);

            OMElement echoStructVarStringElementNode = factory.createOMElement("varString",interopNS);
            echoStructVarStringElementNode.addChild(factory.createText(inputEchoStruct.getVarString()));
            echoStructElementNode.addChild(echoStructVarStringElementNode);

            SOAPEnvelope sendEnvelope = getEmptyEnvelop();
            sendEnvelope.getBody().addChild(echoStructElementNode);

            this.sentEnvelope = sendEnvelope;
            SOAPEnvelope returnEnvelope = getSyncResult(sendEnvelope);
            this.recvdEnvelope = returnEnvelope;

            SOAPBody SOAPBody = returnEnvelope.getBody();
            if (SOAPBody.hasFault()){
                throw new AxisFault("SOAP Fault",SOAPBody.getFault().getException());
            }

            OMElement elt = SOAPBody.getFirstChildWithName(new QName(INTEROP_NS_URI,"echoStructReturn"));
            if (elt==null){
                throw new AxisFault("Return element not found");
            }


            XMLStreamReader pullParser = elt.getXMLStreamReaderWithoutCaching();
            pullParser.next();
            SOAPStruct soapStructure = (SOAPStruct)new SOAPStructEncoder().deSerialize(pullParser);

            return soapStructure;


    }


    public String[] echoStringArray(String[] input) throws Exception{


            OMElement echoStringArrayParamElementNode = factory.createOMElement("echoStringArrayParam",interopNS);
            OMElement echoStringArrayElement;
            for (int i = 0; i < input.length; i++) {
                echoStringArrayElement = factory.createOMElement("string",interopNS);
                echoStringArrayParamElementNode.addChild(echoStringArrayElement);
                echoStringArrayElement.addChild(factory.createText(input[i]));
            }

            SOAPEnvelope sendEnvelope = getEmptyEnvelop();
            sendEnvelope.getBody().addChild(echoStringArrayParamElementNode);

            this.sentEnvelope = sendEnvelope;
            SOAPEnvelope returnEnvelope = getSyncResult(sendEnvelope);
            this.recvdEnvelope = returnEnvelope;


            SOAPBody SOAPBody = returnEnvelope.getBody();
            if (SOAPBody.hasFault()){
                throw new AxisFault("SOAP Fault",SOAPBody.getFault().getException());
            }

            OMElement elt = SOAPBody.getFirstChildWithName(new QName(INTEROP_NS_URI,"echoStringArrayReturn"));
            if (elt==null){
                throw new AxisFault("Return element not found");
            }

            return SimpleTypeEncodingUtils.deserializeStringArray(elt.getXMLStreamReaderWithoutCaching());



    }

    public String echoString(String input) throws Exception{

            OMElement echoStringParamElementNode = factory.createOMElement("echoStringParam",interopNS);
            OMText echoStringTextNode = factory.createText(echoStringParamElementNode,input);
            echoStringParamElementNode.addChild(echoStringTextNode);

            SOAPEnvelope sendEnvelope = getEmptyEnvelop();
            sendEnvelope.getBody().addChild(echoStringParamElementNode);

            this.sentEnvelope = sendEnvelope;
            SOAPEnvelope returnEnvelope = getSyncResult(sendEnvelope);
            this.recvdEnvelope = returnEnvelope;

            SOAPBody SOAPBody = returnEnvelope.getBody();
            if (SOAPBody.hasFault()){
                throw new AxisFault("SOAP Fault",SOAPBody.getFault().getException());
            }

            OMElement elt = SOAPBody.getFirstChildWithName(new QName(INTEROP_NS_URI,"echoStringReturn"));
            if (elt==null){
                throw new AxisFault("Return element not found");
            }

            return elt.getText();


    }


    private SOAPEnvelope getSyncResult(SOAPEnvelope env) throws AxisFault {
        EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO,
                this.endpointURL);
        Call call = new Call();
        call.setTo(targetEPR);
        if (SOAPAction!=null){
            call.setAction(SOAPAction);
        }
        return call.sendReceiveSync(env);

    }


}
