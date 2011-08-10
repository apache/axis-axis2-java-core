/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * Validate the WebServiceExeptionLogger
 */
public class WebServiceExceptionLoggerTests extends TestCase {
    
    /**
     * Validate that if we pass in an exception without any stack information, it does not cause an exception.
     */
    public void testNoStack() {
        
        ServiceClass serviceClass = new ServiceClass();
        Method method = null;
        try {
            method = ImplClass.class.getDeclaredMethod("implMethod");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test implementation error: unable to get test class method due to exception: " + e.toString());
        }
        Exception rootCause = new EmptyStackTraceException();
        InvocationTargetException invTargetExc = new InvocationTargetException(rootCause);
        WebServiceExceptionLogger.log(method, 
                invTargetExc,
                false,
                ImplClass.class,
                serviceClass,
                null);
    }
    
    /**
     * Validate that a exception that is not of type InvocationTargetException does not cause any issues in the logger.
     */
    public void testNonInvocationTargetException() {
        
        ServiceClass serviceClass = new ServiceClass();
        Method method = null;
        try {
            method = ImplClass.class.getDeclaredMethod("implMethod");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test implementation error: unable to get test class method due to exception: " + e.toString());
        }
        Exception rootCause = new NonemptyStackTraceException();
        WebServiceExceptionLogger.log(method, 
                rootCause,
                false,
                ImplClass.class,
                serviceClass,
                null);
    }
    
    /**
     * Validate the stackToString method works if there is no stack returned by the exception passed in.  It is necessary
     * to validate this method directly since the WebServcieExceptionLogger.log method will catch all exceptions so it
     * will never fail.
     */
    public void testStackToStrig_NoStack() {
        String returnedStack = WebServiceExceptionLogger.stackToString(new EmptyStackTraceException());
        // If we don't get an exception, then this test passes.
    }
    
    /**
     * Validate that the returned stack skips over the original throwable's name.
     */
    public void testStackToString() {
        Exception nonEmpty = new NonemptyStackTraceException();
        String returnedStack = WebServiceExceptionLogger.stackToString(nonEmpty);
        assertFalse("Returned stack did not skip the original throwable", returnedStack.contains("NonemptyStackTraceException"));
    }
    
    class ImplClass {
        
        public String implMethod() { return "foo"; }
        
    }
    
    class ServiceClass {
        
    }
    
    class EmptyStackTraceException extends Exception {
        public void printStackTrace(PrintWriter err) { }
    }
    
    class NonemptyStackTraceException extends Exception {
    }
}
