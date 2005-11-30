package org.apache.axis2.databinding.schema.populate.simple;
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

public class SimpleTypegYearMonthPopulateTest extends AbstractSimplePopulater{
    private String xmlString[] = {
            "<yearMonthParam>0001-05</yearMonthParam>",
            "<yearMonthParam>-0234-09</yearMonthParam>",
            "<yearMonthParam>1978-01</yearMonthParam>",
            "<yearMonthParam>1978-01-GMT</yearMonthParam>"

            //todo Need to add the invalid combinations here

    };
    // force others to implement this method
    public void testPopulate() throws Exception {
        process(xmlString[0],"org.soapinterop.yearMonthParam");
        process(xmlString[1],"org.soapinterop.yearMonthParam");
        process(xmlString[2],"org.soapinterop.yearMonthParam");
        
        try {
            process(xmlString[3],"org.soapinterop.yearMonthParam");
            fail();
        } catch (Exception e) {

        }
    }
}
