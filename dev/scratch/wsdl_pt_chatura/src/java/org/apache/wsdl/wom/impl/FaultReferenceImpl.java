/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.wsdl.wom.impl;

import org.apache.wsdl.wom.FaultReference;

/**
 * @author chathura@opensource.lk
 *
 */
public class FaultReferenceImpl extends ComponentImpl implements FaultReference   {

	//TODO make it  a QNAME
	private String ref;
	
	//TODO put the value if available ; if not message lable property of the message with same direction of the interface component.
	private String messageLabel;
	
	private String direction;
	
	/**
	 * Returns the direction of the Fault according the MEP
	 * @return
	 */
	public String getDirection() {
		return direction;
	}
	
	/**
	 * Sets the direction of the Fault.
	 * @param direction
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}
	
	public String getMessageLabel() {
		return messageLabel;
	}
	public void setMessageLabel(String messageLabel) {
		this.messageLabel = messageLabel;
	}
	
	/**
	 * Returns the Fault reference.
	 * @return
	 */
	public String getRef() {
		return ref;
	}
	
	/**
	 * Sets the Fault reference.
	 * @param ref
	 */
	public void setRef(String ref) {
		this.ref = ref;
	}
}
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.wsdl.wom.impl;

import org.apache.wsdl.wom.FaultReference;

/**
 * @author chathura@opensource.lk
 *
 */
public class FaultReferenceImpl extends ComponentImpl implements FaultReference   {

	//TODO make it  a QNAME
	private String ref;
	
	//TODO put the value if available ; if not message lable property of the message with same direction of the interface component.
	private String messageLabel;
	
	private String direction;
	
	/**
	 * Returns the direction of the Fault according the MEP
	 * @return
	 */
	public String getDirection() {
		return direction;
	}
	
	/**
	 * Sets the direction of the Fault.
	 * @param direction
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}
	
	public String getMessageLabel() {
		return messageLabel;
	}
	public void setMessageLabel(String messageLabel) {
		this.messageLabel = messageLabel;
	}
	
	/**
	 * Returns the Fault reference.
	 * @return
	 */
	public String getRef() {
		return ref;
	}
	
	/**
	 * Sets the Fault reference.
	 * @param ref
	 */
	public void setRef(String ref) {
		this.ref = ref;
	}
}
