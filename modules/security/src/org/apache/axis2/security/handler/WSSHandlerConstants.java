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

public interface WSSHandlerConstants {

    public static final String ENFORCE_ACTION_ORDER = "EnforceActionOrder";

    
    /**
     * The following two seot the constants are used to introduce new 
     * parameter names for the two handlers since wss4j expects
     * the same param name irrespective of the param name.
     * 
     * It should be noted that we should only introduce names that are in
     * conflict. For example both handlers expects 'action' param and in 
     * the axis2.xml this should be given as InAction and OutAction to 
     * specify the 'action' to values for the two handlers. Whereas 
     * parameters such as 'encryptionKeyIdentifier' need not be mapped into 
     * new param names since they are specific to a handler
     */
    
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
        public static final String SIGNATURE_PARTS = "OutSignatureParts";

        //Repetition count
		public static final String SENDER_REPEAT_COUNT = "senderRepeteCount";
		public static final String REPETITON = "repetition";
		
		public static final String OPTIMIZE_PARTS = "optimizeParts";
	}
	
	
}
