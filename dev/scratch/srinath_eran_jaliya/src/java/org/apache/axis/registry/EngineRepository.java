/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.registry;

import org.apache.axis.CommonExecutor;
import org.apache.axis.Handler;
/**
 * The Single place where all the instances of the <code>EngineRegistry</code>
 * are created and managed. This is TODO. 
 */
public interface EngineRepository {
    public String register(Object objectToClone);
    public CommonExecutor getCommonExecutor(String id);
    public Handler getHandler(String id);
}
