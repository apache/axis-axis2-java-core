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
package org.apache.axis2.security.trust;

import org.apache.ws.sandbox.security.trust.TrustConstants;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class TrustException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public static final String INVALID_REQUEST = "InvalidRequest";
	public final static String FAILED_AUTHENTICATION = "FailedAuthentication";
	public final static String REQUEST_FAILED = "RequestFailed";
    public final static String INVALID_SECURITY_TOKEN = "InvalidSecurityToken";
    public final static String AUTHENTICATION_BAD_ELEMENTS = "AuthenticationBadElements";
    public final static String BAD_REQUEST = "BadRequest";
    public final static String EXPIREDDATA = "ExpiredData";
    public final static String INVAILD_TIME_RANGE = "InvaildTimeRange";
    public final static String INVAILD_SCOPE = "InvaildScope";
    public final static String RENEW_NEEDED = "RenewNeeded";
    public final static String UNABLE_TO_RENEW = "UnableToRenew";
	
	public static final String DESC_INCORRECT_CHILD_ELEM = "incorrectChildElement";
	public static final String DESC_EXPECTED_CHILD_ELEM = "expectedChildElement";
	public static final String DESC_CHILD_IN_VALUE_ELEM = "childInValueElement";
	public static final String DESC_TEXT_IN_COMPOSITE_ELEM = "textInCompositeElement";
	public final static String ERROR_IN_CONVERTING_TO_OM = "errorInOMConversion";
	public final static String ERROR_IN_CONVERTING_TO_DOM = "errorInDOMConversion";
	
    private static ResourceBundle resources;

    private String faultCode;
    private String faultString;
    
    static {
        try {
            resources = ResourceBundle.getBundle("org.apache.axis2.security.trust.errors");
        } catch (MissingResourceException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 
     * @param faultCode
     * @param msgId
     * @param args
     * @param exception
     */
    public TrustException(String faultCode, String msgId, Object[] args, Throwable exception) {
        super(getMessage(faultCode, null, null),exception);
        this.faultCode = faultCode;
        this.faultString = resources.getString(faultCode);
    }

    /**
     * 
     * @param faultCode
     * @param msgId
     * @param args
     */
    public TrustException(String faultCode, String msgId, Object[] args) {
        super(getMessage(faultCode, null, null));
        this.faultCode = faultCode;
        this.faultString = resources.getString(faultCode);
    }
    
    /**
     * This can be used to set a custom message in the exception
     * @param faultCode
     * @param msg
     */
    public TrustException(String faultCode, String msg) {
    	super(msg);
    	this.faultCode = faultCode;
    	this.faultString = resources.getString(faultCode);
    }

    /**
     * 
     * @param faultCode
     * @param msgId
     * @param args
     * @return
     */
    private static String getMessage(String faultCode, String msgId, Object[] args) {
        String msg = null;
        try {
            msg = resources.getString(faultCode);
            if (msgId != null) {
                return msg += (" (" + MessageFormat.format(resources.getString(msgId), args) + ")");
            }
        } catch (MissingResourceException e) {
            throw new RuntimeException("Undefined '" + msgId + "' resource property");
        }
        return msg;
    }

    /**
     * 
     * @param message
     */
    public TrustException(String message) {
    	super(message);    	
    }
    
    /**
     * 
     * @param message
     * @param ex
     */
    public TrustException(String message, Throwable ex) {
    	super(message,ex);    	
    }
    
    
    /**
     * Return the fault code
     * @return
     */
	public String getFaultCode() {
		return TrustConstants.WST_PREFIX + faultCode;
	}
	
	/**
	 * Return the fault string
	 * @return
	 */
	public String getFaultString() {
		return faultString;
	}
}
