package org.apache.axis.om.storage.table;

import org.apache.axis.om.storage.column.IntegerColumn;
import org.apache.axis.om.util.Sequence;
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
 * Time: 5:21:16 PM
 *
 */
public abstract class AbstractTable implements Table{

    protected IntegerColumn keyColumn = new IntegerColumn();
    protected Sequence pkGenerator = new Sequence();

    /**
     * returns the size of the table
     * Note that this only takes into account the
     * size of the key column and hence even the removed ones
     * will be counted!
     * @return
     */
    public int getSize(){
        return keyColumn.getCurrentSize();
    }

    /**
     * searches for the presence of a given key
     * @param key
     * @return
     */
    public boolean isKeyPresent(int key){
        return ((key != OMConstants.DEFAULT_INT_VALUE) && (key < this.getSize()) && keyColumn.getValue(key) != OMConstants.DEFAULT_INT_VALUE);
    }

    /**
     * implement this seperately for other tables if the need arises to reset ALL the columns
     * this default implementation only erases the key (actually replace it with the default value)
     * If this is to be re written then it would be better to override the isKeyPresnt method as well
     * @param key
     */
    protected void reset(int key){
         this.keyColumn.setValue(key,OMConstants.DEFAULT_INT_VALUE);

    }

}
