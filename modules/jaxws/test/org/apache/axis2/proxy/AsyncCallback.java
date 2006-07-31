/**
 * 
 */
package org.apache.axis2.proxy;

import java.util.concurrent.ExecutionException;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.test.proxy.doclitwrapped.ReturnType;



/**
 * @author nvthaker
 *
 */
public class AsyncCallback implements AsyncHandler {

	/**
	 * 
	 */
	public AsyncCallback() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see javax.xml.ws.AsyncHandler#handleResponse(javax.xml.ws.Response)
	 */
	public void handleResponse(Response response) {
		try{
			Object obj = response.get();
			if(obj instanceof ReturnType){
				ReturnType type = (ReturnType)obj;
				System.out.println(">>Return String = "+type.getReturnStr());
			}
			else{
				System.out.println("Response.get should have been ReturnType" );
			}
		}catch(ExecutionException e){
			e.printStackTrace();
		}catch(InterruptedException e){
			e.printStackTrace();
		}

	}

}
