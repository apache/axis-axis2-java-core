package org.apache.axis.impl.llom;

import org.apache.axis.om.OMNamespace;

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
 * <p/>
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Oct 6, 2004
 * Time: 11:43:32 AM
 */
public class OMNamespaceImpl implements OMNamespace {
    private String prefix;
    private String uri;
    //private String value;

    /**
     * @param uri
     * @param prefix
     */
    public OMNamespaceImpl(String uri, String prefix) {
        this.uri = uri;
        this.prefix = prefix;
    }


    public boolean equals(String uri, String prefix) {
        return ((prefix == null && this.prefix == null) || (prefix != null && prefix.equals(this.prefix)))
                && uri.equals(uri);
    }


    public String getPrefix() {
        return prefix;
    }

    public String getName() {
        return uri;
    }


}
