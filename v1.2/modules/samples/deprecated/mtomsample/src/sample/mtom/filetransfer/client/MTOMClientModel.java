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

package sample.mtom.filetransfer.client;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.wsdl.WSDLConstants;


public class MTOMClientModel {
    private ArrayList fileList = null;

    private String cacheFolder;

    private int cacheThreshold;

    private EndpointReference targetEPR = new EndpointReference("http://localhost:8080/axis2/services/mtomSample");

    public MTOMClientModel() {

    }

    /**
     * @param cacheThreshold Threshold value in bytes
     */
    public void setCacheThreshold(int cacheThreshold) {
        this.cacheThreshold = cacheThreshold;
    }

    /**
     * @param cacheFolder Absolute path of the cache folder
     */
    public void setCacheFolder(String cacheFolder) {
        this.cacheFolder = cacheFolder;
    }

    /**
     * @param folderName Absolute path of the destination folder
     * @param operation  Name of the appropriate operation on the server
     * @return The payload
     * @throws Exception
     */
    private OMElement buildPayloadForMTOM(String folderName, String operation) throws Exception {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");

        OMElement data = fac.createOMElement(operation, omNs);
        OMElement folder = fac.createOMElement("folderName", omNs);
        if (folderName != null) {
            folder.setText(folderName);
        }
        data.addChild(folder);

        OMElement files = fac.createOMElement("files", omNs);
        data.addChild(files);

        DataHandler dataHandler;

        for (int i = 0; i < fileList.size(); i++) {
            OMElement file = fac.createOMElement("file" + (i + 1), omNs);
            file.addAttribute(fac.createOMAttribute("type", omNs, ((File)fileList.get(i)).getName()));
            FileDataSource dataSource = new FileDataSource((File)fileList.get(i));
            dataHandler = new DataHandler(dataSource);
            OMText textData = fac.createOMText(dataHandler, true);
            file.addChild(textData);
            files.addChild(file);
        }
        return data;

    }

    /**
     * @param folderName Absolute path of the destination folder
     * @param operation  Name of the appropriate operation on the server
     * @return The message context
     * @throws Exception
     */
    private MessageContext createMessageContextForSwA(String folderName, String operation) throws Exception {

        MessageContext mc = new MessageContext();
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope soapEnvelope = fac.createSOAPEnvelope();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement data = fac.createOMElement(operation, omNs);
        OMElement folder = fac.createOMElement("folderName", omNs);
        if (folderName != null) {
            folder.setText(folderName);
        }
        data.addChild(folder);
        OMElement files = fac.createOMElement("files", omNs);
        data.addChild(files);

        DataHandler dataHandler;
        for (int i = 0; i < fileList.size(); i++) {
            OMElement file = fac.createOMElement("file" + (i + 1), omNs);
            file.addAttribute(fac.createOMAttribute("type", omNs,((File)fileList.get(i)).getName()));
            FileDataSource dataSource = new FileDataSource((File)fileList.get(i));
            dataHandler = new DataHandler(dataSource);
            String contentID = mc.addAttachment(dataHandler);

            file.setText(contentID);
            files.addChild(file);
        }
        SOAPBody body = fac.createSOAPBody(soapEnvelope);
        body.addChild(data);
        mc.setEnvelope(soapEnvelope);
        return mc;

    }

    /**
     * @param folderName Absolute path of the destination folder
     * @return The response from the server
     * @throws Exception
     */
    public OMElement sendFilesUsingSwA(String folderName) throws Exception {

        Options options = new Options();
        options.setTo(targetEPR);
        options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        ServiceClient sender = new ServiceClient(null, null);
        sender.setOptions(options);
        OperationClient mepClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);

        MessageContext mc = createMessageContextForSwA(folderName, "uploadFileUsingSwA");
        mepClient.addMessageContext(mc);
        mepClient.execute(true);
        MessageContext response = mepClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        return (OMElement) (response.getEnvelope().getBody().getChildren().next());
    }

    /**
     * @param folderName Absolute path of the destination folder
     * @return The response from the server
     * @throws Exception
     */
    public OMElement sendFilesUsingMTOM(String folderName) throws Exception {

        OMElement payload = buildPayloadForMTOM(folderName, "uploadFileUsingMTOM");
        Options options = new Options();
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setTo(targetEPR);
        // enabling MTOM in the client side
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ServiceClient sender = new ServiceClient();
        sender.setOptions(options);
        return sender.sendReceive(payload);
    }

    public OMElement sendReceiveUsingMTOM(String folderName, boolean cacheEnable) throws Exception {
        OMElement payload = buildPayloadForMTOM(folderName, "sendReceiveUsingMTOM");
        Options options = new Options();
        options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setTo(targetEPR);

        // enabling MTOM in the client side
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        // enabling file caching in the client side
        if (cacheEnable) {
            options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
                    Constants.VALUE_TRUE);
            options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR, cacheFolder);
            options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, ("" + cacheThreshold));
        }
        ServiceClient sender = new ServiceClient();
        sender.setOptions(options);
        OMElement response = sender.sendReceive(payload);
        response.buildWithAttachments();
        return handleMTOMResponse(response);
    }

    public OMElement sendReceiveUsingSwA(String folderName, boolean cacheEnable) throws Exception {
        Options options = new Options();
        options.setTo(targetEPR);
        options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        // enabling file caching in the client side
        if (cacheEnable) {
            options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
                    Constants.VALUE_TRUE);
            options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR, cacheFolder);
            options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, ("" + cacheThreshold));
        }

        ServiceClient sender = new ServiceClient(null, null);
        sender.setOptions(options);
        OperationClient mepClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);

        MessageContext mc = createMessageContextForSwA(folderName, "sendReceiveUsingSwA");
        mepClient.addMessageContext(mc);
        mepClient.execute(true);
        MessageContext response = mepClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        return handleSwAResponse(response);

    }


    private OMElement handleMTOMResponse(OMElement element) throws Exception {
        OMElement folder;
        OMElement files;
        OMElement file;
        String folderName;

        Iterator itr = element.getChildElements();
        folder = (OMElement) itr.next();
        if (folder == null) throw new AxisFault("Destination Folder is null");
        folderName = folder.getText();
        File destFolder = new File(folderName);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        files = (OMElement) itr.next();
        itr = files.getChildElements();

        int i = 1;
        String fileName = null;
        while (itr.hasNext()) {
            file = (OMElement) itr.next();
            if (file == null) throw new AxisFault("File " + i + " is null");
            OMText binaryNode = (OMText) file.getFirstOMChild();
            DataHandler dataHandler;
            dataHandler = (DataHandler) binaryNode.getDataHandler();
            fileName = createFileName(folderName, file, i);
            writeData(dataHandler.getDataSource().getInputStream(), fileName);
            i++;
        }
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("urn://fakenamespace", "ns");
        OMElement ele = fac.createOMElement("handledResponse", ns);
        ele.setText("" + (i - 1) + " Files(s) Saved Successfully on Client at " + folderName);
        return ele;
    }

    private OMElement handleSwAResponse(MessageContext mc) throws Exception {
        String folderName;
        OMElement folder;
        OMElement files;
        OMElement file;

        OMElement element = (OMElement) (mc.getEnvelope().getBody().getChildren().next());

        Iterator itr = element.getChildElements();
        folder = (OMElement) itr.next();
        if (folder == null) throw new AxisFault("Destination Folder is null");
        folderName = folder.getText();
        File destFolder = new File(folderName);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        files = (OMElement) itr.next();
        itr = files.getChildElements();

        Attachments attachment = mc.getAttachmentMap();

        int i = 1;
        String fileName;
        DataHandler dataHandler;
        while (itr.hasNext()) {
            file = (OMElement) itr.next();
            if (file == null) throw new AxisFault("File " + i + " is null");
            dataHandler = attachment.getDataHandler(file.getText());
            fileName = createFileName(folderName, file, i);
            writeData(dataHandler.getDataSource().getInputStream(), fileName);
            i++;
        }
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("urn://fakenamespace", "ns");
        OMElement ele = fac.createOMElement("response", ns);
        ele.setText("" + (i - 1) + " File(s) Saved Successfully on Client at " + folderName);
        return ele;

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

    private String createFileName(String folderName, OMElement file, int count) {
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


    public void setTargetEPR(String targetEPR) {
        this.targetEPR = new EndpointReference(targetEPR);

    }


    public void setFileList(ArrayList fileList) {
        this.fileList = fileList;
    }
}
