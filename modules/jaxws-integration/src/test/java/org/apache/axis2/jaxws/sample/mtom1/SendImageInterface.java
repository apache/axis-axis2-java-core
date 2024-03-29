
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.sample.mtom1;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;
import jakarta.xml.ws.WebServiceException;


/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.0_01-b15-fcs
 * Generated source version: 2.0
 * 
 */
@WebService(name = "sendImageInterface", 
//		targetNamespace="http://mtom1.sample.jaxws.axis2.apache.org")
		    targetNamespace = "urn://mtom1.sample.jaxws.axis2.apache.org")
	//	    wsdlLocation = "META-INF/samplemtomjpeg.wsdl")
public interface SendImageInterface {


    /**
     * 
     * @param input
     * @return
     *     returns org.apache.axis2.jaxws.sample.mtom1.ImageDepot
     */
    @WebMethod(action = "sendImage")
    @WebResult(name = "output", targetNamespace = "urn://mtom1.sample.jaxws.axis2.apache.org")
    @RequestWrapper(localName = "invoke", targetNamespace = "urn://mtom1.sample.jaxws.axis2.apache.org", className = "org.apache.axis2.jaxws.sample.mtom1.Invoke")
    @ResponseWrapper(localName = "sendImageResponse", targetNamespace = "urn://mtom1.sample.jaxws.axis2.apache.org", className = "org.apache.axis2.jaxws.sample.mtom1.SendImageResponse")
    public ImageDepot invoke(
        @WebParam(name = "input", targetNamespace = "urn://mtom1.sample.jaxws.axis2.apache.org")
        ImageDepot input) throws WebServiceException;

}
