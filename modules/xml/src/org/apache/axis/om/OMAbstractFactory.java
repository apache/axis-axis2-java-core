package org.apache.axis.om;

import org.apache.axis.om.impl.llom.factory.SOAPLinkedListImplFactory;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 */
public class OMAbstractFactory {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */
    /**
     * Constructor OMFactory
     */
    protected OMAbstractFactory() {
    }

    /**
     * This will pick up the default factory implementation from the classpath
     *
     * @return
     */
    public static OMFactory getOMFactory() {
        return FactoryFinder.findOMFactory(null);
    }

    /**
     * If user needs to provide his own factory implementation, here provide the
     * Class Loader for that.
     * @param classLoader
     * @return
     */
    public static OMFactory getOMFactory(ClassLoader classLoader) {
        return FactoryFinder.findOMFactory(classLoader);
    }

    /**
     * This will pick up the default factory implementation from the classpath
     *
     * @return
     */
    public static SOAPFactory getSOAP11Factory() {
        return FactoryFinder.findSOAP11Factory(null);
    }

    /**
     * If user needs to provide his own factory implementation, here provide the
     * Class Loader for that.
     * @param classLoader
     * @return
     */
    public static SOAPFactory getSOAP11Factory(ClassLoader classLoader) {
        return FactoryFinder.findSOAP11Factory(classLoader);
    }

    /**
     * This will pick up the default factory implementation from the classpath
     *
     * @return
     */
    public static SOAPFactory getSOAP12Factory() {
        return FactoryFinder.findSOAP12Factory(null);
    }

    /**
     * If user needs to provide his own factory implementation, here provide the
     * Class Loader for that.
     * @param classLoader
     * @return
     */
    public static SOAPFactory getSOAP12Factory(ClassLoader classLoader) {
        return FactoryFinder.findSOAP12Factory(classLoader);
    }

    public static SOAPFactory getDefaultSOAPFactory() {
        return new SOAPLinkedListImplFactory();
    }
}
