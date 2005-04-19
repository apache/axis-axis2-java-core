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
    private static final String DEFAULT_CLASS_NAME =
            "org.apache.axis.om.impl.llom.factory.OMLinkedListImplFactory";
    private static final String OM_FACTORY_NAME_PROPERTY = "om.factory";

    /**
     * Returns a factory using the default class loader
     * @see #findFactory(ClassLoader)
     * @return
     * @throws OMFactoryException
     */
    public static OMFactory findFactory()
                throws OMFactoryException {
        return findFactory(null);
    }
    /**
     * The searching for the factory class happens in the following order
     *  1. look for a system property called <b>om.factory</b>. this can be set by passing the
     *     -Dom.factory="classname"
     *  2. Pick the default factory class. it is the class hardcoded at the constant
     *     DEFAULT_CLASS_NAME
     *
     * @param loader
     * @return
     * @throws OMFactoryException
     */


    public static OMFactory findFactory(ClassLoader loader)
            throws OMFactoryException {

        String factoryClassName = DEFAULT_CLASS_NAME;
        //first look for a java system property
       if (System.getProperty(OM_FACTORY_NAME_PROPERTY)!=null){
           factoryClassName = OM_FACTORY_NAME_PROPERTY;
       };

        Object factory = null;
        try {
            if (loader == null) {
                factory = Class.forName(factoryClassName).newInstance();
            } else {
                factory = loader.loadClass(factoryClassName).newInstance();
            }
        } catch (Exception e) {
            throw new OMFactoryException(e);
        }
        return (OMFactory) factory;
    }
}
