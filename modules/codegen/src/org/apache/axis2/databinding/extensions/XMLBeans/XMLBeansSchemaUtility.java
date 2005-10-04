package org.apache.axis2.databinding.extensions.XMLBeans;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.extensions.SchemaUtility;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlObject;

import java.lang.reflect.Method;

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
 *
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

public class XMLBeansSchemaUtility implements SchemaUtility {
    protected Log log = LogFactory.getLog(getClass());

    public boolean isRelevant(ServiceDescription serviceDescription) throws AxisFault {
        try {
            ClassLoader classLoader = serviceDescription.getClassLoader();
            String serviceName = serviceDescription.getName().getLocalPart();
            Class serviceImplementation = classLoader.loadClass(serviceName);

            // get each and every method
            Method[] methods = serviceImplementation.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];

                // get the parameters for the method
                Class[] methodParameterTypes = method.getParameterTypes();
                for (int j = 0; j < methodParameterTypes.length; j++) {
                    Class parameter = methodParameterTypes[j];

                    if (isExtendsFromXMLObject(parameter)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            log.error("Can not load the service " + serviceDescription + " from the given class loader");
            throw new AxisFault(e);
        }
    }

    public OMElement getSchema(ServiceDescription serviceDescription) throws AxisFault {
      return null;
    }

    private boolean isExtendsFromXMLObject(Class checkingClass) {
        if (checkingClass == XmlObject.class) {
            return true;
        } else {
            if (checkingClass.getSuperclass() != null) {
                return isExtendsFromXMLObject(checkingClass.getSuperclass());
            } else {
                return false;
            }
        }


    }
}
