package org.apache.axis2.databinding.utils.support;
/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A replacement for the javax.xml.rpc.holders;
 */
public class BooleanHolder {
      /** The <code>boolean</code> contained by this holder. */
    public boolean value;

    /**
     * Make a new <code>BooleanHolder</code> with a <code>null</code> value.
     */
    public BooleanHolder() {}

    /**
     * Make a new <code>BooleanHolder</code> with <code>value</code> as
     * the value.
     *
     * @param value  the <code>boolean</code> to hold
     */
    public BooleanHolder(boolean value) {
        this.value = value;
    }



}
