/*
 * Copyright  2004 The Apache Software Foundation.
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

/*
 * Created on Sep 25, 2004
 *
 */
package org.apache.axis.om;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dasarath Weeratunge
 *
 */
public class OMAttribute extends OMNamedNode {
	private static String QUOTE_ENTITY= "&quot;";
	private static Matcher matcher= Pattern.compile("\"").matcher(null);

	public OMAttribute(String localName, OMNamespace ns, String value) {
		super(localName, ns, null);
		setValue(value);
	}
	
	public OMAttribute(String localName, OMNamespace ns, String value, OMElement parent) {
		super(localName, ns, parent);
		setValue(value);
	}

	synchronized static String replaceQuoteWithEntity(String value) {
		matcher.reset(value);
		return matcher.replaceAll(QUOTE_ENTITY);
	}

	public void print(PrintStream s) throws OMException {
		super.print(s);
		s.print('=');
		String v= value;
		char quote= '"';
		if (value.indexOf('"') != -1)
			if (value.indexOf('\'') == -1)
				quote= '\'';
			else
				v= replaceQuoteWithEntity(value);
		s.print(quote);
		s.print(v);
		s.print(quote);
	}

	public void detach() throws OMException {
		if (parent == null)
			throw new OMException();
		if (prevSibling == null)
			parent.setFirstAttribute((OMAttribute)nextSibling);
		else
			prevSibling.setNextSibling(nextSibling);
		if (nextSibling != null)
			nextSibling.setPrevSibling(prevSibling);
	}
}
