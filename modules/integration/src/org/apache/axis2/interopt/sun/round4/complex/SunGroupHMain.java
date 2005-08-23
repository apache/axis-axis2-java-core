package org.apache.axis2.interopt.sun.round4.complex;


/**
 * Created by IntelliJ IDEA.
 * User: Nadana
 * Date: Aug 6, 2005
 * Time: 2:39:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class SunGroupHMain {

    public static void main(String[] args) {
        //echoSoapStructFault

//        EchoSOAPStructFaultClientUtil echoSoapStructFaultUtil = new EchoSOAPStructFaultClientUtil();
//        EchoBlockingClient.sendMsg(echoSoapStructFaultUtil, "echoSOAPStructFault", "/interopt/sun/round4/complex/resSoapStructFault.xml");

    //    echoBaseSoapStructFault

        EchoBaseStructFaultClientutil echoBaseSoapFaultUtil = new EchoBaseStructFaultClientutil();
        EchoBlockingClient.sendMsg(echoBaseSoapFaultUtil, "echoBaseStructFault", "/interopt/sun/round4/complex/resBaseStrutFault.xml");

//        //echoExtendedStructFault
//        EchoExtendedStructFaultClientUtil echoExtendedStructFault = new EchoExtendedStructFaultClientUtil();
//        EchoBlockingClient.sendMsg(echoExtendedStructFault, "echoExtendedStructFault", "/interopt/sun/round4/complex/resExtendedStructFault.xml");
//
//        //echomultiplefaults1
//        EchoMultipleFaults1ClientUtil echomultiplefaults1 = new EchoMultipleFaults1ClientUtil();
//        EchoBlockingClient.sendMsg(echomultiplefaults1, "echoMultipleFaults1", "/interopt/sun/round4/complex/resMultipleFaults1.xml");
//
//        //echomultiplefaults 2
//        EchoMultipleFaults2ClientUtil echomultiplefaults2 = new EchoMultipleFaults2ClientUtil();
//        EchoBlockingClient.sendMsg(echomultiplefaults2, "echoMultipleFaults2", "/interopt/sun/round4/complex/resMultipleFaults2.xml");


    }
}
