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

package org.apache.axis2.wsdl.codegen;

import javax.xml.namespace.QName;
 //todo rename this
public interface XSLTConstants {
    QName BASE_64_CONTENT_QNAME = new QName("http://www.w3.org/2001/XMLSchema", "base64Binary");
    QName XMIME_CONTENT_TYPE_QNAME = new QName("http://www.w3.org/2004/06/xmlmime", "contentType");

    String BASE_64_PROPERTY_KEY = "base64map";

    String EXTERNAL_TEMPLATE_PROPERTY_KEY = "externalTemplate";
    String XSLT_INCLUDE_DATABIND_SUPPORTER_HREF_KEY = "databindsupporter";
    String XSLT_INCLUDE_TEST_OBJECT_HREF_KEY = "testObject"; 


    public interface CodegenStyle {
        public static final int AUTOMATIC = 0;
        public static final int INTERFACE = 1;
        public static final int BINDING = 2;
    }


}
