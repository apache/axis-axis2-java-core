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

package org.apache.savan.storage;

import java.util.Iterator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.savan.SavanException;
import org.apache.savan.subscribers.Subscriber;

/**
 * Defines the Storage for storing subscribers. 
 */
public interface SubscriberStore {

	/**
	 * To Initialize the storage.
	 * 
	 * @param configurationContext
	 * @throws SavanException
	 */
	void init (ConfigurationContext configurationContext) throws SavanException;
	
	/**
	 * To store the subscriber.
	 * 
	 * @param s
	 * @throws SavanException
	 */
	void store (Subscriber s) throws SavanException;
	
	/**
	 * To retrieve a previously stored subscriber.
	 * 
	 * @param subscriberID
	 * @return
	 * @throws SavanException
	 */
	Subscriber retrieve (String subscriberID) throws SavanException;
	
	/**
	 * To retrieve all subscribers stored upto now.
	 * 
	 * @return
	 * @throws SavanException
	 */
	Iterator retrieveAll () throws SavanException;
	
	/**
	 * To delete a previously stored subscriber.
	 * 
	 * @param subscriberID
	 * @throws SavanException
	 */
	void delete (String subscriberID) throws SavanException;
}
