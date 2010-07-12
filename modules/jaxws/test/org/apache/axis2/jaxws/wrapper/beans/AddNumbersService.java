package org.apache.axis2.jaxws.wrapper.beans;



@javax.jws.WebService (targetNamespace="http://org/test/addnumbers", serviceName="AddNumbersService", portName="AddNumbersPort")
public class AddNumbersService{

    public int addNumbers(int arg0, int arg1) throws AddNumbersException {
    	if(arg0+arg1<0){
			throw new AddNumbersException("sum is less than 0");
		}
        return arg0+arg1;
    }

}