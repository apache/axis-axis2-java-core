package org.apache.axis.wsdl.wsdlgeneration.impl;

import org.w3c.dom.*;

import org.apache.axis.wsdl.wom.WSDLService;
import org.apache.axis.wsdl.wom.*;
import org.apache.axis.wsdl.wom.impl.WSDLProcessingException;
import org.apache.axis.wsdl.wsdlgeneration.WSDLElementConstants;

import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
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

/**
 * author: farhaan@opensource.lk
 */
public class WSDLServicesEmitter implements WSDLElementConstants {
    private HashMap wsdlService;
    private Document doc;

    /**
     * @param wsdlService The Map should contain <code>WSDLService</code>
     * @param doc
     */
    public WSDLServicesEmitter(HashMap wsdlService, Document doc) {
        this.wsdlService = wsdlService;
        this.doc = doc;
    }

    public void populateToDOM() {
//        WSDLDefinitions wsd = new WSDLDefinitionsImpl();    //instantiating the interface
//        HashMap serviceHashmap = wsd.getServices();
//        Collection serviceCol=  serviceHashmap.values();
        Iterator serviceIter = this.wsdlService.values().iterator();

        while (serviceIter.hasNext()) {
            Object obj = serviceIter.next();
            WSDLService wsdlservice;
            if (obj instanceof WSDLService) {

                wsdlservice = (WSDLService) obj;
            } else {
                throw new WSDLProcessingException("Incorrect Service Object");
            }

            String servicenameValue = wsdlservice.getName();
            WSDLInterface serviceinterfaceValue = wsdlservice.getServiceInterface();
            String interfaceValue = serviceinterfaceValue.getName();

            Element serviceElement = doc.createElement("service");
            serviceElement.setAttribute(SERVICE_NAME, servicenameValue);
            serviceElement.setAttribute(SERVICE_INTERFACE, interfaceValue);

            //Add attribute information items if available


            //Add documentation if available

            // wsdlservice.has documentation?


            HashMap endpointhashmap = wsdlservice.getEndpoints();
            Collection endpointcol = endpointhashmap.values();
            Iterator endpointiter = endpointcol.iterator();
            while (endpointiter.hasNext()) {
                WSDLEndpoint wsdlendpoint = (WSDLEndpoint) endpointiter.next();
                String endpointName = wsdlendpoint.getName();
                String endpointBinding = wsdlendpoint.getBinding().getName();
                //String endpointAddress = wsdlendpoint.getAddress();

                Element endpointElement = doc.createElement("endpoint");
                //Add attributes
                endpointElement.setAttribute("name", endpointName);
                endpointElement.setAttribute("binding", endpointBinding);
                //endpointElement.setAttribute("address", endpointAddress);
            serviceElement.appendChild(endpointElement);
            }

            // Add features and properties

            //add the serviceElement to the DOM doc
        }
    }

}



