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

package org.apache.axis.core;

import java.lang.reflect.InvocationTargetException;


/**
 * An exception which maps cleanly to a SOAP fault.
 * This is a base class for exceptions which are mapped to faults.
 * SOAP faults contain
 * <ol>
 * <li>A fault string
 * <li>A fault code
 * <li>A fault actor
 * <li>Fault details; an xml tree of fault specific stuff
 * </ol>
 * @author Doug Davis (dug@us.ibm.com)
 * @author James Snell (jasnell@us.ibm.com)
 * @author Steve Loughran
 */

public class AxisFault extends java.rmi.RemoteException {
    /**
     * 
     */
    public AxisFault() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public AxisFault(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     * @param arg1
     */
    public AxisFault(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }
    
    /**
    * Make an AxisFault based on a passed Exception.  If the Exception is
    * already an AxisFault, simply use that.  Otherwise, wrap it in an
    * AxisFault.  If the Exception is an InvocationTargetException (which
    * already wraps another Exception), get the wrapped Exception out from
    * there and use that instead of the passed one.
    *
    * @param e the <code>Exception</code> to build a fault for
    * @return  an <code>AxisFault</code> representing <code>e</code>
    */
   public static AxisFault makeFault(Exception e)
   {
       if (e instanceof InvocationTargetException) {
           Throwable t = ((InvocationTargetException)e).getTargetException();
           if (t instanceof Exception) {
               e = (Exception)t;
           }
       }

       if (e instanceof AxisFault) {
           return (AxisFault)e;
       }

       return new AxisFault(e.getMessage(),e);
   }


}
