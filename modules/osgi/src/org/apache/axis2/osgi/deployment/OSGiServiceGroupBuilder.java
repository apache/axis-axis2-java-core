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
package org.apache.axis2.osgi.deployment;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.ServiceGroupBuilder;
import org.apache.axis2.description.AxisServiceGroup;
import static org.apache.axis2.osgi.deployment.OSGiAxis2Constants.MODULE_NOT_FOUND_ERROR;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @see org.apache.axis2.deployment.ServiceGroupBuilder
 *      OSGiServiceGroupBuilder builds AxisServiceGroup from services.xml found
 *      in a bundle. Builder would only success if the releationship between moduels and services
 *      would meet.
 */
public class OSGiServiceGroupBuilder extends ServiceGroupBuilder {

    public OSGiServiceGroupBuilder(OMElement service, HashMap wsdlServices,
                                   ConfigurationContext configCtx) {
        super(service, wsdlServices, configCtx);
    }

    /**
     * Gets the list of modules that is required to be engaged globally.
     * If the required module is not found this will return the error code 1: which is
     * "Error 1: Required module is not found"
     *
     * @param moduleRefs <code>java.util.Iterator</code>
     * @throws org.apache.axis2.deployment.DeploymentException
     *          <code>DeploymentException</code>
     */
    protected void processModuleRefs(Iterator moduleRefs, AxisServiceGroup axisServiceGroup)
            throws DeploymentException {
        while (moduleRefs.hasNext()) {
            OMElement moduleref = (OMElement) moduleRefs.next();
            OMAttribute moduleRefAttribute = moduleref.getAttribute(new QName(TAG_REFERENCE));

            if (moduleRefAttribute != null) {
                String refName = moduleRefAttribute.getAttributeValue();

                if (axisConfig.getModule(refName) == null) {
                    throw new DeploymentException(MODULE_NOT_FOUND_ERROR);
                } else {
                    axisServiceGroup.addModuleref(refName);
                }
            }
        }
    }
}
