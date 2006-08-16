package org.apache.axis2.jaxws.sample.addressbook;

import javax.jws.WebService;

@WebService(endpointInterface="org.apache.axis2.jaxws.sample.addressbook.AddressBook")
public class AddressBookImpl implements AddressBook {

    public Boolean addEntry(AddressBookEntry entry) {
        if (entry != null) {
            System.out.println("New AddressBookEntry received");
            System.out.println("[name]  " + entry.getLastName() + ", " + entry.getFirstName());
            System.out.println("[phone] " + entry.getPhone());
            return true;
        }
        else {
            return false;
        }
    }

}
