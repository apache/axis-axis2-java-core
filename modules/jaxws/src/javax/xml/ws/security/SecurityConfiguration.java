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

package javax.xml.ws.security;

/**
 * Interface SecurityConfiguration
 * The interface SecurityConfiguration abstracts the message security 
 * configuration.
 * 
 * @since JAX-WS 2.0
 * @author sunja07
 */
public interface SecurityConfiguration {
	
	//TODO: This involves generics, needs a thorough revisit.
	/**
	 * Abstract security features. 
	 * 	Integrity 
	 * 		Provide assurance that the data received by a recipient is the 
	 * 		same as the data sent by the originator 
	 * 	Confidentiality
	 * 		Protect data from being read by anyone except the intended 
	 * 		recipient 
	 * 	Authentication 
	 * 		Establish or constrain the identity of the source and/or recipient
	 * 		of a message 
	 */
	public static enum SecurityFeature{ 
		//extends java.lang.Enum <SecurityConfiguration.SecurityFeature> {
		
		CONFIDENTIALITY , INTEGRITY , AUTHENTICATION ;
		
		/**
		 * Method values
		 * Returns an array containing the constants of this enum type, in 
		 * the order they're declared. This method may be used to iterate 
		 * over the constants as follows:
		 * <code>
		 * 		for(SecurityConfiguration.SecurityFeature c 
		 * 			: SecurityConfiguration.SecurityFeature.values())
		 * 			System.out.println(c);
		 * </code>
		 * @return an array containing the constants of this enum type, in 
		 * the order they're declared
		 */
/*		public static final SecurityConfiguration.SecurityFeature[] values() {
			
		}
*/		
		/**
		 * Method valueOf
		 * Returns the enum constant of this type with the specified name. The 
		 * string must match exactly an identifier used to declare an enum 
		 * constant in this type. (Extraneous whitespace characters are not 
		 * permitted.)
		 * @param name - the name of the enum constant to be returned.
		 * @return the enum constant with the specified name 
		 * @throws java.lang.IllegalArgumentException - if this enum type has 
		 * no constant with the specified name
		 */
/*		public static SecurityConfiguration.SecurityFeature valueOf(java.lang.String name) throws java.lang.IllegalArgumentException {
			
		}*/

	}
	
	/**
	 * Method setOutboundConfigId
	 * Sets the configuration for outbound messages. 
	 * 
	 * @param configId - Logical identifier of the configuration entry that 
	 * describes how to fulfil the requested security features.
	 */
	void setOutboundConfigId(java.lang.String configId);
	
	/**
	 * Method getOutboundConfigId
	 * Gets the configuration for outbound messages.
	 * 
	 * @return Logical identifier of the configuration entry that describes 
	 * how to fulfil the requested security features.
	 */
	java.lang.String getOutboundConfigId();
	
	/**
	 * Method setInboundConfigId
	 * Sets the configuration for inbound messages.
	 * 
	 * @param configId - Logical identifier of the configuration entry that 
	 * describes how to fulfil the requested security features.
	 */
	void setInboundConfigId(java.lang.String configId);
	
	/**
	 * Method getInboundConfigId
	 * Gets the configuration for inbound messages.
	 * 
	 * @return Logical identifier of the configuration entry that describes 
	 * how to fulfil the requested security features.
	 */
	java.lang.String getInboundConfigId();
	
	/**
	 * Method setInboundFeatures
	 * Sets the requested security features for inbound messages.
	 * 
	 * @param features - The requested security features.
	 */
	void setInboundFeatures(SecurityConfiguration.SecurityFeature... features);
		
	/**
	 * Method getInbound
	 * Gets the requested security features for inbound messages.
	 * 
	 * @return The requested security features.
	 */
	SecurityConfiguration.SecurityFeature[] getInbound();
	
	/**
	 * Method setOutboundFeatures
	 * Sets the requested security features for outbound messages.
	 * 
	 * @param features - The requested security features.
	 */
	void setOutboundFeatures(SecurityConfiguration.SecurityFeature... features);
	
	/**
	 * Method getOutbound
	 * Gets the requested security features for outbound messages.
	 * 
	 * @return The requested security features.
	 */
	SecurityConfiguration.SecurityFeature[] getOutbound();
	
	/**
	 * Method setCallbackHandler
	 * Sets the JAAS callback handler that may be used to obtain security 
	 * information from the application.
	 * 
	 * @param callbackHandler - The CallbackHandlerinstance to use to 
	 * retrieve security information from the application.
	 */
	void setCallbackHandler(javax.security.auth.callback.CallbackHandler 
			callbackHandler);
	
	/**
	 * Method getCallbackHandler
	 * Gets the JAAS callback handler that may be used to obtain security 
	 * information from the application.
	 * 
	 * @return The CallbackHandlerinstance to use to retrieve security 
	 * information from the application.
	 */
	javax.security.auth.callback.CallbackHandler getCallbackHandler();

}
