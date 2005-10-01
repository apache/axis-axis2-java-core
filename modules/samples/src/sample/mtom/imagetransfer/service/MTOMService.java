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

package sample.mtom.imagetransfer.service;

import org.apache.axis2.attachments.utils.ImageIO;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMText;

import javax.activation.DataHandler;
import java.awt.*;
import java.io.FileOutputStream;

public class MTOMService {
    public OMElement mtomSample(OMElement element) throws Exception {
    	
        OMElement imageEle = element.getFirstElement();
        OMElement imageName = (OMElement) imageEle.getNextOMSibling();
        OMText binaryNode = (OMText) imageEle.getFirstOMChild();
        String fileName = imageName.getText();
        //Extracting the data and saving
        DataHandler actualDH;
        actualDH = (DataHandler)binaryNode.getDataHandler();
        Image actualObject = new ImageIO().loadImage(actualDH.getDataSource()
                .getInputStream());
        FileOutputStream imageOutStream = new FileOutputStream(fileName);
        new ImageIO().saveImage("image/jpeg", actualObject, imageOutStream);
        //setting response
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("urn://fakenamespace", "ns");
        OMElement ele = fac.createOMElement("response", ns);
        ele.setText("Image Saved");
        return ele;
    }
}