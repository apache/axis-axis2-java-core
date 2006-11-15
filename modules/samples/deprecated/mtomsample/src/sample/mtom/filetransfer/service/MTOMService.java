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

package sample.mtom.filetransfer.service;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.activation.DataHandler;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.namespace.QName;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;

public class MTOMService {
    private OperationContext opcts;

    private OMElement folder;
    private OMElement files;
    private OMElement file;
    private String folderName;

    public void setOperationContext(OperationContext oc) throws AxisFault {
        opcts = oc;
    }

    /**
     * @param element The OMElement handed by the Axis2 engine
     * @return An OMElement containing the response of the operation
     * @throws Exception
     */
    public OMElement uploadFileUsingMTOM(OMElement element) throws Exception {

        Iterator itr = element.getChildElements();
        folder = (OMElement) itr.next(); // Get the folderName element

        // Throw AxisFault if destination folder is null
        if (folder == null) throw new AxisFault("Destination Folder is null");

        folderName = folder.getText();

        // Create destination folder hierarchy if it does not exist
        File destFolder = new File(folderName);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        files = (OMElement) itr.next(); // Get the files element
        itr = files.getChildElements(); // Get iterator for file elements


        int i = 1;
        String fileName;

        // loop through each file element
        while (itr.hasNext()) {
            file = (OMElement) itr.next(); // Get next file element

            // Throw AxisFault if the file element is null
            if (file == null) throw new AxisFault("File " + i + " is null");

            OMText binaryNode = (OMText) file.getFirstOMChild();
            binaryNode.setBinary(true);
            DataHandler dataHandler;
            dataHandler = (DataHandler) binaryNode.getDataHandler(); // Get corresponding DataHandler
            fileName = createFileName(i);
            writeData(dataHandler.getDataSource().getInputStream(), fileName);
            i++;
        }

        // Create response element
        OMFactory fac = OMAbstractFactory.getOMFactory(); // Get OMFactory
        OMNamespace ns = fac.createOMNamespace("urn://fakenamespace", "ns"); // Create a namespace
        OMElement ele = fac.createOMElement("response", ns); // Create response element
        ele.setText("" + (i - 1) + " File(s) Saved Successfully on Server at " + folderName);
        return ele;
    }


    /**
     * @param element The OMElement handed by the Axis2 engine
     * @return An OMElement containing the response of the operation
     * @throws Exception
     */
    public OMElement uploadFileUsingSwA(OMElement element) throws Exception {

        Iterator itr = element.getChildElements();

        folder = (OMElement) itr.next(); // Get the folderName element

        // Throw AxisFault if destination folder is null
        if (folder == null) throw new AxisFault("Destination Folder is null");

        folderName = folder.getText();

        // Create destination folder hierarchy if it does not exist
        File destFolder = new File(folderName);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        files = (OMElement) itr.next(); // Get the files element
        itr = files.getChildElements(); // Get iterator for file elements

        // Get attachements from the MessageContext
        Attachments attachment = (opcts.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE)).getAttachmentMap();

        // loop through each file element
        int i = 1;
        String fileName;
        DataHandler dataHandler;
        while (itr.hasNext()) {
            file = (OMElement) itr.next();
            if (file == null) throw new AxisFault("File " + i + " is null");
            dataHandler = attachment.getDataHandler(file.getText()); // Get corresponding DataHandler
            fileName = createFileName(i);
            writeData(dataHandler.getDataSource().getInputStream(), fileName);
            i++;
        }

        // Create response element
        OMFactory fac = OMAbstractFactory.getOMFactory(); // Get OMFactory
        OMNamespace ns = fac.createOMNamespace("urn://fakenamespace", "ns"); // Create a namespace
        OMElement ele = fac.createOMElement("response", ns); // Create response element
        ele.setText("" + (i - 1) + " File(s) Saved Successfully on Server at " + folderName);
        return ele;

    }

    public OMElement sendReceiveUsingMTOM(OMElement element) {
        element.buildWithAttachments();
        element.detach();
        return element;
    }

    public OMElement sendReceiveUsingSwA(OMElement element) throws Exception {
        Attachments attachment = (opcts.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE)).getAttachmentMap();
        opcts.getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE).setAttachmentMap(attachment);
        element.buildWithAttachments();
        element.detach();
        return element;
    }


    private String createFileName(int count) {
        String fileName;
        fileName = folderName + "/" + file.getAttributeValue(
                new QName(file.getNamespace().getNamespaceURI(), "type"));

        if (new File(fileName).exists()) {
            fileName = folderName + "/copy(" + (count - 1) + ")" + file.getAttributeValue(
                    new QName(file.getNamespace().getNamespaceURI(), "type"));
            while (new File(fileName).exists()) {
                count ++;
                fileName = folderName + "/copy(" + (count - 1) + ")" + file.getAttributeValue(
                        new QName(file.getNamespace().getNamespaceURI(), "type"));
            }
        }

        return fileName;
    }

    /**
     * @param inStrm   An input stream linking to the data
     * @param fileName The absolute path of the file to which the data should be written
     * @throws Exception
     */
    private void writeData(InputStream inStrm, String fileName) throws Exception {
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        int b;
        while (true) {
            b = inStrm.read();
            if (b == -1) {
                break;
            }
            raf.writeByte(b);
        }
        inStrm.close();
        raf.close();
    }
}