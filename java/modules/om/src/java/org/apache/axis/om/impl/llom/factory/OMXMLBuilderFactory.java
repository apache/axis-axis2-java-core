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

/**
 * Class OMXMLBuilderFactory
 */
public class OMXMLBuilderFactory {
    /**
     * Field PARSER_XPP
     */
    public static final String PARSER_XPP = "XPP";

    /**
     * Field PARSER_STAX
     */
    public static final String PARSER_STAX = "StAX";

    /**
     * Field MODEL_SOAP_SPECIFIC
     */
    public static final String MODEL_SOAP_SPECIFIC = "SOAP_SPECIFIC";

    /**
     * Field MODEL_OM
     */
    public static final String MODEL_OM = "OM_ONLY";

    /**
     * Method createStAXSOAPModelBuilder
     *
     * @param ombuilderFactory
     * @param parser
     * @return
     */
    public static StAXSOAPModelBuilder createStAXSOAPModelBuilder(
            OMFactory ombuilderFactory, XMLStreamReader parser) {
        return new StAXSOAPModelBuilder(ombuilderFactory, parser);
    }

    /**
     * Method createStAXOMBuilder
     *
     * @param ombuilderFactory
     * @param parser
     * @return
     */
    public static StAXOMBuilder createStAXOMBuilder(OMFactory ombuilderFactory,
                                                    XMLStreamReader parser) {
        return new StAXOMBuilder(ombuilderFactory, parser);
    }
}
