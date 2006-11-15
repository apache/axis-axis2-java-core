package org.apache.axis2.jaxws.sample.mtom;

import javax.xml.ws.Provider;

public class MtomSampleProvider implements Provider<String> {

    public String invoke(String input) {
        System.out.println(">> input[" + input + "]");
        return input;
    }
}
