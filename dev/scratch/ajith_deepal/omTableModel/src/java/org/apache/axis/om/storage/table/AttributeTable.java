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
 * Time: 5:56:31 PM
 * 
 */
public class AttributeTable extends NodeTable {

    private static final int COLUMN_COUNT=6;

    private IntegerColumn nameSpaceKeyColumn = new IntegerColumn();
    private StringColumn localNameColumn = new StringColumn();
    private StringColumn valueColumn = new StringColumn();

    /**
     *
     * @param localName
     * @param value
     * @param parentElementKey
     * @return
     */
    public int addAttribute(String localName,String value,int parentElementKey){

        int key = pkGenerator.nextVal();

        keyColumn.appendValue(key);
        parentColumn.appendValue(parentElementKey);
        nextSiblingColumn.appendValue(OMConstants.DEFAULT_INT_VALUE);
        nameSpaceKeyColumn.appendValue(OMConstants.DEFAULT_INT_VALUE);
        localNameColumn.appendValue(localName);
        valueColumn.appendValue(value);

        return key;

    }

    /**
     * get the values of a particular row
     * @param key
     * @return
     */
    public String[][] getAttribute(int key){
        if (!isKeyPresent(key)){
            throw new OMStorageException();
        }

        String[][] values = new String[2][COLUMN_COUNT];
        //add the lables and values
        values[0][0] = OMConstants.ID_KEY ;values[1][0] = Integer.toString(keyColumn.getValue(key));
        values[0][1] = OMConstants.PARENT_ID_KEY ;values[1][1] = Integer.toString(parentColumn.getValue(key));
        values[0][2] = OMConstants.NEXT_SIBLING_KEY ;values[1][2] = Integer.toString(nextSiblingColumn.getValue(key));
        values[0][3] = OMConstants.NAME_SPACE_KEY ;values[1][3] = Integer.toString(nameSpaceKeyColumn.getValue(key));
        values[0][4] = OMConstants.VALUE_KEY ;values[1][4] = valueColumn.getValue(key);
        values[0][5] = OMConstants.LOCAL_NAME_KEY ;values[1][5] = localNameColumn.getValue(key);

        return values;
    }

    /**
     *
     * @param key
     * @return
     */
    public int removeAttribute(int key){
        if (isKeyPresent(key)){
            //first figure out the siblings of this attribute
            int nextSiblingKey = nextSiblingColumn.getValue(key);
            int previousSibingKey = findPreviousSibling(key);

            //update the value in the previous sibling if it is available
            if (previousSibingKey!=OMConstants.DEFAULT_INT_VALUE){
                nextSiblingColumn.setValue(previousSibingKey,nextSiblingKey);
            }
            //reset the attribute
            reset(key);
        }
        return key;
    }




    private int findPreviousSibling(int key){
        int returnVal=OMConstants.DEFAULT_INT_VALUE;
        for (int i=0;i<this.getSize();i++){
            if (key==nextSiblingColumn.getValue(i)){
                returnVal=i;
                break;
            }
        }
        return returnVal;
    }

    public void updateNextSibling(int key,int nextSiblingKey){
        nextSiblingColumn.setValue(key,nextSiblingKey);
    }

    public void updateNameSpace(int key,int namespaceKey){
        nameSpaceKeyColumn.setValue(key,namespaceKey);
    }

    public void dumpValues(){
        System.out.println("content of "+this);
        System.out.println("key" +
                " -  " + "parent"+
                " -  " + "next sib"+
                " -  " + "ns"+
                " -  " + "value"+
                " -  " +"name"

        );
        for (int i = 0; i < this.getSize(); i++) {

            System.out.println(keyColumn.getValue(i) +
                    " -  " + parentColumn.getValue(i)+
                    " -  " + nextSiblingColumn.getValue(i)+
                    " -  " + nameSpaceKeyColumn.getValue(i)+
                    " -  " + valueColumn.getValue(i)+
                    " -  " + localNameColumn.getValue(i)

            );


        }
    }

}
