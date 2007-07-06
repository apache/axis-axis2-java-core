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

package javax.xml.ws.soap;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.Binding;
import java.util.Set;

public interface SOAPBinding
        extends Binding {

    public abstract Set<java.lang.String> getRoles();

    public abstract void setRoles(Set<java.lang.String> set);

    public abstract boolean isMTOMEnabled();

    public abstract void setMTOMEnabled(boolean flag);

    public abstract SOAPFactory getSOAPFactory();

    public abstract MessageFactory getMessageFactory();

    public static final String SOAP11HTTP_BINDING = "http://schemas.xmlsoap.org/wsdl/soap/http";
    public static final String SOAP12HTTP_BINDING = "http://www.w3.org/2003/05/soap/bindings/HTTP/";
    public static final String SOAP11HTTP_MTOM_BINDING = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true";
    public static final String SOAP12HTTP_MTOM_BINDING = "http://www.w3.org/2003/05/soap/bindings/HTTP/?mtom=true";
}
