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
 
package org.apache.axis.om.impl.llom.factory;

import org.apache.axis.om.OMFactory;
import org.apache.axis.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis.om.impl.llom.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLStreamReader;

public class OMXMLBuilderFactory {
    public static final String PARSER_XPP = "XPP";
    public static final String PARSER_STAX = "StAX";

    public static final String MODEL_SOAP_SPECIFIC = "SOAP_SPECIFIC";
    public static final String MODEL_OM = "OM_ONLY";

    public static StAXSOAPModelBuilder createStAXSOAPModelBuilder(OMFactory ombuilderFactory, XMLStreamReader parser) {
        return new StAXSOAPModelBuilder(ombuilderFactory, parser);
    }

    public static StAXOMBuilder createStAXOMBuilder(OMFactory ombuilderFactory, XMLStreamReader parser) {
        return new StAXOMBuilder(ombuilderFactory, parser);
    }

}
