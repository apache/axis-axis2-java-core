package org.apache.axis2.description;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.impl.MessageReferenceImpl;

import javax.xml.namespace.QName;
import java.util.ArrayList;
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
*/

/**
 * This class represents the messages in WSDL. There can be message element in services.xml
 * which are representd by this class.
 */
public class AxisMessage extends AxisDescription {
    private ArrayList handlerChain;
    private String name;

    //to keep data in WSDL message refference and to keep the Java2WSDL data
    // such as SchemaElementName , direction etc.
    private MessageReference messageReference;

    // private PolicyInclude policyInclude;


    public AxisMessage() {
        handlerChain = new ArrayList();
        messageReference = new MessageReferenceImpl();
    }

    public ArrayList getMessageFlow() {
        return handlerChain;
    }

    public boolean isParameterLocked(String parameterName) {

        // checking the locked value of parent
        boolean loscked = false;

        if (getParent() != null) {
            loscked = getParent().isParameterLocked(parameterName);
        }

        if (loscked) {
            return true;
        } else {
            Parameter parameter = getParameter(parameterName);

            return (parameter != null) && parameter.isLocked();
        }
    }

    public void setMessageFlow(ArrayList operationFlow) {
        this.handlerChain = operationFlow;
    }

    public String getDirection() {
        return messageReference.getDirection();
    }

    public void setDirection(String direction) {
        messageReference.setDirection(direction);
    }

    public QName getElementQName() {
        return messageReference.getElementQName();
    }

    public void setElementQName(QName element) {
        messageReference.setElementQName(element);
    }

    public Object getKey() {
        return getElementQName();
    }

    public XmlSchemaElement getSchemaElement() {
        AxisService service = (AxisService) getParent().getParent();
        ArrayList schemas = service.getSchema();
        for (int i = 0; i < schemas.size(); i++) {
            XmlSchema schema = (XmlSchema) schemas.get(i);
            Iterator scheamItms = schema.getItems().getIterator();
            while (scheamItms.hasNext()) {
                XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) scheamItms.next();
                if (xmlSchemaElement.getName().equals(getElementQName().getLocalPart())) {
                    return xmlSchemaElement;
                }
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
