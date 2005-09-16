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

package sample.om.binary;

import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class FIUtil {

    public File getBinaryXML(String inFileName) {
        try {
            XMLToFastInfosetSAXSerializer docSerializer = new XMLToFastInfosetSAXSerializer();
            //XML input file, such as ./data/inv100.xml
            File input = new File(inFileName);
            //FastInfoset output file, such as ./data/inv100_sax.finf.
            String finf = getFinfFilename(inFileName);
            File ouput = new File(finf);
            docSerializer.write(input, ouput);
            return ouput;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public XMLStreamReader getEventReaderFromBinaryFile(File binaryFile) {
        InputStream document = null;
        try {
            document = new BufferedInputStream(new FileInputStream(binaryFile));
            return new StAXDocumentParser(document);
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }

    private static String getFinfFilename(String xmlFilename) {
        int ext = xmlFilename.lastIndexOf(".");
        return xmlFilename.substring(0, ext) + ".finf";
    }

    public void testReadBinaryToOMAndSerialize() {
        String inFileName = "resources/om/binary/binary.xml";

//        FIUtil fiUtil = new FIUtil();
//        File binaryXML = fiUtil.getBinaryXML(inFileName);
//        System.out.println("Created binary file " + binaryXML.getName() + " from " + inFileName + "......");


        try {
            System.out.println("********** XML ==> OM **************");
            XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(inFileName));
            StAXOMBuilder omStAXOMBuilder = new StAXOMBuilder(xmlStreamReader);
            omStAXOMBuilder.setDoDebug(true);
            System.out.println("StAXOMBuilder created from " + inFileName + " ........");

            System.out.println("********** OM ==> Binary **************");
            File binaryFile = getBinaryXML(inFileName);
            StAXDocumentSerializer binaryStAXSerializer = new StAXDocumentSerializer(new FileOutputStream(binaryFile));
            omStAXOMBuilder.getDocumentElement().build();
            omStAXOMBuilder.getDocumentElement().serializeWithCache(binaryStAXSerializer);
//            binaryStAXSerializer.flush();
            System.out.println("Created binary file " + binaryFile.getName() + " from OM ......");

            System.out.println("********** Binary ==> OM **************");
            XMLStreamReader eventReaderFromBinaryFile = getEventReaderFromBinaryFile(binaryFile);
            System.out.println("Created XMLStreamReader from the binary file .....");

            StAXOMBuilder builder = new StAXOMBuilder(eventReaderFromBinaryFile);
            System.out.println("StAXOMBuilder created from the given event reader ....");

            OMElement documentElement = builder.getDocumentElement();
            documentElement.build();

            System.out.println("********** OM ==> XML **************");
            OMOutputImpl output = new OMOutputImpl(System.out, false);
            documentElement.serialize(output);
            output.flush();

        } catch (XMLStreamException e) {
            throw new UnsupportedOperationException();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException();
        } catch (IOException e) {
            throw new UnsupportedOperationException();
        } 


    }

    public static void main(String[] args) {
        FIUtil util = new FIUtil();
        util.testReadBinaryToOMAndSerialize();
    }


}

