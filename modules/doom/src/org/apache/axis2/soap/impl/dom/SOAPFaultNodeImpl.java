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
import org.apache.ws.commons.om.impl.llom.OMSerializerUtil;
import org.apache.ws.commons.om.impl.serialize.StreamWriterToContentHandlerConverter;
import org.apache.ws.commons.soap.*;

import javax.xml.stream.XMLStreamException;

public abstract class SOAPFaultNodeImpl extends SOAPElement implements SOAPFaultNode {

    public SOAPFaultNodeImpl(SOAPFault parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME, true, factory);
    }

    public SOAPFaultNodeImpl(SOAPFault parent, OMXMLParserWrapper builder,
            SOAPFactory factory) {
        super(parent, SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME, builder,
                factory);
    }

    public void setNodeValue(String uri) {
        this.setText(uri);
    }

    public String getNodeValue() {
        return this.getText();
    }

    protected void serialize(
            org.apache.ws.commons.om.impl.OMOutputImpl omOutput, boolean cache)
            throws XMLStreamException {
            // select the builder
            short builderType = PULL_TYPE_BUILDER;    // default is pull type
            if (builder != null) {
                builderType = this.builder.getBuilderType();
            }
            if ((builderType == PUSH_TYPE_BUILDER)
                    && (builder.getRegisteredContentHandler() == null)) {
                builder.registerExternalContentHandler(new StreamWriterToContentHandlerConverter(
                            omOutput));
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
