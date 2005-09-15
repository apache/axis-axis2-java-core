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
package org.apache.axis2.om;

/**
 * Interface OMConstants
 */
public interface OMConstants {


    // OMBuilder constants
    /**
     * Field PUSH_TYPE_BUILDER
     */
    public static final short PUSH_TYPE_BUILDER = 0;

    /**
     * Field PULL_TYPE_BUILDER
     */
    public static final short PULL_TYPE_BUILDER = 1;

    /**
     * Field ARRAY_ITEM_NSURI
     */
    public static final String ARRAY_ITEM_NSURI =
            "http://axis.apache.org/encoding/Arrays";

    /**
     * Field ARRAY_ITEM_LOCALNAME
     */
    public static final String ARRAY_ITEM_LOCALNAME = "item";

    /**
     * Field ARRAY_ITEM_NS_PREFIX
     */
    public static final String ARRAY_ITEM_NS_PREFIX = "arrays";

    /**
     * Field ARRAY_ITEM_QNAME
     */
    public static final String ARRAY_ITEM_QNAME =
            OMConstants.ARRAY_ITEM_NS_PREFIX + ':'
            + OMConstants.ARRAY_ITEM_LOCALNAME;

    /**
     * Field DEFAULT_CHAR_SET_ENCODING specifies the default
     * character encoding scheme to be used
     */
    public static final String DEFAULT_CHAR_SET_ENCODING = "utf-8";
    public static final String DEFAULT_XML_VERSION = "1.0";
}
