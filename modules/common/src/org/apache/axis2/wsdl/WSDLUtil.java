package org.apache.axis2.wsdl;
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

/**
 * Some utility methods for the WSDL users
 */
public class WSDLUtil {

    /**
     * returns whether the given mep uri is one of the
     * input meps
     * @param mep
     * @return
     */
    public static boolean isInputPresentForMEP(String mep) {
        return WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_ONLY.equals(mep) ||
                WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(mep) ||
                WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(mep) ||
                WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(mep) ||
                WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(mep) ||
                WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(mep) ||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OPTIONAL_OUT.equals(mep)||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY.equals(mep)||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT.equals(mep)||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_IN.equals(mep)||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_OPTIONAL_IN.equals(mep)||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY.equals(mep);
    }

    /**
     * returns whether the given mep URI is one of the output meps
     * @param MEP
     * @return
     */
    public static boolean isOutputPresentForMEP(String MEP) {
        return WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_ONLY.equals(MEP) ||
                WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP) ||
                WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP) ||
                WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(MEP) ||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT.equals(MEP) ||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_IN.equals(MEP) ||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_ONLY.equals(MEP) ||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP);
    }
}
