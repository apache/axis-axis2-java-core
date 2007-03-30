/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.axis2.jibx;

import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;

public class NullBindingFactory implements IBindingFactory {

    private static final String[] EMPTY_STRINGS = new String[0];

    public IMarshallingContext createMarshallingContext() throws JiBXException {
        return new MarshallingContext(EMPTY_STRINGS, EMPTY_STRINGS, EMPTY_STRINGS, this);
    }

    public IUnmarshallingContext createUnmarshallingContext()
            throws JiBXException {
        return new UnmarshallingContext(0, EMPTY_STRINGS, EMPTY_STRINGS, EMPTY_STRINGS,
                                        EMPTY_STRINGS, this);
    }

    public String getCompilerDistribution() {
        // normally only used by BindingDirectory code, so okay to punt
        return "";
    }

    public int getCompilerVersion() {
        // normally only used by BindingDirectory code, so okay to punt
        return 0;
    }

    public String[] getElementNames() {
        return EMPTY_STRINGS;
    }

    public String[] getElementNamespaces() {
        return EMPTY_STRINGS;
    }

    public String[] getMappedClasses() {
        return EMPTY_STRINGS;
    }

    public String[] getNamespaces() {
        return EMPTY_STRINGS;
    }

    public String[] getPrefixes() {
        return EMPTY_STRINGS;
    }

    public int getTypeIndex(String type) {
        return -1;
    }
}