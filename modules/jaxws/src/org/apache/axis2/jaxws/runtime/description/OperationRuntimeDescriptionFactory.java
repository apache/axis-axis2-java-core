/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.runtime.description;

import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.runtime.description.impl.OperationRuntimeDescriptionBuilder;

public class OperationRuntimeDescriptionFactory {

    /**
     * intentionally private
     */
    private OperationRuntimeDescriptionFactory() {}

    /**
     * Get or create OperationRuntimeDescription
     * @param opDesc
     * @return OperationRuntimeDescription
     */
    public static OperationRuntimeDescription get(OperationDescription opDesc) {
        OperationRuntimeDescription opRTDesc = (OperationRuntimeDescription) opDesc.getOperationRuntimeDesc("JAXWS");
        if (opRTDesc == null) {
            opRTDesc = OperationRuntimeDescriptionBuilder.create(opDesc);
            opDesc.setOperationRuntimeDesc(opRTDesc);
        }
        return opRTDesc;
    }
}
