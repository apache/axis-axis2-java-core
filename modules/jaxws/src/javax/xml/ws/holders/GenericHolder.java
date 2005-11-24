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

package javax.xml.rpc.holders;

/**
 * @author sunja07
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
//This involves 1.5 new feature 'generics', needs a revisit
public final class GenericHolder<T> implements Holder {
	
	/**
	 * Comment for <code>value</code>
	 */
	public T value;
	
	/**
	 * Empty Constructor
	 */
	public GenericHolder() {}
	
	/**
	 * Constructor
	 * Sets the value of <code>value</code> property to the given input 
	 * parameter value
	 * @param v
	 */
	public GenericHolder(T v){}

}
