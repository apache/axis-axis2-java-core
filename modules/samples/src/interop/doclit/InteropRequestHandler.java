package interop.doclit;

import interop.util.InteropTO;
import interop.util.Constants;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;

import org.apache.axis.om.SOAPEnvelope;

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
*
*
*/
public class InteropRequestHandler {

    private  XMLOutputFactory writerFactory = XMLOutputFactory.newInstance();


    public void handleInteropRequest(InteropTO interopTransferTo) throws Exception{
        int requestType = interopTransferTo.getType();
        if (requestType==Constants.InteropConstants.ECHO_STRING_SERVICE){
            interopEchoString(interopTransferTo);
        }else if (requestType==Constants.InteropConstants.ECHO_STRUCT_SERVICE){
            interopEchoStruct(interopTransferTo);
        }else if (requestType==Constants.InteropConstants.ECHO_STRING_ARRAY_SERVICE){
           interopEchoStringArray(interopTransferTo);
        }
    }

    private void interopEchoStruct(InteropTO transferTo) throws Exception{
            String endpointURL = transferTo.getURL();
            String SOAPAction = transferTo.getSOAPAction();

            InteropStub interopStub = new InteropStub(endpointURL);
            interopStub.setSOAPAction(SOAPAction);
            //call method
            SOAPStruct soapStruct = new SOAPStruct();
            soapStruct.setVarFloat(transferTo.getStructfloat());
            soapStruct.setVarInt(transferTo.getStructint());
            soapStruct.setVarString(transferTo.getStructString());
            interopStub.echoStruct(soapStruct);
            //update the envelopes
            updateEnvelopes(interopStub,transferTo);

        }


     private void interopEchoStringArray(InteropTO transferTo) throws Exception{
        String endpointURL = transferTo.getURL();
        String SOAPAction = transferTo.getSOAPAction();

        InteropStub interopStub = new InteropStub(endpointURL);
        interopStub.setSOAPAction(SOAPAction);
        //call method
        interopStub.echoStringArray(transferTo.getArraValue());
        //update the envelopes
        updateEnvelopes(interopStub,transferTo);

    }

    private void interopEchoString(InteropTO transferTo) throws Exception{
        String endpointURL = transferTo.getURL();
        String SOAPAction = transferTo.getSOAPAction();

        InteropStub interopStub = new InteropStub(endpointURL);
        interopStub.setSOAPAction(SOAPAction);
        //call method
        interopStub.echoString(transferTo.getStringValue());
        //update the envelopes
        updateEnvelopes(interopStub,transferTo);

    }

    private void updateEnvelopes(InteropStub stub,InteropTO transferTo) throws Exception{
        transferTo.setRequest(writeStringFromEnvelope(stub.getSentEnvelope()));
        transferTo.setResponse(writeStringFromEnvelope(stub.getRecvdEnvelope()));

    }

    private String writeStringFromEnvelope(SOAPEnvelope env) throws XMLStreamException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(100);
        XMLStreamWriter writer = writerFactory.createXMLStreamWriter(output);
        env.serializeWithCache(writer);
        writer.flush();
        return new String(output.toByteArray());
    }

}
