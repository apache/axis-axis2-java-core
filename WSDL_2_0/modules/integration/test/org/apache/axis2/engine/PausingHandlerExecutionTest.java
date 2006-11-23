/*
* Copyright 2006 The Apache Software Foundation.
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

package org.apache.axis2.engine;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

public class PausingHandlerExecutionTest extends UtilServerBasedTestCase implements TestConstants
{
  private static boolean initDone = false;
  private static ArrayList testResults;
  private AxisService testService;
  private static TestHandler middleGlobalInHandler;
  private TestHandler firstOperationInHandler;
  private TestHandler middleOperationInHandler;
  private TestHandler middleOperationOutHandler;

  public PausingHandlerExecutionTest()
  {
    super(PausingHandlerExecutionTest.class.getName());
  }

  public PausingHandlerExecutionTest(String testName)
  {
    super(testName);
  }

  public static Test suite()
  {
    return getTestSetup(new TestSuite(PausingHandlerExecutionTest.class));
  }

  protected void setUp() throws Exception
  {
    testResults = new ArrayList();

    if (!initDone)
    {
      initDone = true;
      ArrayList globalInPhases = UtilServer.getConfigurationContext().getAxisConfiguration().getGlobalInFlow();
      for (int i = 0; i < globalInPhases.size(); i++)
      {
        Phase globalInPhase = (Phase)globalInPhases.get(i);
        if (PhaseMetadata.PHASE_PRE_DISPATCH.equals(globalInPhase.getPhaseName()))
        {
          globalInPhase.addHandler(new TestHandler("In1"));
          middleGlobalInHandler = new TestHandler("In2");
          globalInPhase.addHandler(middleGlobalInHandler);
          globalInPhase.addHandler(new TestHandler("In3"));
        }
      }
    }
    
    testService = Utils.createSimpleService(serviceName, Echo.class.getName(),
                                            operationName);
    UtilServer.deployService(testService);
    AxisOperation operation = testService.getOperation(operationName);

    ArrayList operationSpecificPhases = new ArrayList();
    operationSpecificPhases.add(new Phase(
                                          PhaseMetadata.PHASE_POLICY_DETERMINATION));
    operation.setRemainingPhasesInFlow(operationSpecificPhases);
    ArrayList phaseList = operation.getRemainingPhasesInFlow();
    for (int i = 0; i < phaseList.size(); i++)
    {
      Phase operationSpecificPhase = (Phase)phaseList.get(i);
      if (PhaseMetadata.PHASE_POLICY_DETERMINATION.equals(operationSpecificPhase.getPhaseName()))
      {
        firstOperationInHandler = new TestHandler("In4");
        operationSpecificPhase.addHandler(firstOperationInHandler);
        middleOperationInHandler = new TestHandler("In5");
        operationSpecificPhase.addHandler(middleOperationInHandler);
        operationSpecificPhase.addHandler(new TestHandler("In6"));
      }
    }

    operationSpecificPhases = new ArrayList();
    operationSpecificPhases.add(new Phase(
                                          PhaseMetadata.PHASE_POLICY_DETERMINATION));
    operation.setPhasesOutFlow(operationSpecificPhases);
    phaseList = operation.getPhasesOutFlow();
    for (int i = 0; i < phaseList.size(); i++)
    {
      Phase operationSpecificPhase = (Phase)phaseList.get(i);
      if (PhaseMetadata.PHASE_POLICY_DETERMINATION.equals(operationSpecificPhase.getPhaseName()))
      {
        operationSpecificPhase.addHandler(new TestHandler("Out1"));
        middleOperationOutHandler = new TestHandler("Out2");
        operationSpecificPhase.addHandler(middleOperationOutHandler);
        operationSpecificPhase.addHandler(new TestHandler("Out3"));
      }
    }
  }

  protected void tearDown() throws Exception
  {
    UtilServer.unDeployService(serviceName);
    UtilServer.unDeployClientService();
  }

  private ServiceClient createClient() throws Exception
  {
    Options options = new Options();
    options.setTo(targetEPR);
    options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
    options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
    options.setAction(operationName.getLocalPart());
    options.setUseSeparateListener(true);
    
    ConfigurationContext configContext = UtilServer.createClientConfigurationContext();
    
    ServiceClient sender = new ServiceClient(configContext, null);
    sender.setOptions(options);
    sender.engageModule(new QName("addressing"));
    return sender;
  }

  private void executeClient() throws Exception
  {
    OMElement payload = TestingUtils.createDummyOMElement();
    OMElement result = createClient().sendReceive(payload);

    TestingUtils.campareWithCreatedOMElement(result);
  }

  public void testSuccessfulInvocation() throws Exception
  {
    System.out.println("Starting testSuccessfulInvocation");
    middleGlobalInHandler.shouldPause(true);
    executeClient();

    /*Since the response is going back separately, we need to give the server
     *time to unwind the rest of the invocation.*/
    Thread.sleep(5000);

    List expectedExecutionState = Arrays.asList(new String[] {"In1", "In2", "In2", "In3", "In4", "In5", "In6", "Out1", "Out2", "Out3", "FCOut3", "FCOut2", "FCOut1", "FCIn6", "FCIn5", "FCIn4", "FCIn3", "FCIn2", "FCIn1"});
    assertEquals(expectedExecutionState, testResults);
  }
  
  private class TestHandler extends AbstractHandler
  {
    private String handlerName;
    private boolean shouldFail;
    private boolean shouldPause;

    public TestHandler(String handlerName)
    {
      this.handlerName = handlerName;
    }

    public void shouldFail(boolean fail)
    {
      this.shouldFail = fail;
    }

    public void shouldPause(boolean pause)
    {
      System.out.println("Setting shouldPause to "+pause+" for "+handlerName);
      this.shouldPause = pause;
    }

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault
    {
      System.out.println("TestHandler " + handlerName + " invoked");
      if (shouldFail)
      {
        testResults.add("kaboom");
        System.out.println("Handler went kaboom");
        throw new AxisFault("Handler failed");
      }
      testResults.add(handlerName);
      if (shouldPause)
      {
        System.out.println("Handler pausing");
        msgContext.pause();
        shouldPause = false;
        new Worker(msgContext).start();
        return InvocationResponse.SUSPEND;
      }
      return InvocationResponse.CONTINUE;      
    }

    public void flowComplete(MessageContext msgContext)
    {
      System.out.println("TestHandler " + handlerName
          + " called for flowComplete()");
      testResults.add("FC" + handlerName);
    }
  }
  
  private class Worker extends Thread
  {
    private MessageContext msgContext;
    
    public Worker(MessageContext msgContext)
    {
      this.msgContext = msgContext;
    }
    
    public void run()
    {
      try
      {
        System.out.println("Worker thread started");
        Thread.sleep(5000);
        AxisEngine axisEngine = new AxisEngine(msgContext.getConfigurationContext());
        axisEngine.resume(msgContext);
      }
      catch (Exception e)
      {
        e.printStackTrace();
        fail("An error occurred in the worker thread");
      }
    }
  }
}
