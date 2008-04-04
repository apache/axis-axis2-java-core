package org.apache.axis2.jaxws.addressbook;

// NOTE: This Service Endpoint Interface (SEI) is NOT CURRENTLY USED in this example

public interface AddressBook {
    
    public void addEntry(String firstName, String lastName, String phone, String street, String city, String state);
    
    public AddressBookEntry findByLastName(String lastName);
}
