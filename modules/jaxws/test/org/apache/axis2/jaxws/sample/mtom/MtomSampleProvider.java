package org.apache.axis2.jaxws.sample.mtom;

import org.apache.axis2.jaxws.TestLogger;

import javax.xml.ws.Provider;

public class MtomSampleProvider implements Provider<String> {

    public String invoke(String input) {
        TestLogger.logger.debug(">> input[" + input + "]");
        return input;
    }
}
