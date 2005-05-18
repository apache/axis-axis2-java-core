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
package org.apache.axis.om.impl.llom.mtom;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.CommandInfo;
import javax.activation.CommandMap;
import javax.activation.DataContentHandlerFactory;

import org.apache.axis.encoding.Base64;
import org.apache.axis.om.DataHandler;
import org.apache.axis.om.DataSource;

public class DataHandlerImpl implements DataHandler {
    private javax.activation.DataHandler datahandler;
    public DataHandlerImpl(javax.activation.DataHandler datahandler){
        this.datahandler =datahandler;
    }

    public void init(String value, String mimeType) {

        ByteArrayDataSource dataSource;
        byte[] data = Base64.decode(value);
        if (mimeType != null) {
            dataSource = new ByteArrayDataSource(data, mimeType);
        } else {
            // Assumes type as application/octet-stream
            dataSource = new ByteArrayDataSource(data);
        }
        datahandler = new javax.activation.DataHandler(dataSource);
    }
    /**
     * @param arg0
     */
    public synchronized static void setDataContentHandlerFactory(DataContentHandlerFactory arg0) {
        javax.activation.DataHandler.setDataContentHandlerFactory(arg0);
    }

    /**
     * @return
     */
    public CommandInfo[] getAllCommands() {
        return datahandler.getAllCommands();
    }

    /**
     * @param arg0
     * @return
     */
    public Object getBean(CommandInfo arg0) {
        return datahandler.getBean(arg0);
    }

    /**
     * @param arg0
     * @return
     */
    public CommandInfo getCommand(String arg0) {
        return datahandler.getCommand(arg0);
    }

    /**
     * @return
     * @throws java.io.IOException
     */
    public Object getContent() throws IOException {
        return datahandler.getContent();
    }

    /**
     * @return
     */
    public String getContentType() {
        return datahandler.getContentType();
    }

    /**
     * @return
     */
    public DataSource getDataSource() {
        return new DataSourceImpl(datahandler.getDataSource());
    }

    /**
     * @return
     * @throws java.io.IOException
     */
    public InputStream getInputStream() throws IOException {
        return datahandler.getInputStream();
    }

    /**
     * @return
     */
    public String getName() {
        return datahandler.getName();
    }

    /**
     * @return
     * @throws java.io.IOException
     */
    public OutputStream getOutputStream() throws IOException {
        return datahandler.getOutputStream();
    }

    /**
     * @return
     */
    public CommandInfo[] getPreferredCommands() {
        return datahandler.getPreferredCommands();
    }

    /**
     * @param flavor
     * @return
     * @throws java.awt.datatransfer.UnsupportedFlavorException
     * @throws java.io.IOException
     */
    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
        return datahandler.getTransferData(flavor);
    }

    /**
     * @return
     */
    public DataFlavor[] getTransferDataFlavors() {
        return datahandler.getTransferDataFlavors();
    }

    /**
     * @param flavor
     * @return
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return datahandler.isDataFlavorSupported(flavor);
    }

    /**
     * @param arg0
     */
    public synchronized void setCommandMap(CommandMap arg0) {
        datahandler.setCommandMap(arg0);
    }

    /**
     * @param arg0
     * @throws java.io.IOException
     */
    public void writeTo(OutputStream arg0) throws IOException {
        datahandler.writeTo(arg0);
    }

}
