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
 * Created on Sep 24, 2004
 *
 */
package org.apache.axis.om;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMNodeImpl;

import java.io.PrintStream;

/**
 * @author Dasarath Weeratunge
 *
 */
public class OMText extends OMNodeImpl {
	public OMText(String s){
		super();
		setValue(s);
	}

	public OMText(OMElement parent, String s) {
		super(parent);
		setValue(s);
	}

	public OMNode getFirstChild() throws OMException {
		throw new OMException();
	}

	public void setFirstChild(OMNode node) throws OMException {
		throw new OMException();
	}

	public void print(PrintStream s) throws OMException {
		s.print(value);
	}
}
