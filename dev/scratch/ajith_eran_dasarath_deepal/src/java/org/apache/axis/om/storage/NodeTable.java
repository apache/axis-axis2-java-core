package org.apache.axis.om.storage;

import java.util.ArrayList;

/**
 *  * Copyright 2001-2004 The Apache Software Foundation.
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
 * Date: Sep 19, 2004
 * Time: 4:00:48 PM
 *
 * The difference between a simple table and the node table is the specific
 * search mechanism with the parent
 */
public class NodeTable extends Table {

    /**
     *
     * @param parentKey
     * @return
     */
    public Object[] getRowsbyParent(Object parentKey){

        ArrayList list = new ArrayList();

        for (int i = 0; i < rowList.size(); i++) {
            NodeRow nodeRow = (NodeRow) rowList.get(i);
            if (parentKey==null){
                if (nodeRow.getParent()==parentKey){
                    list.add(nodeRow);
                }
            } else{
                if (nodeRow.getParent().equals(parentKey)){
                    list.add(nodeRow);
                }
            }
        }

        Object[] rowObjects = list.toArray();
        return rowObjects;

    }





}
