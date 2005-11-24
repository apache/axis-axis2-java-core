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
package org.apache.axis2.jaxws.factory;

import java.net.URL;
import org.apache.axis2.jaxws.JAXRPCWSDLInterface;

/**
 * @author sunja07
 *
 */
public class WSDLFactoryImpl {
	
	static final int WSDL4J_PARSER_CHOICE = 0;
	
	static final int WODEN_PARSER_CHOICE = 1;
	
	static final int CUSTOM_PARSER_CHOICE = 2;
	
	static final int DEFAULT_CHOICE = 0;

	/**
	 * 
	 */
	public WSDLFactoryImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Method getParser
	 * Based on the choice indicated, appropriate adapter class that implements
	 * the JAXRPCWSDLInterface leveraging the parsing mechanism functionality
	 * offered by the parser of the indicated choice is instantiated and 
	 * returned.
	 * @param choice
	 * @return
	 */
	public static JAXRPCWSDLInterface getParser(int choice, URL wsdlLocation) {
		//May be in this method you need to see what kind of wsdl version is
		//this wsdl and get the appropriate adapter class instantiated.
		//All that I wanted to hint is there can be different adapter classes
		//for 1.1 and 2.0, as we have JAXRPCWSDL11Interface different from
		//JAXRPCWSDL20Interface. So in such a case single adapter class will
		//not be sufficient, but you will have mulitple adapter classes. And
		//which class to instantiate would be decided by checking the wsdl type
		switch(choice) {
		case 0: 
			//return the wsdl4j adapter class instance
		case 1:
			//return the woden adapter class instance
		case 2:
			//return the custom wsdl parsing adapter class instance
			//and so on
		}
		//For now, no adapter class is coded. So...
		return null;
	}

}
