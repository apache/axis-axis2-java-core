package org.apache.axis2.jaxws;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import client.EchoStringResponse;

public class JAXBCallbackHandler<T> implements AsyncHandler<T> {

    public void handleResponse(Response response) {
        try {
            EchoStringResponse esr = (EchoStringResponse) response.get();
            System.out.println(">> Async response received: " + esr.getEchoStringReturn());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
