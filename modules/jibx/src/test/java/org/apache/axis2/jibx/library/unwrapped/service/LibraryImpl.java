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
package org.apache.axis2.jibx.library.unwrapped.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.jibx.beans.Book;
import org.apache.axis2.jibx.beans.Type;

public class LibraryImpl implements LibrarySkeletonInterface {
    private final Map<String,Book> books = new HashMap<String,Book>();
    
    public Type[] getTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    public Book getBook(String isbn) {
        return books.get(isbn);
    }

    public void addBookInstance(Book book) {
        books.put(book.getIsbn(), book);
    }

    public Book[] getBooksByType(String type) {
        List<Book> result = new ArrayList<Book>();
        for (Book book : books.values()) {
            if (book.getType().equals(type)) {
                result.add(book);
            }
        }
        return result.toArray(new Book[result.size()]);
    }

    public boolean addBook(String type, String isbn, String[] authors, String title) {
        books.put(isbn, new Book(type, isbn, title, authors));
        return true;
    }
}
