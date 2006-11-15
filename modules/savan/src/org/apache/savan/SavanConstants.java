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

package org.apache.savan;

/**
 * Contains the constants used by Savan
 * 
 *
 */
public interface SavanConstants {

	String CONFIGURATION_MANAGER = "SavanConfigurationManager"; //Property name to store the CM in the ConfigCtx.
	
	String VALUE_TRUE = "true";
	String VALUE_FALSE = "false";

	String MESSAGE_TYPE = "SavanMessageType";
	String PUBLICATION_MESSAGE = "SavanPublicationMessage";
	
	String SUBSCRIBER_STORE = "SubscriberStore"; //AxisService property
	String SUBSCRIBER_STORE_KEY = "SubscriberStoreKey"; //to mention the key in the services.xml
	String PROTOCOL = "Protocol";
	
	String CONFIG_FILE = "savan-config.xml";
	String UTIL_FACTORY = "UtilFactory";
	
	String DEFAULT_SUBSCRIBER_STORE_KEY = "default";
	
	interface MessageTypes {
		int UNKNOWN = -1;
		int SUBSCRIPTION_MESSAGE = 1;
		int SUBSCRIPTION_RESPONSE_MESSAGE = 2;
		int UNSUBSCRIPTION_MESSAGE = 3;
		int UNSUBSCRIPTION_RESPONSE_MESSAGE = 4;
		int RENEW_MESSAGE = 5;
		int RENEW_RESPONSE_MESSAGE = 6;
		int GET_STATUS_MESSAGE = 7;
		int GET_STATUS_RESPONSE_MESSAGE = 8;
	}
	
	interface Properties {
		String SUBSCRIBER_STORE = "SubscriberStore";
	}
	

	
}
