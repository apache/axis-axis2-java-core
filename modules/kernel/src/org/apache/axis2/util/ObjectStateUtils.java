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


import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Provides functions for saving and restoring an
 * object's state.
 */
public class ObjectStateUtils {
    /*
     * setup for logging
     */
    private static final Log log = LogFactory.getLog(ObjectStateUtils.class);


    // used as part of the metadata written out 
    // indicating a null or empty object
    public static boolean EMPTY_OBJECT = false;

    // used as part of the metadata written out 
    // indicating a non-null or live object
    public static boolean ACTIVE_OBJECT = true;

    // used to indicate the end of a list
    public static String LAST_ENTRY = "LAST_ENTRY";

    // used to indicate an "empty" object
    public static String EMPTY_MARKER = "EMPTY_MARKER";

    // used to indicate an valid "null" object, 
    // typically used in key-value pairs where a non-null key refers to a null value
    public static String NULL_OBJECT = "NULL_OBJECT";

    // message/trace/logging strings
    public static final String UNSUPPORTED_SUID  = "Serialization version ID is not supported.";
    public static final String UNSUPPORTED_REVID = "Revision ID is not supported.";

    public static final String OBJ_SAVE_PROBLEM    = "The object could not be saved to the output stream.  The object may or may not be important for processing the message when it is restored. Look at how the object is to be used during message processing.";
    public static final String OBJ_RESTORE_PROBLEM = "The object could not be restored from the input stream.  The object may or may not be important for processing the message when it is restored. Look at how the object is to be used during message processing.";

    // as a way to improve performance and reduce trace logging with
    // extra exceptions, keep a table of classes that are not serializable
    // and only log the first time it that the class is encountered in
    // an NotSerializableException
    // note that the Hashtable is synchronized by Java so we shouldn't need to 
    // do extra control over access to the table
    public static Hashtable NotSerializableList = new Hashtable();

    //--------------------------------------------------------------------
    // Save/Restore methods
    //--------------------------------------------------------------------

    /**
     * Write a string to the specified output stream.
     * <p/>
     * The format of the information written to the output stream is:
     * <BOLD>Non-Null String</BOLD>
     * <LI> UTF     - class name string 
     * <LI> boolean - active flag
     * <LI> int     - number of string sections
     * <LI> int     - byte buffer size
     * <LI> bytes(UTF) - string data
     * <p/>
     * <BOLD>Null String</BOLD>
     * <LI> UTF     - description
     * <LI> boolean - empty flag
     * <p/>
     * 
     * @param out    The output stream
     * @param str    The string to write
     * @param desc   A text description to use for logging
     * @throws IOException Exception
     */
    public static void writeString(ObjectOutput out, String str, String desc) throws IOException {
        // The total number of bytes needed to represent all 
        // the characters of a string is calculated when the string
        // is serialized. If this number is larger than 65535 (ie, 64 KB)
        // then a java.io.UTFDataFormatException is thrown 

        if (str != null) {
            String str_desc = str.getClass().getName();
            // this string is expected to fit the writeUTF limitations
            out.writeUTF(str_desc);

            out.writeBoolean(ACTIVE_OBJECT);

            // set the number of sections for the string to be processed
            // for now, the string will be treated as a single section
            // notes: this would be used for strings that don't fit the
            // writeUTF(string) limitations
            int numberStringSections = 1;

            // set up a temporary stream to use for the string
            ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();

            // -------------------------------------------------------------
            // notes: there is some problem with the writeUTF(str) below
            //        the ObjectOutputStream is getting the UTF string
            //        but isn't reporting the correct size or converting
            //        to the correct byte array with the UTF data
            // -------------------------------------------------------------
            //ObjectOutputStream objOut = new ObjectOutputStream(outBuffer);
            //
            //try
            //{
            //    objOut.writeUTF(str);
            //}
            //catch (java.io.UTFDataFormatException udfe)
            //{
            //    // break string into sections and write the sections
            //    log.trace("ObjectStateUtils:writeString(): ACTIVE string: UTFDataFormatException ["+udfe.getMessage()+"]");
            //}
            // -------------------------------------------------------------

            outBuffer.write(str.getBytes() ,0, str.length());

            out.writeInt(numberStringSections);
            int outSize = outBuffer.size();
            out.writeInt(outSize);
            out.write(outBuffer.toByteArray());

            outBuffer.close();

            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:writeString(): ACTIVE string: str_desc ["+str_desc+"]    string ["+str+"]   desc ["+desc+"]   output byte buffer size ["+outSize+"]");
                log.trace("ObjectStateUtils:writeString(): ACTIVE string: outBuffer ["+outBuffer.toString()+"]");
            }

        } else {
            // this string is expected to fit the writeUTF limitations
            out.writeUTF(desc);

            out.writeBoolean(EMPTY_OBJECT);

            // for now, don't trace the EMPTY lines
            //  // trace point
            //  if (log.isTraceEnabled())
            //  {
            //      log.trace("ObjectStateUtils:writeString(): EMPTY String    desc ["+desc+"]  ");
            //  }
        }

    }


    /**
     * Read a string from the specified input stream. Returns null if no string
     * is available.
     * <p/>
     * The format of the information to be read from the input stream should be
     * <BOLD>Non-Null String</BOLD>
     * <LI> UTF     - class name string 
     * <LI> boolean - active flag
     * <LI> int     - number of string sections
     * <LI> int     - byte buffer size
     * <LI> bytes(UTF) - string data
     * <p/>
     * <BOLD>Null String</BOLD>
     * <LI> UTF     - description
     * <LI> boolean - empty flag
     * <p/>
     * 
     * @param in     The input stream
     * @param desc   A text description to use for logging
     * @return The string or null, if not available
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static String readString(ObjectInput in, String desc) throws IOException, ClassNotFoundException {
        String str = null;

        // get the marker
        String str_desc = in.readUTF();

        // get the flag
        boolean isActive = in.readBoolean();

        if (isActive == ACTIVE_OBJECT) {
            // then should have one or more sections of the string to get
            int numberStringSections = in.readInt();

            if (numberStringSections > 1) {
                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:readString(): ACTIVE string: the ["+desc+"] string with saved description ["+str_desc+"] has ["+numberStringSections+"] sections");
                }

                // TODO: read the sections and concatenate them together
                //       may need to adjust the saved information to make 
                //       putting the sections back together easier

                StringBuffer sbuff = new StringBuffer();

                for (int k = 0; k < numberStringSections; k++) {
                    String section = in.readUTF();
                    sbuff.append(section);
                }

                str = sbuff.toString();
            } else {
                // one string section

                // get the size of the string in bytes
                int bufSize = in.readInt();

                // set up a byte buffer to read into
                byte [] buffer = new byte [bufSize];

                int bytesRead = in.read(buffer, 0, bufSize);

                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:readString(): ACTIVE string: str_desc ["+str_desc+"]    bufSize ["+bufSize+"]   bytesRead ["+bytesRead+"]   desc ["+desc+"]");
                }

                if (bytesRead > 0) {
                    // -------------------------------------------------------------
                    // notes: there is some problem with the writeUTF(str) 
                    //        the ObjectOutputStream is getting the UTF string
                    //        but isn't reporting the correct size or converting
                    //        to the correct byte array with the UTF data
                    // -------------------------------------------------------------
                    //ByteArrayInputStream strBuffer = null;
                    //ObjectInputStream    objin = null;
                    //
                    //// use a byte array input stream to read the UTF
                    //strBuffer = new ByteArrayInputStream(buffer);
                    //objin = new ObjectInputStream(strBuffer);
                    //
                    // read the UTF data
                    //str = objin.readUTF();
                    //
                    //log.trace("ObjectStateUtils:readString(): UTF str from buffer  ["+str+"]");
                    //
                    // close internal streams
                    //objin.close();
                    //strBuffer.close();
                    // -------------------------------------------------------------

                    str = new String(buffer);
                }
            }
        }

        String value = "null";
        if (str != null) {
            value = str;
        }

        if (log.isTraceEnabled()) {
            log.trace("ObjectStateUtils:readString(): ["+desc+"]  returning  ["+value+"]  for  saved ["+str_desc+"]");
        }

        return str;
    }


    /**
     * Write an object to the specified output stream.
     * <p/>
     * The format of the information written to the output stream is
     * <p/>
     * <BOLD>Non-Null Object</BOLD>
     * <LI> UTF     - class name string 
     * <LI> boolean - active flag
     * <LI> object  - object if no error 
     * <LI> LAST_ENTRY marker 
     * <p/>
     * <BOLD>Null Object</BOLD>
     * <LI> UTF     - description
     * <LI> boolean - empty flag
     * <p/>
     * 
     * @param out    The output stream
     * @param obj    The object to write
     * @param desc   A text description to use for logging
     * @throws IOException Exception
     */
    public static void writeObject(ObjectOutput out, Object obj, String desc) throws IOException {
        IOException returned_exception = null;

        if (obj != null) {
            String objClassName = obj.getClass().getName();
            String fullDesc     = desc +":"+ objClassName;
            // this string is expected to fit the writeUTF limitations
            out.writeUTF(fullDesc);

            out.writeBoolean(ACTIVE_OBJECT);

            // put the object into a test output buffer to see if it can be saved
            // this technique preserves the integrity of the real output stream in the
            // event of a serialization error
            ByteArrayOutputStream test_outBuffer = new ByteArrayOutputStream();
            ObjectOutputStream test_objOut = new ObjectOutputStream(test_outBuffer);

            boolean  canWrite = false;

            try {
                // write the object to the test buffer
                test_objOut.writeObject(obj);
                canWrite = true;
            }
            catch (NotSerializableException nse2) {
                returned_exception = nse2;
                // only trace the first time a particular class causes this exception
                traceNotSerializable(obj, nse2, desc, "ObjectStateUtils.writeObject()", OBJ_SAVE_PROBLEM);
            }
            catch (IOException exc2) {
                // use this as a generic point for exceptions for the test output stream
                returned_exception = exc2;

                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:writeObject(): object["+obj.getClass().getName()+"]  ***Exception***  ["+exc2.getClass().getName()+" : "+exc2.getMessage()+"]  "+OBJ_SAVE_PROBLEM, exc2);
                    //exc2.printStackTrace();
                }
            }

            if (canWrite) {
                // write the object to the real output stream
                try {
                    out.writeObject(obj);

                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace("ObjectStateUtils:writeObject(): Object ["+objClassName+"]  desc ["+desc+"]");
                    }
                }
                catch (NotSerializableException nse) {
                    returned_exception = nse;
                    // only trace the first time a particular class causes this exception
                    traceNotSerializable(obj, nse, desc, "ObjectStateUtils.writeObject()", OBJ_SAVE_PROBLEM);
                }
                catch (IOException exc) {
                    // use this as a generic point for exceptions for the test output stream
                    returned_exception = exc;

                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace("ObjectStateUtils:writeObject(): object["+obj.getClass().getName()+"]  ***Exception***  ["+exc.getClass().getName()+" : "+exc.getMessage()+"]  "+OBJ_SAVE_PROBLEM, exc);
                        //exc.printStackTrace();
                    }
                }
            }

            // put the end-of-marker in the stream
            out.writeObject(LAST_ENTRY);

            test_outBuffer.close();
            test_objOut.close();

            if (returned_exception != null) {
                // let the caller know that there was a problem
                // note the integrity of the real output stream has been preserved
                throw returned_exception;
            }
        } else {
            // this string is expected to fit the writeUTF limitations
            out.writeUTF(desc);

            out.writeBoolean(EMPTY_OBJECT);

            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:writeObject(): EMPTY Object ["+desc+"]  ");
            }
        }
    }


    /**
     * Read an object from the specified input stream. Returns null if no object
     * is available.
     * <p/>
     * The format of the information to be read from the input stream should be
     * <BOLD>Non-Null Object</BOLD>
     * <LI> UTF     - class name string 
     * <LI> boolean - active flag
     * <LI> object  - object if no error 
     * <LI> LAST_ENTRY marker 
     * <p/>
     * <BOLD>Null Object</BOLD>
     * <LI> UTF     - description
     * <LI> boolean - empty flag
     * <p/>
     * 
     * @param in     The input stream
     * @param desc   A text description to use for logging
     * @return The object or null, if not available
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readObject(ObjectInput in, String desc) throws IOException, ClassNotFoundException {
        Object obj = null;

        String str_desc = in.readUTF();

        boolean isActive = in.readBoolean();

        if (isActive == ACTIVE_OBJECT) {
            boolean done = false;

            obj = in.readObject();

            if (obj != null) {
                if (obj instanceof String) {
                    String tmp = (String) obj;
                    if (tmp.equalsIgnoreCase(LAST_ENTRY)) {
                        // this is the last entry
                        done = true;

                        // reset the object to be returned
                        obj = null;
                    }
                }
            }

            // if we haven't got the end marker, then pull it from the stream
            if (done == false) {
                Object obj2 = in.readObject();
                
                // verify that this is the end marker
                boolean isConsistent = false;

                if (obj2 != null) {
                    if (obj2 instanceof String) {
                        String tmp2 = (String) obj2;
                        if (tmp2.equalsIgnoreCase(LAST_ENTRY)) {
                            // ok
                            isConsistent = true;
                        }
                    }
                }

                if (isConsistent == false) {
                    // trace the inconsistency
                    if (log.isTraceEnabled()) {
                        log.trace("ObjectStateUtils:readObject(): Inconsistent results reading the stream for ["+desc+"]  for saved ["+str_desc+"]");
                    }
                    //System.trace.println("ObjectStateUtils:readObject(): Inconsistent results reading the stream for ["+desc+"]  ");
                }
            }

        }

        String value = "null";

        if (obj != null) {
            value = "("+str_desc+")" + ":" + obj.getClass().getName();
        }

        // trace point
        if (log.isTraceEnabled()) {
            log.trace("ObjectStateUtils:readObject(): ["+desc+"]  returning  ["+value+"]   for saved ["+str_desc+"]");
        }

        return obj;
    }


    /**
     * Write an array of objects to the specified output stream.
     * <p/>
     * The format of the information written to the output stream is
     * <LI> class name of the array
     * <LI> active or empty
     * <LI> data
     * <p/>
     * NOTE: each object in the array should implement either 
     * java.io.Serializable or java.io.Externalizable in order to be
     * saved
     * <p/>
     *
     * @param out    The output stream
     * @param al     The ArrayList to write
     * @param desc   A text description to use for logging
     * @throws IOException Exception
     */
    public static void writeArrayList(ObjectOutput out, ArrayList al, String desc) throws IOException {
        // The format of the data is
        //
        //  Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - expected number of entries in the list
        //                        not including the last entry marker
        //    objects      - objects from list
        //                        last entry will be the LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last entry
        //    
        //  Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //

        out.writeUTF(desc);

        if ((al == null) || (al.isEmpty())) {
            // handle null or empty

            out.writeBoolean(EMPTY_OBJECT);

            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:writeArrayList(): EMPTY List ["+desc+"]  ");
            }
        } else {
            // active flag
            out.writeBoolean(ACTIVE_OBJECT);

            int listSize = al.size();
            int savedListSize = 0;

            // expected list size
            out.writeInt(listSize);

            // put each list entry into a test output buffer to see if it can be saved
            // this technique preserves the integrity of the real output stream in the
            // event of a serialization error
            ByteArrayOutputStream test_outBuffer = new ByteArrayOutputStream();
            ObjectOutputStream test_objOut = new ObjectOutputStream(test_outBuffer);

            // setup an iterator for the list
            Iterator i = al.iterator();

            while (i.hasNext()) {
                Object obj = i.next();
                String tmpClassName = obj.getClass().getName();

                boolean  canWrite = false;

                try {
                    // write the object to the test buffer
                    test_objOut.writeObject(obj);
                    canWrite = true;
                }
                catch (NotSerializableException nse2) {
                    // only trace the first time a particular class causes this exception
                    traceNotSerializable(obj, nse2, desc, "ObjectStateUtils.writeArrayList()", OBJ_SAVE_PROBLEM);
                }
                catch (Exception exc) {
                    // use this as a generic point for exceptions

                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace("ObjectStateUtils:writeArrayList(): object["+obj.getClass().getName()+"]  ***Exception***  ["+exc.getClass().getName()+" : "+exc.getMessage()+"]  "+OBJ_SAVE_PROBLEM, exc);
                        //exc.printStackTrace();
                    }
                }

                if (canWrite) {
                    // write the object to the real output stream
                    try {
                        out.writeObject(obj);
                        savedListSize++;

                        // trace point
                        if (log.isTraceEnabled()) {
                            log.trace("ObjectStateUtils:writeArrayList(): "+desc+" ["+obj.getClass().getName()+"]");
                        }

                    }
                    catch (NotSerializableException nse) {
                        // only trace the first time a particular class causes this exception
                        traceNotSerializable(obj, nse, desc, "ObjectStateUtils.writeArrayList()", OBJ_SAVE_PROBLEM);
                    }
                    catch (Exception ex) {
                        // use this as a generic point for exceptions

                        // trace point
                        if (log.isTraceEnabled()) {
                            log.trace("ObjectStateUtils:writeArrayList(): "+desc+" ["+obj.getClass().getName()+"]  ***Exception***  ["+ex.getClass().getName()+" : "+ex.getMessage()+"]  "+OBJ_SAVE_PROBLEM, ex);
                            //ex.printStackTrace();
                        }
                    }
                }

                // reset the temporary buffer for the next round
                test_objOut.reset();
            }

            // put the end-of-marker in the stream
            out.writeObject(LAST_ENTRY);
            savedListSize++;

            out.writeInt(savedListSize);

            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:writeArrayList(): List ["+desc+"]   members saved ["+savedListSize+"]");
            }

            test_outBuffer.close();
            test_objOut.close();
        }

    }


    /**
     * Reads an array of objects from the specified input stream.  Returns
     * null if no array is available.
     * <p/>
     * The format of the information to be read from the input stream should be
     * <LI> class name
     * <LI> active or empty
     * <LI> data
     * <p/>
     * NOTE: each object in the array should implement either 
     * java.io.Serializable or java.io.Externalizable in order to be
     * saved
     * <p/>
     * 
     * @param in     The input stream
     * @param desc   A text description to use for logging
     * @return The ArrayList or null, if not available
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static ArrayList readArrayList(ObjectInput in, String desc) throws IOException {
        // The format of the data is
        //
        //  Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - expected number of entries in the list
        //                        not including the last entry marker
        //    objects      - objects from list
        //                        last entry will be the LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last entry
        //    
        //  Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //

        ArrayList list = new ArrayList();

        String str_desc = in.readUTF();

        boolean isActive = in.readBoolean();

        if (isActive == ACTIVE_OBJECT) {
            // get the expected number of entries
            int expectedListSize = in.readInt();

            // process the objects
            boolean keepGoing = true;
            int     count     = 0;
            Object  obj       = null;

            while (keepGoing) {
                // stop when we get to the end-of-list marker

                // get the object
                try {
                    obj = in.readObject();
                    count++;

                    if (obj != null) {
                        if (obj instanceof String) {
                            String tmp = (String) obj;
                            if (tmp.equalsIgnoreCase(LAST_ENTRY)) {
                                // this is the last entry
                                keepGoing = false;

                            } //end if last entry marker
                        } // end if a String object

                        if (keepGoing) {
                            String tmpClassName = obj.getClass().getName();

                            // not at the end of the list so
                            // add the entry to the list
                            list.add(obj);

                            // trace point
                            if (log.isTraceEnabled()) {
                                log.trace("ObjectStateUtils:readArrayList(): ["+desc+"]  index ["+count+"]  object ["+tmpClassName+"]   for saved ["+str_desc+"]");
                            }
                        }
                    } else {
                        // some other problem occurred
                        // the object to be read is null
                        // trace point
                        if (log.isTraceEnabled()) {
                            log.trace("ObjectStateUtils:readArrayList(): ["+desc+"]  object index ["+count+"] ***Unexpected null object***   for saved ["+str_desc+"]");
                        }
                        keepGoing = false;
                    }
                }
                catch (Exception ex) {
                    // use this as a generic point for all exceptions

                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace("ObjectStateUtils:readArrayList(): ["+desc+"]  object index ["+count+"]   for saved ["+str_desc+"]  ***Exception***  ["+ex.getClass().getName()+" : "+ex.getMessage()+"]  "+OBJ_RESTORE_PROBLEM, ex);
                        //ex.printStackTrace();
                    }

                    keepGoing = false;
                }

            } // end while keep going

            int adjustedNumberEntries = in.readInt();

            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:readArrayList(): adjusted number of entries ["+adjustedNumberEntries+"]     for saved ["+str_desc+"] ");
            }


            if (list.isEmpty()) {
                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:readArrayList(): ["+desc+"]  returning  [null]  for saved ["+str_desc+"]");
                }

                return null;
            } else {
                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:readArrayList(): ["+desc+"]  returning  [listsize="+list.size()+"]  for saved ["+str_desc+"]");
                }

                return list;
            }
        } else {
            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:readArrayList(): ["+desc+"]  returning  [null]    for saved ["+str_desc+"]");
            }

            return null;
        }
    }


    /**
     * Write a hashmap of objects to the specified output stream.
     * <p/>
     * The format of the information written to the output stream is
     * <LI> class name of the array
     * <LI> active or empty
     * <LI> data
     * <p/>
     * NOTE: each object in the map should implement either 
     * java.io.Serializable or java.io.Externalizable in order to be
     * saved
     * <p/>
     *
     * @param out    The output stream
     * @param map    The HashMap to write
     * @param desc   A text description to use for logging
     * @throws IOException Exception
     */
    public static void writeHashMap(ObjectOutput out, HashMap map, String desc) throws IOException {
        // The format of the data is
        //
        //  Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - expected number of entries in the list
        //                        not including the last entry marker
        //    objects      - object,object pairs from list
        //                        last entry will be a single LAST_ENTRY marker object
        //    int          - adjusted number of entries in the list
        //                        includes the last entry
        //    
        //  Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //

        out.writeUTF(desc);

        if ((map == null) || (map.isEmpty())) {
            // handle null or empty

            out.writeBoolean(EMPTY_OBJECT);

            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:writeHashMap(): EMPTY map ["+desc+"]  ");
            } 
        } else {
            out.writeBoolean(ACTIVE_OBJECT);

            // the expected number of pairs in the map
            int listSize = map.size();
            out.writeInt(listSize);

            // this will be the actual number of pairs saved
            int savedListSize = 0;

            // put each pair into a buffer to see if they can be saved
            ByteArrayOutputStream pair_outBuffer = new ByteArrayOutputStream();
            ObjectOutputStream pair_objOut = new ObjectOutputStream(pair_outBuffer);

            Set keyset = map.keySet();
            Iterator i = keyset.iterator();

            while (i.hasNext()) {
                // handle errors when can't access the value for the key

                Object key = i.next();
                Object value = map.get(key);

                boolean  canWritePair = false;

                try {
                    // write the objects in pairs
                    pair_objOut.writeObject(key);

                    try {
                        if (value == null) {
                            pair_objOut.writeObject(NULL_OBJECT);
                        } else {
                            pair_objOut.writeObject(value);
                        }

                        // ok, the pair can be saved so write them to our "real" buffer
                        canWritePair = true;
                    }
                    catch (NotSerializableException nse) {
                        // only trace the first time a particular class causes this exception
                        traceNotSerializable(value, nse, desc, "ObjectStateUtils.writeHashMap() map value", OBJ_SAVE_PROBLEM);
                    }
                    catch (Exception ex) {
                        // use this as a generic point for exceptions

                        // trace point
                        if (log.isTraceEnabled()) {
                            log.trace("ObjectStateUtils:writeHashMap(): map value ["+value.getClass().getName()+"]  ***Exception***  ["+ex.getClass().getName()+" : "+ex.getMessage()+"]  "+OBJ_SAVE_PROBLEM, ex);
                            //ex.printStackTrace();
                        }
                    }
                }
                catch (NotSerializableException nse2) {
                    // only trace the first time a particular class causes this exception
                    traceNotSerializable(key, nse2, desc, "ObjectStateUtils.writeHashMap() map key", OBJ_SAVE_PROBLEM);
                }
                catch (Exception exc) {
                    // use this as a generic point for exceptions

                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace("ObjectStateUtils:writeHashMap(): map key ["+key.getClass().getName()+"]  ***Exception***  ["+exc.getClass().getName()+" : "+exc.getMessage()+"]  "+OBJ_SAVE_PROBLEM, exc);
                        //exc.printStackTrace();
                    }
                }

                if (canWritePair) {
                    try {
                        // write the objects in pairs
                        out.writeObject(key);

                        // TODO: what if there is an error with the real output stream?
                        try {
                            if (value == null) {
                                out.writeObject(NULL_OBJECT);
                            } else {
                                out.writeObject(value);
                            }
                        }
                        catch (NotSerializableException nse3) {
                            // only trace the first time a particular class causes this exception
                            traceNotSerializable(value, nse3, desc, "ObjectStateUtils.writeHashMap() map value output error", OBJ_SAVE_PROBLEM);
                        }
                        catch (Exception excp) {
                            // trace point
                            if (log.isTraceEnabled()) {
                                log.trace("ObjectStateUtils:writeHashMap(): output error: map value ["+value.getClass().getName()+"]  ***Exception***  ["+excp.getClass().getName()+" : "+excp.getMessage()+"]  "+OBJ_SAVE_PROBLEM, excp);
                                //excp.printStackTrace();
                            }

                            // put an "null" marker here
                            out.writeObject(EMPTY_MARKER);
                        }

                        savedListSize++;
                    }
                    catch (NotSerializableException nse4) {
                        // only trace the first time a particular class causes this exception
                        traceNotSerializable(key, nse4, desc, "ObjectStateUtils.writeHashMap() map key output error", OBJ_SAVE_PROBLEM);
                    }
                    catch (Exception ex) {
                        // there was an error with the key to the real output stream
                        // so skip to the next pair

                        // trace point
                        if (log.isTraceEnabled()) {
                            log.trace("ObjectStateUtils:writeHashMap(): output error: map key ["+key.getClass().getName()+"]  ***Exception***  ["+ex.getClass().getName()+" : "+ex.getMessage()+"]  "+OBJ_SAVE_PROBLEM, ex);
                            //ex.printStackTrace();
                        }
                    }
                }


                // reset the temporary buffer for the next round
                pair_objOut.reset();
            }

            // write out a marker for the end of list
            out.writeObject(LAST_ENTRY);
            savedListSize++;

            // put what we got into our stream
            out.writeInt(savedListSize);

            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:writeHashMap(): map ["+desc+"]   members saved ["+savedListSize+"]");
            }

            pair_outBuffer.close();
            pair_objOut.close();
        }
    }


    /**
     * Read a hashmap of objects from the specified input stream. Returns
     * null if no hashmap is available.
     * <p/>
     * The format of the information to be read from the input stream should be
     * <LI> class name
     * <LI> active or empty
     * <LI> data
     * <p/>
     * NOTE: each object in the array should implement either 
     * java.io.Serializable or java.io.Externalizable in order to be
     * saved
     * <p/>
     * 
     * @param in     The input stream
     * @param desc   A text description to use for logging
     * @return The HashMap or null, if not available
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static HashMap readHashMap(ObjectInput in, String desc) throws IOException {
        // The format of the data is
        //
        //  Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - expected number of entries in the list
        //                        not including the last entry marker
        //    objects      - object,object pairs from list
        //                        last entry will be a single LAST_ENTRY marker object
        //    int          - adjusted number of entries in the list
        //                        includes the last entry
        //    
        //  Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //

        HashMap map = new HashMap();

        String str_desc = in.readUTF();

        boolean isActive = in.readBoolean();

        if (isActive == ACTIVE_OBJECT) {
            // get the hashmap

            // first, get the expected number of pairs
            int expectedListSize = in.readInt();

            // count the pairs as we get them from the in buffer,
            // including pairs that we don't keep
            int obtainedListSize = 0;

            // process the object pairs
            boolean keepGoing = true;

            while (keepGoing) {
                Object key = null;
                Object value = null;

                try {

                    key = in.readObject();

                    if (key instanceof String) {
                        String tmpkey = (String) key;

                        // check to see if this is the last entry
                        if (tmpkey.equalsIgnoreCase(LAST_ENTRY) == true) {
                            // stop here
                            keepGoing = false;
                            break;
                        }
                    }

                    // that object was not the last entry marker,
                    // so get the next object in the pair

                    value = in.readObject();

                    boolean keepPair = true;

                    if (value instanceof String) {
                        String tmpvalue = (String) value;

                        // if the value object is not available, then
                        // don't preserve the key-value setting
                        if (tmpvalue.equalsIgnoreCase(EMPTY_MARKER) == true) {
                            // an empty value, so skip pair
                            keepPair = false;

                            // trace point
                            if (log.isTraceEnabled()) {
                                log.trace("ObjectStateUtils:readHashMap(): ["+desc+"]  object pair index ["+obtainedListSize+"]  will be skipped because the value object is unavailable.    For saved ["+str_desc+"]");
                            }
                        } else if (tmpvalue.equalsIgnoreCase(NULL_OBJECT) == true) {
                            value = null;
                        }
                    }

                    if (keepPair) {
                        map.put(key,value);

                        // trace point
                        if (log.isTraceEnabled()) {
                            log.trace("ObjectStateUtils:readHashMap(): ["+desc+"]  object pair index ["+obtainedListSize+"]   for saved ["+str_desc+"]");
                        }
                    }

                    obtainedListSize++;

                }
                catch (Exception ex) {
                    // use this as a generic point for all exceptions

                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace("ObjectStateUtils:readHashMap(): ["+desc+"]  object pair index ["+obtainedListSize+"]   for saved ["+str_desc+"] ***Exception***  ["+ex.getClass().getName()+" : "+ex.getMessage()+"]  "+OBJ_RESTORE_PROBLEM,ex);
                        //ex.printStackTrace();
                    }
                }

            }

            // get the adjusted list size from the buffer
            int savedListSize = in.readInt();


            if (map.isEmpty()) {
                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:readHashMap(): ["+desc+"]  returning  [null]  for saved ["+str_desc+"]");
                }

                return null;
            } else {
                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:readHashMap(): ["+desc+"]  returning  [mapsize="+map.size()+"]    for saved ["+str_desc+"]");
                }

                return map;
            }

        } else {
            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:readHashMap(): ["+desc+"]  returning  [null]    for saved ["+str_desc+"]");
            }

            return null;
        }

    }

    /**
     * Write a linked list of objects to the specified output stream.
     * <p/>
     * The format of the information written to the output stream is
     * <LI> class name of the array
     * <LI> active or empty
     * <LI> data
     * <p/>
     * NOTE: each object in the array should implement either 
     * java.io.Serializable or java.io.Externalizable in order to be
     * saved
     * <p/>
     *
     * @param out    The output stream
     * @param list   The LinkedList to write
     * @param desc   A text description to use for logging
     * @throws IOException Exception
     */
    public static void writeLinkedList(ObjectOutput out, LinkedList objlist, String desc) throws IOException {
        // The format of the data is
        //
        //  Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - expected number of entries in the list
        //                        not including the last entry marker
        //    objects      - objects from list
        //                        last entry will be the LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last entry
        //    
        //  Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //

        out.writeUTF(desc);

        if ((objlist == null) || (objlist.isEmpty())) {
            // handle null or empty

            out.writeBoolean(EMPTY_OBJECT);

            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:writeLinkedList(): EMPTY List ["+desc+"]  ");
            }
        } else {
            // active flag
            out.writeBoolean(ACTIVE_OBJECT);

            int listSize = objlist.size();
            int savedListSize = 0;

            // expected list size
            out.writeInt(listSize);

            // put each list entry into a test output buffer to see if it can be saved
            // this technique preserves the integrity of the real output stream in the
            // event of a serialization error
            ByteArrayOutputStream test_outBuffer = new ByteArrayOutputStream();
            ObjectOutputStream test_objOut = new ObjectOutputStream(test_outBuffer);

            // setup an iterator for the list
            Iterator i = objlist.iterator();

            while (i.hasNext()) {
                Object obj = i.next();
                String tmpClassName = obj.getClass().getName();

                boolean  canWrite = false;

                try {
                    // write the object to the test buffer
                    test_objOut.writeObject(obj);
                    canWrite = true;
                }
                catch (NotSerializableException nse2) {
                    // only trace the first time a particular class causes this exception
                    traceNotSerializable(obj, nse2, desc, "ObjectStateUtils.writeLinkedList()", OBJ_SAVE_PROBLEM);
                }
                catch (Exception exc) {
                    // use this as a generic point for exceptions

                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace("ObjectStateUtils:writeLinkedList(): object["+obj.getClass().getName()+"]  ***Exception***  ["+exc.getClass().getName()+" : "+exc.getMessage()+"]  "+OBJ_SAVE_PROBLEM, exc);
                        //exc.printStackTrace();
                    }
                }

                if (canWrite) {
                    // write the object to the real output stream
                    try {
                        out.writeObject(obj);
                        savedListSize++;

                        // trace point
                        if (log.isTraceEnabled()) {
                            log.trace("ObjectStateUtils:writeLinkedList(): "+desc+" ["+obj.getClass().getName()+"]");
                        }

                    }
                    catch (NotSerializableException nse) {
                        // only trace the first time a particular class causes this exception
                        traceNotSerializable(obj, nse, desc, "ObjectStateUtils.writeLinkedList()", OBJ_SAVE_PROBLEM);
                    }
                    catch (Exception ex) {
                        // use this as a generic point for exceptions

                        // trace point
                        if (log.isTraceEnabled()) {
                            log.trace("ObjectStateUtils:writeLinkedList(): "+desc+" ["+obj.getClass().getName()+"]  ***Exception***  ["+ex.getClass().getName()+" : "+ex.getMessage()+"] "+OBJ_SAVE_PROBLEM, ex);
                            //ex.printStackTrace();
                        }
                    }
                }

                // reset the temporary buffer for the next round
                test_objOut.reset();
            }

            // put the end-of-marker in the stream
            out.writeObject(LAST_ENTRY);
            savedListSize++;

            out.writeInt(savedListSize);

            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:writeLinkedList(): List ["+desc+"]   members saved ["+savedListSize+"]");
            }

            test_outBuffer.close();
            test_objOut.close();
        }

    }


    /**
     * Reads a linked list of objects from the specified input stream.  Returns
     * null if no array is available.
     * <p/>
     * The format of the information to be read from the input stream should be
     * <LI> class name
     * <LI> active or empty
     * <LI> data
     * <p/>
     * NOTE: each object in the array should implement either 
     * java.io.Serializable or java.io.Externalizable in order to be
     * saved
     * <p/>
     * 
     * @param in     The input stream
     * @param desc   A text description to use for logging
     * @return The linked list or null, if not available
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static LinkedList readLinkedList(ObjectInput in, String desc) throws IOException {
        // The format of the data is
        //
        //  Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - expected number of entries in the list
        //                        not including the last entry marker
        //    objects      - objects from list
        //                        last entry will be the LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last entry
        //    
        //  Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //

        LinkedList list = new LinkedList();

        String str_desc = in.readUTF();

        boolean isActive = in.readBoolean();

        if (isActive == ACTIVE_OBJECT) {
            // get the expected number of entries
            int expectedListSize = in.readInt();

            // process the objects
            boolean keepGoing = true;
            int     count     = 0;
            Object  obj       = null;

            while (keepGoing) {
                // stop when we get to the end-of-list marker

                // get the object
                try {
                    obj = in.readObject();
                    count++;

                    if (obj != null) {
                        if (obj instanceof String) {
                            String tmp = (String) obj;
                            if (tmp.equalsIgnoreCase(LAST_ENTRY)) {
                                // this is the last entry
                                keepGoing = false;

                            } //end if last entry marker
                        } // end if a String object

                        if (keepGoing) {
                            String tmpClassName = obj.getClass().getName();

                            // not at the end of the list so
                            // add the entry to the list
                            list.add(obj);

                            // trace point
                            if (log.isTraceEnabled()) {
                                log.trace("ObjectStateUtils:readLinkedList(): ["+desc+"]  index ["+count+"]  object ["+tmpClassName+"]   for saved ["+str_desc+"]");
                            }
                        }
                    } else {
                        // some other problem occurred
                        // the object to be read is null
                        // trace point
                        if (log.isTraceEnabled()) {
                            log.trace("ObjectStateUtils:readLinkedList(): ["+desc+"]  object index ["+count+"] ***Unexpected null object***   for saved ["+str_desc+"]");
                        }
                        keepGoing = false;
                    }
                }
                catch (Exception ex) {
                    // use this as a generic point for all exceptions

                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace("ObjectStateUtils:readLinkedList(): ["+desc+"]  object index ["+count+"]   for saved ["+str_desc+"] ***Exception***  ["+ex.getClass().getName()+" : "+ex.getMessage()+"]  "+OBJ_RESTORE_PROBLEM,ex);
                        //ex.printStackTrace();
                    }

                    keepGoing = false;
                }

            } // end while keep going

            int adjustedNumberEntries = in.readInt();

            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:readLinkedList(): adjusted number of entries ["+adjustedNumberEntries+"]     for saved ["+str_desc+"] ");
            }


            if (list.isEmpty()) {
                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:readLinkedList(): ["+desc+"]  returning  [null]  for saved ["+str_desc+"]");
                }

                return null;
            } else {
                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:readLinkedList(): ["+desc+"]  returning  [listsize="+list.size()+"]   for saved ["+str_desc+"]");
                }

                return list;
            }
        } else {
            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:readLinkedList(): ["+desc+"]  returning  [null]   for saved ["+str_desc+"]");
            }

            return null;
        }
    }



    //--------------------------------------------------------------------
    // Finder methods
    //--------------------------------------------------------------------

    /**
     * Find the AxisOperation object that matches the criteria
     * 
     * @param axisConfig The AxisConfiguration object
     * @param opClassName the class name string for the target object
     *                   (could be a derived class)
     * @param opQName    the name associated with the operation
     * @return the AxisOperation object that matches the given criteria
     */
    public static AxisOperation findOperation(AxisConfiguration axisConfig, String opClassName, QName opQName) {
        HashMap services = axisConfig.getServices();

        Iterator its = services.values().iterator();

        while (its.hasNext()) {
            AxisService service = (AxisService)its.next();

            Iterator ito = service.getOperations();

            while (ito.hasNext()) {
                AxisOperation operation = (AxisOperation)ito.next();

                String tmpOpName  = operation.getClass().getName();
                QName  tmpOpQName = operation.getName();

                if ((tmpOpName.equals(opClassName)) && (tmpOpQName.equals(opQName))) {
                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace("ObjectStateUtils:findOperation(axisCfg): returning  ["+opClassName+"]   ["+opQName.toString()+"]");
                    }

                    return operation;
                }
            }
        }

        // trace point
        if (log.isTraceEnabled()) {
            log.trace("ObjectStateUtils:findOperation(axisCfg): ["+opClassName+"]   ["+opQName.toString()+"]  returning  [null]");
        }

        return null;
    }


    /**
     * Find the AxisOperation object that matches the criteria
     * 
     * @param service    The AxisService object
     * @param opClassName The class name string for the target object
     *                   (could be a derived class)
     * @param opQName    the name associated with the operation
     * @return the AxisOperation object that matches the given criteria
     */
    public static AxisOperation findOperation(AxisService service, String opClassName, QName opQName) {
        if (service == null) {
            return null;
        }

        Iterator ito = service.getOperations();

        while (ito.hasNext()) {
            AxisOperation operation = (AxisOperation)ito.next();

            String tmpOpName  = operation.getClass().getName();
            QName  tmpOpQName = operation.getName();

            if ((tmpOpName.equals(opClassName)) && (tmpOpQName.equals(opQName))) {
                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:findOperation(service): returning  ["+opClassName+"]   ["+opQName.toString()+"]");
                }

                return operation;
            }
        }

        // trace point
        if (log.isTraceEnabled()) {
            log.trace("ObjectStateUtils:findOperation(service): ["+opClassName+"]   ["+opQName.toString()+"]  returning  [null]");
        }

        return null;
    }


    /**
     * Find the AxisService object that matches the criteria
     * 
     * @param axisConfig The AxisConfiguration object
     * @param serviceClassName the class name string for the target object
     *                   (could be a derived class)
     * @param serviceName      the name associated with the service
     * @return the AxisService object that matches the criteria
     */
    public static AxisService findService(AxisConfiguration axisConfig, String serviceClassName, String serviceName) {
        HashMap services = axisConfig.getServices();

        Iterator its = services.values().iterator();

        while (its.hasNext()) {
            AxisService service = (AxisService)its.next();

            String tmpServClassName = service.getClass().getName();
            String tmpServName      = service.getName();

            if ((tmpServClassName.equals(serviceClassName)) && (tmpServName.equals(serviceName))) {
                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:findService(): returning  ["+serviceClassName+"]   ["+serviceName+"]");
                }

                return service;
            }
        }

        // trace point
        if (log.isTraceEnabled()) {
            log.trace("ObjectStateUtils:findService(): ["+serviceClassName+"]   ["+serviceName+"]  returning  [null]");
        }

        return null;
    }

    /**
     * Find the AxisServiceGroup object that matches the criteria
     * <p/>
     * <B>Note<B> the saved service group meta information may not
     * match up with any of the serviceGroups that
     * are in the current AxisConfiguration object.
     * 
     * @param axisConfig The AxisConfiguration object
     * @param serviceGrpClassName the class name string for the target object
     *                   (could be a derived class)
     * @param serviceGrpName      the name associated with the service group
     * @return the AxisServiceGroup object that matches the criteria
     */
    public static AxisServiceGroup findServiceGroup(AxisConfiguration axisConfig, String serviceGrpClassName, String serviceGrpName) {
        Iterator its = axisConfig.getServiceGroups();

        while (its.hasNext()) {
            AxisServiceGroup serviceGroup = (AxisServiceGroup)its.next();

            String tmpSGClassName = serviceGroup.getClass().getName();
            String tmpSGName      = serviceGroup.getServiceGroupName();

            if (tmpSGClassName.equals(serviceGrpClassName)) {
                boolean found = false;

                // the serviceGroupName can be null, so either both the 
                // service group names are null or they match
                if ((tmpSGName == null) && (serviceGrpName == null)) {
                    found = true;
                } else if ((tmpSGName != null) && (tmpSGName.equals(serviceGrpName))) {
                    found = true;
                }

                if (found) {
                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace("ObjectStateUtils:findServiceGroup(): returning  ["+serviceGrpClassName+"]   ["+serviceGrpName+"]");
                    }

                    return serviceGroup;
                }
            }
        }

        // trace point
        if (log.isTraceEnabled()) {
            log.trace("ObjectStateUtils:findServiceGroup(): ["+serviceGrpClassName+"]   ["+serviceGrpName+"]  returning  [null]");
        }

        return null;
    }


    /**
     * Find the AxisMessage object that matches the criteria
     * 
     * @param op             The AxisOperation object
     * @param msgName        The name associated with the message
     * @param msgElementName The name associated with the message element
     * @return the AxisMessage object that matches the given criteria
     */
    public static AxisMessage findMessage(AxisOperation op, String msgName, String msgElementName) {
        // Several kinds of AxisMessages can be associated with a particular 
        // AxisOperation.  The kinds of AxisMessages that are typically
        // accessible are associated with "in" and "out".  
        // There are also different kinds of AxisOperations, and each
        // type of AxisOperation can have its own mix of AxisMessages
        // depending on the style of message exchange pattern (mep)

        if (op == null) {
            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:findMessage(): ["+msgName+"]  ["+msgElementName+"] returning  [null] - no AxisOperation");
            }

            return null;
        }

        if (msgName == null) {
            // nothing to match with, expect to match against a name
            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:findMessage(): ["+msgName+"]  ["+msgElementName+"] returning  [null] - message name is not set");
            }

            return null;
        }


        String  tmpName        = null;
        String  tmpElementName = null;

        //-------------------------------------
        // first try the "out" message
        //-------------------------------------
        AxisMessage out = null;
        try {
            out = op.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        }
        catch (Exception ex) {
            // just absorb the exception
        }

        if (out != null) {
            tmpName = out.getName();

            QName tmpQout = out.getElementQName();
            if (tmpQout != null) {
                tmpElementName = tmpQout.toString();
            }
        }

        // check the criteria for a match

        boolean matching = matchMessageNames(tmpName, tmpElementName, msgName, msgElementName);

        if (matching) {
            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:findMessage(): returning OUT message  ["+msgName+"]  ["+msgElementName+"] ");
            }

            return out;
        }

        //-------------------------------------
        // next, try the "in" message 
        //-------------------------------------
        AxisMessage in = null;
        try {
            in = op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        }
        catch (Exception ex) {
            // just absorb the exception
        }

        if (in != null) {
            tmpName = in.getName();

            QName tmpQin = in.getElementQName();
            if (tmpQin != null) {
                tmpElementName = tmpQin.toString();
            }
        } else {
            tmpName        = null;
            tmpElementName = null;
        }

        // check the criteria for a match

        matching = matchMessageNames(tmpName, tmpElementName, msgName, msgElementName);

        if (matching) {
            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:findMessage(): returning IN message ["+msgName+"]  ["+msgElementName+"] ");
            }

            return in;
        }

        // if we got here, then no match was found

        // trace point
        if (log.isTraceEnabled()) {
            log.trace("ObjectStateUtils:findMessage(): ["+msgName+"]  ["+msgElementName+"] returning  [null]");
        }

        return null;
    }

    /**
     * Check the first set of names for a match against
     * the second set of names.  These names are 
     * associated with AxisMessage objects. Message names
     * are expected to be non-null.  Element names could
     * be either null or non-null.
     * 
     * @param name1  The name for the first message
     * @param elementName1 The element name for the first message
     * @param name2  The name for the second message
     * @param elementName2 The element name for the second message
     * @return TRUE if there's a match,
     *         FALSE otherwise
     */
    private static boolean matchMessageNames(String name1, String elementName1, String name2, String elementName2) {
        // the name for the message must exist
        if ((name1 != null) && (name2 != null) && (name1.equals(name2))) {
            // there's a match on the name associated with the message object

            // element names need to match, including being null
            if ((elementName1 == null) && (elementName2 == null)) {
                // there's a match for the nulls
                return true;
            } else if ((elementName1 != null) && (elementName2 != null) && (elementName1.equals(elementName2))) {
                // there's a match for the element names
                return true;
            } else {
                // there's some mismatch
                return false;
            }
        } else {
            // either a message name is null or the names don't match
            return false;
        }
    }


    /**
     * Find the Handler object that matches the criteria
     * 
     * @param existingHandlers The list of existing handlers and phases
     * @param handlerClassName the class name string for the target object
     *                   (could be a derived class)
     * @return the Handler object that matches the criteria
     */
    public static Object findHandler(ArrayList existingHandlers, MetaDataEntry metaDataEntry) //String handlerClassName)
    {

        String title = "ObjectStateUtils:findHandler(): ";

        String handlerClassName = metaDataEntry.getClassName();
        String qNameAsString = metaDataEntry.getQNameAsString();
        
        for (int i = 0; i < existingHandlers.size(); i++) {
            if (existingHandlers.get(i) != null) {
                String tmpClassName = existingHandlers.get(i).getClass().getName();
                String tmpName      = ((Handler)existingHandlers.get(i)).getName().toString();

                if ( (tmpClassName.equals(handlerClassName))
                        && (tmpName.equals(qNameAsString))) {
                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace(title+" ["+handlerClassName+"]  name ["+qNameAsString+"]  returned");
                    }

                    return(Handler)(existingHandlers.get(i));
                }
            }
        }

        // trace point
        if (log.isTraceEnabled()) {
            log.trace(title + " ["+handlerClassName+"]  name ["+qNameAsString+"] was not found in the existingHandlers list");
        }
        
        return null;
    }


    /**
     * Find the TransportListener object that matches the criteria
     * <p/>
     * <B>Note<B> the saved meta information may not
     * match up with any of the objects that
     * are in the current AxisConfiguration object.
     * 
     * @param axisConfig The AxisConfiguration object
     * @param listenerClassName the class name string for the target object
     *                   (could be a derived class)
     * @return the TransportListener object that matches the criteria
     */
    public static TransportListener findTransportListener(AxisConfiguration axisConfig, String listenerClassName) {
        // TODO: investigate a better technique to match up with a TransportListener

        HashMap transportsIn = axisConfig.getTransportsIn();

        // get a collection of the values in the map
        Collection values = transportsIn.values();

        Iterator i = values.iterator();
        
        while (i.hasNext()) {
            TransportInDescription ti = (TransportInDescription)i.next();

            TransportListener tl = ti.getReceiver();
            String tlClassName = tl.getClass().getName();

            if (tlClassName.equals(listenerClassName)) {
                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:findTransportListener():  ["+listenerClassName+"]  returned");
                }

                return tl;
            }
        }

        // trace point
        if (log.isTraceEnabled()) {
            log.trace("ObjectStateUtils:findTransportListener(): returning  [null]");
        }

        return null;
    }

    /**
     * Compares the two collections to see if they are equivalent.
     * 
     * @param a1  The first collection
     * @param a2  The second collection
     * @param strict  Indicates whether strict checking is required.  Strict
     *                checking means that the two collections must have the
     *                same elements in the same order.  Non-strict checking
     *                means that the two collections must have the same 
     *                elements, but the order is not significant.
     * @return TRUE if the two collections are equivalent
     *         FALSE, otherwise
     */
    public static boolean isEquivalent(ArrayList a1, ArrayList a2, boolean strict) {
        if ((a1 != null) && (a2 != null)) {
            // check number of elements in lists
            int size1 = a1.size();
            int size2 = a2.size();

            if (size1 != size2) {
                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:isEquivalent(ArrayList,ArrayList): FALSE - size mismatch ["+size1+"] != ["+size2+"]");
                }
                return false;
            }

            if (strict) {
                // Strict checking
                // The lists must contain the same elements in the same order.
                return (a1.equals(a2));
            } else {
                // Non-strict checking
                // The lists must contain the same elements but the order is not required.
                Iterator i1 = a1.iterator();

                while (i1.hasNext()) {
                    Object obj1 = i1.next();

                    if (!a2.contains(obj1)) {
                        // trace point
                        if (log.isTraceEnabled()) {
                            log.trace("ObjectStateUtils:isEquivalent(ArrayList,ArrayList): FALSE - mismatch with element ["+obj1.getClass().getName()+"] ");
                        }
                        return false;
                    }
                }

                return true;
            }

        } else if ((a1 == null) && (a2 == null)) {
            return true;
        } else if ((a1 != null) && (a2 == null)) {
            if (a1.size() == 0) {
                return true;
            }
            return false;
        } else if ((a1 == null) && (a2 != null)) {
            if (a2.size() == 0) {
                return true;
            }
            return false;
        } else {
            // mismatch

            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:isEquivalent(ArrayList,ArrayList): FALSE - mismatch in lists");
            }
            return false;
        }
    }

    /**
     * Compares the two collections to see if they are equivalent.
     * 
     * @param m1  The first collection
     * @param m2  The second collection
     * @param strict  Indicates whether strict checking is required.  Strict
     *                checking means that the two collections must have the
     *                same mappings.  Non-strict checking means that the 
     *                two collections must have the same keys.  In both
     *                cases, the order is not significant.
     * @return TRUE if the two collections are equivalent
     *         FALSE, otherwise
     */
    public static boolean isEquivalent(Map m1, Map m2, boolean strict) {
        if ((m1 != null) && (m2 != null)) {
            if (strict) {
                // This is a strict test.
                // Returns true if the given object is also a map and the two Maps 
                // represent the same mappings. 
                return (m1.equals(m2));
            } else {
                int size1 = m1.size();
                int size2 = m2.size();

                if (size1 != size2) {
                    return false;
                }

                // check the keys, ordering is not important between the two maps
                Iterator it1 = m1.keySet().iterator();

                while (it1.hasNext()) {
                    Object key1 = it1.next();

                    if (m2.containsKey(key1) == false) {
                        return false;
                    }
                }

                return true;
            }
        } else if ((m1 == null) && (m2 == null)) {
            return true;
        } else {
            // mismatch
            return false;
        }
    }


    /**
     * Compares the two collections to see if they are equivalent.
     * 
     * @param l1  The first collection
     * @param l2  The second collection
     * @return TRUE if the two collections are equivalent
     *         FALSE, otherwise
     */
    public static boolean isEquivalent(LinkedList l1, LinkedList l2) {
        if ((l1 != null) && (l2 != null)) {
            // This is a strict test.
            // Returns true if the specified object is also a list, 
            // both lists have the same size, and all corresponding pairs 
            // of elements in the two lists are equal where
            // they contain the same elements in the same order.
            return (l1.equals(l2));
        } else if ((l1 == null) && (l2 == null)) {
            return true;
        } else {
            // mismatch
            return false;
        }
    }


    /**
     * Trace the NotSerializable exception for the specified object
     * if this is the first time that the specified
     * object has caused the exception.
     * 
     * @param obj         The object being saved or restored
     * @param nse         The exception object with the details of the error
     * @param objDesc     The description of the object, eg, like the field name where it is being used
     * @param methodName  The method name which encountered the exception
     * @param desc        Text to be used for tracing
     */
    public static void traceNotSerializable(Object obj, NotSerializableException nse, String objDesc, String methodName, String desc) {
        if (log.isTraceEnabled() == false) {
            // if no tracing is being done, there's nothing to do
            // exit quickly
            return;
        }

        if (obj != null) {
            String objName = obj.getClass().getName();

            if (NotSerializableList.get(objName) == null) {
                // set up some information about the exception
                // for now, just use an initial counter, which we aren't doing much with
                // but takes less space than the original object that caused the exception
                // future: consider using a trace information object that would
                //         contain a count of the times that a particular class
                //         caused the exception, the class name of that class,
                //         and the stack trace for the first time - this information
                //         could then be accessed from a utility
                Integer counter = new Integer(1);

                // add to table
                NotSerializableList.put(objName,counter);

                // trace point
                log.trace("ObjectStateUtils: ***NotSerializableException*** ["+nse.getMessage()+"] in method ["+methodName+"] for object ["+objName+"]  associated with ["+objDesc+"].  "+desc);
            }

        }
        
    }

}
