package org.apache.axis.om;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *         This is the static factory finder. It searches for the relevant class
 *         Note - It has only package access!!!
 */
class FactoryFinder {

    private static final String defaultClassName = "org.apache.axis.impl.llom.factory.OMLinkedListImplFactory";

    /**
     * This needs to be improved. Currently the factory is loaded only from the default implementation
     * However provisions should be made to load a custom factory depending on the users setting
     * Say an environment variable
     *
     * @param loader
     * @return
     */
    public static OMFactory findFactory(ClassLoader loader) throws OMFactoryException {
        Object factory = null;

        try {
            if (loader == null) {
                factory = Class.forName(defaultClassName).newInstance();
            } else {
                factory = loader.loadClass(defaultClassName).newInstance();
            }

        } catch (Exception e) {
            throw new OMFactoryException(e);
        }

        return (OMFactory) factory;

    }
}
