package org.apache.axis.om.storage.table;

import org.apache.axis.om.storage.column.IntegerColumn;

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
 * Time: 5:57:11 PM
 *
 * A simple abstraction to make the life easier with Attribute and Element tables
 */
public abstract class NodeTable extends AbstractTable{

    protected IntegerColumn parentColumn = new IntegerColumn();
    protected IntegerColumn nextSiblingColumn = new IntegerColumn();
}
