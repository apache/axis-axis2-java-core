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

package org.apache.axis.engine.registry;

import org.apache.axis.registry.ConcreateFlow;
import org.apache.axis.registry.Flow;

public class MockFlow extends ConcreateFlow implements Flow{
    public MockFlow(String message,int length){
        super();
        for(int i = 0;i<length;i++){
            SpeakingHandler h1 = new SpeakingHandler("Executing "+ i +" inside "+message);
            super.addHandler(h1);
        }
    }

}
