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
import java.util.Map;

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
import org.apache.savan.subscribers.AbstractSubscriber;
import org.apache.savan.subscribers.Subscriber;
import org.apache.savan.util.UtilFactory;

/**
 * This is responsible for loading Savan configuration data from a resource
 * for e.g. from a savan-config.xml fie
 */
public class ConfigurationManager {
	
	private HashMap protocolMap = null;
	private HashMap subscriberStoreNamesMap = null;
	private HashMap filterMap = null;
	private HashMap subscribersMap = null;
	
	private final String SAVAN_CONFIG = "savan-config";
	private final String PROTOCOLS = "protocols";
	private final String PROTOCOL = "protocol";
	private final String NAME = "name";
	private final String UTIL_FACTORY = "utilFactory";
	private final String MAPPING_RULES = "mapping-rules";
	private final String ACTION = "mapping-rules";
	private final String SOAP_ACTION = "mapping-rules";
	private final String SUBSCRIBER_STORES = "subscriberStores";
	private final String SUBSCRIBER_STORE = "subscriberStore";
	private final String FILTERS = "filters";
	private final String FILTER = "filter";
	private final String KEY = "key";
	private final String CLASS = "class";
	private final String IDENTIFIER = "identifier";
	private final String SUBSCRIBERS = "subscribers";
	private final String SUBSCRIBER = "subscriber";
	private final String URL_APPENDER = "urlAppender";
	private final String DEFAULT_SUBSCRIBER = "defaultSubscriber";
	private final String DEFAULT_FILTER = "defaultFilter";
	
	
	public ConfigurationManager () {
		protocolMap = new HashMap ();
		subscriberStoreNamesMap = new HashMap ();
		filterMap = new HashMap ();
		subscribersMap = new HashMap ();
	}
	
	/**
	 * To load configurations from a savan-config.xml file in the classpath.
	 * 
	 * @throws SavanException
	 */
	public void configure () throws SavanException {
		ClassLoader classLoader = getClass().getClassLoader();

		configure(classLoader);
	}
	
	public void configure (ClassLoader classLoader) throws SavanException {
		InputStream in = classLoader.getResourceAsStream(SavanConstants.CONFIG_FILE);

		if (in==null)
			throw new SavanException ("Cannot find the savan configuration file. Initialation cannot continue.");
		
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
			String message = "Invalid InputStream.";
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
		if (!SAVAN_CONFIG.equals(element.getLocalName())) {
			throw new SavanException ("'savan-config'should be the document element of the savan configuration xml file");
		}
		
		OMElement protocolsElement = element.getFirstChildWithName(new QName (PROTOCOLS));
		if (protocolsElement==null) {
			throw new SavanException ("'protocols' element should be present, as a sub-element of the 'savan-config' element");
		}
		processProtocols(protocolsElement);
		
		OMElement subscriberStoresElement = element.getFirstChildWithName(new QName (SUBSCRIBER_STORES));
		if (subscriberStoresElement==null) {
			throw new SavanException ("'subscriberStores' element should be present, as a sub-element of the 'savan-config' element");
		}
		processSubscriberStores(subscriberStoresElement);
		
		OMElement filtersElement = element.getFirstChildWithName(new QName (FILTERS));
		if (subscriberStoresElement==null) {
			throw new SavanException ("'Filters' element should be present, as a sub-element of the 'savan-config' element");
		}
		processFilters (filtersElement);
		
		OMElement subscribersElement = element.getFirstChildWithName(new QName (SUBSCRIBERS));
		if (subscriberStoresElement==null) {
			throw new SavanException ("'Subscribers' element should be present as a sub-element of the 'savan-config' element");
		}
		processSubscribers (subscribersElement);
		
	}
	
	private void processProtocols (OMElement element) throws SavanException {
		Iterator protocolElementsIterator = element.getChildrenWithName(new QName (PROTOCOL));
		while (protocolElementsIterator.hasNext()) {
			OMElement protocolElement = (OMElement) protocolElementsIterator.next();
			processProtocol(protocolElement);
		}
	}
	
	private void processProtocol (OMElement element) throws SavanException {
		Protocol protocol = new Protocol ();
		
		OMElement nameElement = element.getFirstChildWithName(new QName (NAME));
		if (nameElement==null)
			throw new SavanException ("Protocol must have a 'Name' subelement");
		String name = nameElement.getText();
		protocol.setName(name);
		
		OMElement utilFactoryNameElement = element.getFirstChildWithName(new QName (UTIL_FACTORY));
		if (utilFactoryNameElement==null)
			throw new SavanException ("Protocol must have a 'UtilFactory' subelement");
		String utilFactoryName = utilFactoryNameElement.getText();
		Object obj = getObject(utilFactoryName);
		if (!(obj instanceof UtilFactory))
			throw new SavanException ("UtilFactory element" + utilFactoryName + "is not a subtype of the UtilFactory class");
		protocol.setUtilFactory((UtilFactory) obj);
		
		OMElement mappingRulesElement = element.getFirstChildWithName(new QName (MAPPING_RULES));
		if (mappingRulesElement==null)
			throw new SavanException ("Protocol must have a 'mappingRules' sub-element");
		processMappingRules (mappingRulesElement,protocol);
		
		OMElement defaultSubscriberElement = element.getFirstChildWithName(new QName (DEFAULT_SUBSCRIBER));
		if (defaultSubscriberElement==null)
			throw new SavanException ("Protocols must have a 'defaultSubscriber' sub-element");
		String defaultSubscriber = defaultSubscriberElement.getText();
		protocol.setDefaultSubscriber(defaultSubscriber);
		
		OMElement defaultFilterElement = element.getFirstChildWithName(new QName (DEFAULT_FILTER));
		if (defaultFilterElement==null)
			throw new SavanException ("Protocols must have a 'defaultFilter' sub-element");
		String defaultFilter = defaultFilterElement.getText();
		protocol.setDefaultFilter(defaultFilter);
		
		protocolMap.put(protocol.getName(),protocol);
	}
	
	private void processMappingRules (OMElement element, Protocol protocol) {
		
		MappingRules mappingRules = new MappingRules ();
		
		Iterator actionsIterator = element.getChildrenWithName(new QName (ACTION));
		while (actionsIterator.hasNext()) {
			OMElement actionElement = (OMElement) actionsIterator.next();
			String action = actionElement.getText();
			mappingRules.addAction(action);
		}
		
		Iterator SOAPActionsIterator = element.getChildrenWithName(new QName (SOAP_ACTION));
		while (SOAPActionsIterator.hasNext()) {
			OMElement SOAPactionElement = (OMElement) SOAPActionsIterator.next();
			String SOAPaction = SOAPactionElement.getText();
			mappingRules.addAction(SOAPaction);
		}
	}
	
	private void processSubscriberStores (OMElement element) throws SavanException {
		Iterator subscriberStoreElementsIterator = element.getChildrenWithName(new QName (SUBSCRIBER_STORE));
		while (subscriberStoreElementsIterator.hasNext()) {
			OMElement subscriberStoreElement = (OMElement) subscriberStoreElementsIterator.next();
			processSubscriberStore(subscriberStoreElement);
		}
	}
	
	private void processSubscriberStore (OMElement element) throws SavanException {
		OMElement keyElement = element.getFirstChildWithName(new QName (KEY));
		if (keyElement==null)
			throw new SavanException ("SubscriberStore must have a 'key' subelement");
		String key = keyElement.getText();
		
		OMElement classElement = element.getFirstChildWithName(new QName (CLASS));
		if (classElement==null)
			throw new SavanException ("SubscriberStore must have a 'Clazz' subelement'");
		
		String clazz = classElement.getText();
		
		//initialize the class to check weather it is value
		Object obj = getObject(clazz);
		
		if (!(obj instanceof SubscriberStore)) {
			String message = "Class " + clazz + " does not implement the  SubscriberStore interface.";
			throw new SavanException (message);
		}
		
		subscriberStoreNamesMap.put(key,clazz);
	}

	public HashMap getProtocolMap () {
		return protocolMap;
	}
	
	public Protocol getProtocol (String name) {
		return (Protocol) protocolMap.get(name);
	}
	
	public SubscriberStore getSubscriberStoreInstance (String key) throws SavanException {
		String name = (String) subscriberStoreNamesMap.get(key);
		return (SubscriberStore) getObject(name);
	}
	
	public Filter getFilterInstanceFromName (String name) throws SavanException {
		for (Iterator it=filterMap.keySet().iterator();it.hasNext();) {
			String key = (String) it.next();
			FilterBean filterBean = (FilterBean) filterMap.get(key);
			if (name.equals(filterBean.getName()))
				return (Filter) getObject(filterBean.getClazz());
		}
		
		return null;
	}
	
	public Filter getFilterInstanceFromId (String id) throws SavanException {
		FilterBean filterBean = (FilterBean) filterMap.get(id);
		String filterClass = filterBean.getClazz();
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
		Iterator filterElementsIterator = element.getChildrenWithName(new QName (FILTER));
		while (filterElementsIterator.hasNext()) {
			OMElement filterElement = (OMElement) filterElementsIterator.next();
			processFilter (filterElement);
		}
	}
	
	private void processFilter (OMElement element) throws SavanException {
		OMElement nameElement = element.getFirstChildWithName(new QName (NAME));
		OMElement identifierElement = element.getFirstChildWithName(new QName (IDENTIFIER));
		OMElement classElement = element.getFirstChildWithName(new QName (CLASS));
		
		if (nameElement==null)
			throw new SavanException ("Name element is not present within the Filter");
		if (identifierElement==null)
			throw new SavanException ("Identifier element is not present within the Filter");
		if (classElement==null)
			throw new SavanException ("Class element is not present within the Filter");
		
		String name = nameElement.getText();
		String identifier = identifierElement.getText();
		String clazz = classElement.getText();
		
		//initialize the class to check weather it is value
		Object obj = getObject(clazz);
		
		if (!(obj instanceof Filter)) {
			String message = "Class " + clazz + " does not implement the  Filter interface.";
			throw new SavanException (message);
		}
		
		FilterBean bean = new FilterBean ();
		bean.setName(name);
		bean.setIdentifier(identifier);
		bean.setClazz(clazz);
		
		filterMap.put(identifier,bean);
	}
	
	private void processSubscribers (OMElement element) throws SavanException {
		Iterator subscriberElementsIterator = element.getChildrenWithName(new QName (SUBSCRIBER));
		while (subscriberElementsIterator.hasNext()) {
			OMElement subscriberElement = (OMElement) subscriberElementsIterator.next();
			processSubscriber (subscriberElement);
		}
	}
	
	private void processSubscriber (OMElement element) throws SavanException {
		OMElement nameElement = element.getFirstChildWithName(new QName (NAME));
		OMElement urlAppenderElement = element.getFirstChildWithName(new QName (URL_APPENDER));
		OMElement classElement = element.getFirstChildWithName(new QName (CLASS));
		
		if (nameElement==null)
			throw new SavanException ("Name element is not present within the AbstractSubscriber");
		if (classElement==null)
			throw new SavanException ("Class element is not present within the Filter");
		
		String name = nameElement.getText();
		String clazz = classElement.getText();
		
		//initialize the class to check weather it is valid
		Object obj = getObject(clazz);
		
		if (!(obj instanceof Subscriber)) {
			String message = "Class " + clazz + " does not implement the  Subscriber interface.";
			throw new SavanException (message);
		}
		
		SubscriberBean bean = new SubscriberBean ();
		bean.setName(name);
		bean.setClazz(clazz);
		
		subscribersMap.put(name,bean);
	}
	
	public Map getSubscriberBeans () {
		return subscribersMap;
	}
	
	public Map getFilterBeans () {
		return filterMap;
	}
	
	public SubscriberBean getSubscriberBean (String subscriberName) {
		return (SubscriberBean) subscribersMap.get(subscriberName);
	}
	
	public AbstractSubscriber getSubscriberInstance (String subscriberName) throws SavanException {
		SubscriberBean subscriberBean = (SubscriberBean) subscribersMap.get(subscriberName);
		if (subscriberBean==null) {
			String message = "A subscriber with the name '" + subscriberName + "' was not found.";
			throw new SavanException (message);
		}
		
		return (AbstractSubscriber) getObject(subscriberBean.getClazz());
	}
	
}
