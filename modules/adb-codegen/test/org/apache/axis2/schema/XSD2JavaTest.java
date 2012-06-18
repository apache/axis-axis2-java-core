/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.schema;

import java.io.File;
import org.junit.Test;

public class XSD2JavaTest extends XMLSchemaTest {

    @Test
    public void testMain() throws Exception {
        File file = null;

        for (int i = 0; i <= 4; i++) {
            file = new File(SampleSchemasDirectory + "sampleSchema" + i + ".xsd");
           
            if (file.exists()) {
                XSD2Java.main(new String[] { SampleSchemasDirectory + "sampleSchema" + i + ".xsd",
                        "target" + File.separator + "generated" + File.separator + "XSD2JAVA" + i });
                File temp = new File("target" + File.separator + "generated" + File.separator
                        + "XSD2JAVA" + i);
                //checks whether XSD2Java executed successfully
                assertTrue(temp.exists());
            }
        }

    }
}
