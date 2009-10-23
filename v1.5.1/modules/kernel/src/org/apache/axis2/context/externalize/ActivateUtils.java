/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.context.externalize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.util.MetaDataEntry;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Some Externalize objects must be "activated" after they are read.
 * Activation normally involves associating the object with objects in the current
 * runtime.
 * 
 * ActivateUtils provides activation related utilities
 */
public class ActivateUtils {
    
    private static final Log log = LogFactory.getLog(ActivateUtils.class);
    
    /**
     * Private Constructor
     * All methods in ActivateUtils are static.
     */
    private ActivateUtils() {}

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
    public static AxisServiceGroup findServiceGroup(AxisConfiguration axisConfig,
                                                    String serviceGrpClassName,
                                                    String serviceGrpName) {
        Iterator its = axisConfig.getServiceGroups();

        while (its.hasNext()) {
            AxisServiceGroup serviceGroup = (AxisServiceGroup) its.next();

            String tmpSGClassName = serviceGroup.getClass().getName();
            String tmpSGName = serviceGroup.getServiceGroupName();

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
                        log.trace("ObjectStateUtils:findServiceGroup(): returning  ["
                                + serviceGrpClassName + "]   [" + serviceGrpName + "]");
                    }

                    return serviceGroup;
                }
            }
        }

        // trace point
        if (log.isTraceEnabled()) {
            log.trace("ObjectStateUtils:findServiceGroup(): [" + serviceGrpClassName + "]   ["
                    + serviceGrpName + "]  returning  [null]");
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
    public static AxisService findService(AxisConfiguration axisConfig, String serviceClassName,
                                          String serviceName) {
        HashMap services = axisConfig.getServices();

        Iterator its = services.values().iterator();

        while (its.hasNext()) {
            AxisService service = (AxisService) its.next();

            String tmpServClassName = service.getClass().getName();
            String tmpServName = service.getName();

            if ((tmpServClassName.equals(serviceClassName)) && (tmpServName.equals(serviceName))) {
                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:findService(): returning  [" + serviceClassName
                            + "]   [" + serviceName + "]");
                }

                return service;
            }
        }

        // trace point
        if (log.isTraceEnabled()) {
            log.trace("ObjectStateUtils:findService(): [" + serviceClassName + "]   ["
                    + serviceName + "]  returning  [null]");
        }

        return null;
    }
    
    /**
     * Find the AxisOperation object that matches the criteria
     * 
     * @param axisConfig The AxisConfiguration object
     * @param opClassName the class name string for the target object
     *                   (could be a derived class)
     * @param opQName    the name associated with the operation
     * @return the AxisOperation object that matches the given criteria
     */
    public static AxisOperation findOperation(AxisConfiguration axisConfig, String opClassName,
                                              QName opQName) {
        HashMap services = axisConfig.getServices();

        Iterator its = services.values().iterator();

        while (its.hasNext()) {
            AxisService service = (AxisService) its.next();

            Iterator ito = service.getOperations();

            while (ito.hasNext()) {
                AxisOperation operation = (AxisOperation) ito.next();

                String tmpOpName = operation.getClass().getName();
                QName tmpOpQName = operation.getName();

                if ((tmpOpName.equals(opClassName)) && (tmpOpQName.equals(opQName))) {
                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace("ObjectStateUtils:findOperation(axisCfg): returning  ["
                                + opClassName + "]   [" + opQName.toString() + "]");
                    }

                    return operation;
                }
            }
        }

        // trace point
        if (log.isTraceEnabled()) {
            log.trace("ObjectStateUtils:findOperation(axisCfg): [" + opClassName + "]   ["
                    + opQName.toString() + "]  returning  [null]");
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
    public static AxisOperation findOperation(AxisService service, String opClassName,
                                              QName opQName) {
        if (service == null) {
            return null;
        }

        Iterator ito = service.getOperations();

        // Previous versions of Axis2 didn't use a namespace on the operation name, so they wouldn't
        // have externalized a namespace.  If that's the case, only compare the localPart of the
        // operation name
        String namespace = opQName.getNamespaceURI();
        boolean ignoreNamespace = false;
        if (namespace == null || "".equals(namespace)) {
            ignoreNamespace = true;
        }
        
        while (ito.hasNext()) {
            AxisOperation operation = (AxisOperation) ito.next();

            String tmpOpName = operation.getClass().getName();
            QName tmpOpQName = operation.getName();
            
            if ((tmpOpName.equals(opClassName)) && 
                ((ignoreNamespace && (tmpOpQName.getLocalPart().equals(opQName.getLocalPart())) || (tmpOpQName.equals(opQName))))) {

                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:findOperation(service): ignoreNamespace [" + ignoreNamespace
                    		+ "] returning  ["
                            + opClassName + "]   [" + opQName.toString() + "]");
                }

                return operation;
            }
        }

        // trace point
        if (log.isTraceEnabled()) {
            log.trace("ObjectStateUtils:findOperation(service): ignoreNamespace [" + ignoreNamespace
                    + " classname [" + opClassName + "]  QName ["
                    + opQName.toString() + "]  returning  [null]");
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
                log.trace("ObjectStateUtils:findMessage(): [" + msgName + "]  [" + msgElementName
                        + "] returning  [null] - no AxisOperation");
            }

            return null;
        }

        if (msgName == null) {
            // nothing to match with, expect to match against a name
            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:findMessage(): [" + msgName + "]  [" + msgElementName
                        + "] returning  [null] - message name is not set");
            }

            return null;
        }


        String tmpName = null;
        String tmpElementName = null;

        //-------------------------------------
        // first try the "out" message
        //-------------------------------------
        AxisMessage out = null;
        try {
            out = op.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        } catch (Exception ex) {
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
                log.trace("ObjectStateUtils:findMessage(): returning OUT message  [" + msgName
                        + "]  [" + msgElementName + "] ");
            }

            return out;
        }

        //-------------------------------------
        // next, try the "in" message 
        //-------------------------------------
        AxisMessage in = null;
        try {
            in = op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        } catch (Exception ex) {
            // just absorb the exception
        }

        if (in != null) {
            tmpName = in.getName();

            QName tmpQin = in.getElementQName();
            if (tmpQin != null) {
                tmpElementName = tmpQin.toString();
            }
        } else {
            tmpName = null;
            tmpElementName = null;
        }

        // check the criteria for a match

        matching = matchMessageNames(tmpName, tmpElementName, msgName, msgElementName);

        if (matching) {
            // trace point
            if (log.isTraceEnabled()) {
                log.trace("ObjectStateUtils:findMessage(): returning IN message [" + msgName
                        + "]  [" + msgElementName + "] ");
            }

            return in;
        }

        // if we got here, then no match was found

        // trace point
        if (log.isTraceEnabled()) {
            log.trace("ObjectStateUtils:findMessage(): [" + msgName + "]  [" + msgElementName
                    + "] returning  [null]");
        }

        return null;
    }
    
    /**
     * Find the Handler object that matches the criteria
     * 
     * @param existingHandlers The list of existing handlers and phases
     * @param handlerClassName the class name string for the target object
     *                   (could be a derived class)
     * @return the Handler object that matches the criteria
     */
    public static Object findHandler(List<Handler> existingHandlers, MetaDataEntry metaDataEntry) //String handlerClassName)
    {

        String title = "ObjectStateUtils:findHandler(): ";

        String handlerClassName = metaDataEntry.getClassName();
        String qNameAsString = metaDataEntry.getQNameAsString();

        for (int i = 0; i < existingHandlers.size(); i++) {
            if (existingHandlers.get(i) != null) {
                String tmpClassName = existingHandlers.get(i).getClass().getName();
                String tmpName = ((Handler) existingHandlers.get(i)).getName().toString();

                if ((tmpClassName.equals(handlerClassName)) && (tmpName.equals(qNameAsString))) {
                    // trace point
                    if (log.isTraceEnabled()) {
                        log.trace(title + " [" + handlerClassName + "]  name [" + qNameAsString
                                + "]  returned");
                    }

                    return (Handler) (existingHandlers.get(i));
                }
            }
        }

        // trace point
        if (log.isTraceEnabled()) {
            log.trace(title + " [" + handlerClassName + "]  name [" + qNameAsString
                    + "] was not found in the existingHandlers list");
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
    public static TransportListener findTransportListener(AxisConfiguration axisConfig,
                                                          String listenerClassName) {
        // TODO: investigate a better technique to match up with a TransportListener

        HashMap transportsIn = axisConfig.getTransportsIn();

        // get a collection of the values in the map
        Collection values = transportsIn.values();

        Iterator i = values.iterator();

        while (i.hasNext()) {
            TransportInDescription ti = (TransportInDescription) i.next();

            TransportListener tl = ti.getReceiver();
            String tlClassName = tl.getClass().getName();

            if (tlClassName.equals(listenerClassName)) {
                // trace point
                if (log.isTraceEnabled()) {
                    log.trace("ObjectStateUtils:findTransportListener():  [" + listenerClassName
                            + "]  returned");
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
                    log.trace("ObjectStateUtils:isEquivalent(ArrayList,ArrayList): FALSE - size mismatch ["
                            + size1 + "] != [" + size2 + "]");
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
                            log.trace("ObjectStateUtils:isEquivalent(ArrayList,ArrayList): FALSE - mismatch with element ["
                                    + obj1.getClass().getName() + "] ");
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
    private static boolean matchMessageNames(String name1, String elementName1, String name2,
                                             String elementName2) {
        // the name for the message must exist
        if ((name1 != null) && (name2 != null) && (name1.equals(name2))) {
            // there's a match on the name associated with the message object

            // element names need to match, including being null
            if ((elementName1 == null) && (elementName2 == null)) {
                // there's a match for the nulls
                return true;
            } else if ((elementName1 != null) && (elementName2 != null)
                    && (elementName1.equals(elementName2))) {
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

}
