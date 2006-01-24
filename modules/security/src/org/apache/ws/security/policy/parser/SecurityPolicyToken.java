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
package org.apache.ws.security.policy.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

public class SecurityPolicyToken {

	/**
	 * The following values describe the type of the security token. A complex
	 * token starts a transaction because it contains nested tokens. A simple
	 * token does not contain nested tokens but stands allone an defines a
	 * simple assertion or property.
	 * 
	 * If Content is set then this token contains additional text content, e.g.
	 * XPath expressions.
	 */
	public static final int COMPLEX_TOKEN = 1;

	public static final int SIMPLE_TOKEN = 2;

	public static final int WITH_CONTENT = 100;

	private String tokenName;

	private int tokenType = 0;

	// private boolean supported = false;

	private String[] attributes = null;

	private Object handler = null;

	private Method processTokenMethod = null;

	private ArrayList childTokens = null;

	/**
	 * Create a SecurityPolicyToken data structure.
	 * 
	 * @param token
	 *            The name of the token, equals to to local name of the XML
	 *            element
	 * @param type
	 *            Type of the token. Either complex or simple. Can have a flag
	 *            if the token containes some data.
	 * @param attribs
	 *            The names of allowed attributes on this token
	 * @param h
	 *            The handler object that implements the processing method. The
	 *            name of a processing method is constructed by prepending a
	 *            "do" to the token name
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public SecurityPolicyToken(String token, int type, String[] attribs,
			Object h) throws SecurityException, NoSuchMethodException {
		this(token, type, attribs);

		setProcessTokenMethod(h);
	}

	/**
	 * Create a SecurityPolicyToken data structure.
	 * 
	 * @param token
	 *            The name of the token, equals to to local name of the XML
	 *            element
	 * @param type
	 *            Type of the token. Either complex or simple. Can have a flag
	 *            if the token containes some data.
	 * @param attribs
	 *            The names of allowed attributes on this token processing
	 *            method is constructed by prepending a "do" to the token name
	 */
	public SecurityPolicyToken(String token, int type, String[] attribs) {
		tokenName = token;
		tokenType = type;
		attributes = attribs;

		if (tokenType == COMPLEX_TOKEN) {
			childTokens = new ArrayList();
		}
	}

	/**
	 * @return Returns the attributes.
	 */
	public String[] getAttributes() {
		return attributes;
	}

	/**
	 * Set the method which processes this security token.
	 * 
	 * @param h
	 *            The handler object that implements the processing method. The
	 *            name of a processing method is constructed by prepending a
	 *            "do" to the token name.
	 * 
	 * @throws NoSuchMethodException
	 */
	public void setProcessTokenMethod(Object h) throws NoSuchMethodException {

		if (h == null) {
			return;
		}
		handler = h;
		Class handlerCls = h.getClass();
		Class[] parameters = new Class[] { SecurityProcessorContext.class };

		processTokenMethod = handlerCls.getDeclaredMethod("do" + tokenName,
				parameters);
	}

	/**
	 * Invoke the processing method for this token.
	 * 
	 * @param spc
	 *            The SecurityProcessContext handed over to the processing
	 *            method
	 * @return True if the token is processed successfully
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public boolean invokeProcessTokenMethod(SecurityProcessorContext spc)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {

		if (processTokenMethod == null) {
			return false;
		}
		Object[] parameter = new Object[] { spc };
		Object ret = processTokenMethod.invoke(handler, parameter);
		Boolean bool;
		if (ret instanceof Boolean) {
			bool = (Boolean) ret;
			return bool.booleanValue();
		}
		return false;
	}

	/**
	 * @return Returns the tokenName.
	 */
	public String getTokenName() {
		return tokenName;
	}

	/**
	 * Add a Child token to this complex token.
	 * 
	 * @param spt
	 *            The child token to add to this Complex token
	 */
	public void setChildToken(SecurityPolicyToken spt) {
		childTokens.add(spt);
	}

	/**
	 * Gets a named child token,
	 * 
	 * @param sptName
	 *            The token name to check for
	 * @return the SecurityPolicyToken if this token contains the named token as
	 *         child token, null otherwise.
	 */
	public SecurityPolicyToken getChildToken(String sptName) {
		Iterator it = childTokens.iterator();
		while (it.hasNext()) {
			SecurityPolicyToken tmpSpt = (SecurityPolicyToken) it.next();
			if (sptName.equals(tmpSpt.getTokenName())) {
				return tmpSpt;
			}
		}
		return null;
	}

	/**
	 * Remove a named child token,
	 * 
	 * @param sptName
	 *            The token name to remove
	 */
	public void removeChildToken(String sptName) {
		Iterator it = childTokens.iterator();
		while (it.hasNext()) {
			SecurityPolicyToken tmpSpt = (SecurityPolicyToken) it.next();
			if (sptName.equals(tmpSpt.getTokenName())) {
				childTokens.remove(tmpSpt);
				return;
			}
		}
	}

	/**
	 * Copy this SecurityPolicyToken and return the copy.
	 * 
	 * Produce a copy of this SPT. The imutable fields (token name, token type,
	 * and attributes) are copied by reference. The child tokens are copied by
	 * value thus they can be modified. The handler object and the associated
	 * processing method are not copied and must be intialized.
	 * 
	 * @return A new SecurityPolicyToken
	 */
	public SecurityPolicyToken copy() {
		SecurityPolicyToken spt = new SecurityPolicyToken(tokenName, tokenType,
				attributes);
		if (childTokens != null) {
			Iterator it = childTokens.iterator();
			while (it.hasNext()) {
				SecurityPolicyToken tmpSpt = (SecurityPolicyToken) it.next();
				spt.setChildToken(tmpSpt);
			}
		}
		return spt;
	}
    
    public int getTokenType() {
        return this.tokenType;
    }
}
