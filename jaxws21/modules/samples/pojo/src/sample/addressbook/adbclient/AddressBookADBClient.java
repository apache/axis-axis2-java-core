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

import sample.addressbook.stub.AddressBookServiceAddressBookServiceSOAP11Port_httpStub;
import sample.addressbook.stub.AddressBookServiceAddressBookServiceSOAP11Port_httpStub.AddEntry;
import sample.addressbook.stub.AddressBookServiceAddressBookServiceSOAP11Port_httpStub.Entry;
import sample.addressbook.stub.AddressBookServiceAddressBookServiceSOAP11Port_httpStub.FindEntry;
import sample.addressbook.stub.AddressBookServiceAddressBookServiceSOAP11Port_httpStub.FindEntryResponse;


public class AddressBookADBClient {

    private static String URL = "http://127.0.0.1:8080/axis2/services/AddressBookService";

    public static void main(String[] args) {

        try {
            AddressBookServiceAddressBookServiceSOAP11Port_httpStub stub;

            if (args != null && args.length != 0) {
                stub = new AddressBookServiceAddressBookServiceSOAP11Port_httpStub(args[0]);
                
            } else {
                stub = new AddressBookServiceAddressBookServiceSOAP11Port_httpStub(URL);
            }
            
            AddEntry addEntry = new AddEntry();
            Entry entry = new Entry();
            
            entry.setName("Abby Cadabby");
            entry.setStreet("Sesame Street");
            entry.setCity("Sesame City");
            entry.setState("Sesame State");
            entry.setPostalCode("11111");
            
            addEntry.setParam0(entry);
            stub.addEntry(addEntry);
            
            FindEntry findEntry = new FindEntry();
            
            findEntry.setParam0("Abby Cadabby");
            
            FindEntryResponse response = stub.findEntry(findEntry);
            Entry responseEntry = response.get_return();
            
            System.out.println("Name   :" + responseEntry.getName());
            System.out.println("Street :" + responseEntry.getStreet());
            System.out.println("City   :" + responseEntry.getCity());
            System.out.println("State  :" + responseEntry.getState());
            System.out.println("Postal Code :" + responseEntry.getPostalCode());

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

}
