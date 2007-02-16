/*
* Copyright 2007 The Apache Software Foundation.
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

package org.apache.axis2.mex;

/**
 * Contains all the MetadataExchange constants for WS-Mex.
 * 
 */

public interface MexConstants {
	public interface SPEC_VERSIONS {
		String v1_0 = "Spec_2004_09";
	}
    public interface Spec_2004_09 {
		
		String NS_URI = "http://schemas.xmlsoap.org/ws/2004/09/mex";

	    public interface Actions {
			String GET_METADATA_REQUEST = "http://schemas.xmlsoap.org/ws/2004/09/mex/GetMetadata/Request";
			String GET_METADATA_RESPONSE = "http://schemas.xmlsoap.org/ws/2004/09/mex/GetMetadata/Response";
			
	    }
    }
    
  
    public interface SOAPVersion {
     		int v1_1 = 1;

    		int v1_2 = 2;
    
	}
    
    
    public interface SPEC {
		String NS_PREFIX = "mex";
		String GET_METADATA = "GetMetadata";
		String DIALECT = "Dialect";
		String IDENTIFIER = "Identifier";
		String METADATA = "Metadata";
		String METADATA_SECTION = "MetadataSection";
		String METADATA_REFERENCE = "MetadataReference";
		String LOCATION = "Location";
		String TYPE = "type";
		
		String DIALECT_TYPE_WSDL = "http://schemas.xmlsoap.org/wsdl/";
		String DIALECT_TYPE_POLICY = "http://schemas.xmlsoap.org/ws/2004/09/policy";
		String DIALECT_TYPE_SCHEMA = "http://www.w3.org/2001/XMLSchema";
		String DIALECT_TYPE_MEX = "http://schemas.xmlsoap.org/ws/2004/09/mex";
		
	}
  
}
