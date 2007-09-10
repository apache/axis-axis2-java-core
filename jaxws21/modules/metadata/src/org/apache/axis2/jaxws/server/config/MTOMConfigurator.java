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
package org.apache.axis2.jaxws.server.config;

import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.axis2.jaxws.feature.ServerConfigurator;

/**
 *
 */
public class MTOMConfigurator implements ServerConfigurator {

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.WebServiceFeatureConfigurator#configure(org.apache.axis2.jaxws.description.EndpointDescription)
     */
    public void configure(EndpointDescription endpointDescription) {
    	MTOM mtomAnnoation =
    		(MTOM) ((EndpointDescriptionJava) endpointDescription).getAnnoFeature(MTOMFeature.ID);
    	AxisService service = endpointDescription.getAxisService();
    	
    	//Disable MTOM
    	Parameter enableMTOM = new Parameter("enableMTOM", Boolean.FALSE);

//      TODO NLS enable.
        if (mtomAnnoation == null)
            throw ExceptionFactory.makeWebServiceException("The MTOM annotation was unspecified.");
    	
        //Enable MTOM.
    	if (mtomAnnoation.enabled()) {
        	enableMTOM.setValue(Boolean.TRUE);
    	}
    	
    	try {
    		service.addParameter(enableMTOM);
    	}
    	catch (Exception e) {
            //TODO NLS enable.
            throw ExceptionFactory.makeWebServiceException("Unable to enable MTOM.", e);    		
    	}
    }    
}
