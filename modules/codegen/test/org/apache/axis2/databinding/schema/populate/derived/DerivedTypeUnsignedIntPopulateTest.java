package org.apache.axis2.databinding.schema.populate.derived;

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

public class DerivedTypeUnsignedIntPopulateTest extends AbstractDerivedPopulater{
    private String xmlString[] = {
            "<DerivedUnsignedInt>1</DerivedUnsignedInt>",
            "<DerivedUnsignedInt>0</DerivedUnsignedInt>",
            "<DerivedUnsignedInt>267582233</DerivedUnsignedInt>",
            "<DerivedUnsignedInt>-267582233</DerivedUnsignedInt>"
    };
    // force others to implement this method
    public void testPopulate() throws Exception {
        process(xmlString[0],"org.soapinterop.DerivedUnsignedInt");
        process(xmlString[1],"org.soapinterop.DerivedUnsignedInt");
        process(xmlString[2],"org.soapinterop.DerivedUnsignedInt");

        try {
            process(xmlString[3],"org.soapinterop.DerivedUnsignedInt");
            fail();
        } catch (Exception e) {

        }
    }
}
