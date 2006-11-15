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

package org.apache.savan.publication;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.savan.SavanException;

/**
 * This will encapsulate error information of a specific publication.
 * Probably will contain details of each subscriber to which the message could not
 * be delivered successfully. 
 */
public class PublicationReport {

	/**
	 * The susbscribers to which this msg could not be sent. Probably their ID and the
	 * Exception that occured.
	 */
	private Hashtable errors = null;
	
	/**
	 * Ids of the subscribers to which this msg could be sent successfully.
	 */
	private ArrayList notifiedSubscribers;
	
	public PublicationReport () {
		errors = new Hashtable ();
		notifiedSubscribers = new ArrayList ();
	}
	
	public void addErrorReportEntry (String id, SavanException reason) {
		errors.put(id,reason);
	}
	
	public void addNotifiedSubscriber (String subscriberID) {
		notifiedSubscribers.add(subscriberID);
	}

	public Hashtable getErrors() {
		return errors;
	}

	public ArrayList getNotifiedSubscribers() {
		return notifiedSubscribers;
	}
	
	
}
