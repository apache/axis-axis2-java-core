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
 */

package org.apache.axis.schema;

import org.apache.axis.core.Constants;

import javax.xml.namespace.QName;

/**
 * 2001 Schema characteristics.
 *
 * @author Glen Daniels (gdaniels@apache.org)
 */
public class SchemaVersion2001 implements SchemaVersion {
    public static QName QNAME_NIL = new QName(Constants.URI_2001_SCHEMA_XSI,
                                              "nil");

    /**
     * Package-access constructor - access this through SchemaVersion
     * constants.
     */
    SchemaVersion2001() {
    }

    /**
     * Get the appropriate QName for the "null"/"nil" attribute for this
     * Schema version.
     * @return {http://www.w3.org/2001/XMLSchema-instance}nil
     */
    public QName getNilQName() {
        return QNAME_NIL;
    }

    /**
     * The XSI URI
     * @return the XSI URI
     */
    public String getXsiURI() {
        return Constants.URI_2001_SCHEMA_XSI;
    }

    /**
     * The XSD URI
     * @return the XSD URI
     */
    public String getXsdURI() {
        return Constants.URI_2001_SCHEMA_XSD;
    }
}
