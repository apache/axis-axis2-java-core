/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.engine.async;

/**
 * <P>This is the code that do the Dispatching from the Mesage Quaue. But 
 * this dispatacher is a convience purpose only and do not cover all the cases. e.g. If we need a Web Service 
 * that put the results in to a Data base and after one or more message recived some event would generate the 
 * response to the incoming Mesages then the data base stand at the palce of the Dispatcher. 
 * 
 * For async case in the server side the engine thinks it is one way and the generation of the second message back
 * is resposnibility of the Service developer. The Dispatcher makes his work easier yet it can not hamdle all the senario's.  
 *  
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public class Dispatcher {

}
