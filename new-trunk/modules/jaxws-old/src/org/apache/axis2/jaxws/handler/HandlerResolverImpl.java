package org.apache.axis2.jaxws.handler;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.handler.soap.SOAPHandler;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.spi.ServiceDelegate;

/* 
 * This class should be created by the ServiceDelegate.
 * HandlerResolverImpl.getHandlerChain(PortInfo) will be called by the
 * InvocationContext, and the return value will be set on the Binding
 * under the BindingProvider.
 * 
 * HandlerResolverImpl.getHandlerChain(PortInfo) will be responsible for
 * starting each Handler's lifecycle according to JAX-WS spec 9.3.1
 */

public class HandlerResolverImpl implements HandlerResolver {

	/*
	 * TODO:  is there any value/reason in caching the list we collect from the
	 * ports?  It is a "live" list in the sense that we could possibly return
	 * a List or ArrayList object to a service or client application, where
	 * they could manipulate it.
	 */

	// we'll need to refer to this object to get the port, and thus handlers
	private ServiceDelegate delegate;

	public HandlerResolverImpl(ServiceDelegate delegate) {
		this.delegate = delegate;
	}

	public List<Handler> getHandlerChain(PortInfo portinfo) {
		// TODO:  would check and/or build cache here if implemented later
		return resolveHandlers(portinfo);
	}

	/*
	 * The list of handlers (rather, list of class names) is already
	 * available per port.  Ports are stored under the ServiceDelegate
	 * as PortData objects.
	 * 
	 * The resolveHandlers method is responsible for instantiating each Handler,
	 * running the annotated PostConstruct method, sorting the list, resolving the list,
	 * and returning it
	 */
	private List<Handler> resolveHandlers(PortInfo portinfo) throws WebServiceException {
		EndpointDescription edesc = delegate.getServiceDescription().getEndpointDescription(portinfo.getPortName());
		
		ArrayList handlers = new ArrayList<Handler>();
		ArrayList logicalHandlers = new ArrayList<Handler>();
		ArrayList protocolHandlers = new ArrayList<Handler>();

		/*
		 * TODO: the list returned by getHandlerList() eventually will contain
		 * more information than just a list of strings.  We will need to
		 * do a better job checking that the return value (a HandlerDescription
		 * object?) matches up with the PortInfo object before we add it to the
		 * chain.
		 */
		
		for (String portHandler : edesc.getHandlerList()) {
			Handler handlerClass;
			// instantiate portHandler class
			try {
				// TODO: ok to use system classloader?
				handlerClass = (Handler) loadClass(portHandler).newInstance();
				callHandlerPostConstruct(handlerClass);
			} catch (ClassNotFoundException e) {
				// TODO: should we just ignore this problem?
				// TODO: NLS log and throw
				throw ExceptionFactory.makeWebServiceException(e);
			} catch (InstantiationException ie) {
				// TODO: should we just ignore this problem?
				// TODO: NLS log and throw
				throw ExceptionFactory.makeWebServiceException(ie);
			} catch (IllegalAccessException e) {
				// TODO: should we just ignore this problem?
				// TODO: NLS log and throw
				throw ExceptionFactory.makeWebServiceException(e);
			}
			
			// 9.2.1.2 sort them by Logical, then SOAP
			if (LogicalHandler.class.isAssignableFrom(handlerClass.getClass()))
				logicalHandlers.add((LogicalHandler)handlerClass);
			else if (SOAPHandler.class.isAssignableFrom(handlerClass.getClass()))
				// instanceof ProtocolHandler
				protocolHandlers.add((SOAPHandler)handlerClass);
			else if (Handler.class.isAssignableFrom(handlerClass.getClass())) {
				// TODO: NLS better error message
				throw ExceptionFactory.makeWebServiceException(Messages.getMessage("handlerChainErr1", handlerClass.getClass().getName()));
			} else {
				// TODO: NLS better error message
				throw ExceptionFactory.makeWebServiceException(Messages.getMessage("handlerChainErr2", handlerClass.getClass().getName()));
			}
		}
		
		handlers.addAll(logicalHandlers);
		handlers.addAll(protocolHandlers);
		return handlers;
	}
	
	
	private static Class loadClass(String clazz) throws ClassNotFoundException {
		try {
			return Class.forName(clazz, true, ClassLoader.getSystemClassLoader());
		} catch (ClassNotFoundException e) {
			throw e;
		}
	}


	private static void callHandlerPostConstruct(Object handlerClass) {
		/*
		 * TODO apparently there's no javax.annotation.* package in Java
		 * EE 5 ?? We need to call @PostConstruct method on handler if present
		for (Method method : handlerClass.getClass().getMethods()) {
			if (method.getAnnotation(javax.annotation.PostConstruct.class) != null) {
				try {
					method.invoke(handlerClass, new Object [0]);
					break;
				} catch (Exception e) {
					// TODO: log it, but otherwise ignore
				}
			}
		}
		*/
	}
	
	/*
	 * Helper method to destroy all instantiated Handlers once the runtime
	 * is done with them.
	 */
	public static void destroyHandlers(List<Handler> handlers) {
		/*
		 * TODO apparently there's no javax.annotation.* package in Java
		 * EE 5 ?? We need to call @PostConstruct method on handler if present
		for (Handler handler: handlers) {
			for (Method method: handler.getClass().getMethods()) {
				if (method.getAnnotation(javax.annotation.PreDestroy.class) != null) {
					try {
						method.invoke(handlerClass, new Object[0]);
						break;
					} catch (Exception e) {
						// TODO: log it, but otherwise ignore
					}
				}
			}
		}
		*/
	}
}
