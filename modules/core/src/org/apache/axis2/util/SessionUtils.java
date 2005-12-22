package org.apache.axis2.util;

import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;

import java.util.Iterator;
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
* @author : Deepal Jayasinghe (deepal@apache.org)
*
*/

public class SessionUtils {

    public static String calculateMaxScopeForServiceGroup(AxisServiceGroup axisServiceGroup) {
        Iterator servics = axisServiceGroup.getServices();
        int maxScope = 1;
        while (servics.hasNext()) {
            AxisService axisService = (AxisService) servics.next();
            int scopeIntValue = getScopeIntValue(axisService.getScope());
            if (maxScope < scopeIntValue) {
                maxScope = scopeIntValue;
            }
        }
        return getScopeString(maxScope);
    }

    private static int getScopeIntValue(String scope) {
        if (Constants.REQUEST_SCOPE.equals(scope)) {
            return 1;
        } else if (Constants.TRANSPORT_SESSION_SCOPE.equals(scope)) {
            return 2;
        } else if (Constants.SOAP_SESSION_SCOPE.equals(scope)) {
            return 3;
        } else if (Constants.APPLICATION_SCOPE.equals(scope)) {
            return 4;
        } else {
            return 2;
        }
    }

    private static String getScopeString(int scope) {
        switch (scope) {
            case 1 : {
                return Constants.REQUEST_SCOPE;
            }
            case 2 : {
                return Constants.TRANSPORT_SESSION_SCOPE;
            }
            case 3 : {
                return Constants.SOAP_SESSION_SCOPE;
            }
            case 4 : {
                return Constants.APPLICATION_SCOPE;
            }
            default : {
                return Constants.TRANSPORT_SESSION_SCOPE;
            }
        }
    }
}
