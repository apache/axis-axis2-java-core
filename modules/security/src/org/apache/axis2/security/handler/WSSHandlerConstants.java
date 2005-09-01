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
package org.apache.axis2.security.handler;

/**
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public interface WSSHandlerConstants {

	interface In {
		public static final String ACTION = "InAction";
		public static final String PW_CALLBACK_CLASS = "InPasswordCallbackClass";
		public static final String SIG_PROP_FILE = "InSignaturePropFile";
		public static final String SIG_KEY_ID = "InSignatureKeyIdentifier";
	}
	
	interface Out {
		public static final String ACTION = "OutAction";
		public static final String PW_CALLBACK_CLASS = "OutPasswordCallbackClass";
		public static final String SIG_PROP_FILE = "OutSignaturePropFile";
		public static final String SIG_KEY_ID = "OutSignatureKeyIdentifier";
		
		//Repetition count
		public static final String SENDER_REPEAT_COUNT = "senderRepeteCount";
		public static final String REPETITON = "repetition";
	}
	
	
}
