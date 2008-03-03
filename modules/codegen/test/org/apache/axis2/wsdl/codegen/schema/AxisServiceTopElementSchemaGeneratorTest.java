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
package org.apache.axis2.wsdl.codegen.schema;

import junit.framework.TestCase;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.wsdl.codegen.schema.exception.DummySchemaGenerationException;

import javax.xml.namespace.QName;
import java.util.*;


public class AxisServiceTopElementSchemaGeneratorTest extends TestCase {

    public void testSchemaGeneration(){

        AxisServiceTopElementSchemaGenerator schemaGenerator = new AxisServiceTopElementSchemaGenerator(null);

        Set topElements = new HashSet();

        TopElement topElement;

        topElement = new TopElement(new QName("http://test.com","testElement1"));
        topElements.add(topElement);
        topElement = new TopElement(new QName("http://test1.com","testElement2"));
        topElements.add(topElement);
        topElement = new TopElement(new QName("http://test1.com","testElement3"));
        topElement.setTypeQName(new QName(Constants.URI_2001_SCHEMA_XSD,"string"));
        topElements.add(topElement);

        topElement = new TopElement(new QName("http://test1.com","testElement4"));
        topElement.setTypeQName(new QName("http://test1.com","testComplexType1"));
        topElements.add(topElement);

        topElement = new TopElement(new QName("http://test1.com","testElement5"));
        topElement.setTypeQName(new QName("http://test.com","testComplexType2"));
        topElements.add(topElement);

        topElement = new TopElement(new QName("http://test.com","testElement6"));
        topElement.setTypeQName(new QName("http://test2.com","testComplexType2"));
        topElements.add(topElement);


        Map schemaMap = schemaGenerator.getSchemaMap(topElements);
        try {
            List xmlSchemaList = schemaGenerator.getXmlSchemaList(schemaMap);
            org.apache.ws.commons.schema.XmlSchema xmlSchema;
            for (Iterator iter = xmlSchemaList.iterator();iter.hasNext();){
                xmlSchema = (org.apache.ws.commons.schema.XmlSchema) iter.next();
//                xmlSchema.write(System.out);
            }
        } catch (DummySchemaGenerationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

}
