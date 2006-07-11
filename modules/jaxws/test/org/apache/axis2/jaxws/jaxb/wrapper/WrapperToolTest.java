/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.jaxws.jaxb.wrapper;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.axis2.jaxws.jaxb.stockquote.GetPrice;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;

import junit.framework.TestCase;


public class WrapperToolTest extends TestCase {
	public void testWrapStockQuote(){
		try{
			JAXBWrapperTool wrapper = new JAXBWrapperToolImpl();
			
			String jaxbClassName = "org.apache.axis2.jaxws.jaxb.stockquote.GetPrice";
			Class jaxbClass = Class.forName(jaxbClassName, false, ClassLoader.getSystemClassLoader());
			ArrayList<String> childNames = new ArrayList<String>();
			String childName = "symbol";
			childNames.add(childName);
			String symbolObj = new String("IBM");
			Map<String, Object> childObjects= new WeakHashMap<String, Object>();
			childObjects.put(childName, symbolObj);
			Object jaxbObject = wrapper.wrap(jaxbClass, jaxbClassName,childNames, childObjects);
			GetPrice getPrice = (GetPrice)jaxbObject;
			
		}catch(JAXBWrapperException e){
			e.printStackTrace();
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}
	}
	
	public void testUnwrapStockQuote(){
		try{
			JAXBWrapperTool wrapper = new JAXBWrapperToolImpl();
			GetPrice price = new GetPrice();
			price.setSymbol("IBM");
			
			ArrayList<String> childNames = new ArrayList<String>();
			String childName = "symbol";
			childNames.add(childName);
			
			Object[] jaxbObjects = wrapper.unWrap(price, childNames);
		
		}catch(JAXBWrapperException e){
			e.printStackTrace();
		}
	}
	
	public void testWrapMFQuote(){
		try{
			JAXBWrapperTool wrapper = new JAXBWrapperToolImpl();
			
			String jaxbClassName = "org.apache.axis2.jaxws.jaxb.mfquote.GetPrice";
			Class jaxbClass = Class.forName(jaxbClassName, false, ClassLoader.getSystemClassLoader());
			ArrayList<String> childNames = new ArrayList<String>();
			String fund ="fund";
			String fundName = new String("PRGFX");
			String holding = "_10Holdings";
			String topHolding = new String("GE");
			String nav ="nav";
			String navInMillion = new String("700");
			
			childNames.add(fund);
			childNames.add(holding);
			childNames.add(nav);
			
			Map<String, Object> childObjects= new WeakHashMap<String, Object>();
			
			childObjects.put(fund, fundName);
			childObjects.put(holding, topHolding);
			childObjects.put(nav, navInMillion);
			
			Object jaxbObject = wrapper.wrap(jaxbClass, jaxbClassName,childNames, childObjects);
			org.apache.axis2.jaxws.jaxb.mfquote.GetPrice getPrice = (org.apache.axis2.jaxws.jaxb.mfquote.GetPrice)jaxbObject;
			
		}catch(JAXBWrapperException e){
			e.printStackTrace();
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}
	}
	
	public void testUnwrapMFQuote(){
		try{
			JAXBWrapperTool wrapper = new JAXBWrapperToolImpl();
			org.apache.axis2.jaxws.jaxb.mfquote.GetPrice price = new org.apache.axis2.jaxws.jaxb.mfquote.GetPrice();
			price.setFund("PRGFX");
			price.set10Holdings("GE");
			price.setNav("700");
			
			ArrayList<String> childNames = new ArrayList<String>();
			String fund ="fund";
			childNames.add(fund);
			String holding = "_10Holdings";
			childNames.add(holding);
			String nav ="nav";
			childNames.add(nav);
			
			Object[] jaxbObjects = wrapper.unWrap(price, childNames);
			System.out.println();
		}catch(JAXBWrapperException e){
			e.printStackTrace();
		}
	}
}
