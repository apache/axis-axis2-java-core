/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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

package org.apache.axis2.jaxws.param;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;

public abstract class SourceParameter implements Parameter {

    protected Source value;
    
    public Object getValue() {
        return value;
    }

    public void setValue(Object o) {
        Class c = o.getClass();
        if (!Source.class.isAssignableFrom(c)) {
            throw new WebServiceException("Unsupported Source parameter type: " + c.getName());
        }
        
        value = (Source) o;        
    }
    
    public abstract void fromOM(OMElement omElement);
    
    public OMElement toOMElement() {
        try {
            XMLStreamReader parser = XMLInputFactory.newInstance()
                    .createXMLStreamReader(value);

            StAXOMBuilder builder = new StAXOMBuilder(OMAbstractFactory
                    .getOMFactory(), parser);

            return builder.getDocumentElement();
        } catch (Exception e) {
            throw new WebServiceException(e);
        }       
    }
    
    public XMLStreamReader getValueAsStreamReader() {
        if (value != null) {
            try {
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(value);
                return reader;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            throw new WebServiceException("Cannot create XMLStreamReader from null value");
        }
        
        return null;
    }

    public void fromEnvelope(Mode mode, SOAPEnvelope env) {
        ParameterUtils.fromEnvelope(mode, env, this);
    }

    public SOAPEnvelope toEnvelope(Mode mode, String soapVersionURI) {
        return ParameterUtils.toEnvelope(mode, soapVersionURI, this);
    }
}
