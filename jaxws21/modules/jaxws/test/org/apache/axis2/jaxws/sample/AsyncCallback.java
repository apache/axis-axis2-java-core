/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import java.util.concurrent.ExecutionException;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.test.sample.nonwrap.ReturnType;
import org.test.sample.nonwrap.TwoWayHolder;
import org.apache.axis2.jaxws.TestLogger;


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
                TestLogger.logger.debug(">>Return String = " + type.getReturnStr());
				return;
			}
			if(obj instanceof TwoWayHolder){
				TwoWayHolder twh = (TwoWayHolder)obj;
                TestLogger.logger.debug("AsyncCallback Holder string =" + twh.getTwoWayHolderStr());
                TestLogger.logger.debug("AsyncCallback Holder int =" + twh.getTwoWayHolderInt());
			}
			
		}catch(ExecutionException e){
			e.printStackTrace();
		}catch(InterruptedException e){
			e.printStackTrace();
		}

	}

}
