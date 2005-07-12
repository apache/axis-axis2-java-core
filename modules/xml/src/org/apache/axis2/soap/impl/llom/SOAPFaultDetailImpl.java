package org.apache.axis2.soap.impl.llom;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.OMOutput;
import org.apache.axis2.om.impl.llom.serialize.StreamWriterToContentHandlerConverter;
import org.apache.axis2.om.impl.llom.OMSerializerUtil;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.soap.SOAPFaultDetail;
import org.apache.axis2.soap.impl.llom.soap12.SOAP12Constants;

import javax.xml.stream.XMLStreamException;
import java.util.Iterator;

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
 *
 * author : Eran Chinthaka (chinthaka@apache.org)
 */

public abstract class SOAPFaultDetailImpl extends SOAPElement implements SOAPFaultDetail {

    protected SOAPFaultDetailImpl(SOAPFault parent,
                                  boolean extractNamespaceFromParent) throws SOAPProcessingException {
        super(parent,
                SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME,
                extractNamespaceFromParent);
    }

    protected SOAPFaultDetailImpl(SOAPFault parent,
                                  OMXMLParserWrapper builder) {
        super(parent, SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME, builder);
    }

    public void addDetailEntry(OMElement detailElement) {
        this.addChild(detailElement);
    }

    public Iterator getAllDetailEntries() {
        return this.getChildren();
    }

    protected void serialize(OMOutput omOutput, boolean cache) throws XMLStreamException {
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
                firstChild.serialize(omOutput);
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
