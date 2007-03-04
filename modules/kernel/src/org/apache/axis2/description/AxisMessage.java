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

package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class represents the messages in WSDL. There can be message element in services.xml
 * which are represented by this class.
 */
public class AxisMessage extends AxisDescription {

    private ArrayList handlerChain;
    private String name;
    private ArrayList soapHeaders;

    //to keep data in WSDL message reference and to keep the Java2WSDL data
    // such as SchemaElementName , direction etc.
    private QName elementQname;
    private String direction;

    // private PolicyInclude policyInclude;


    public AxisMessage() {
        soapHeaders = new ArrayList();
        handlerChain = new ArrayList();
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
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public QName getElementQName() {
        return this.elementQname;
    }

    public void setElementQName(QName element) {
        this.elementQname = element;
    }

    public Object getKey() {
        return this.elementQname;
    }

    public XmlSchemaElement getSchemaElement() {
        AxisService service = (AxisService) getParent().getParent();
        ArrayList schemas = service.getSchema();
        for (int i = 0; i < schemas.size(); i++) {
            XmlSchema schema = (XmlSchema) schemas.get(i);
            if (schema.getItems() != null) {
                Iterator schemaItems = schema.getItems().getIterator();
                while (schemaItems.hasNext()) {
                    Object item = schemaItems.next();
                    if (item instanceof XmlSchemaElement) {
                        XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) item;
                        if (xmlSchemaElement.getQName().equals(this.elementQname)) {
                            return xmlSchemaElement;
                        }
                    }
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

    /**
     * This will return a list of WSDLExtensibilityAttribute
     */
    public List getExtensibilityAttributes() {
        // TODO : Deepal implement this properly.

        // the list should contain list of WSDLExtensibilityAttribute
        return new ArrayList(0);
    }

    public void addSoapHeader(SOAPHeaderMessage soapHeaderMessage) {
        soapHeaders.add(soapHeaderMessage);
    }

    public ArrayList getSoapHeaders
            () {
        return soapHeaders;
    }

    public void engageModule(AxisModule axisModule, AxisConfiguration axisConfig) throws AxisFault {
        throw new UnsupportedOperationException("Sorry we do not support this");
    }

    public boolean isEngaged(QName moduleName) {
        throw new UnsupportedOperationException("axisMessage.isEngaged(qName) is not supported");

    }

}
