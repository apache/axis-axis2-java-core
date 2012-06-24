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
package org.apache.axis2.jibx.library.unwrapped;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jibx.beans.Book;
import org.apache.axis2.jibx.library.unwrapped.client.LibraryStub;
import org.apache.axis2.jibx.library.unwrapped.service.LibraryImpl;
import org.apache.axis2.testutils.UtilServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LibraryTest {
    @BeforeClass
    public static void startServer() throws Exception {
        UtilServer.start(System.getProperty("basedir", ".") + "/target/repo/library-unwrapped");
        AxisConfiguration axisConfiguration = UtilServer.getConfigurationContext().getAxisConfiguration();
        AxisService service = axisConfiguration.getService("library");
        service.getParameter(Constants.SERVICE_CLASS).setValue(LibraryImpl.class.getName());
        service.setScope(Constants.SCOPE_APPLICATION);
    }
    
    @AfterClass
    public static void stopServer() throws Exception {
        UtilServer.stop();
    }
    
    @Test
    public void test1() throws Exception {
        LibraryStub stub = new LibraryStub(UtilServer.getConfigurationContext(), "http://127.0.0.1:" + UtilServer.TESTING_PORT + "/axis2/services/library");
        
        stub.addBook("Paperback", "0618918248", new String[] { "Richard Dawkins" }, "The God Delusion");
        
        Book book = stub.getBook("0618918248");
        assertNotNull(book);
        assertEquals("Paperback", book.getType());
        assertEquals("0618918248", book.getIsbn());
        assertEquals("The God Delusion", book.getTitle());
        String[] authors = book.getAuthors();
        assertEquals(1, authors.length);
        assertEquals("Richard Dawkins", authors[0]);
        
        Book[] books = stub.getBooksByType("Paperback");
        assertEquals(1, books.length);
        assertEquals("0618918248", books[0].getIsbn());
    }
    
    @Test
    public void test2() throws Exception {
        LibraryStub stub = new LibraryStub(UtilServer.getConfigurationContext(), "http://127.0.0.1:" + UtilServer.TESTING_PORT + "/axis2/services/library");
        
        stub.addBookInstance(new Book("Hardcover", "8854401765", "The Voyage of the Beagle", new String[] { "Charles Darwin" }));
        Book book = stub.getBook("8854401765");
        assertNotNull(book);
        assertEquals("Hardcover", book.getType());
        assertEquals("8854401765", book.getIsbn());
        assertEquals("The Voyage of the Beagle", book.getTitle());
        String[] authors = book.getAuthors();
        assertEquals(1, authors.length);
        assertEquals("Charles Darwin", authors[0]);
    }
}
