package org.apache.axis.om.storage.table;

import org.apache.axis.om.storage.OMStorageException;
import org.apache.axis.om.storage.column.IntegerColumn;
import org.apache.axis.om.storage.column.StringColumn;
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
 * Time: 6:15:05 PM
 * 
 */
public class TextTable extends NodeTable {

    private StringColumn valueColumn = new StringColumn();
    private IntegerColumn typeColumn = new IntegerColumn();
    private IntegerColumn nextsiblingTypeColumn = new IntegerColumn();
    private static int COLUMN_COUNT=6;

    /**
     *
     * @param value
     * @param type
     * @param parentKey
     * @return
     */
    public int addText(String value,int type,int parentKey){
        int key = pkGenerator.nextVal();

        keyColumn.appendValue(key);
        parentColumn.appendValue(parentKey);
        valueColumn.appendValue(value);
        typeColumn.appendValue(type);
        nextSiblingColumn.appendValue(OMConstants.DEFAULT_INT_VALUE);
        nextsiblingTypeColumn.appendValue(OMConstants.DEFAULT_INT_VALUE);

        return key;
    }
    /**
     *  ID
     * PARENT
     * NEXTSIBLING
     * TYPE
     * VALUE
     * NEXT_SIBLING_TYPE
     * @param key
     * @return
     */
    public String[] getRow(int key) {
        if (!isKeyPresent(key)){
            throw new OMStorageException();
        }

        String[] values = new String[COLUMN_COUNT];
        values[OMConstants.ID_INDEX] = Integer.toString(keyColumn.getValue(key));
        values[OMConstants.PARENT_INDEX] = Integer.toString(parentColumn.getValue(key));
        values[OMConstants.NEXT_SIBLING_INDEX] = Integer.toString(nextSiblingColumn.getValue(key));
        values[OMConstants.TEXT_TYPE_INDEX] =Integer.toString(typeColumn.getValue(key));
        values[OMConstants.VALUE_INDEX] =(valueColumn.getValue(key));
        values[OMConstants.TEXT_NEXTSIBLING_TYPE_INDEX] =Integer.toString(nextsiblingTypeColumn.getValue(key));

        return values;
    }


    /**
     *
     * @param key
     * @param nextSiblingkey
     * @param nextSiblingType
     */
    public void updateNextSibling(int key,int nextSiblingkey,int nextSiblingType){
        nextSiblingColumn.setValue(key,nextSiblingkey);
        nextsiblingTypeColumn.setValue(key,nextSiblingType);
    }


    /**
     * Debug method
     */
    public void dumpValues(){
        System.out.println("content of "+this);
        System.out.println("Key" +
                " -  " + "parent"+
                " -  " + "next sib"+
                " -  " +" type"+
                " -  " + "value"+
                " -  " + "next sibling type"

        );
        for (int i = 0; i < this.getSize(); i++) {

            System.out.println(keyColumn.getValue(i) +
                    " -  " + parentColumn.getValue(i)+
                    " -  " + nextSiblingColumn.getValue(i)+
                    " -  " + typeColumn.getValue(i)+
                    " -  " + valueColumn.getValue(i)+
                    " -  " + nextsiblingTypeColumn.getValue(i)

            );


        }
    }
}
