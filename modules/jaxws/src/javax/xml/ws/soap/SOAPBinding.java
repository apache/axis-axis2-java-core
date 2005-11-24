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
 */

package javax.xml.ws.soap;

import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;

/**
 * Interface SOAPBinding
 * The javax.xml.rpc.SOAPBinding interface is an abstraction for the JAX-RPC 
 * SOAP binding.
 *
 * @version 1.0
 * @author sunja07
 */
public interface SOAPBinding extends Binding {

    /**
     * A constant representing the identity of the SOAP 1.1 over HTTP binding.
     */
    static final java.lang.String SOAP11HTTP_BINDING = 
    	"http://schemas.xmlsoap.org/wsdl/soap/http";

    /**
     * A constant representing the identity of the SOAP 1.2 over HTTP binding.
     */
    static final java.lang.String SOAP12HTTP_BINDING = 
    	"http://www.w3.org/2003/05/soap/bindings/HTTP/";

    /**
     * Method getRoles
     * Gets the roles played by the SOAP binding instance.
     *
     * @return Set the set of roles played by the binding instance.
     */
    java.util.Set<java.net.URI> getRoles();

    /**
     * Method setRoles
     * Sets the roles played by the SOAP binding instance.
     *
     * @param roles - The set of roles played by the binding instance.
     * @throws WebServiceException - On an error in the configuration of the 
     * list of roles.
     */
    void setRoles(java.util.Set<java.net.URI> roles);
    
    /**
     * Returns true if the use of MTOM is enabled.
     * <p>
     * @return true if and only if the use of MTOM is enabled.
     */
    boolean isMTOMEnabled();
    
    /**
     * Enables or disables use of MTOM.
     * <p>
     * @param flag - A boolean specifying whether the use of MTOM should be enabled or disabled.
     * @throws WebServiceException - If the specified setting is not supported by this binding instance.
     */
    void setMTOMEnabled(boolean flag) throws WebServiceException;
    
    /**
     * Gets the SAAJ SOAPFactory instance used by this SOAP binding.
     * <p>
     * @return SOAPFactory instance used by this SOAP binding.
     */
    javax.xml.soap.SOAPFactory getSOAPFactory();
    
    /**
     * Gets the SAAJ MessageFactory instance used by this SOAP binding.
     * <p>
     * @return MessageFactory instance used by this SOAP binding.
     */
    javax.xml.soap.MessageFactory getMessageFactory();

}
