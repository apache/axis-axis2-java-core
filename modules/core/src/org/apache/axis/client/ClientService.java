package org.apache.axis.client;

import org.apache.axis.om.SOAPEnvelope;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Apr 11, 2005
 * Time: 11:00:53 AM
 */
public class ClientService {

    public static String SERVIC_NAME = "ClientService";

    private HashMap callbackstore;

    public ClientService() {
        callbackstore = new HashMap();
    }

    public void addCallback(String MsgID , Callback callback){
       callbackstore.put(MsgID,callback);
    }

    public void invoke(String MsgID, SOAPEnvelope result){
       Callback callback = (Callback)callbackstore.get(MsgID);
       if(callback != null ){
           callback.onComplete(result);
       }
    }
}
