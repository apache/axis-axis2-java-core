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


package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.i18n.Messages;

/**
 * This is the factory for OperationContext.
 */
public class OperationContextFactory implements WSDLConstants {

    /**
     * Creates the operation context. When you call this make sure you set the parent later.
     *
     * @param mepURI
     * @param axisOp
     * @return Returns OperationContext.
     * @throws AxisFault
     */
    public static OperationContext createOperationContext(int mepURI, AxisOperation axisOp)
            throws AxisFault {
        if ((WSDL20_2004Constants.MEP_CONSTANT_IN_OUT == mepURI) || (WSDL20_2004Constants.MEP_CONSTANT_IN_ONLY == mepURI)
                || (WSDL20_2004Constants.MEP_CONSTANT_IN_OPTIONAL_OUT == mepURI)
                || (WSDL20_2004Constants.MEP_CONSTANT_ROBUST_IN_ONLY == mepURI) || (WSDL20_2004Constants.MEP_CONSTANT_OUT_ONLY == mepURI)
                || (WSDL20_2004Constants.MEP_CONSTANT_OUT_IN == mepURI) || (WSDL20_2004Constants.MEP_CONSTANT_OUT_OPTIONAL_IN == mepURI)
                || (WSDL20_2004Constants.MEP_CONSTANT_ROBUST_OUT_ONLY == mepURI)) {
            return new OperationContext(axisOp);
        } else {
            throw new AxisFault(Messages.getMessage("unSupportedMEP", "ID is " + mepURI));
        }
    }

    public static OperationContext createOperationContext(int mepURI, AxisOperation axisOp,
                                                          ServiceContext serviceContext)
            throws AxisFault {
        if ((WSDL20_2004Constants.MEP_CONSTANT_IN_OUT == mepURI) || (WSDL20_2004Constants.MEP_CONSTANT_IN_ONLY == mepURI)
                || (WSDL20_2004Constants.MEP_CONSTANT_IN_OPTIONAL_OUT == mepURI)
                || (WSDL20_2004Constants.MEP_CONSTANT_ROBUST_IN_ONLY == mepURI) || (WSDL20_2004Constants.MEP_CONSTANT_OUT_ONLY == mepURI)
                || (WSDL20_2004Constants.MEP_CONSTANT_OUT_IN == mepURI) || (WSDL20_2004Constants.MEP_CONSTANT_OUT_OPTIONAL_IN == mepURI)
                || (WSDL20_2004Constants.MEP_CONSTANT_ROBUST_OUT_ONLY == mepURI)) {
            return new OperationContext(axisOp, serviceContext);
        } else {
            throw new AxisFault(Messages.getMessage("unSupportedMEP", "ID is " + mepURI));
        }
    }
}
