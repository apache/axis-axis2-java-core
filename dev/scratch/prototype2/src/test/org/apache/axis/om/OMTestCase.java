package org.apache.axis.om;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.impl.llom.builder.StAXSOAPModelBuilder;

import javax.xml.stream.*;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;


/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Nov 2, 2004
 * Time: 2:15:28 PM
 */
public abstract class OMTestCase extends AbstractTestCase {

    protected static final String IN_FILE_NAME = "soap/soapmessage.xml";
    protected  StAXSOAPModelBuilder builder;
    protected OMFactory ombuilderFactory;

    protected SOAPEnvelope soapEnvelope;

    public OMTestCase(String testName) {
        super(testName);
        ombuilderFactory = OMFactory.newInstance();
    }


    protected void setUp() throws Exception {
        super.setUp();
        soapEnvelope = (SOAPEnvelope)getOMBuilder("").getDocumentElement();
    }

    protected StAXSOAPModelBuilder getOMBuilder(String fileName) throws Exception {
        if(fileName == "" || fileName == null){
            fileName = IN_FILE_NAME;
        }
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(getTestResourceFile(fileName)));
        builder = new StAXSOAPModelBuilder(OMFactory.newInstance(),parser);
        return builder;
    }

    protected StAXSOAPModelBuilder getOMBuilder(InputStream in) throws Exception {

        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(in);
        builder = new StAXSOAPModelBuilder(OMFactory.newInstance(),parser);
        return builder;
    }

    protected XMLStreamWriter getStAXStreamWriter(OutputStream out) throws XMLStreamException {
        return XMLOutputFactory.newInstance().createXMLStreamWriter(out);
    }


}
