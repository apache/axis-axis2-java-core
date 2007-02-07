package org.apache.axis2.jaxws.dispatch;

import java.util.concurrent.ExecutionException;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

public class AsyncCallback<T> implements AsyncHandler<T> {

    private T value;
    private Throwable exception;
    
    public void handleResponse(Response<T> response) {
        try {
            value = response.get();
        } catch (Throwable t) {
            exception = t;
        }
    }
    
    public boolean hasError() {
        return (exception != null);
    }
    
    public Throwable getError() {
        return exception;
    }
    
    public T getValue() {
        return value;
    }
}
