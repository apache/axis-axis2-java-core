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

public class SimpleTypeAnyURIPopulateTest extends AbstractSimplePopulater{
    private String xmlString[] = {
            "<anyURIParam>http://www.wisc.edu/grad/education/mas/229.html</anyURIParam>",
            "<anyURIParam>ftp://grad/education/mas/229.html</anyURIParam>",
            "<anyURIParam>http://mail.google.com/mail/?auth=DQAAAHEAAAC041</anyURIParam>"
    };
    // force others to implement this method
    public void testPopulate() throws Exception {
        process(xmlString[0],"org.soapinterop.anyURIParam");
        process(xmlString[1],"org.soapinterop.anyURIParam");
        process(xmlString[2],"org.soapinterop.anyURIParam");
    }
}
