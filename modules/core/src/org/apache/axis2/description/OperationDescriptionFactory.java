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

/**
 * Author: Deepal Jayasinghe
 * Date: Oct 1, 2005
 * Time: 6:41:39 PM
 */
public class OperationDescriptionFactory implements WSDLConstants {

    public static OperationDescription getOperetionDescription(int mepURI) throws AxisFault {
        OperationDescription abOpdesc ;
        switch(mepURI){
            case MEP_CONSTANT_IN_ONLY : {
                abOpdesc = new InOnlyOperationDescription();
                break;
            }
            case MEP_CONSTANT_OUT_ONLY : {
                abOpdesc = new OutOnlyOperationDescription();
                break;
            }
            case MEP_CONSTANT_IN_OUT : {
                abOpdesc = new InOutOperationDescrition();
                break;
            }
            case MEP_CONSTANT_IN_OPTIONAL_OUT : {
                abOpdesc = new InOutOperationDescrition();
                break;
            }
            case MEP_CONSTANT_ROBUST_IN_ONLY : {
                abOpdesc = new InOutOperationDescrition();
                break;
            }
            case MEP_CONSTANT_OUT_IN : {
                abOpdesc = new OutInOperationDescription();
                break;
            }
            case MEP_CONSTANT_OUT_OPTIONAL_IN : {
                abOpdesc = new OutInOperationDescription();
                break;
            }
            case MEP_CONSTANT_ROBUST_OUT_ONLY : {
                abOpdesc = new OutInOperationDescription();
                break;
            }
            default : {
                throw new AxisFault(Messages.getMessage("unSupportedMEP","ID is "+ mepURI));
            }
        }
        return abOpdesc;
    }



    public static OperationDescription getOperetionDescription(String mepURI) throws AxisFault {
        OperationDescription abOpdesc ;
        if(MEP_URI_IN_ONLY.equals(mepURI)){
            abOpdesc = new InOnlyOperationDescription();
        } else if (MEP_URI_OUT_ONLY.equals(mepURI)) {
            abOpdesc = new OutOnlyOperationDescription();
        } else if (MEP_URI_IN_OUT.equals(mepURI)) {
            abOpdesc = new InOutOperationDescrition();
        }else if(MEP_URI_IN_OPTIONAL_OUT.equals(mepURI)){
            abOpdesc = new InOutOperationDescrition();
        } else if(MEP_URI_IN_ONLY.equals(mepURI)) {
            abOpdesc = new InOutOperationDescrition();
        } else if(MEP_URI_OUT_IN.equals(mepURI)) {
            abOpdesc = new OutInOperationDescription();
        } else if (MEP_URI_OUT_OPTIONAL_IN.equals(mepURI)) {
            abOpdesc = new OutInOperationDescription();
        } else if (MEP_URI_ROBUST_OUT_ONLY.equals(mepURI) ) {
            abOpdesc = new OutInOperationDescription();
        }  else  {
            throw new AxisFault(Messages.getMessage("unSupportedMEP","ID is "+ mepURI));
        }
        return abOpdesc;
    }
}
