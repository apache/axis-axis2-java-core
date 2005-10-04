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

package org.apache.axis2.attachments;

import org.apache.axis2.attachments.utils.ImageDataSource;
import org.apache.axis2.attachments.utils.ImageIO;
import org.apache.axis2.om.AbstractTestCase;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.OMElementImpl;
import org.apache.axis2.om.impl.llom.OMNamespaceImpl;
import org.apache.axis2.om.impl.llom.OMTextImpl;
import org.apache.axis2.om.impl.llom.mtom.MTOMStAXSOAPModelBuilder;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ImageSampleTest extends AbstractTestCase {

    public ImageSampleTest(String testName) {
        super(testName);
    }

    /*
     * @see TestCase#setUp()
     */
    Image expectedImage;

    MTOMStAXSOAPModelBuilder builder;

    DataHandler expectedDH;

    File outMTOMFile;

    File outBase64File;

    String outFileName = "target/ActualImageMTOMOut.bin";

    String outBase64FileName = "target/OMSerializeBase64Out.xml";

    String imageInFileName = "mtom/img/test.jpg";

    String imageOutFileName = "target/testOut.jpg";

    String inMimeFileName = "mtom/ImageMTOMOut.bin";

    String contentTypeString = "multipart/Related; type=\"application/xop+xml\";start=\"<SOAPPart>\"; boundary=\"----=_AxIs2_Def_boundary_=42214532\"";



    public void testImageSampleSerialize() throws Exception {

        outMTOMFile = new File(outFileName);
        outBase64File = new File(outBase64FileName);
        org.apache.axis2.om.impl.OMOutputImpl mtomOutput = new OMOutputImpl(new FileOutputStream(outMTOMFile),
                true);
        org.apache.axis2.om.impl.OMOutputImpl baseOutput = new OMOutputImpl(new FileOutputStream(outBase64File),
                false);

        OMNamespaceImpl soap = new OMNamespaceImpl(
                "http://schemas.xmlsoap.org/soap/envelope/", "soap");
        OMElement envelope = new OMElementImpl("Envelope", soap);
        OMElement body = new OMElementImpl("Body", soap);

        OMNamespaceImpl dataName = new OMNamespaceImpl(
                "http://www.example.org/stuff", "m");
        OMElement data = new OMElementImpl("data", dataName);

        expectedImage =
                new ImageIO().loadImage(
                        new FileInputStream(
                                getTestResourceFile(imageInFileName)));
        ImageDataSource dataSource = new ImageDataSource("WaterLilies.jpg",
                expectedImage);
        expectedDH = new DataHandler(dataSource);
        OMText binaryNode = new OMTextImpl(expectedDH, true);

        envelope.addChild(body);
        body.addChild(data);
        data.addChild(binaryNode);

        envelope.serializeAndConsume(baseOutput);
        baseOutput.flush();

        envelope.serializeAndConsume(mtomOutput);
        mtomOutput.flush();
    }

    public void testImageSampleDeserialize() throws Exception {
        InputStream inStream = new FileInputStream(
                getTestResourceFile(inMimeFileName));
        MIMEHelper mimeHelper = new MIMEHelper(inStream, contentTypeString);
        XMLStreamReader reader = XMLInputFactory.newInstance()
                .createXMLStreamReader(
                        new BufferedReader(
                                new InputStreamReader(
                                        mimeHelper
                .getSOAPPartInputStream())));
        builder = new MTOMStAXSOAPModelBuilder(reader, mimeHelper, null);
        OMElement root = builder.getDocumentElement();
        OMElement body = (OMElement) root.getFirstOMChild();
        OMElement data = (OMElement) body.getFirstOMChild();
        OMText blob = (OMText) data.getFirstOMChild();
        /*
         * Following is the procedure the user has to follow to read objects in
         * OBBlob User has to know the object type & whether it is serializable.
         * If it is not he has to use a Custom Defined DataSource to get the
         * Object.
         */

        DataHandler actualDH;
        actualDH = (DataHandler)blob.getDataHandler();
        Image actualObject = new ImageIO().loadImage(actualDH.getDataSource()
                .getInputStream());
        FileOutputStream imageOutStream = new FileOutputStream(
                new File(imageOutFileName));
        new ImageIO().saveImage("image/jpeg", actualObject, imageOutStream);

    }

}