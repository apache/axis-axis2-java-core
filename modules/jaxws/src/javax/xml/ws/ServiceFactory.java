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

/**
 * Class ServiceFactory
 * The javax.xml.rpc.ServiceFactory is an abstract class that provides a 
 * factory for the creation of instances of the type javax.xml.rpc.Service. 
 * This abstract class follows the abstract static factory design pattern. 
 * This enables a J2SE based client to create a Service instance in a portable 
 * manner without using the constructor of the Service implementation class.
 * The ServiceFactory implementation class is set using the system property 
 * SERVICEFACTORY_PROPERTY.
 * 
 * @version 1.1
 * @author sunja07
 */
public abstract class ServiceFactory {

	/**
	 * A constant representing the property used to lookup the name of a 
	 * ServiceFactory implementation class. 
	 */
	public static final java.lang.String SERVICEFACTORY_PROPERTY = 
		"javax.xml.rpc.ServiceFactory";
	
	private static ServiceFactory serviceFactoryImpl = null;
	
	/**
	 * Empty constructor
	 */
	protected ServiceFactory() {}
	
	/**
	 * Gets an instance of the ServiceFactory 
	 * Only one copy of a factory exists and is returned to the application 
	 * each time this method is called.
	 * The implementation class to be used can be overridden by setting the 
	 * javax.xml.rpc.ServiceFactory system property.
	 * @return static instance of ServiceFactory
	 */
	public static ServiceFactory newInstance(){
		
		if (serviceFactoryImpl != null)
			return serviceFactoryImpl;
		
		//<TBR>: Comment to be removed
		//I must be returning the implementation class set using the property 
		//SERVICEFACTORY_PROPERTY. This class will be set at the
		//configuration time(?), using configuration data viz. system props
		//or XML/properites config files or user and system preference data
		//</TBR>		
		try{
			String serviceFactoryImplName;
			//<TBR>: Comment to be removed
			// Here actually initialization of the name with the corresponding
			// hashmap entry value of SERVICEFACTORY_PROPERTY key should happen 
			// Since we didn't finalize the hash map for config information 
			// we will use the default impl class
			//</TBR>
			serviceFactoryImplName = "org.apache.axis2.jaxws.ServiceFactoryImpl";
			Class loadedClass;
			
			loadedClass = Thread.currentThread().getContextClassLoader().loadClass(serviceFactoryImplName);
			serviceFactoryImpl = (ServiceFactory)loadedClass.newInstance();
		} catch(Exception e){
			
		}
		return serviceFactoryImpl;
	}
	
	/**
	 * Method createService
	 * Create a Service instance.
	 * @param wsdlDocumentLocation URL for the WSDL document location for the 
	 * service
	 * @param serviceName QName for the service.
	 * @return a <code>Service</code> instance
	 * @throws WebServiceException
	 */
	public abstract Service createService(java.net.URL wsdlDocumentLocation,
            javax.xml.namespace.QName serviceName) throws WebServiceException;
	
	/**
	 * Create a Service instance.
	 * @param serviceName - QName for the service
	 * @return a <code>Service</code> instance
	 * @throws WebServiceException - If any error in creation of the specified service
	 */
	public abstract Service createService(javax.xml.namespace.QName serviceName)
			throws WebServiceException;
	
	/**
	 * Create and return an instance implementing a given service interface.
	 * <p>
	 * The service interface must be annotated with a @WebServiceClient annotation. If the wsdlDocumentLocation parameter
	 * is specified, it will take priority over the one specified by the annotation.
	 * @param <T>
	 * @param wsdlDocumentLocation - URL for the WSDL document location for the service or null
	 * @param serviceInterface - Service interface
	 * @return
	 * @throws WebServiceException - If there is any error while creating the specified service
	 * @see <code>WebServiceClient</code>
	 */
	public abstract <T extends Service> T createService(java.net.URL wsdlDocumentLocation,
			 java.lang.Class<T> serviceInterface) throws WebServiceException;
	
	/**
	 * Create and return an instance implementing a given service interface.
	 * <p>
	 * The service interface must be annotated with a @WebServiceClient annotation specifying a WSDL document and a
	 * qualified name of a .
	 * @param <T>
	 * @param serviceInterface - Service interface
	 * @return
	 * @throws WebServiceException  - If there is any error while creating the specified service
	 * @see <code>WebServiceClient</code>
	 */
	public abstract <T extends Service> T createService(java.lang.Class<T> serviceInterface) throws WebServiceException;
}
