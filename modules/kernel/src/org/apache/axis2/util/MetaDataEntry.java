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

package org.apache.axis2.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;



/**
 * An internal class for holding a set of information 
 * about an object.  
 */
public class MetaDataEntry implements Externalizable
{
    // serialization identifier
    private static final long serialVersionUID = 8978361069526299875L;

    // supported revision levels, add a new level to manage compatible changes
    private static final int REVISION_1 = 1;
    // current revision level of this object
    private static final int revisionID = REVISION_1;

    // data to keep on an object

    private String className = null;
    private String qnameAsString = null;
    private String extraName = null;

    // list of MetaDataEntry objects that are owned by the
    // original object referred to by this MetaDataEntry
    private ArrayList list = null;

    // marker to indicate end-of-list
    public static String END_OF_LIST = "LAST_ENTRY";

    /**
     * Simple constructor
     */
    public MetaDataEntry()
    {
    }

    /**
     * Constructor
     */
    public MetaDataEntry(String c, String n)
    {
        className = c;
        qnameAsString = n;
    }

    /**
     * Constructor
     */
    public MetaDataEntry(String c, String n, String e)
    {
        className = c;
        qnameAsString = n;
        extraName = e;
    }

    /**
     * Constructor
     */
    public MetaDataEntry(String c, String n, ArrayList l)
    {
        className = c;
        qnameAsString = n;
        list = l;
    }


    /**
     * Get the class name
     * 
     * @return the class name string
     */
    public String getClassName()
    {
        return className;
    }


    /**
     * Set the class name
     * 
     * @param c      the class name string
     */
    public void setClassName(String c)
    {
        className = c;
    }


    /**
     * Get the QName
     * 
     * @return the QName based on the qnameAsString value
     */
    public QName getQName()
    {
        if (qnameAsString != null)
        {
            return QName.valueOf(qnameAsString);
        }
        else
        {
            return null;
        }
    }


    /**
     * Set the QName
     *
     * @param q      the QName
     */
    public void setQName(QName q)
    {
        if (q != null)
        {
            qnameAsString = q.toString();
        }
        else
        {
            qnameAsString = null;
        }
    }

    /**
     * Set the QName
     *
     * @param n      the QName as a string
     */
    public void setQName(String n)
    {
        qnameAsString = n;
    }


    /**
     * Get the QName as a string
     * 
     * @return the QName as a string
     */
    public String getQNameAsString()
    {
        return qnameAsString;
    }


    /**
     * This is a convenience method.
     * Returns the string that is used as a name.
     * 
     * @return the name 
     */
    public String getName()
    {
        return qnameAsString;
    }


    /**
     * Get the additional name associated with the object
     * 
     * @return the additional name string
     */
    public String getExtraName()
    {
        return extraName;
    }


    /**
     * Set the additional name associated with the object
     * 
     * @param c      the extra name string
     */
    public void setExtraName(String e)
    {
        extraName = e;
    }


    /**
     * Indicates whether the list is empty or not
     * 
     * @return false for a non-empty list, true for an empty list
     */
    public boolean isListEmpty()
    {
        if (list == null)
        {
            return true;
        }

        return list.isEmpty();
    }


    /**
     * Get the list
     * 
     * @return the array list
     */
    public ArrayList getList()
    {
        return list;
    }


    /**
     * Set the list
     * 
     * @param L      the ArrayList of MetaDataEntry objects
     */
    public void setList(ArrayList L)
    {
        list = L;
    }

    /**
     * Add to the list
     * 
     * @param e      the MetaDataEntry object to add to the list
     */
    public void addToList(MetaDataEntry e)
    {
        if (list == null)
        {
            list = new ArrayList();
        }
        list.add(e);
    }

    /**
     * Remove the list
     * 
     */
    public void removeList()
    {
        list = null;
    }




    // message strings 
    private static final String UNSUPPORTED_SUID  = "Serialization version ID is not supported.";
    private static final String UNSUPPORTED_REVID = "Revision ID is not supported.";


    /**
     * Save the contents of this object
     *
     * @param out    The stream to write the object contents to
     * 
     * @exception IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException
    {
        // write out contents of this object

        //---------------------------------------------------------
        // in order to handle future changes to the message 
        // context definition, be sure to maintain the 
        // object level identifiers
        //---------------------------------------------------------
        // serialization version ID
        out.writeLong(serialVersionUID);

        // revision ID
        out.writeInt(revisionID);

        //---------------------------------------------------------
        // various simple fields
        //---------------------------------------------------------
        ObjectStateUtils.writeString(out, className, "MetaDataEntry.className");
        ObjectStateUtils.writeString(out, qnameAsString, "MetaDataEntry.qnameAsString");
        ObjectStateUtils.writeString(out, extraName, "MetaDataEntry.extraName");
        ObjectStateUtils.writeArrayList(out, list, "MetaDataEntry.list");

    }


    /**
     * Restore the contents of the object that was 
     * previously saved. 
     * <p> 
     * NOTE: The field data must read back in the same order and type
     * as it was written.
     *
     * @param in    The stream to read the object contents from 
     * 
     * @exception IOException
     * @exception ClassNotFoundException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {

        // serialization version ID
        long suid = in.readLong();

        // revision ID
        int  revID = in.readInt();

        // make sure the object data is in a version we can handle
        if (suid != serialVersionUID)
        {
            throw new ClassNotFoundException(UNSUPPORTED_SUID);
        }

        // make sure the object data is in a revision level we can handle
        if (revID != REVISION_1)
        {
            throw new ClassNotFoundException(UNSUPPORTED_REVID);
        }


        //---------------------------------------------------------
        // various simple fields
        //---------------------------------------------------------

        className = ObjectStateUtils.readString(in, "MetaDataEntry.className");
        qnameAsString = ObjectStateUtils.readString(in, "MetaDataEntry.qnameAsString");
        extraName = ObjectStateUtils.readString(in, "MetaDataEntry.extraName");
        list = ObjectStateUtils.readArrayList(in, "MetaDataEntry.list");

    }

}
