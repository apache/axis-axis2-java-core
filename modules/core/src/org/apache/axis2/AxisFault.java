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

package org.apache.axis2;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

/**
 * An exception which maps cleanly to a SOAP fault.
 * This is a base class for exceptions which are mapped to faults.
 * SOAP faults contain
 * <ol>
 * <li>A fault string
 * <li>A fault code
 * <li>A fault actor
 * <li>Fault details; an xml tree of fault specific elements
 * </ol>
 */

public class AxisFault extends RemoteException {
    private String soapFaultCode;

    public AxisFault(Throwable arg1) {
        super(arg1.getMessage(), arg1);
    }

    /**
     * @param arg0
     */
    public AxisFault(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public AxisFault(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Make an AxisFault based on a passed Exception.  If the Exception is
     * already an AxisFault, simply use that.  Otherwise, wrap it in an
     * AxisFault.  If the Exception is an InvocationTargetException (which
     * already wraps another Exception), get the wrapped Exception out from
     * there and use that instead of the passed one.
     *
     * @param e the <code>Exception</code> to build a fault for
     * @return an <code>AxisFault</code> representing <code>e</code>
     */
    public static AxisFault makeFault(Exception e) {
        if (e instanceof InvocationTargetException) {
            Throwable t = ((InvocationTargetException) e).getTargetException();
            if (t instanceof Exception) {
                e = (Exception) t;
            }
        }
        if (e instanceof AxisFault) {
            return (AxisFault) e;
        }
        return new AxisFault(e.getMessage(), e);
    }

     /**
     *
     * @param messageText - this will appear as the Text in the Reason information item of SOAP Fault
     * @param faultCode - this will appear as the Value in the Code information item of SOAP Fault
     * @param cause - this will appear under the Detail information item of SOAP Fault
     */
    public AxisFault(String messageText, String faultCode, Throwable cause) {
        super(messageText, cause);
        this.soapFaultCode = faultCode;
    }

    /**
     *
     * @param messageText - this will appear as the Text in the Reason information item of SOAP Fault
     * @param faultCode - this will appear as the Value in the Code information item of SOAP Fault
     */
    public AxisFault(String messageText, String faultCode) {
        super(messageText);
        this.soapFaultCode = faultCode;
    }

    public String getFaultCode() {
        return soapFaultCode;
    }

    public void setFaultCode(String soapFaultCode) {
        this.soapFaultCode = soapFaultCode;
    }
}
