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
package org.apache.axis2.security.util;

import java.util.Hashtable;

import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerConstants;

/**
 * 
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class WSHandlerConstantsMapper {

	private static Hashtable inHandlerConstants = new Hashtable();
	
	private static Hashtable outHandlerConstants = new Hashtable();

	
	
	/**
	 * The parameter names that are shared across the two handlers are mapped in this situation
	 */
	static {
		//Mapping the in handler constants
		inHandlerConstants.put(WSHandlerConstants.ACTION, WSSHandlerConstants.In.ACTION);
		inHandlerConstants.put(WSHandlerConstants.PW_CALLBACK_CLASS, WSSHandlerConstants.In.PW_CALLBACK_CLASS);
		inHandlerConstants.put(WSHandlerConstants.SIG_PROP_FILE, WSSHandlerConstants.In.SIG_PROP_FILE);
		inHandlerConstants.put(WSHandlerConstants.SIG_KEY_ID, WSSHandlerConstants.In.SIG_KEY_ID);
		
		//Mapping the out handler constants
		outHandlerConstants.put(WSHandlerConstants.ACTION, WSSHandlerConstants.Out.ACTION);
		outHandlerConstants.put(WSHandlerConstants.PW_CALLBACK_CLASS, WSSHandlerConstants.Out.PW_CALLBACK_CLASS);
		outHandlerConstants.put(WSHandlerConstants.SIG_PROP_FILE, WSSHandlerConstants.Out.SIG_PROP_FILE);
		outHandlerConstants.put(WSHandlerConstants.SIG_KEY_ID, WSSHandlerConstants.Out.SIG_KEY_ID);
		
	}
	
	/**
	 * If the mapping is there then the mapped value will be returned
	 * Otherwise the original value will be returned since no mapping was required
	 * @param axiskey
	 * @return
	 */
	public static String getMapping(String axiskey, boolean inHandler, int repetition) {
		String newKey = null;
		if(inHandler) {
			newKey = (String)inHandlerConstants.get(axiskey);
		} else {
			newKey = (String)outHandlerConstants.get(axiskey);	
		}
		if(repetition > 0 && axiskey != WSSHandlerConstants.Out.SENDER_REPEAT_COUNT && !inHandler) {
			if(newKey == null) {
				return axiskey + repetition;
			} else {
				return newKey + repetition;
			}
		}
		return (newKey == null)?axiskey:newKey;
	}
	
}
