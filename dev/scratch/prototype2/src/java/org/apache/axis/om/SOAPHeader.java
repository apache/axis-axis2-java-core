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

import java.util.Iterator;


public interface SOAPHeader extends OMElement {



    /**
     * Creates a new <CODE>SOAPHeaderBlock</CODE> object
     * initialized with the specified name and adds it to this
     * <CODE>SOAPHeader</CODE> object.
     * @return the new <CODE>SOAPHeaderBlock</CODE> object that
     *     was inserted into this <CODE>SOAPHeader</CODE>
     *     object
     * @throws  OMException if a SOAP error occurs
     */
    public abstract SOAPHeaderBlock addHeaderBlock(String localName, OMNamespace ns)
        throws OMException ;

    /**
     * Returns a list of all the <CODE>SOAPHeaderBlock</CODE>
     * objects in this <CODE>SOAPHeader</CODE> object that have the
     * the specified actor. An actor is a global attribute that
     * indicates the intermediate parties to whom the message should
     * be sent. An actor receives the message and then sends it to
     * the next actor. The default actor is the ultimate intended
     * recipient for the message, so if no actor attribute is
     * included in a <CODE>SOAPHeader</CODE> object, the message is
     * sent to its ultimate destination.
     * @param   actor  a <CODE>String</CODE> giving the
     *     URI of the actor for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE>
     *     SOAPHeaderBlock</CODE> objects that contain the
     *     specified actor
     * @see #extractHeaderBlocks(java.lang.String) extractHeaderBlocks(java.lang.String)
     */
    public abstract Iterator examineHeaderBlocks(String actor);

    /**
     * Returns a list of all the <CODE>SOAPHeaderBlock</CODE>
     *   objects in this <CODE>SOAPHeader</CODE> object that have
     *   the the specified actor and detaches them from this <CODE>
     *   SOAPHeader</CODE> object.
     *
     *   <P>This method allows an actor to process only the parts of
     *   the <CODE>SOAPHeader</CODE> object that apply to it and to
     *   remove them before passing the message on to the next
     *   actor.
     * @param   actor  a <CODE>String</CODE> giving the
     *     URI of the actor for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE>
     *     SOAPHeaderBlock</CODE> objects that contain the
     *     specified actor
     * @see #examineHeaderBlocks(java.lang.String) examineHeaderBlocks(java.lang.String)
     */
    public abstract Iterator extractHeaderBlocks(String actor);

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderBlock</code> objects in this <code>SOAPHeader</code>
     * object that have the specified actor and that have a MustUnderstand
     * attribute whose value is equivalent to <code>true</code>.
     *
     * @param actor a <code>String</code> giving the URI of the actor for which
     *              to search
     * @return an <code>Iterator</code> object over all the
     *              <code>SOAPHeaderBlock</code> objects that contain the
     *              specified actor and are marked as MustUnderstand
     */
    public abstract Iterator examineMustUnderstandHeaderBlocks(String actor);

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderBlock</code> objects in this <code>SOAPHeader</code>
     * object.
     *
     * @return an <code>Iterator</code> object over all the
     *              <code>SOAPHeaderBlock</code> objects contained by this
     *              <code>SOAPHeader</code>
     */
    public abstract Iterator examineAllHeaderBlocks();

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderBlock</code> objects in this <code>SOAPHeader </code>
     * object and detaches them from this <code>SOAPHeader</code> object.
     *
     * @return an <code>Iterator</code> object over all the
     *              <code>SOAPHeaderBlock</code> objects contained by this
     *              <code>SOAPHeader</code>
     */
    public abstract Iterator extractAllHeaderBlocks();
}
