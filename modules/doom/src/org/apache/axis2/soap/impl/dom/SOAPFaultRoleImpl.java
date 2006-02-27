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

package org.apache.axis2.soap.impl.dom;

import org.apache.ws.commons.om.OMXMLParserWrapper;
import org.apache.ws.commons.om.impl.OMOutputImpl;
import org.apache.ws.commons.om.impl.llom.OMSerializerUtil;
import org.apache.ws.commons.om.impl.llom.serialize.StreamWriterToContentHandlerConverter;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPFault;
import org.apache.ws.commons.soap.SOAPProcessingException;

import javax.xml.stream.XMLStreamException;


public abstract class SOAPFaultRoleImpl extends SOAPElement implements org.apache.ws.commons.soap.SOAPFaultRole {

    public SOAPFaultRoleImpl(SOAPFault parent,
                             String localName,
                             boolean extractNamespaceFromParent) throws SOAPProcessingException {
        super(parent,
              localName,
              extractNamespaceFromParent);
    }

    public SOAPFaultRoleImpl(SOAPFault parent, OMXMLParserWrapper builder) {
        super(parent, SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME, builder);
    }

    public void setRoleValue(String uri) {
        if (firstChild != null) {
            firstChild.detach();
        }
        this.setText(uri);
    }

    public String getRoleValue() {
        return this.getText();
    }

    protected void serialize(OMOutputImpl omOutput, boolean cache) throws XMLStreamException {
        // select the builder
        short builderType = PULL_TYPE_BUILDER;    // default is pull type
        if (builder != null) {
            builderType = this.builder.getBuilderType();
        }
        if ((builderType == PUSH_TYPE_BUILDER)
            && (builder.getRegisteredContentHandler() == null)) {
            builder.registerExternalContentHandler(new StreamWriterToContentHandlerConverter(omOutput));
        }

        if (!cache) {
            //No caching
            if (this.firstChild != null) {
                OMSerializerUtil.serializeStartpart(this, omOutput);
                firstChild.serializeAndConsume(omOutput);
                OMSerializerUtil.serializeEndpart(omOutput);
            } else if (!this.done) {
                if (builderType == PULL_TYPE_BUILDER) {
                    OMSerializerUtil.serializeByPullStream(this, omOutput);
                } else {
                    OMSerializerUtil.serializeStartpart(this, omOutput);
                    builder.setCache(cache);
                    builder.next();
                    OMSerializerUtil.serializeEndpart(omOutput);
                }
            } else {
                OMSerializerUtil.serializeNormal(this, omOutput, cache);
            }
            // do not serialise the siblings


        } else {
            //Cached
            OMSerializerUtil.serializeNormal(this, omOutput, cache);

            // do not serialise the siblings
        }
    }
}
