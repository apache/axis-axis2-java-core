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
package org.apache.axis2.jibx.library.wrapped.service;

import org.apache.axis2.jibx.wrappers.AddBookInstanceRequest;
import org.apache.axis2.jibx.wrappers.AddBookInstanceResponse;
import org.apache.axis2.jibx.wrappers.AddBookRequest;
import org.apache.axis2.jibx.wrappers.AddBookResponse;
import org.apache.axis2.jibx.wrappers.GetBookRequest;
import org.apache.axis2.jibx.wrappers.GetBookResponse;
import org.apache.axis2.jibx.wrappers.GetBooksByTypeRequest;
import org.apache.axis2.jibx.wrappers.GetBooksByTypeResponse;
import org.apache.axis2.jibx.wrappers.GetTypesRequest;
import org.apache.axis2.jibx.wrappers.GetTypesResponse;

public class LibraryImpl implements LibrarySkeletonInterface {
    public GetTypesResponse getTypes(GetTypesRequest getTypes) {
        // TODO Auto-generated method stub
        return null;
    }

    public GetBookResponse getBook(GetBookRequest getBook) {
        // TODO Auto-generated method stub
        return null;
    }

    public AddBookInstanceResponse addBookInstance(AddBookInstanceRequest addBookInstance) {
        // TODO Auto-generated method stub
        return null;
    }

    public GetBooksByTypeResponse getBooksByType(GetBooksByTypeRequest getBooksByType) {
        // TODO Auto-generated method stub
        return null;
    }

    public AddBookResponse addBook(AddBookRequest addBook) {
        return new AddBookResponse();
    }
}
