package org.apache.axis.om.util;

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
 * Time: 1:17:22 PM
 *
 * This is a minimum integer stack. I wanted a simpler stack that does not have the
 * overhead of the List. that is why the standard stack is not used. Besides I just want
 * to have ints in my stack!
 */
public class IntegerStack {

    private static int CHUNK_SIZE=10;
    private int[] values = new int[CHUNK_SIZE];
    private int length = 0;

    /**
     * Push the value
     * @param val
     */
    public void push(int val){
        int valueLength = values.length;
        if (length==valueLength){
            //make a new array and put all the contents of the older array in that
            int[] newArray  = new int[valueLength + CHUNK_SIZE];
            System.arraycopy(values,0,newArray,0,valueLength);
            values = newArray;
        }

        values[length++] = val;
    }

    /**
     * pop the value
     * @return
     */
    public int pop(){
        if (length>0){
            return values[--length];
        }else{
            throw new RuntimeException("Stack is Empty!!!");
        }

    }

    /**
     * have alook at the top most value
     * @return
     */
    public int peek(){
        return values[length-1];
    }

    /**
     * Rest the stack
     */
    public void reset(){
        values = new int[CHUNK_SIZE];
        length = 0;
    }

    /**
     * Check whether the stack is empty
     * @return
     */
    public boolean isEmpty(){
        return (length==0);
    }

}
