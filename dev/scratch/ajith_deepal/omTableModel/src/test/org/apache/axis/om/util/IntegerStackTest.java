package org.apache.axis.om.util;

import junit.framework.TestCase;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Axis team
 * Date: Sep 28, 2004
 * Time: 1:29:23 PM
 */
public class IntegerStackTest extends TestCase{

    private IntegerStack stack = null;

    protected void setUp() throws Exception {
        super.setUp();
        stack = new IntegerStack();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        stack=null;
    }

    public void testPop(){
        stack.reset();
        stack.push(10);
        stack.push(20);
        stack.push(30);

        assertEquals(stack.pop(),30);
        assertEquals(stack.pop(),20);
        assertEquals(stack.pop(),10);


    }

    public void testPeek(){
            stack.reset();
            stack.push(10);
            stack.push(20);
            stack.push(30);

            assertEquals(stack.peek(),30);
            stack.pop();
            assertEquals(stack.peek(),20);
            stack.pop();
            assertEquals(stack.peek(),10);


        }

    public void tetsIsEmpty(){

        stack.reset();
        assertEquals(stack.isEmpty(),true);

        stack.push(10);
        assertEquals(stack.isEmpty(),false);
    }

    public void testExpansion(){
        stack.reset();
        for (int i = 0; i < 15; i++) {
              stack.push(i+10);
        }
        assertEquals(stack.peek(),24);
    }


}
