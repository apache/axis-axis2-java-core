package org.apache.axis2.phaseresolver.util;

import org.apache.axis2.phaseresolver.PhaseMetadata;

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
 * Author : Deepal Jayasinghe
 * Date: May 12, 2005
 * Time: 7:49:43 PM
 */
public class PhaseValidator {

    public static int SYSTEM_MODULE = 0;
    public static int SERVICE_MODULE = 1;

    public static boolean isSystemPhases(String phaseName) {
        return ((PhaseMetadata.PHASE_TRANSPORTIN.equals(phaseName)) ||
                (PhaseMetadata.PHASE_DISPATCH.equals(phaseName)) ||
                (PhaseMetadata.PHASE_POST_DISPATCH.equals(phaseName)) ||
                (PhaseMetadata.PHASE_PRE_DISPATCH.equals(phaseName)));
    }
}
