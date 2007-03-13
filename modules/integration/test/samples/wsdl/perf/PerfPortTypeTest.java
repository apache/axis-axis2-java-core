/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package samples.wsdl.perf;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

public class PerfPortTypeTest extends UtilServerBasedTestCase {

	private static final Log log = LogFactory.getLog(PerfPortTypeTest.class);

    protected static final String SERVICE_REPOSITORY = "target/perf/build/repo";

    private String targetEpr = "http://127.0.0.1:" +
            UtilServer.TESTING_PORT +
            "/axis2/services/PerfPortType.Performance/handleStringArray";

    public PerfPortTypeTest() {
        super();
    }

    public PerfPortTypeTest(String arg0) {
        super(arg0);
    }   

    public static Test suite() {
        return getTestSetup2(new TestSuite(PerfPortTypeTest.class),SERVICE_REPOSITORY);
    }

    /**
     * Auto generated test method
     */
   public void testhandleStringArray() throws java.lang.Exception {
        PerfPortTypePerformanceStub stub = new PerfPortTypePerformanceStub(null, targetEpr);
        //create a new databinder
        stub._getServiceClient().getOptions().setAction("handleStringArray");
        log.info(">>>> Warming up...");
        pump(stub, 1);
        log.info(">>>> Running volume tests...");
        pump(stub, 100);
        pump(stub, 1000);
        pump(stub, 10000);
        pump(stub, 100000);
    }

    private void pump(PerfPortTypePerformanceStub stub, int count) throws Exception {
        InputElementDocument input =
                InputElementDocument.Factory.newInstance();
        String[] s = new String[count];
        for (int i = 0; i < s.length; i++) {
            s[i] = "qwertyuiop?asdfghjkl??zxcvbnm";
        }
        input.addNewInputElement().setItemArray(s);
        Date start = new Date();
        OutputElementDocument output = stub.handleStringArray(input);
        Date end = new Date();

        log.info("##### Count:" + count + " \tTime consumed: " +
                (end.getTime() - start.getTime()) + "\tReturn:" + output.getOutputElement());
    }

    public static void main(String[] args) throws Exception {
        PerfPortTypeTest test = new PerfPortTypeTest();
        test.testhandleStringArray();
    }
}
