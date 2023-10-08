/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.util;

import jakarta.activation.ActivationDataFlavor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.activation.CommandInfo;
import jakarta.activation.CommandMap;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class acts as a wrapper for the jakarta.activation.DataHandler class.
 * It is used to store away a (potentially) user-defined content-type value along with
 * the DataHandler instance.   We'll delegate all method calls except for getContentType()
 * to the real DataHandler instance.   
 */
public class WrappedDataHandler extends DataHandler {
    
    private static final Log log = LogFactory.getLog(WrappedDataHandler.class);
    
    private final DataHandler parent;
    private final String contentType;
    
    /**
     * Constructs a new instance of the WrappedDataHandler.
     * @param parent the real DataHandler instance being wrapped
     * @param contentType the user-defined contentType associated with the DataHandler instance
     */
    public WrappedDataHandler(DataHandler parent, String contentType) {
        super((DataSource) null);
        
        this.parent = parent;
        this.contentType = contentType;
        
        if (log.isDebugEnabled()) {
            log.debug("Created instance of WrappedDatahandler: " + this.toString() + ", contentType=" + contentType
                + "\nDelegate DataHandler: " + parent.toString());
        }
    }

    @Override
    public String getContentType() {
        return contentType != null ? contentType : parent.getContentType();
    }

    @Override
    public CommandInfo[] getAllCommands() {
        return parent.getAllCommands();
    }

    @Override
    public Object getBean(CommandInfo cmdinfo) {
        return parent.getBean(cmdinfo);
    }

    @Override
    public CommandInfo getCommand(String cmdName) {
        return parent.getCommand(cmdName);
    }

    @Override
    public Object getContent() throws IOException {
        return parent.getContent();
    }

    @Override
    public DataSource getDataSource() {
        return parent.getDataSource();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return parent.getInputStream();
    }

    @Override
    public String getName() {
        return parent.getName();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return parent.getOutputStream();
    }

    @Override
    public CommandInfo[] getPreferredCommands() {
        return parent.getPreferredCommands();
    }

    @Override
    public Object getTransferData(ActivationDataFlavor flavor)
            throws IOException {
        return parent.getTransferData(flavor);
    }

    @Override
    public ActivationDataFlavor[] getTransferDataFlavors() {
        return parent.getTransferDataFlavors();
    }

    @Override
    public boolean isDataFlavorSupported(ActivationDataFlavor flavor) {
        return parent.isDataFlavorSupported(flavor);
    }

    @Override
    public void setCommandMap(CommandMap commandMap) {
        parent.setCommandMap(commandMap);
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        parent.writeTo(os);
    }
}
