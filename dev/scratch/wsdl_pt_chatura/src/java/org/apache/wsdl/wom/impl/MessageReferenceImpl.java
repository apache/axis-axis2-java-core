/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wsdl.wom.impl;

import org.apache.wsdl.wom.MessageReference;


/**
 * @author Chathura Herath
 *
 */
public class MessageReferenceImpl  extends ComponentImpl implements MessageReference  {

			
	//Referes to the MEP the Message relates to.
	private String messageLabel;
	
	// Can be "in" or "out" depending on the element name being "input" or "output" respectively; 
	private String Direction;
	
	
	//TODO Do we need it "Message content model"
	
	//TODO can make it a QNAME value is a QName that needs to be resolved by the types element children
	private String element;
	
	
	
	public String getDirection() {
		return Direction;
	}
	public void setDirection(String direction) {
		Direction = direction;
	}
	public String getElement() {
		return element;
	}
	public void setElement(String element) {
		this.element = element;
	}
	public String getMessageLabel() {
		return messageLabel;
	}
	public void setMessageLabel(String messageLabel) {
		this.messageLabel = messageLabel;
	}
}
/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wsdl.wom.impl;

import org.apache.wsdl.wom.MessageReference;


/**
 * @author Chathura Herath
 *
 */
public class MessageReferenceImpl  extends ComponentImpl implements MessageReference  {

			
	//Referes to the MEP the Message relates to.
	private String messageLabel;
	
	// Can be "in" or "out" depending on the element name being "input" or "output" respectively; 
	private String Direction;
	
	
	//TODO Do we need it "Message content model"
	
	//TODO can make it a QNAME value is a QName that needs to be resolved by the types element children
	private String element;
	
	
	
	public String getDirection() {
		return Direction;
	}
	public void setDirection(String direction) {
		Direction = direction;
	}
	public String getElement() {
		return element;
	}
	public void setElement(String element) {
		this.element = element;
	}
	public String getMessageLabel() {
		return messageLabel;
	}
	public void setMessageLabel(String messageLabel) {
		this.messageLabel = messageLabel;
	}
}
