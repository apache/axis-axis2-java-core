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

package org.apache.axis.wsdl.codegen.emitter;

/**
 * @author chathura@opensource.lk
 *
 */
public interface ClassWriterConstants {
	
	public static final String CLASS_FILE_EXTENSION = ".java";	
	
	public static final String PUBLIC_INTERFACE = "public interface ";
	
	public static final String PUBLIC_CLASS = "public class";
	
	
	public static final String REMOTE_INTERFACE = "java.rmi.Remote";
	
	public static final String REMOTE_EXCEPTION = "java.rmi.RemoteException";
	
	public static final String ABSTRACT_STUB = "org.apache.axis.clientapi.Stub";
	
	public static final String AXIS_OPERATION = "org.apache.axis.description.AxisOperation";
	
	
	public static final String INDENDATION_TAB = "\t";
	
	public static final String INDENDATION_DOUBLE_TAB = "\t\t";
	
	public static final String INDENDATION_SPACE = " ";
	
	
	public static final String STUB_VARIABLE__OPERATION_ARRRAY = "_operations";
	
	public static final String STUB_VARIABLE___OPERATION ="__operation";

}
