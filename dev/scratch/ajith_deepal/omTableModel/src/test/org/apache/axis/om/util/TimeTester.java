package org.apache.axis.om.util;

import java.util.Stack;

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
 * Date: Oct 8, 2004
 * Time: 8:55:10 AM
 * 
 */
public class TimeTester {

    Stack stack = new Stack();

    public void enter(){
        stack.push(new Long(System.currentTimeMillis()));
    }

    public void exit(){
        long currenttime = System.currentTimeMillis();
        long prevtime = ((Long)stack.pop()).longValue();

        System.out.println("Time taken = " + (currenttime - prevtime));

    }

}
