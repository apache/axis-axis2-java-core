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

//package test;
//
//import junit.framework.TestCase;
//import org.soapinterop.xsd.Document;
//import org.soapinterop.xsd.XDocumentDocument1;
//import org.soapinterop.xsd.Person;
//import org.soapinterop.xsd.XPersonDocument;
//import test.stub1.SoapInteropCompound1PortTypeStub;
//
//public class Round3DocLitInteropTest extends TestCase{
//
//    public void testTest1() throws Exception{
//        SoapInteropCompound1PortTypeStub stub = new SoapInteropCompound1PortTypeStub();
//            Document xDoc = Document.Factory.newInstance();
//            xDoc.setID("123");
//            xDoc.setStringValue("Gayan Asanka");
//            XDocumentDocument1 doc = XDocumentDocument1.Factory.newInstance();
//            doc.setXDocument(xDoc);
//            System.out.println( stub.echoDocument(doc));
//
//            Person pers = Person.Factory.newInstance();
//            pers.setName("Gayan Asanka");
//            pers.setAge(28);
//            pers.setID((float)123.456);
//            pers.setMale(true);
//            XPersonDocument xPersDoc = XPersonDocument.Factory.newInstance();
//            xPersDoc.setXPerson(pers);
//            System.out.println(stub.echoPerson(xPersDoc));
//    }
//
//}
