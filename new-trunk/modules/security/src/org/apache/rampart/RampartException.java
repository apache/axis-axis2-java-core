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

package org.apache.rampart;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class RampartException extends Exception {
    
    private static final long serialVersionUID = 8674795537585339704L;

    private static ResourceBundle resources;

    private String faultCode;
    private String faultString;
    
    static {
        try {
            resources = ResourceBundle.getBundle("org.apache.rampart.errors");
        } catch (MissingResourceException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public RampartException(String faultCode, Object[] args) {
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
        //TODO check for spec specific error codes
        return code;
    }

    public RampartException(String faultCode) {
        this(faultCode, (Object[])null);
    }
    
    public RampartException(String faultCode, Object[] args, Throwable e) {
        super(getMessage(faultCode, args),e);
        this.faultCode = faultCode;
        this.faultString = getMessage(faultCode, args);
    }
    
    public RampartException(String faultCode, Throwable e) {
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
