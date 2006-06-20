package org.apache.axis2.jaxws;

import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

public class CallbackHandler<T> implements AsyncHandler <T> {

    public void handleResponse(Response response) {
        System.out.println(">> Processing async reponse");
        try{
            T res = (T) response.get();
            
            if(res instanceof String){
                System.out.println("Response [" + res + "]");
            }
            else if(res instanceof SAXSource){
            	
    			SAXSource retVal = (SAXSource)res;
    			StringBuffer buffer = new StringBuffer();
    			byte b;
    			while ((b = (byte) retVal.getInputSource().getByteStream().read()) != -1) {
    				char c = (char) b;
    				buffer.append(c);

    			}
    			System.out.println(">> Response [" + buffer + "]");
            }
            else if(res instanceof StreamSource){
            	StreamSource retVal = (StreamSource) res;

    			byte b;
    			StringBuffer buffer = new StringBuffer();
    			while ((b = (byte) retVal.getInputStream().read()) != -1) {
    				char c = (char) b;
    				buffer.append(c);

    			}
    			System.out.println(">> Response [" + buffer + "]");
            }
            else if(res instanceof DOMSource){
            	DOMSource retVal = (DOMSource) res;

            	StringWriter writer = new StringWriter();
    			Transformer trasformer = TransformerFactory.newInstance().newTransformer();
    			Result result = new StreamResult(writer);
    			trasformer.transform(retVal, result);
    			StringBuffer buffer = writer.getBuffer();
    			System.out.println(">> Response [" + buffer + "]");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
