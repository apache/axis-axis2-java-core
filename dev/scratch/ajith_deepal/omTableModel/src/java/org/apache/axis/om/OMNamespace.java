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
 * <p/>
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Oct 4, 2004
 * Time: 1:24:00 PM
 */
public interface OMNamespace extends OMNode{

    /**
     *
     * @param ns
     * @return whether this is equivalent to the
     * given namespace or not
     */
    boolean equals(OMNamespace ns) ;

    /**
     *
     * @param uri
     * @param prefix
     * @return
     */
    boolean equals(String uri, String prefix);

    /**
     *
     * @return a boolean that says whether this is the default
     * namespace or not
     */
    boolean isDefaultNs();
    /**
     *
     * @return the prefix of this namepace
     */
    String getPrefix() ;

    /**
     *
     * @return the uri of this namespace
     */
    String getURI();
}
