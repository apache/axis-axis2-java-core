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
 * Date: Sep 28, 2004
 * Time: 11:38:23 AM
 */
public class NameSpaceTable extends AbstractTable {

    private StringColumn nameSpaceURIColumn = new StringColumn();
    private StringColumn nameSpacePrefixColumn = new StringColumn();
    // parent means the declared Element. The namespace of the parent
    // may or may not be the namespace in question
    private IntegerColumn parentColumn  = new IntegerColumn();
    private static int COLUMN_COUNT=4;


    /**
     * Adds a namespace
     * @param nameSpaceURI
     * @param nameSpacePrefix
     * @param parent
     * @return
     */
    public int addNamespace(String nameSpaceURI,String nameSpacePrefix,int parent){
        int key = pkGenerator.nextVal();

        keyColumn.appendValue(key);
        parentColumn.appendValue(parent);
        nameSpaceURIColumn.appendValue(nameSpaceURI);
        nameSpacePrefixColumn.appendValue(nameSpacePrefix);

        return key;
    }

    /**
     * ID
     * PARENT
     * URI
     * PREFIX
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
        values[OMConstants.NAMESPACE_URI_INDEX] = nameSpaceURIColumn.getValue(key);
        values[OMConstants.NAMESPACE_PREFIX_INDEX] = nameSpacePrefixColumn.getValue(key);

        return values;
    }


    /**
     * find a namepace
     * @param parent  the parent element of the namespace
     * @param URI
     * @param prefix
     * @return
     */
    public int findNamespace(int parent,String URI,String prefix){
        int size = this.getSize();
        String tempUri;
        String tempPrefix;
        for (int i = 0; i < size; i++) {
            if (parent == parentColumn.getValue(i)){
                tempUri = nameSpaceURIColumn.getValue(i);
                tempPrefix = nameSpacePrefixColumn.getValue(i);
                if (prefix!=null){
                    if (prefix.equals(tempPrefix) && URI.equals(tempUri)){
                        return keyColumn.getValue(i);
                    }
                }else{
                    if (URI.equals(tempUri)){
                        return keyColumn.getValue(i);
                    }
                }
            }

        }

        return OMConstants.DEFAULT_INT_VALUE;
    }

    /**
     * Debug method
     */
    public void dumpValues(){
        System.out.println("content of "+this);
        System.out.println("Key" +
                " -  " + "parent"+
                " -  " + "URI"+
                " -  " +" prefix"

        );
        for (int i = 0; i < this.getSize(); i++) {

            System.out.println(keyColumn.getValue(i) +
                    " -  " + parentColumn.getValue(i) +
                    " -  " + nameSpaceURIColumn.getValue(i) +
                    " -  " + nameSpacePrefixColumn.getValue(i)
            );


        }
    }

}
