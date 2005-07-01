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
package org.apache.axis2.saaj;

import org.apache.axis2.om.OMElement;

import javax.xml.soap.SOAPBodyElement;

/**
 * Class SOAPBodeElementImpl
 * 
 * @author Ashutosh Shahi
 * ashutosh.shahi@gmail.com
 */
public class SOAPBodyElementImpl extends SOAPElementImpl implements
		SOAPBodyElement {
	
	/**
	 * Constructor SOAPBodeElementImpl
	 *
	 */
	public SOAPBodyElementImpl(){
		super();
	}
	
	/**
	 * Constructor SOAPBodeElementImpl
	 * @param bodyElement
	 */
	public SOAPBodyElementImpl(OMElement bodyElement){
		super(bodyElement);
	}

}
