package sample.addressbook.service;


import java.util.HashMap;

import sample.addressbook.entry.Entry;



public class AddressBookService {
    
    private HashMap entries = new HashMap();

    /**
     * Add an Entry to the Address Book
     * @param entry
     */
    public void addEntry(Entry entry) {
        this.entries.put(entry.getName(), entry);
    }
    
    /**
     * Search an address of a person
     * 
     * @param name the name of the person whose address needs to be found
     * @return return the address entry of the person. 
     */
    public Entry findEntry(String name) {
        return (Entry) this.entries.get(name);
    }
}
