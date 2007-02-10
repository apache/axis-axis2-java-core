/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.marshaller.impl.alt;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * Characteristics of the "Element" value.
 *    * The Element value is ready for marshalling or is the result of unmarshalling.
 *    * The Element value represents the element rendering.  Thus it is either
 *      a JAXBElement or has the @XmlRootElement annotation.  (i.e. it is never a 
 *      java.lang.String)
 *    * The Element value is not a JAX-WS object. (i.e. it is not a holder or exception)
 * Characteristis of the "Type" value
 *    * It is the type value associated with the element value.  (Thus it is either
 *      the element value or it is value of the JAXBElement
 *    * The type value is usually the object needed for the method signature (i.e. String)
 */
public class Element {

    private QName qName;
    private Object elementValue;
    private Object typeValue;
    private Class typeClass;
    
    /**
     * Create Element from an
     * @param elementValue must be JAXBElement or @XmlRootElement rendered
     * @param qName associated QName
     */
    public Element(Object elementValue, QName qName) {
        if (elementValue != null) {
            this.qName = qName;
            this.elementValue = elementValue;
        } else {
            this.qName = qName;
            this.typeValue = null;
            this.typeClass = Object.class;
        }
    }
    
    /**
     * @param typeValue must not be a JAXBElement.  Must not have @XmlRootElement rendering.
     *        typeValue must not be a Holder or other JAXWS api value.
     * @param qName associated QName
     * @param cls
     */
    public Element(Object typeValue, QName qName, Class cls) {
        this.qName = qName;
        this.typeValue = typeValue;
        this.typeClass = cls;
        
    }
    
    public Object getElementValue() {
        if (elementValue == null) {
            // Create ElementValue from type information
            elementValue= new JAXBElement(qName, typeClass, typeValue);
        }
        return elementValue;
    }
    
    public Object getTypeValue() {
        if (elementValue != null) {
            if (elementValue.getClass() == JAXBElement.class) {
                return ((JAXBElement) elementValue).getValue();
            } else {
                return elementValue;
            }
        } else {
            return typeValue;
        }
    }

    public QName getQName() {
        return qName;
    }
}
