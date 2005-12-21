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

package org.apache.axis2.databinding;

/**
 * A DeserializationTarget is anything that receives a value from a
 * Deserializer.  Typical DeserializationTargets are things like RPC parameters,
 * fields in a JavaBean, and items in an array.
 * 
 * Since the deserialization system deals with multirefs that potentially
 * point to values further forward in the data stream, we always use instances
 * of this interface to set values.  This allows the deserialization framework
 * to store the targets and fill them in later if necessary.
 */
public interface DeserializationTarget {
    void setValue(Object value) throws Exception;
}
