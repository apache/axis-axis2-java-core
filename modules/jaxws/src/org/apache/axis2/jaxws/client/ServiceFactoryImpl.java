package org.apache.axis2.jaxws.client;

import java.net.URL;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceException;
import javax.xml.ws.ServiceFactory;

import org.apache.axis2.jaxws.JAXRPCWSDLInterface;
import org.apache.axis2.jaxws.factory.WSDLFactoryImpl;

public class ServiceFactoryImpl extends ServiceFactory {

	private Service service;
	
	private static JAXRPCWSDLInterface parserWrapper=null;
	
	public ServiceFactoryImpl() {
		super();
	}

	/**
	 * Method createService
	 * Create a Service instance.
	 * @param wsdlDocumentLocation URL for the WSDL document location for the service
	 * @param serviceName QName for the service.
	 * @return a <code>Service</code> instance
	 * @throws ServiceException
	 */
	@Override
	public Service createService(URL wsdlDocumentLocation, QName serviceName)
			throws ServiceException {
		return createService(wsdlDocumentLocation, serviceName, true);
	}
	
	public Service createService(URL wsdlDocumentLocation, QName serviceName, boolean jaxbUsage)
		throws ServiceException {
		if(parserWrapper==null) {
			//Here am hard coding the parser choice. Should think of better
			//flexible implementation
			parserWrapper = WSDLFactoryImpl.getParser(0, wsdlDocumentLocation);
			
		}
		javax.wsdl.Service wsdlService = parserWrapper.getService(wsdlDocumentLocation, serviceName);
		service = (Service) new ServiceImpl(parserWrapper, wsdlService, jaxbUsage);
		
		return service;
	}

	/**
	 * Method loadService
	 * Create an instance of the generated service implementation class for a given service interface, if available.
	 * @param serviceInterface Service interface 
	 * @return ??? read the spec once again
	 * @throws ServiceException If there is any error while creating the specified service, including the case where a generated service implementation class cannot be located
	 */
	@Override
	public Service loadService(Class serviceInterface) throws ServiceException {
		return loadService(null, serviceInterface, null);
	}

	/**
	 * Method loadService
	 * Create an instance of the generated service implementation class for a 
	 * given service interface, if available. An implementation may use the 
	 * provided wsdlDocumentLocation and properties to help locate the 
	 * generated implementation class. If no such class is present, a 
	 * ServiceException will be thrown.
	 * @param wsdlDocumentLocation URL for the WSDL document location for the service or null
	 * @param serviceInterface Service interface
	 * @param properties A set of implementation-specific properties to help locate the generated service implementation class 
	 * @return ??? read the spec once again
	 * @throws ServiceException If there is any error while creating the specified service, including the case where a generated service implementation class cannot be located
	 */
	@Override
	public Service loadService(URL wsdlDocumentLocation,
			Class serviceInterface, Properties properties)
			throws ServiceException {
		Service returnClass;
		//Check if serviceInterface is already available to load and return
		try {
			returnClass = (Service)serviceInterface.newInstance();
		} catch (InstantiationException e) {
			//attempt to load the interface failed, so lets check other
			//alternative - interpret the generated service class using wsdl
			//location and or properties and see if you have any luck
			String serviceClassName;
			serviceClassName = interpretServiceClassName(wsdlDocumentLocation, properties);
			Class loadedClass;
			try {
				loadedClass = Thread.currentThread().getContextClassLoader().loadClass(serviceClassName);
				returnClass = (Service)loadedClass.newInstance();
			} catch (Exception e1) {
				throw new ServiceException(e1);
			}
		} catch (IllegalAccessException e) {
			throw new ServiceException(e);
		}
		return returnClass;
	}

	/**
	 * Method loadService
	 * Create an instance of the generated service implementation class for a 
	 * given service, if available. The service is uniquely identified by the 
	 * wsdlDocumentLocation and serviceName arguments. An implementation may 
	 * use the provided properties to help locate the generated implementation 
	 * class. If no such class is present, a ServiceException will be thrown.
	 * @param wsdlDocumentLocation URL for the WSDL document location for the service or null
	 * @param serviceName Qualified name for the service
	 * @param properties A set of implementation-specific properties to help locate the generated service implementation class 
	 * @return ??? read the spec once again
	 * @throws ServiceException If there is any error while creating the specified service, including the case where a generated service implementation class cannot be located
	 */
	@Override
	public Service loadService(URL wsdlDocumentLocation, QName serviceName,
			Properties properties) throws ServiceException {
		Service returnClass = null;
		// TODO Need to consult someone, I'm not fully clear abt this implmntn
		// as to how should wsdlDocumentLocation be used etc.
		if (properties != null) {
			//TODO Do something and get the name of the generated service 
			//implementation class name
			//load the class with that name, instantiate it and return
			return null;
		}
		else {
			//Interpreting the name of generated class for this service
			//WITHOUT taking into consideration name-collisions.
			//TODO Revisit to embedd name collision logic
			String localPart = serviceName.getLocalPart();
			String packageName = getGeneratedClassPackageName(serviceName);
			try {
				returnClass = (Service)Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + localPart).newInstance();
			} catch (Exception e) {
				throw new ServiceException(e);
			}
		}
		return returnClass;
	}

	/**
	 * Method getServiceClassName
	 * This will interpret and return the name of the generated Service 
	 * implementation class using wsdl location and or properties
	 * @param wsdlDocumentLocation
	 * @param properties
	 */
	private String interpretServiceClassName(URL wsdlDocumentLocation, Properties properties) {
		//This code could be a tiny beast, code little by little
		String serviceClassName = null;
		if (wsdlDocumentLocation==null && properties == null) {
			//Not wise to spend time trying to interpret serviceName out of nulls :)
			return null;
		}
		//TODO method implementation is not complete
		
		return serviceClassName;
	}
	
	/**
	 * Method getGeneratedClassPackageName
	 * Returns the package in which requested class was generated
	 * @param className
	 * @return
	 */
	public String getGeneratedClassPackageName (QName className) {
		//TODO Providing just a hard coded makeshift implementation for now
		//Need to properly code the method.
		return new String("defaultPackage");
	}
}//class ServiceFactoryImpl.
