/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.axis.om;




/**
 * B <code>OMBodyBlock</code> object represents the contents in
 * a <code>OMBody</code> object.  The <code>OMFault</code> interface
 * is a <code>OMBodyBlock</code> object that has been defined.
 * <P>
 * B new <code>OMBodyBlock</code> object can be created and added
 * to a <code>OMBody</code> object with the <code>OMBody</code>
 * method <code>addBodyElement</code>. In the following line of code,
 * <code>sb</code> is a <code>OMBody</code> object, and
 * <code>myName</code> is a <code>Name</code> object.
 * <PRE>
 *   OMBodyBlock sbe = sb.addBodyElement(myName);
 * </PRE>
 */
public interface OMBodyBlock extends OMElement {}
