package org.apache.axis.om.storage.column;

import org.apache.axis.om.storage.OMStorageException;

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
 * Date: Sep 27, 2004
 * Time: 5:15:08 PM
 * 
 */
public class BooleanColumn extends AbstractColumn{

    private boolean[] values = new boolean[chunkSize];

   /**
    * Append a boolean value to this column
    * @param value
    */
    public void appendValue(boolean value){
        int valueLength = values.length;
        if (length==valueLength){
            //make a new array and put all the contents of the older array in that
            boolean[] newArray  = new boolean[valueLength + chunkSize];
            System.arraycopy(values,0,newArray,0,valueLength);
            values = newArray;
        }

        values[length++] = value;


    }

   /**
    * return a boolean given the index
    * @param index
    * @return
    */
    public boolean getValue(int index){
        if (index>length)
            throw new OMStorageException();

        return values[index];

    }

    /**
     * set the value at a given location
     * @param index
     * @param value
     */
    public void setValue(int index,boolean value){
        if (index>length)
            throw new OMStorageException();
        values[index] = value ;

    }
}
