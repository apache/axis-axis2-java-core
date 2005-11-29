package org.apache.axis2.databinding.schema.populate.simple;

import junit.framework.TestCase;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
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

public class SimpleTypegMonthPopulateTest  extends AbstractSimplePopulater {
    private String xmlString[] = {
            "<monthParam>--01--</monthParam>",
            "<monthParam>--12--+02:50</monthParam>",
            "<monthParam>--03---03:00</monthParam>"
    };

    /**
     *  test the poplate methos=d
     */
    public void testPopulate() throws Exception{
       process(xmlString[0],"org.soapinterop.monthParam");
       process(xmlString[1],"org.soapinterop.monthParam");
       process(xmlString[2],"org.soapinterop.monthParam");

    }


}
