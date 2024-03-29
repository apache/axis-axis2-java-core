
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

package org.apache.axis2.jaxws.sample.wsgen;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
//import jakarta.xml.ws.RequestWrapper;
//import jakarta.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.0_01-b15-fcs
 * Generated source version: 2.0
 * 
 */
@WebService(name = "WSGenInterface", targetNamespace = "http://wsgen.sample.jaxws.axis2.apache.org")
public interface WSGenInterface {


    /**
     * 
     * @param arg0
     * @return
     *     returns java.lang.String
     */
    @WebMethod(action = "urn:EchoString")
    //@WebResult(targetNamespace = "")
    //@RequestWrapper(localName = "echoString", targetNamespace = "http://wsgen.sample.jaxws.axis2.apache.org", className = "org.apache.axis2.jaxws.sample.wsgen.EchoString")
    //@ResponseWrapper(localName = "echoStringResponse", targetNamespace = "http://wsgen.sample.jaxws.axis2.apache.org", className = "org.apache.axis2.jaxws.sample.wsgen.EchoStringResponse")
    public String echoString(
        //@WebParam(name = "arg0", targetNamespace = "")
        String arg0);

}
