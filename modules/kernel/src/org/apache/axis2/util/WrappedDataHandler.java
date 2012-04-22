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

import javax.activation.DataHandler;

import org.apache.axiom.util.activation.DataHandlerWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class acts as a wrapper for the javax.activation.DataHandler class.
 * It is used to store away a (potentially) user-defined content-type value along with
 * the DataHandler instance.   We'll delegate all method calls except for getContentType()
 * to the real DataHandler instance.   
 */
public class WrappedDataHandler extends DataHandlerWrapper {
    
    private static final Log log = LogFactory.getLog(WrappedDataHandler.class);
    
    private final String contentType;
    
    /**
     * Constructs a new instance of the WrappedDataHandler.
     * @param _delegate the real DataHandler instance being wrapped
     * @param _contentType the user-defined contentType associated with the DataHandler instance
     */
    public WrappedDataHandler(DataHandler _delegate, String _contentType) {
        super(_delegate);
        
        contentType = _contentType;
        
        if (log.isDebugEnabled()) {
            log.debug("Created instance of WrappedDatahandler: " + this.toString() + ", contentType=" + contentType
                + "\nDelegate DataHandler: " + _delegate.toString());
        }
    }

    @Override
    public String getContentType() {
        return (contentType != null ? contentType : super.getContentType());
    }
}
