/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.axis.samples.utils;

import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMException;
import org.apache.axis.impl.llom.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringReader;


public class OMUtil {

    private String basicSOAPXML = "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"> \n" +
            " <env:Header>\n" +
            " </env:Header>\n" +
            " <env:Body>\n" +
            " </env:Body>\n" +
            "</env:Envelope>";

	public SOAPEnvelope getEmptySoapEnvelop(){
        XMLStreamReader parser = null;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(basicSOAPXML));
        } catch (XMLStreamException e) {
            throw new OMException("Exception in StAX parser", e);
        }
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(OMFactory.newInstance(),parser);
        return builder.getSOAPEnvelope();
	}

}
