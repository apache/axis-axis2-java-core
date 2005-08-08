/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package sample.mtom.imagetransfer.service;

import org.apache.axis2.attachments.utils.JDK13IO;
import org.apache.axis2.om.*;

import javax.activation.DataHandler;
import java.awt.*;
import java.io.FileOutputStream;

/**
 * @author <a href="mailto:thilina@opensource.lk">Thilina Gunarathne </a>
 */
public class MTOMService {
    public OMElement mtomSample(OMElement element) throws Exception {
    	
        OMElement imageEle = element.getFirstElement();
        OMElement imageName = (OMElement) imageEle.getNextSibling();
        OMText binaryNode = (OMText) imageEle.getFirstChild();
        String fileName = imageName.getText();
        //Extracting the data and saving
        DataHandler actualDH;
        actualDH = binaryNode.getDataHandler();
        Image actualObject = new JDK13IO().loadImage(actualDH.getDataSource()
                .getInputStream());
        FileOutputStream imageOutStream = new FileOutputStream(fileName);
        new JDK13IO().saveImage("image/jpeg", actualObject, imageOutStream);
        //setting response
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("urn://fakenamespace", "ns");
        OMElement ele = fac.createOMElement("response", ns);
        ele.setText("Image Saved");
        return ele;
    }
}