/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.provider.jaxb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;

import org.test.mtom.ObjectFactory;
import org.test.mtom.SendImage;
import org.test.mtom.SendImageResponse;

/**
 * A JAXWS Source Provider implementation
 *
 */
@WebServiceProvider()
public class JAXBProvider implements Provider<Source> {
    
    /**
     * Required impl method from javax.xml.ws.Provider interface
     * @param obj
     * @return
     */
    public Source invoke(Source obj) {
        System.out.println(">> JAXB Provider Service: Request received.\n");
        SendImage siRequest = null;
        SendImageResponse siResponse = null;
        StreamSource streamSource = null;
        
        try {
        	//Create a request object
            siRequest = new ObjectFactory().createSendImage();
            
            //Unmarshall recieved Source to get request param.
            JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
            Unmarshaller um = jbc.createUnmarshaller();
            siRequest = (SendImage)um.unmarshal(obj);
            
            //Create a response object
            siResponse = new ObjectFactory().createSendImageResponse();
            siResponse.setOutput(siRequest.getInput());
            
            //Marshall the response object and create a StreamSource from the 
            //resulting byte array input stream
            Marshaller m = jbc.createMarshaller();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            m.marshal(siResponse, baos);
            byte []bite = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(bite);
            streamSource = new StreamSource(bais);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return streamSource;

    }
}
