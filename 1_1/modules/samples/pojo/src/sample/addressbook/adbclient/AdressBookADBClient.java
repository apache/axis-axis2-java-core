/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package sample.addressbook.adbclient;

import sample.addressbook.stub.AdressBookServiceStub;

public class AdressBookADBClient {

    private static String URL = "http://127.0.0.1:8080/axis2/services/AdressBookService";

    public static void main(String[] args) {

        try {

            if (args != null && args.length != 0) {
                AdressBookServiceStub stub = new AdressBookServiceStub(args[0]);
            } else {
                
            }

        } catch (Exception ex) {

        }
    }

}
