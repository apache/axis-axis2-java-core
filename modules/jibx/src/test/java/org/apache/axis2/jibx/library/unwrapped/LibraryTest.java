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

import org.apache.axis2.jibx.beans.Book;
import org.apache.axis2.jibx.library.unwrapped.client.LibraryStub;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

public class LibraryTest {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo/library-unwrapped");
    
    @Test
    public void test1() throws Exception {
        LibraryStub stub = new LibraryStub(server.getConfigurationContext(), server.getEndpoint("library"));
        
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
        LibraryStub stub = new LibraryStub(server.getConfigurationContext(), server.getEndpoint("library"));
        
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
