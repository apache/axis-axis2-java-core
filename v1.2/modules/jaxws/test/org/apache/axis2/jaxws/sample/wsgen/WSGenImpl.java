package org.apache.axis2.jaxws.sample.wsgen;

// SERVER-SIDE CLASS

import javax.jws.WebService;

@WebService(endpointInterface = "org.apache.axis2.jaxws.sample.wsgen.WSGenInterface",
        serviceName="WSGenService",
        portName="WSGenPort",
        targetNamespace="http://wsgen.sample.jaxws.axis2.apache.org"
    )
public class WSGenImpl implements WSGenInterface {

    public WSGenImpl() {
        super();
    }

    public String echoString(String arg0) {
        return arg0;

    }

}
