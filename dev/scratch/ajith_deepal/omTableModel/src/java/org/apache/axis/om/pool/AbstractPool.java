package org.apache.axis.om.pool;

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
 * Time: 10:10:50 PM
 *
 * An abstract pool to make the life easier
 */
public abstract class AbstractPool implements ObjectPool{

    protected int poolSize = 20;

    /**
     * the default implementation is to reset the object
     * while taking it back
     * @param po
     */
    public void putObjectIntoPool(PooledObject po) {
        po.reset();
    }


}
