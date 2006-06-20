package org.apache.axis2.jaxws.provider;

import javax.xml.ws.Provider;

public class StringProvider implements Provider<String> {

    private static String responseGood = "<provider><message>request processed</message></provider>";
    private static String responseBad  = "<provider><message>ERROR:null request received</message><provider>";
    
    public String invoke(String obj) {
        if (obj != null) {
            String str = (String) obj;
            System.out.println(">> StringProvider received a new request");
            System.out.println(">> request [" + str + "]");
            
            return responseGood;
        }
        System.out.println(">> ERROR:null request received");
        return responseBad;
    }
}
