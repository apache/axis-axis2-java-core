package org.apache.axis.om.storage.table;

import org.apache.axis.om.storage.OMStorageException;
import org.apache.axis.om.storage.column.IntegerColumn;
import org.apache.axis.om.util.OMConstants;

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
 * Time: 6:30:37 PM
 * 
 */
public class EventTable extends AbstractTable {

    private IntegerColumn typeColumn = new IntegerColumn();
    private IntegerColumn referenceKeyColumn = new IntegerColumn();
    private static int COLUMN_COUNT=3;

    /**
     *
     * @param type
     * @param referencekey
     */
    public int addEvent(int type, int referencekey){
        int key = pkGenerator.nextVal();

        keyColumn.appendValue(key);
        typeColumn.appendValue(type);
        referenceKeyColumn.appendValue(referencekey);

        return  key;
    }

    /**
     *
     * @return
     */
    public String[][] getEvent(int key){
        if (!isKeyPresent(key)){
            throw new OMStorageException();
        }

        String[][] values = new String[2][COLUMN_COUNT];
        //add the lables and values
        values[0][0] = OMConstants.ID_KEY ;values[1][0] = Integer.toString(keyColumn.getValue(key));
        values[0][1] = OMConstants.TYPE_KEY ;values[1][1] = Integer.toString(typeColumn.getValue(key));
        values[0][2] = OMConstants.REFERENCE_KEY ;values[1][2] = Integer.toString(referenceKeyColumn.getValue(key));

        return values;
    }

    /**
     * get the pull event
     * @param key
     * @return
     */
    public int getPullEvent(int key){
        if (key > this.getSize()){
            throw new OMStorageException();
        }

        return typeColumn.getValue(key);
    }

    /**
     * Debug method
     */

    public void dumpValues(){
        System.out.println("content of "+this);
        System.out.println("key" +
                " -  " + "type"+
                " -  " + "reference")
                ;


        for (int i = 0; i < this.getSize(); i++) {

            System.out.println(keyColumn.getValue(i) +
                    " -  " + typeColumn.getValue(i)+
                    " -  " + referenceKeyColumn.getValue(i)


            );


        }
    }
}
