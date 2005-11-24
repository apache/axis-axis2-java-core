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
 * Interface Dispatch<T>
 * The javax.xml.rpc.Dispatch interface provides support for the dynamic 
 * invocation of a service endpoint operations. The javax.xml.rpc.Service 
 * interface acts as a factory for the creation of Dispatch instances.
 * 
 * @since JAX-WS 2.0
 * @author sunja07
 */
public interface Dispatch<T> extends BindingProvider{
	
	/**
	 * Method invoke
	 * Invoke a service operation synchronously. The client is responsible 
	 * for ensuring that the msg object when marshalled is formed according 
	 * to the requirements of the protocol binding in use.
	 * 
	 * @param msg - An object that will form the message or payload of the 
	 * message used to invoke the operation.
	 * @return The response message or message payload to the operation 
	 * invocation.
	 * @throws java.rmi.RemoteException - If a fault occurs during 
	 * communication with the service
	 * @throws WebServiceException - If there is any error in the configuration 
	 * of the Dispatch instance. 
	 */
	T invoke(T msg) throws java.rmi.RemoteException, WebServiceException;
	
	/**
	 * Method invokeAsync
	 * Invoke a service operation asynchronously. The method returns without 
	 * waiting for the response to the operation invocation, the results of 
	 * the operation are obtained by polling the returned Response. The 
	 * client is responsible for ensuring that the msg object when marshalled 
	 * is formed according to the requirements of the protocol binding in use.
	 * 
	 * @param msg - An object that will form the message or payload of the 
	 * message used to invoke the operation.
	 * @return The response message or message payload to the operation 
	 * invocation.
	 * @throws WebServiceException - If there is any error in the configuration 
	 * of the Dispatch instance.
	 * @throws java.rmi.RemoteException
	 */
	Response<T> invokeAsync(T msg) throws java.rmi.RemoteException, WebServiceException;
	
	/**
	 * Method invokeAsync
	 * Invoke a service operation asynchronously. The method returns without 
	 * waiting for the response to the operation invocation, the results of 
	 * the operation are communicated to the client via the passed in handler.
	 * The client is responsible for ensuring that the msg object when 
	 * marshalled is formed according to the requirements of the protocol 
	 * binding in use.
	 * 
	 * @param msg - An object that will form the message or payload of the 
	 * message used to invoke the operation.
	 * @param handler - The handler object that will receive the response to 
	 * the operation invocation.
	 * @return A Future object that may be used to check the status of the 
	 * operation invocation. This object must not be used to try to obtain 
	 * the results of the operation - the object returned from Future.get() 
	 * is implementation dependent and any use of it will result in 
	 * non-portable behaviour.
	 * @throws WebServiceException - If there is any error in the configuration 
	 * of the Dispatch instance
	 */
	java.util.concurrent.Future<?> invokeAsync(T msg,
            AsyncHandler<T> handler) throws WebServiceException;
	
	/**
	 * Method invokeOneWay
	 * Invokes a service operation using the one-way interaction mode. The 
	 * operation invocation is logically non-blocking, subject to the 
	 * capabilities of the underlying protocol, no results are returned. 
	 * When the protocol in use is SOAP/HTTP, this method must block until an 
	 * HTTP response code has been received or an error occurs. The client is 
	 * responsible for ensuring that the msg object when marshalled is formed 
	 * according to the requirements of the protocol binding in use.
	 * 
	 * @param msg - An object that will form the message or payload of the 
	 * message used to invoke the operation.
	 * @throws WebServiceException - If there is any error in the configuration 
	 * of the Dispatch instance or if an error occurs during the invocation.
	 */
	void invokeOneWay(T msg) throws WebServiceException;

}
