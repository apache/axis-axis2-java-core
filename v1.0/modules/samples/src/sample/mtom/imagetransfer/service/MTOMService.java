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

import org.apache.axiom.attachments.utils.ImageIO;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axis2.AxisFault;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.awt.*;
import java.io.FileOutputStream;
import java.util.Iterator;

public class MTOMService {

    public OMElement mtomSample(OMElement element) throws Exception {

        OMElement _fileNameEle = null;
        OMElement _imageElement = null;

        for (Iterator _iterator = element.getChildElements(); _iterator.hasNext();) {
             OMElement _ele = (OMElement) _iterator.next();
            if (_ele.getLocalName().equalsIgnoreCase("fileName")) {
                  _fileNameEle = _ele;
            }
            if (_ele.getLocalName().equalsIgnoreCase("image")) {
                  _imageElement = _ele;
            }
        }

        if (_fileNameEle == null || _imageElement == null ) {
            throw new AxisFault("Either Image or FileName is null");
        }

        OMText binaryNode = (OMText) _imageElement.getFirstOMChild();

        String fileName = _fileNameEle.getText();

        //Extracting the data and saving
        DataHandler actualDH;
        actualDH = (DataHandler) binaryNode.getDataHandler();
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