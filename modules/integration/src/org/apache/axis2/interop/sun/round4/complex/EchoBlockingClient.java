package org.apache.axis2.interopt.sun.round4.complex;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.om.OMElement;

import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Nadana
 * Date: Aug 8, 2005
 * Time: 8:32:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class EchoBlockingClient {



     public OMElement sendMsg(SunGroupHClientUtil util,String soapAction){
        OMElement firstchild=null;

        EndpointReference targetEPR = new EndpointReference("http://soapinterop.java.sun.com:80/round4/grouph/complexrpcenc" );
        try {


            Call call = new Call();
            call.setTo(targetEPR);
            call.setExceptionToBeThrownOnSOAPFault(false);
            call.setTransportInfo(Constants.TRANSPORT_HTTP,Constants.TRANSPORT_HTTP,false);
            call.setSoapAction(soapAction);

            //Blocking invocation

            firstchild = call.invokeBlocking("",util.getEchoOMElement());

            StringWriter writer = new StringWriter();

            System.out.println(writer.toString());

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();

        }
        return firstchild;

    }








}
