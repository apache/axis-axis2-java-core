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
package org.apache.axis2.jaxws;

import javax.wsdl.Service;
import javax.xml.namespace.QName;
import java.net.URL;

public interface JAXRPCWSDLInterface {
	
	static final String WSDL2_VERSION = "2.0";
	
	static final String WSDL11_VERSION = "1.1";
	
	public String getWSDLVersion();

	public Service getService(URL wsdlLocation, QName serviceName);
}
