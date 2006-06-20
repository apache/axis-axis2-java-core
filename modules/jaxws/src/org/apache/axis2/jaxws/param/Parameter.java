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



import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;

public interface Parameter {
    
    /**
     * Returns the value object that is being handled by this Parameter
     * instance.
     */
    public Object getValue();
    
    /**
     * Sets a value object data for the Parameter instance.
     */
    public void setValue(Object o);
    
    /**
     * Does the parameter specific processing necessary to wrap the value with an
     * XMLStreamReader, and returns the newly created reader.
     * 
     * @return - An XMLStreamReader that walks the data of the value object.
     */
    public XMLStreamReader getValueAsStreamReader();
    
    /**
     * Returns an OMElement representation of the value object that the Parameter
     * class holds.  If no value has been set, an Exception will be thrown.
     * 
     * @return - An OMElement representation
     */
    public OMElement toOMElement();
    
    /**
     * Converts the OMElement passed in to the appropriate value type and holds the value
     * object data within the Parameter instance.
     */
    public void fromOM(OMElement omElement);
    
    /**
     * TODO: Need to re-think this one.  Parameter should be a standalone thing and not tied
     * to a SOAPEnvelope.
     * 
     * @param mode
     * @param soapVersionURI
     * @return
     */
    public SOAPEnvelope toEnvelope(Mode mode, String soapVersionURI);
    
    /**
     * TODO: Need to re-think this one.  Parameter should be a standalone thing and not tied
     * to a SOAPEnvelope.
     *  
     * @param mode
     * @param env
     */
    public void fromEnvelope(Mode mode, SOAPEnvelope env);
}
