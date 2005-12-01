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

package org.apache.axis2.soap.impl.llom;

import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.util.ElementHelper;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.OMNodeEx;
import org.apache.axis2.om.impl.llom.OMSerializerUtil;
import org.apache.axis2.om.impl.llom.serialize.StreamWriterToContentHandlerConverter;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.soap.SOAPFaultCode;
import org.apache.axis2.soap.SOAPFaultSubCode;
import org.apache.axis2.soap.SOAPFaultValue;
import org.apache.axis2.soap.SOAPProcessingException;

import javax.xml.stream.XMLStreamException;

public abstract class SOAPFaultCodeImpl extends SOAPElement implements SOAPFaultCode {


    /**
     * Constructor OMElementImpl
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     */
    public SOAPFaultCodeImpl(SOAPFault parent, OMXMLParserWrapper builder) {
        super(parent, SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, builder);
    }

    /**
     * @param parent
     * @param parent
     */
    public SOAPFaultCodeImpl(SOAPFault parent,
                             boolean extractNamespaceFromParent) throws SOAPProcessingException {
        super(parent,
                SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME,
                extractNamespaceFromParent);
    }

    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */
    public void setValue(SOAPFaultValue value) throws SOAPProcessingException {
        ElementHelper.setNewElement(this, value, value);
    }

    public SOAPFaultValue getValue() {
        return (SOAPFaultValue) ElementHelper.getChildWithName(this,
                SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME);
    }

    public void setSubCode(SOAPFaultSubCode value) throws SOAPProcessingException {
        ElementHelper.setNewElement(this, getSubCode(), value);
    }

    public SOAPFaultSubCode getSubCode() {
        return (SOAPFaultSubCode) ElementHelper.getChildWithName(this,
                SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME);
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
                ((OMNodeEx)firstChild).serializeAndConsume(omOutput);
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
