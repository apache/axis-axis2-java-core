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

package org.apache.rahas;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class TrustException extends Exception {

    private static final long serialVersionUID = -445341784514373965L;

    public final static String INVALID_REQUEST = "InvalidRequest";
    public final static String FAILED_AUTHENTICATION = "FailedAuthentication";
    public final static String REQUEST_FAILED = "RequestFailed";
    public final static String INVALID_SECURITY_TOKEN = "InvalidSecurityToken";
    public final static String AUTHENTICATION_BAD_ELEMENTS = "AuthenticationBadElements";
    public final static String BAD_REQUEST = "BadRequest";
    public final static String EXPIRED_DATA = "ExpiredData";
    public final static String INVALID_TIME_RANGE = "InvalidTimeRange";
    public final static String INVALID_SCOPE = "InvalidScope";
    public final static String RENEW_NEEDED = "RenewNeeded";
    public final static String UNABLE_TO_RENEW = "UnableToRenew";
    
    
    private static ResourceBundle resources;

    private String faultCode;
    private String faultString;
    
    static {
        try {
            resources = ResourceBundle.getBundle("org.apache.rahas.errors");
        } catch (MissingResourceException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public TrustException(String faultCode, Object[] args) {
        super(getMessage(faultCode, args));
        this.faultCode = getFaultCode(faultCode);
        this.faultString = getMessage(faultCode, args);
    }
    
    /**
     * Construct the fault properly code for the standard faults
     * @param faultCode2
     * @return
     */
    private String getFaultCode(String code) {
        if(AUTHENTICATION_BAD_ELEMENTS.equals(code) ||
           BAD_REQUEST.equals(code) ||
           EXPIRED_DATA.equals(code) ||
           FAILED_AUTHENTICATION.equals(code) ||
           INVALID_REQUEST.equals(code) ||
           INVALID_SCOPE.equals(code) ||
           INVALID_SECURITY_TOKEN.equals(code) ||
           INVALID_TIME_RANGE.equals(code) ||
           RENEW_NEEDED.equals(code) ||
           REQUEST_FAILED.equals(code) ||
           UNABLE_TO_RENEW.equals(code)) {
            return RahasConstants.WST_PREFIX + ":" + code;
        } else {
            return code;
        }
    }

    public TrustException(String faultCode) {
        this(faultCode, (Object[])null);
    }
    
    public TrustException(String faultCode, Object[] args, Throwable e) {
        super(getMessage(faultCode, args),e);
        this.faultCode = faultCode;
        this.faultString = getMessage(faultCode, args);
    }
    
    public TrustException(String faultCode, Throwable e) {
        this(faultCode, null, e);
    }

    /**
     * get the message from resource bundle.
     * <p/>
     *
     * @return the message translated from the property (message) file.
     */
    protected static String getMessage(String faultCode, Object[] args) {
        String msg = null;
        try {
            msg = MessageFormat.format(resources.getString(faultCode), args);
        } catch (MissingResourceException e) {
            throw new RuntimeException("Undefined '" + faultCode + "' resource property");
        }
        if(msg != null) {
            return msg;
        } else {
            return faultCode;
        }
    }

    /**
     * @return Returns the faultCode.
     */
    protected String getFaultCode() {
        return faultCode;
    }

    /**
     * @return Returns the faultString.
     */
    protected String getFaultString() {
        return faultString;
    }
    
    
}
