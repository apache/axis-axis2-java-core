
package org.apache.axis2.jaxws.sample.faults;

import java.util.concurrent.Future;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.Response;
import javax.xml.ws.ResponseWrapper;

import org.test.faults.FaultyWebServiceResponse;

@WebService(name = "FaultyWebServicePortType", targetNamespace = "http://org/test/faults")
public interface FaultyWebServicePortType {


    /**
     * 
     * @param arg1
     * @param arg0
     * @return
     *     returns int
     * @throws FaultyWebServiceFault_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "http://org/test/faults")
    @RequestWrapper(localName = "faultyWebService", targetNamespace = "http://org/test/faults", className = "org.test.faults.FaultyWebService")
    @ResponseWrapper(localName = "faultyWebServiceResponse", targetNamespace = "http://org/test/faults", className = "org.test.faults.FaultyWebServiceResponse")
    public int faultyWebService(
        @WebParam(name = "arg0", targetNamespace = "http://org/test/faults")
        int arg0)
        throws FaultyWebServiceFault_Exception
    ;
    
    /**
     * 
     * @param asyncHandler
     * @param arg0
     * @return
     *     returns java.util.concurrent.Future<? extends java.lang.Object>
     */
    @WebMethod(operationName = "faultyWebService")
    @RequestWrapper(localName = "faultyWebService", targetNamespace = "http://org/test/faults", className = "org.test.faults.FaultyWebService")
    @ResponseWrapper(localName = "faultyWebServiceResponse", targetNamespace = "http://org/test/faults", className = "org.test.faults.FaultyWebServiceResponse")
    public Future<?> faultyWebServiceAsync(
        @WebParam(name = "arg0", targetNamespace = "http://org/test/faults")
        int arg0,
        @WebParam(name = "asyncHandler", targetNamespace = "")
        AsyncHandler<FaultyWebServiceResponse> asyncHandler);

    /**
     * 
     * @param arg0
     * @return
     *     returns javax.xml.ws.Response<org.test.faults.FaultyWebServiceResponse>
     */
    @WebMethod(operationName = "faultyWebService")
    @RequestWrapper(localName = "faultyWebService", targetNamespace = "http://org/test/faults", className = "org.test.faults.FaultyWebService")
    @ResponseWrapper(localName = "faultyWebServiceResponse", targetNamespace = "http://org/test/faults", className = "org.test.faults.FaultyWebServiceResponse")
    public Response<FaultyWebServiceResponse> faultyWebServiceAsync(
        @WebParam(name = "arg0", targetNamespace = "http://org/test/faults")
        int arg0);

}
