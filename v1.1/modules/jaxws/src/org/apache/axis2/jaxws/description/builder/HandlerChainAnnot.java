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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;

import javax.jws.HandlerChain;
import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.ExceptionFactory;

import java.lang.annotation.Annotation;

public class HandlerChainAnnot implements javax.jws.HandlerChain{

	private String file = "";
	private String name = "";
	
	/**
     * A WebServiceAnnot cannot be instantiated.
     */
	private  HandlerChainAnnot(){
		
	}
	
    public static HandlerChainAnnot createHandlerChainAnnotImpl() {
        return new HandlerChainAnnot();
    }

    public String file(){
		return this.file;
	}
		
	public String name(){
		return this.name;
	}
	
	/**
	 * @param file The file to set.
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	//hmm, should we really do this
	public Class<Annotation> annotationType(){
		return Annotation.class;
	}
}
