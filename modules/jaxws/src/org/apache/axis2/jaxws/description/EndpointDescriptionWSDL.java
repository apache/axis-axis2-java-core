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
package org.apache.axis2.jaxws.description;

/**
 * 
 */
public interface EndpointDescriptionWSDL {
    
    /**
     * Is the WSDL definition fully specified for the endpoint (WSDL 1.1 port)
     * represented by this EndpointDescription.  If the WSDL is Partial, that means
     * the Endpoint could not be created with the infomation contained in the WSDL file,
     * and annotations were used.
     * 
     * @return true if the WSDL was fully specified; false if it was partial WSDL
     */
    public boolean isWSDLFullySpecified();

}
