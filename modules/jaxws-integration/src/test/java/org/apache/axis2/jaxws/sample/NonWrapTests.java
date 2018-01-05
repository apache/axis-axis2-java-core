/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapPortType;
import org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapService;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.test.sample.nonwrap.ObjectFactory;
import org.test.sample.nonwrap.ReturnType;
import org.test.sample.nonwrap.TwoWay;
import org.test.sample.nonwrap.TwoWayHolder;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

import static org.apache.axis2.jaxws.framework.TestUtils.await;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.concurrent.Future;

public class NonWrapTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    private static String getEndpoint() throws Exception {
        return server.getEndpoint("DocLitNonWrapService.DocLitNonWrapPortTypeImplPort");
    }

    @Test
    public void testTwoWaySync(){
        TestLogger.logger.debug("------------------------------");
        try{
            TwoWay twoWay = new ObjectFactory().createTwoWay();
            twoWay.setTwowayStr("testing sync call for java bean non wrap endpoint");
            DocLitNonWrapService service = new DocLitNonWrapService();
            DocLitNonWrapPortType proxy = service.getDocLitNonWrapPort();

            BindingProvider p =    (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());

            ReturnType returnValue = proxy.twoWay(twoWay);
            TestLogger.logger.debug(returnValue.getReturnStr());
            
            // Repeat to verify behavior
            returnValue = proxy.twoWay(twoWay);
            TestLogger.logger.debug(returnValue.getReturnStr());
            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }

    @Ignore
    @Test
    public void testTwoWaySyncNull() throws Exception{
        TestLogger.logger.debug("------------------------------");
        try{
            TwoWay twoWay = null;  // This should cause an WebServiceException
            DocLitNonWrapService service = new DocLitNonWrapService();
            DocLitNonWrapPortType proxy = service.getDocLitNonWrapPort();
            
            BindingProvider p =    (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());
            
            ReturnType returnValue = proxy.twoWay(twoWay);
            
            // Repeat to verify behavior
            returnValue = proxy.twoWay(twoWay);
            
            // TODO Revisit JAXB validation
            // JAXWS does not make the decision of whether a
            // null parameter can be marshalled.  This decision is
            // delegated to JAXB.  In this case, the schema indicates
            // that this is not a nillable element.  The assumption is
            // that JAXB will fail.  However the current version of 
            // JAXB considers this as 'validation checking' and is not
            // supported.  Thus JAXB will marshal the element without
            // an exception (and unmarshal without exception) even though
            // this is a violation of the schema.
            
            //fail("Expected WebServiceException");
            
        } catch(WebServiceException e){
            TestLogger.logger.debug(e.toString());
        }
    }

    @Test
    public void testTwoWayASyncCallback(){
        TestLogger.logger.debug("------------------------------");
        try{
            TwoWay twoWay = new ObjectFactory().createTwoWay();
            twoWay.setTwowayStr("testing Async call for java bean non wrap endpoint");
            DocLitNonWrapService service = new DocLitNonWrapService();
            DocLitNonWrapPortType proxy = service.getDocLitNonWrapPort();

            BindingProvider p =    (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());

            AsyncCallback callback = new AsyncCallback();
            Future<?> monitor = proxy.twoWayAsync(twoWay, callback);
            assertNotNull(monitor);
            
            // Repeat to verify behavior
            callback = new AsyncCallback();
            monitor = proxy.twoWayAsync(twoWay, callback);
            assertNotNull(monitor);
            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void testTwoWayHolder(){
        TestLogger.logger.debug("------------------------------");
        try{
            TwoWayHolder twh = new TwoWayHolder();
            twh.setTwoWayHolderInt(new Integer(0));
            twh.setTwoWayHolderStr(new String("Request Holder String"));
            Holder<TwoWayHolder> holder = new Holder<TwoWayHolder>(twh);
            TwoWay twoWay = new ObjectFactory().createTwoWay();
            twoWay.setTwowayStr("testing sync call for java bean non wrap endpoint");
            DocLitNonWrapService service = new DocLitNonWrapService();
            DocLitNonWrapPortType proxy = service.getDocLitNonWrapPort();

            BindingProvider p =    (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());

            proxy.twoWayHolder(holder);
            twh = holder.value;
            TestLogger.logger.debug("Holder string =" + twh.getTwoWayHolderStr());
            TestLogger.logger.debug("Holder int =" + twh.getTwoWayHolderInt());
            
            // Repeat to verify behavior
            proxy.twoWayHolder(holder);
            twh = holder.value;
            TestLogger.logger.debug("Holder string =" + twh.getTwoWayHolderStr());
            TestLogger.logger.debug("Holder int =" + twh.getTwoWayHolderInt());

            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void testTwoWayHolderAsync(){
        TestLogger.logger.debug("------------------------------");
        try{
            TwoWayHolder twh = new TwoWayHolder();
            twh.setTwoWayHolderInt(new Integer(0));
            twh.setTwoWayHolderStr(new String("Request Holder String"));
            Holder<TwoWayHolder> holder = new Holder<TwoWayHolder>(twh);
            TwoWay twoWay = new ObjectFactory().createTwoWay();
            twoWay.setTwowayStr("testing sync call for java bean non wrap endpoint");
            DocLitNonWrapService service = new DocLitNonWrapService();
            DocLitNonWrapPortType proxy = service.getDocLitNonWrapPort();

            BindingProvider p =    (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());

            AsyncCallback callback = new AsyncCallback();
            Future<?> monitor =proxy.twoWayHolderAsync(twh, callback);
            await(monitor);
            assertNotNull(monitor);
            
            // Repeat to verify behavior
            callback = new AsyncCallback();
            monitor =proxy.twoWayHolderAsync(twh, callback);
            await(monitor);
            assertNotNull(monitor);

            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }
}
