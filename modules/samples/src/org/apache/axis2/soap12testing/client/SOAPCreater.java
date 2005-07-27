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

package org.apache.axis2.soap12testing.client;

import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.om.OMXMLParserWrapper;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import java.io.*;

public class SOAPCreater {
    public String getStringFromSOAPMessage(String testNumber) {
         //D:\Projects\LSF\Axis2\Axis1.0\modules\samples\target\Repository
//        File file = new File("D:\\Projects\\LSF\\Axis2\\Axis1.0\\modules\\samples/test-resources\\SOAP12Testing\\RequestMessages\\SOAP12ReqT" + testNumber + ".xml");
        File file = new File("./test-resources\\SOAP12Testing\\RequestMessages\\SOAP12ReqT" + testNumber + ".xml");
        try {
            FileInputStream stream = new FileInputStream(file);
            BufferedInputStream bf = new BufferedInputStream(stream);
            DataInputStream ds = new DataInputStream(bf);
            StringBuffer sb = new StringBuffer();
            String record;
            while ((record = ds.readLine()) != null) {
                sb.append(record.trim());
            }
            System.out.println("record = " + sb.toString());
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public SOAPEnvelope getEnvelopeFromSOAPMessage(String pathAndFileName) {
        File file = new File(pathAndFileName);
        try {
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file));
            OMXMLParserWrapper builder = new StAXSOAPModelBuilder(parser,null);
            return (SOAPEnvelope) builder.getDocumentElement();            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
