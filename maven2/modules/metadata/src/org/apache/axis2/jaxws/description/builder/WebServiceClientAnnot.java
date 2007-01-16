/* Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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

package org.apache.axis2.jaxws.description.builder;

import java.lang.annotation.Annotation;

public class WebServiceClientAnnot implements javax.xml.ws.WebServiceClient{

	private String 	name;
	private String 	targetNamespace;
	private String 	wsdlLocation;			

	
	/**
     * A WebServiceClientAnnot cannot be instantiated.
     */
	private  WebServiceClientAnnot(){
		
	}
	
	private  WebServiceClientAnnot(
			String name,
			String targetNamespace,
			String wsdlLocation)
	{
		this.name = name;
		this.targetNamespace = targetNamespace;
		this.wsdlLocation = wsdlLocation;
	}

    public static WebServiceClientAnnot createWebServiceClientAnnotImpl() {
        return new WebServiceClientAnnot();
    }

    public static WebServiceClientAnnot createWebServiceClientAnnotImpl( 
    			String name,
    			String targetNamespace,
    			String wsdlLocation
    		) 
    {
        return new WebServiceClientAnnot( name, 
        								targetNamespace, 
        								wsdlLocation);
    }
	
	
	/**
	 * @return Returns the name.
	 */
	public String name() {
		return name;
	}
	
	/**
	 * @return Returns the targetNamespace.
	 */
	public String targetNamespace() {
		return targetNamespace;
	}
	
	/**
	 * @return Returns the wsdlLocation.
	 */
	public String wsdlLocation() {
		return wsdlLocation;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param targetNamespace The targetNamespace to set.
	 */
	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	/**
	 * @param wsdlLocation The wsdlLocation to set.
	 */
	public void setWsdlLocation(String wsdlLocation) {
		this.wsdlLocation = wsdlLocation;
	}
	
	//hmm, should we really do this
	public Class<Annotation> annotationType(){
		return Annotation.class;
	}

	/**
	 * Convenience method for unit testing. We will print all of the 
	 * data members here.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		String newLine = "\n";
		sb.append(newLine);
		sb.append("@WebServiceClient.name= " + name);
		sb.append(newLine);
		sb.append("@WebServiceClient.targetNamespace= " + targetNamespace);
		sb.append(newLine);
		sb.append("@WebServiceClient.wsdlLocation= " + wsdlLocation);
		sb.append(newLine);
		return sb.toString();
	}

}
