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



import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * The SchemaVersion interface allows us to abstract out the differences
 * between the 1999, 2000, and 2001 versions of XML Schema.
 *
 * @author Glen Daniels (gdaniels@apache.org)
 */
public interface SchemaVersion extends Serializable {
    public static SchemaVersion SCHEMA_1999 = new SchemaVersion1999();
    public static SchemaVersion SCHEMA_2000 = new SchemaVersion2000();
    public static SchemaVersion SCHEMA_2001 = new SchemaVersion2001();

    /**
     * Get the appropriate QName for the "null"/"nil" attribute for this
     * Schema version.
     * @return the appropriate "null"/"nil" QName
     */
    public QName getNilQName();

    /**
     * The XSI URI
     * @return the XSI URI
     */
    public String getXsiURI();

    /**
     * The XSD URI
     * @return the XSD URI
     */
    public String getXsdURI();
    
}
