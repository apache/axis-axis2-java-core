package org.apache.axis.om.storage;

import java.util.ArrayList;

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
 * @author Ajith Ranabahu
 *         Date: Sep 16, 2004
 *         Time: 6:08:12 PM
 */
public class Table {

    protected ArrayList rowList = new ArrayList();
    protected ArrayList keyList = new ArrayList();

    /**
     * @param key
     * @return
     */
    public Row getRowByKey(Object key) {
        //search for the key
        int i = 0;
        for (; i < keyList.size(); i++) {
            if (keyList.get(i).equals(key)) {
                return (Row) rowList.get(i);
            }
        }

        return null;
    }

    /**
     * @param index
     * @return
     */
    public Row getRowByIndex(int index) {
        return (Row) rowList.get(index);
    }

    /**
     * @param row
     */
    public void addRow(Row row) {
        rowList.add(row);
        keyList.add(row.getKey());
    }

    /**
     * @return
     */
    public int getSize() {
        return rowList.size();
    }

    /**
     * @return
     */
    public String toString() {
        String returnStr = "";
        for (int i = 0; i < rowList.size(); i++) {
            Row row = (Row) rowList.get(i);
            returnStr = returnStr + row.toString() + "\n";
        }
        return returnStr;
    }

}
