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
package javax.xml.ws;

import java.util.Map;

public interface BindingProvider {

    public abstract Map<java.lang.String, java.lang.Object>      getRequestContext();

    public abstract Map<java.lang.String, java.lang.Object>      getResponseContext();

    public abstract Binding getBinding();

    public static final String USERNAME_PROPERTY = "javax.xml.ws.security.auth.username";
    public static final String PASSWORD_PROPERTY = "javax.xml.ws.security.auth.password";
    public static final String ENDPOINT_ADDRESS_PROPERTY = "javax.xml.ws.service.endpoint.address";
    public static final String SESSION_MAINTAIN_PROPERTY = "javax.xml.ws.session.maintain";
    public static final String SOAPACTION_USE_PROPERTY = "javax.xml.ws.soap.http.soapaction.use";
    public static final String SOAPACTION_URI_PROPERTY = "javax.xml.ws.soap.http.soapaction.uri";
}
