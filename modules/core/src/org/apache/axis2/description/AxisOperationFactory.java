package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.i18n.Messages;
import org.apache.wsdl.WSDLConstants;

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
*
*
*/

public class AxisOperationFactory implements WSDLConstants {
    public static AxisOperation getAxisOperation(int mepURI) throws AxisFault {
        AxisOperation abOpdesc;

        switch (mepURI) {
            case MEP_CONSTANT_IN_ONLY : {
                abOpdesc = new InOnlyAxisOperation();

                break;
            }

            case MEP_CONSTANT_OUT_ONLY : {
                abOpdesc = new OutOnlyAxisOperation();

                break;
            }

            case MEP_CONSTANT_IN_OUT : {
                abOpdesc = new InOutAxisOperation();

                break;
            }

            case MEP_CONSTANT_IN_OPTIONAL_OUT : {
                abOpdesc = new InOutAxisOperation();

                break;
            }

            case MEP_CONSTANT_ROBUST_IN_ONLY : {
                abOpdesc = new InOutAxisOperation();

                break;
            }

            case MEP_CONSTANT_OUT_IN : {
                abOpdesc = new OutInAxisOperation();

                break;
            }

            case MEP_CONSTANT_OUT_OPTIONAL_IN : {
                abOpdesc = new OutInAxisOperation();

                break;
            }

            case MEP_CONSTANT_ROBUST_OUT_ONLY : {
                abOpdesc = new OutInAxisOperation();

                break;
            }

            default : {
                throw new AxisFault(Messages.getMessage("unSupportedMEP", "ID is " + mepURI));
            }
        }

        return abOpdesc;
    }

    public static AxisOperation getOperetionDescription(String mepURI) throws AxisFault {
        AxisOperation abOpdesc;

        if (MEP_URI_IN_ONLY.equals(mepURI)) {
            abOpdesc = new InOnlyAxisOperation();
        } else if (MEP_URI_OUT_ONLY.equals(mepURI)) {
            abOpdesc = new OutOnlyAxisOperation();
        } else if (MEP_URI_IN_OUT.equals(mepURI)) {
            abOpdesc = new InOutAxisOperation();
        } else if (MEP_URI_IN_OPTIONAL_OUT.equals(mepURI)) {
            abOpdesc = new InOutAxisOperation();
        } else if (MEP_URI_IN_ONLY.equals(mepURI)) {
            abOpdesc = new InOutAxisOperation();
        } else if (MEP_URI_OUT_IN.equals(mepURI)) {
            abOpdesc = new OutInAxisOperation();
        } else if (MEP_URI_OUT_OPTIONAL_IN.equals(mepURI)) {
            abOpdesc = new OutInAxisOperation();
        } else if (MEP_URI_ROBUST_OUT_ONLY.equals(mepURI)) {
            abOpdesc = new OutInAxisOperation();
        } else {
            throw new AxisFault(Messages.getMessage("unSupportedMEP", "ID is " + mepURI));
        }

        return abOpdesc;
    }
}
