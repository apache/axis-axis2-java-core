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
package org.apache.axis.om;

/**
 * Class FactoryFinder
 */
class FactoryFinder {
    /**
     * Field DEFAULT_CLASS_NAME
     */
    private static final String DEFAULT_CLASS_NAME =
            "org.apache.axis.om.impl.llom.factory.OMLinkedListImplFactory";

    /**
     * This needs to be improved. Currently the factory is loaded only from the default implementation
     * However provisions should be made to load a custom factory depending on the users setting
     * Say an environment variable
     *
     * @param loader
     * @return
     * @throws OMFactoryException
     */
    public static OMFactory findFactory(ClassLoader loader)
            throws OMFactoryException {
        Object factory = null;
        try {
            if (loader == null) {
                factory = Class.forName(DEFAULT_CLASS_NAME).newInstance();
            } else {
                factory = loader.loadClass(DEFAULT_CLASS_NAME).newInstance();
            }
        } catch (Exception e) {
            throw new OMFactoryException(e);
        }
        return (OMFactory) factory;
    }
}
