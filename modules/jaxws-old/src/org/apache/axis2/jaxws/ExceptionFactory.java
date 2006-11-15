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

package org.apache.axis2.jaxws;

import java.lang.reflect.InvocationTargetException;

import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPException;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.MessageInternalException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
  * ExceptionFactory is used to create exceptions within the JAX-WS implementation.
  * There are several reasons for using a factory to create exceptions.
  *    1. We can intercept all exception creation and add the appropriate logging/serviceability.
  *    2. Exceptions are chained.  ExceptionFactory can lengthen or reduce the cause chains as
  *       necessary to support the JAX-WS programming model.
  *    3. Prevents construction of the same exception.  Uses similar principles as AxisFault.makeException.
  *   
  * Example Usage:
  *          // Example usage
  *          
  *          public fooMethod() throws WebServiceException {
  *             try{
  *                ...
  *             }
  *             catch(Exception e){
  *                throw ExceptionFactory.makeWebServiceException(e);
  *             }
  *          }
  *     
  *    
  */
public class ExceptionFactory {
	
	 protected static Log log =
	        LogFactory.getLog(ExceptionFactory.class.getName());
	
	/**
	 * Private Constructor
	 * All methods are static.  The private constructor prevents instantiation.
	 */
	private ExceptionFactory() {
	}
	
	/**
	 * Create a WebServiceException using the information from a given Throwable instance
	 * and message
	 * @param message
	 * @param throwable
	 * @return WebServiceException
	 */
	public static WebServiceException makeWebServiceException(String message, Throwable throwable) {
		try {
			// See if there is already a WebServiceException (Note that the returned exception could be a ProtocolException or
			// other kind of exception)
			WebServiceException e = (WebServiceException) findException(throwable, WebServiceException.class);
			if (e == null) {
				e = createWebServiceException(message, throwable);
			}
			return e;
		} catch (RuntimeException re) {
			// TODO 
			// This is not a good situation, an exception occurred while building the exception.
			// This should never occur!  For now log the problem and rethrow...we may revisit this later
			if (log.isDebugEnabled()) {
				log.debug(Messages.getMessage("exceptionDuringExceptionFlow"), re);
			}
			throw re;
		}
	}
	
	/**
	 * Create a ProtocolException using the information from a Throwable and message
	 * @param message
	 * @param throwable
	 * @return ProtocolException
	 */
	public static ProtocolException makeProtocolException(String message, Throwable throwable) {
		try {
			// See if there is already a ProtocolException 
			ProtocolException e = (ProtocolException) findException(throwable, ProtocolException.class);
			if (e == null) {
				e = createProtocolException(message, throwable);
			}
			return e;
		} catch (RuntimeException re) {
			// TODO 
			// This is not a good situation, an exception occurred while building the exception.
			// This should never occur!  For now log the problem and rethrow...we may revisit this later
			if (log.isDebugEnabled()) {
				log.debug(Messages.getMessage("exceptionDuringExceptionFlow"), re);
			}
			throw re;
		}
	}
	
	/**
	 * Create a MessageException using the information from a Throwable and message
	 * @param message
	 * @param throwable
	 * @return MessageException
	 */
	public static MessageException makeMessageException(String message, Throwable throwable) {
		try {
			// See if there is already a MessgeException 
			MessageException e = (MessageException) findException(throwable, MessageException.class);
			if (e == null) {
				e = createMessageException(message, throwable);
			}
			return e;
		} catch (RuntimeException re) {
			// TODO 
			// This is not a good situation, an exception occurred while building the exception.
			// This should never occur!  For now log the problem and rethrow...we may revisit this later
			if (log.isDebugEnabled()) {
				log.debug(Messages.getMessage("exceptionDuringExceptionFlow"), re);
			}
			throw re;
		}
	}
	
	/**
	 * Create a MessageInternalException using the information from a Throwable and message
	 * @param message
	 * @param throwable
	 * @return MessageInternalException
	 */
	public static MessageInternalException makeMessageInternalException(String message, Throwable throwable) {
		try {
			// See if there is already a HTTPException 
			MessageInternalException e = (MessageInternalException) findException(throwable, MessageInternalException.class);
			if (e == null) {
				e = createMessageInternalException(message, throwable);
			}
			return e;
		} catch (RuntimeException re) {
			// TODO 
			// This is not a good situation, an exception occurred while building the exception.
			// This should never occur!  For now log the problem and rethrow...we may revisit this later
			if (log.isDebugEnabled()) {
				log.debug(Messages.getMessage("exceptionDuringExceptionFlow"), re);
			}
			throw re;
		}
	}
	
	/**
	 * Make a WebServiceException with a given message
	 * @param message
	 * @return WebServiceException
	 */
	public static WebServiceException makeWebServiceException(String message) {
		return makeWebServiceException(message, null);  
	}
	
	/**
	 * Create a WebServiceException using the information from a given Throwable instance
	 * @param throwable
	 * @return WebServiceException
	 */
	public static WebServiceException makeWebServiceException(Throwable throwable){
		return makeWebServiceException(null, throwable);
	}
	
	/**
	 * Create a MessageException using the information from a given Throwable instance
	 * @param throwable
	 * @return MessageException
	 */
	public static MessageException makeMessageException(Throwable throwable){
		return makeMessageException(null, throwable);
	}
	
	/**
	 * Make a MessageException with a given message
	 * @param message
	 * @return MessageException
	 */
	public static MessageException makeMessageException(String message) {
		return makeMessageException(message, null);  
	}
	
	/**
	 * Create a WebServiceException
	 * @param message
	 * @param t Throwable
	 * @return WebServiceException
	 */
	private static WebServiceException createWebServiceException(String message, Throwable t) {
		Throwable rootCause = null;
		if (t != null) {
			rootCause = getRootCause(t);
		}
		WebServiceException e = new WebServiceException(message, t);
		if (log.isDebugEnabled()) {
			log.debug("Create Exception:", e);
		}
		return e;
	}
	
	/**
	 * Create a ProtocolException
	 * @param message
	 * @param t Throwable
	 * @return ProtocolException
	 */
	private static ProtocolException createProtocolException(String message, Throwable t) {
		Throwable rootCause = null;
		if (t != null) {
			rootCause = getRootCause(t);
		}
		ProtocolException e = new ProtocolException(message, t);
		if (log.isDebugEnabled()) {
			log.debug("create Exception:", t);
		}
		return e;
	}
	
	/**
	 * Create a MessageException
	 * @param message
	 * @param t Throwable
	 * @return MessageException
	 */
	private static MessageException createMessageException(String message, Throwable t) {
		Throwable rootCause = null;
		if (t != null) {
			rootCause = getRootCause(t);
		}
		MessageException e = new MessageException(message, t);
		if (log.isDebugEnabled()) {
			log.debug("create Exception:", t);
		}
		return e;
	}
	
	/**
	 * Create a MessageInternalException
	 * @param message
	 * @param t Throwable
	 * @return MessageException
	 */
	private static MessageInternalException createMessageInternalException(String message, Throwable t) {
		Throwable rootCause = null;
		if (t != null) {
			rootCause = getRootCause(t);
		}
		MessageInternalException e = new MessageInternalException(message, t);
		if (log.isDebugEnabled()) {
			log.debug("create Exception:", t);
		}
		return e;
	}
	
    /**
     * Return the exception or nested cause that is assignable from the specified class
     * @param t Throwable
     * @param cls
     * @return Exception or null
     */
    private static Exception findException(Throwable t, Class cls) {
    	while(t != null) {
    		if (cls.isAssignableFrom(t.getClass())) {
                return (Exception) t;
    		}
    		t = getCause(t);
    	}
    	return null;
    }
    
    /**
     * Gets the Throwable cause of the Exception.  Some exceptions
     * store the cause in a different field, which is why this method
     * should be used when walking the causes.
     * @param t Throwable
     * @return Throwable or null
     */
    private static Throwable getCause(Throwable t) {
    	Throwable cause = null;
    	
    	// Look for a specific cause for this kind of exception
    	if (t instanceof InvocationTargetException) {
    		cause = ((InvocationTargetException) t).getTargetException();
    	}
    	
    	// If no specific cause, fall back to the general cause.
    	if (cause == null) {
    		cause = t.getCause();
    	}
    	return null;
    }
    
    /**
     * This method searches the causes of the specified throwable
     * until it finds one that is acceptable as a "root" cause. 
     * 
     * Example: If t is an AxisFault, the code traverses to the next cause.
     * 
     * @param t Throwable
     * @return Throwable root cause
     */
    private static Throwable getRootCause(Throwable t) {
    	while (t != null) {
    		Throwable nextCause = null;
    		if (t instanceof InvocationTargetException ||
    		    t instanceof AxisFault) {
    			// Skip over this cause
    			nextCause = getCause(t);
    			if (nextCause == null) {
    				return t;
    			}
    			t = nextCause;
    		} else {
    			// This is the root cause
    			return t;
    		}
    	}
    	return t;
    }

}

