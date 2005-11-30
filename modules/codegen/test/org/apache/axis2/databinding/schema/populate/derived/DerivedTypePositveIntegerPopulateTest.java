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

public class DerivedTypePositveIntegerPopulateTest extends AbstractDerivedPopulater{
    private String xmlString[] = {
            "<DerivedPositiveInteger>18443</DerivedPositiveInteger>",
            "<DerivedPositiveInteger>1</DerivedPositiveInteger>",
            "<DerivedPositiveInteger>2633</DerivedPositiveInteger>",
            "<DerivedPositiveInteger>-267582233</DerivedPositiveInteger>",
            "<DerivedPositiveInteger>0</DerivedPositiveInteger>"
    };
    // force others to implement this method
    public void testPopulate() throws Exception {
        process(xmlString[0],"org.soapinterop.DerivedPositiveInteger");
        process(xmlString[1],"org.soapinterop.DerivedPositiveInteger");
        process(xmlString[2],"org.soapinterop.DerivedPositiveInteger");

        try {
            process(xmlString[3],"org.soapinterop.DerivedPositiveInteger");
            fail();
        } catch (Exception e) {}

        try {
            process(xmlString[4],"org.soapinterop.DerivedPositiveInteger");
            fail();
        } catch (Exception e) {}
    }
}
