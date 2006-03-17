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

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class TrustException extends Exception {

    private static final long serialVersionUID = -445341784514373965L;

    public final static String INVALID_REQUEST = "wst:InvalidRequest";
    public final static String FAILED_AUTHENTICATION = "wst:FailedAuthentication";
    public final static String REQUEST_FAILED = "wst:RequestFailed";
    public final static String INVALID_SECURITY_TOKEN = "wst:InvalidSecurityToken";
    public final static String AUTHENTICATION_BAD_ELEMENTS = "wst:AuthenticationBadElements";
    public final static String BAD_REQUEST = "wst:BadRequest";
    public final static String EXPIRED_DATA = "wst:ExpiredData";
    public final static String INVALID_TIME_RANGE = "wst:InvalidTimeRange";
    public final static String INVALID_SCOPE = "wst:InvalidScope";
    public final static String RENEW_NEEDED = "wst:RenewNeeded";
    public final static String UNABLE_TO_RENEW = "wst:UnableToRenew";
    
    
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
    
    public TrustException(String faultCode, Object[] args) {
        super(getMessage(faultCode, args));
        this.faultCode = faultCode;
        this.faultString = getMessage(faultCode, args);
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
    private static String getMessage(String faultCode, Object[] args) {
        String msg = null;
        try {
            msg = MessageFormat.format(resources.getString(faultCode), args);
        } catch (MissingResourceException e) {
            throw new RuntimeException("Undefined '" + faultCode + "' resource property");
        }
        return msg;
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
