/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.savan.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.filters.Filter;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.util.UtilFactory;

/**
 * This is responsible for loading Savan configuration data from a resource
 * for e.g. from a savan-config.xml fie
 */
public class ConfigurationManager {
	
	private HashMap protocolMap = null;
	private HashMap subscriberStoreNamesMap = null;
	private HashMap filterMap = null;
	
	private final String SavanConfig = "savan-config";
	private final String Protocols = "protocols";
	private final String Protocol = "protocol";
	private final String Name = "name";
	private final String UtilFactory = "utilFactory";
	private final String MappingRules = "mapping-rules";
	private final String Action = "mapping-rules";
	private final String SOAPAction = "mapping-rules";
	private final String SubscriberStores = "subscriberStores";
	private final String SubscriberStore = "subscriberStore";
	private final String Filters = "filters";
	private final String Filter = "filter";
	private final String Key = "key";
	private final String Clazz = "class";
	private final String Identifier = "identifier";
	
	public ConfigurationManager () {
		protocolMap = new HashMap ();
		subscriberStoreNamesMap = new HashMap ();
		filterMap = new HashMap ();
	}
	
	/**
	 * To load configurations from a savan-config.xml file in the classpath.
	 * 
	 * @throws SavanException
	 */
	public void configure () throws SavanException {
		InputStream in = Thread.currentThread().getContextClassLoader().
					getResourceAsStream(SavanConstants.CONFIG_FILE);

		configure(in);
	}
	
	/**
	 * To Load configurations from a file.
	 * 
	 * @param file
	 * @throws SavanException
	 */
	public void configure (File file) throws SavanException {
		try {
			InputStream in = new FileInputStream (file);
			configure(in);
		} catch (IOException e) {
			throw new SavanException (e);
		}
	}
	
	/**
	 * To load configurations from a InputStream.
	 * 
	 * @param in
	 * @throws SavanException
	 */
	public void configure (InputStream in) throws SavanException {

		if (in==null) {
			String message = "Cant create an InputStream from the property file";
			throw new SavanException (message);
		}
		
		try {
			XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(in);
			OMFactory factory = OMAbstractFactory.getOMFactory();
			
			StAXOMBuilder builder = OMXMLBuilderFactory.createStAXOMBuilder(factory,parser);
			OMElement document = builder.getDocumentElement();
			
			if (document==null) {
				throw new SavanException ("Configuration XML does not have a document element");
			}
			
			processSavanConfig(document);
		} catch (Exception e) {
			throw new SavanException (e);
		} 
	}
	
	
	private void processSavanConfig (OMElement element) throws SavanException {
		if (!SavanConfig.equals(element.getLocalName())) {
			throw new SavanException ("'savan-config'should be the document element of the savan configuration xml file");
		}
		
		OMElement protocolsElement = element.getFirstChildWithName(new QName (Protocols));
		if (protocolsElement==null) {
			throw new SavanException ("'protocols' element should be present, as a sub-element of the 'savan-config' element");
		}
		processProtocols(protocolsElement);
		
		OMElement subscriberStoresElement = element.getFirstChildWithName(new QName (SubscriberStores));
		if (subscriberStoresElement==null) {
			throw new SavanException ("'subscriberStores' element should be present, as a sub-element of the 'savan-config' element");
		}
		processSubscriberStores(subscriberStoresElement);
		
		OMElement filtersElement = element.getFirstChildWithName(new QName (Filters));
		if (subscriberStoresElement==null) {
			throw new SavanException ("'Filters' element should be present, as a sub-element of the 'savan-config' element");
		}
		processFilters (filtersElement);
	}
	
	private void processProtocols (OMElement element) throws SavanException {
		Iterator protocolElementsIterator = element.getChildrenWithName(new QName (Protocol));
		while (protocolElementsIterator.hasNext()) {
			OMElement protocolElement = (OMElement) protocolElementsIterator.next();
			processProtocol(protocolElement);
		}
	}
	
	private void processProtocol (OMElement element) throws SavanException {
		Protocol protocol = new Protocol ();
		
		OMElement nameElement = element.getFirstChildWithName(new QName (Name));
		if (nameElement==null)
			throw new SavanException ("Protocol must have a 'Name' subelement");
		String name = nameElement.getText();
		protocol.setName(name);
		
		OMElement utilFactoryNameElement = element.getFirstChildWithName(new QName (UtilFactory));
		if (utilFactoryNameElement==null)
			throw new SavanException ("Protocol must have a 'UtilFactory' subelement");
		String utilFactoryName = utilFactoryNameElement.getText();
		Object obj = getObject(utilFactoryName);
		if (!(obj instanceof UtilFactory))
			throw new SavanException ("UtilFactory element" + utilFactoryName + "is not a subtype of the UtilFactory class");
		protocol.setUtilFactory((UtilFactory) obj);
		
		OMElement mappingRulesElement = element.getFirstChildWithName(new QName (MappingRules));
		if (mappingRulesElement==null)
			throw new SavanException ("Protocol must have a 'MappingRules' subelement");
		processMappingRules (mappingRulesElement,protocol);
		
		protocolMap.put(protocol.getName(),protocol);
		
	}
	
	private void processMappingRules (OMElement element, Protocol protocol) {
		
		MappingRules mappingRules = new MappingRules ();
		
		Iterator actionsIterator = element.getChildrenWithName(new QName (Action));
		while (actionsIterator.hasNext()) {
			OMElement actionElement = (OMElement) actionsIterator.next();
			String action = actionElement.getText();
			mappingRules.addAction(action);
		}
		
		Iterator SOAPActionsIterator = element.getChildrenWithName(new QName (SOAPAction));
		while (SOAPActionsIterator.hasNext()) {
			OMElement SOAPactionElement = (OMElement) SOAPActionsIterator.next();
			String SOAPaction = SOAPactionElement.getText();
			mappingRules.addAction(SOAPaction);
		}
	}
	
	private void processSubscriberStores (OMElement element) throws SavanException {
		Iterator subscriberStoreElementsIterator = element.getChildrenWithName(new QName (SubscriberStore));
		while (subscriberStoreElementsIterator.hasNext()) {
			OMElement subscriberStoreElement = (OMElement) subscriberStoreElementsIterator.next();
			processSubscriberStore(subscriberStoreElement);
		}
	}
	
	private void processSubscriberStore (OMElement element) throws SavanException {
		OMElement keyElement = element.getFirstChildWithName(new QName (Key));
		if (keyElement==null)
			throw new SavanException ("SubscriberStore must have a 'key' subelement");
		String key = keyElement.getText();
		
		OMElement classElement = element.getFirstChildWithName(new QName (Clazz));
		if (classElement==null)
			throw new SavanException ("SubscriberStore must have a 'Clazz' subelement'");
		
		String clazz = classElement.getText();
		subscriberStoreNamesMap.put(key,clazz);
	}

	public HashMap getProtocolMap () {
		return protocolMap;
	}
	
	public HashMap getSubscriberStoreNamesMap () {
		return subscriberStoreNamesMap;
	}
	
	public SubscriberStore getSubscriberStoreInstance (String key) throws SavanException {
		String name = (String) subscriberStoreNamesMap.get(key);
		return (SubscriberStore) getObject(name);
	}
	
	public Filter getFilterInstance (String key) throws SavanException {
		String filterClass = (String) filterMap.get(key);
		if (filterClass==null)
			return null;
		
		return (Filter) getObject(filterClass);
	}
	
	private Object getObject (String className) throws SavanException {
	
		Object obj;
		try {
			Class c = Class.forName (className);
			 obj = c.newInstance();
		} catch (Exception e) {
			String message = "Can't instantiate the class:" + className;
			throw new SavanException (message,e);
		}
		 
		return obj;
	}
	
	private void processFilters (OMElement element) throws SavanException {
		Iterator filterElementsIterator = element.getChildrenWithName(new QName (Filter));
		while (filterElementsIterator.hasNext()) {
			OMElement filterElement = (OMElement) filterElementsIterator.next();
			processFilter (filterElement);
		}
	}
	
	private void processFilter (OMElement element) throws SavanException {
		OMElement identifierElement = element.getFirstChildWithName(new QName (Identifier));
		OMElement classElement = element.getFirstChildWithName(new QName (Clazz));
		
		if (identifierElement==null)
			throw new SavanException ("Identifier element is not present within the Filter");
		if (classElement==null)
			throw new SavanException ("Class element is not present within the Filter");
		
		String identifier = identifierElement.getText();
		String clazz = classElement.getText();
		
		filterMap.put(identifier,clazz);
	}
	
}
