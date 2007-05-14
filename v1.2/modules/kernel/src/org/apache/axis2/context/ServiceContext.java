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


package org.apache.axis2.context;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.MetaDataEntry;
import org.apache.axis2.util.ObjectStateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

/**
 * Well this is never clearly defined, what it does or the life-cycle.
 * So do NOT use this as it might not live up to your expectation.
 */
public class ServiceContext extends AbstractContext implements Externalizable {

    /*
     * setup for logging
     */
    private static final Log log = LogFactory.getLog(ServiceContext.class);

    private static final String myClassName = "ServiceContext";

    /**
     * An ID which can be used to correlate operations on an instance of
     * this object in the log files
     */
    private String logCorrelationIDString = myClassName + "@" + UUIDGenerator.getUUID();


    /**
     * @serial The serialization version ID tracks the version of the class.
     * If a class definition changes, then the serialization/externalization
     * of the class is affected. If a change to the class is made which is
     * not compatible with the serialization/externalization of the class,
     * then the serialization version ID should be updated.
     * Refer to the "serialVer" utility to compute a serialization
     * version ID.
     */
    private static final long serialVersionUID = 8265625275015738957L;

    /**
     * @serial Tracks the revision level of a class to identify changes to the
     * class definition that are compatible to serialization/externalization.
     * If a class definition changes, then the serialization/externalization
     * of the class is affected.
     * Refer to the writeExternal() and readExternal() methods.
     */
    // supported revision levels, add a new level to manage compatible changes
    private static final int REVISION_1 = 1;
    // current revision level of this object
    private static final int revisionID = REVISION_1;


    public static final String SERVICE_OBJECT = "serviceObject";

    private EndpointReference targetEPR;
    private EndpointReference myEPR;

    private transient AxisService axisService;

    // the service group context is the same as the parent
    private transient ServiceGroupContext serviceGroupContext;

    private transient ConfigurationContext configContext;

    /**
     * Should we cache the last OperationContext?
     */
    private boolean cachingOperationContext;
    /**
     * A cache for the last OperationContext
     */
    private transient OperationContext lastOperationContext;

    //----------------------------------------------------------------
    // MetaData for data to be restored in activate after readExternal
    //----------------------------------------------------------------

    /**
     * Indicates whether the message context has been reconstituted
     * and needs to have its object references reconciled
     */
    private transient boolean needsToBeReconciled = false;

    /**
     * The AxisService metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaAxisService = null;

    /**
     * The ServiceGroupContext object will be used during
     * activate to finish its restoration
     */
    private transient ServiceGroupContext metaParent = null;

    //----------------------------------------------------------------
    // end MetaData section
    //----------------------------------------------------------------

    // simple constructor
    public ServiceContext() {
        super(null);
    }


    public ServiceContext(AxisService serviceConfig, ServiceGroupContext serviceGroupContext) {
        super(serviceGroupContext);
        this.serviceGroupContext = serviceGroupContext;
        this.axisService = serviceConfig;
        this.configContext = (ConfigurationContext) parent.getParent();
    }

    public OperationContext createOperationContext(QName name) {
        AxisOperation axisOp = axisService.getOperation(name);
        return createOperationContext(axisOp);
    }

    public OperationContext createOperationContext(AxisOperation axisOp) {
        OperationContext ctx = ContextFactory.createOperationContext(axisOp, this);
        if (cachingOperationContext) {
            // Squirrel this away for anyone who wants it later
            lastOperationContext = ctx;
        }
        return ctx;
    }

    public AxisService getAxisService() {
        checkActivateWarning("getAxisService");
        return axisService;
    }

    public ConfigurationContext getConfigurationContext() {
        checkActivateWarning("getConfigurationContext");
        return configContext;
    }

    public ServiceGroupContext getServiceGroupContext() {
        checkActivateWarning("getServiceGroupContext");
        return serviceGroupContext;
    }

    /**
     * To get the ERP for a given service , if the transport is present and not
     * running then it will add as a listener to ListenerManager , there it will
     * init that and start the listener , and finally ask the EPR from transport
     * for a given service
     *
     * @param transport : Name of the transport
     * @return
     * @throws AxisFault
     */
    public EndpointReference getMyEPR(String transport) throws AxisFault {
        axisService.isEnableAllTransports();
        ConfigurationContext configctx = this.configContext;
        if (configctx != null) {
            ListenerManager lm = configctx.getListenerManager();
            if (!lm.isListenerRunning(transport)) {
                TransportInDescription trsin =
                        configctx.getAxisConfiguration().getTransportIn(transport);
                if (trsin != null) {
                    lm.addListener(trsin, false);
                } else {
                    throw new AxisFault(Messages.getMessage("transportnotfound",
                                                            transport));
                }
            }
            if (!lm.isStopped()) {
                return lm.getEPRforService(axisService.getName(), null, transport);
            }
        }
        return null;
    }

    public EndpointReference getTargetEPR() {
        return targetEPR;
    }

    public void setTargetEPR(EndpointReference targetEPR) {
        this.targetEPR = targetEPR;
    }

    public EndpointReference getMyEPR() {
        if (myEPR == null) {
            try {
                if (ListenerManager.defaultConfigurationContext != null) {
                    ListenerManager listenerManager =
                            ListenerManager.defaultConfigurationContext.getListenerManager();
                    myEPR = listenerManager.getEPRforService(axisService.getName(), null, null);
                }
            } catch (AxisFault axisFault) {
                // what else I can do 
                myEPR = null;
            }
        }
        return myEPR;
    }

    public void setMyEPR(EndpointReference myEPR) {
        this.myEPR = myEPR;
    }

    public OperationContext getLastOperationContext() {
        return lastOperationContext;
    }

    public void setLastOperationContext(OperationContext lastOperationContext) {
        this.lastOperationContext = lastOperationContext;
    }

    public boolean isCachingOperationContext() {
        return cachingOperationContext;
    }

    public void setCachingOperationContext(boolean cacheLastOperationContext) {
        this.cachingOperationContext = cacheLastOperationContext;
    }


    /**
     * Returns a name associated with this ServiceContext.
     * <p/>
     * Note: this name is from the corresponding
     * AxisService object.
     *
     * @return The name string, or null if no name can be found
     */
    public String getName() {
        if (axisService != null) {
            return axisService.getName();
        }

        if (metaAxisService != null) {
            return metaAxisService.getName();
        }

        return null;
    }


    /**
     * Returns a name associated with the ServiceGroupContext
     * associated with this ServiceContext.
     *
     * @return The name string, or null if no name can be found
     */
    public String getGroupName() {
        if (serviceGroupContext != null) {
            return serviceGroupContext.getId();
        }

        if (metaParent != null) {
            return metaParent.getId();
        }

        return null;
    }

    /* ===============================================================
     * Externalizable support 
     * ===============================================================
     */


    /**
     * Save the contents of this object.
     * <p/>
     * NOTE: Transient fields and static fields are not saved.
     * Also, objects that represent "static" data are
     * not saved, except for enough information to be
     * able to find matching objects when the message
     * context is re-constituted.
     *
     * @param out The stream to write the object contents to
     * @throws IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
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

        out.writeLong(getLastTouchedTime());

        out.writeBoolean(cachingOperationContext);

        ObjectStateUtils.writeString(out, logCorrelationIDString,
                                     logCorrelationIDString + ".logCorrelationIDString");

        // put some try..catch blocks around the following objects
        // so that the writing to the output stream continues
        // even if one of the objects can't be serialized

        try {
            // EndpointReference targetEPR
            ObjectStateUtils.writeObject(out, targetEPR, "ServiceContext.targetEPR");
        }
        catch (Exception e1) {
            // note that the utility class will provide the trace for the 
            // exception so we won't have to
            // so just consume the exception for now
        }


        try {
            // EndpointReference myEPR
            ObjectStateUtils.writeObject(out, myEPR, "ServiceContext.myEPR");
        }
        catch (Exception e2) {
            // note that the utility class will provide the trace for the 
            // exception so we won't have to
            // so just consume the exception for now
        }

        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------
        Map tmpMap = getProperties();

        HashMap tmpHashMap = null;

        if ((tmpMap != null) && (!tmpMap.isEmpty())) {
            tmpHashMap = new HashMap(tmpMap);
        }

        ObjectStateUtils.writeHashMap(out, tmpHashMap, "ServiceContext.properties");

        //---------------------------------------------------------
        // AxisService
        //---------------------------------------------------------

        String axisServMarker = "ServiceContext.metaAxisService";
        ObjectStateUtils.writeString(out, axisServMarker, axisServMarker);

        if (axisService == null) {
            out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);
        } else {
            out.writeBoolean(ObjectStateUtils.ACTIVE_OBJECT);
            metaAxisService =
                    new MetaDataEntry(axisService.getClass().getName(), axisService.getName());
            ObjectStateUtils.writeObject(out, metaAxisService, "ServiceContext.metaAxisService");
        }

        //---------------------------------------------------------
        // parent 
        //---------------------------------------------------------
        // ServiceGroupContext serviceGroupContext;

        ServiceGroupContext myParent = (ServiceGroupContext) getParent();

        ObjectStateUtils.writeObject(out, myParent, "ServiceContext.parent ServiceGroupContext");

    }


    /**
     * Restore the contents of the object that was previously saved.
     * <p/>
     * NOTE: The field data must read back in the same order and type
     * as it was written.  Some data will need to be validated when
     * resurrected.
     *
     * @param in The stream to read the object contents from
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // set the flag to indicate that the message context is being
        // reconstituted and will need to have certain object references 
        // to be reconciled with the current engine setup
        needsToBeReconciled = true;

        // trace point
        log.trace(myClassName + ":readExternal():  BEGIN  bytes available in stream [" +
                in.available() + "]  ");

        //---------------------------------------------------------
        // object level identifiers
        //---------------------------------------------------------

        // serialization version ID
        long suid = in.readLong();

        // revision ID
        int revID = in.readInt();

        // make sure the object data is in a version we can handle
        if (suid != serialVersionUID) {
            throw new ClassNotFoundException(ObjectStateUtils.UNSUPPORTED_SUID);
        }

        // make sure the object data is in a revision level we can handle
        if (revID != REVISION_1) {
            throw new ClassNotFoundException(ObjectStateUtils.UNSUPPORTED_REVID);
        }

        //---------------------------------------------------------
        // various simple fields
        //---------------------------------------------------------

        long time = in.readLong();
        setLastTouchedTime(time);

        cachingOperationContext = in.readBoolean();

        logCorrelationIDString =
                ObjectStateUtils.readString(in, myClassName + ".logCorrelationIDString");

        // trace point
        log.trace(myClassName + ":readExternal():  reading input stream for [" +
                logCorrelationIDString + "]  ");

        // EndpointReference targetEPR
        targetEPR = (EndpointReference) ObjectStateUtils.readObject(in, "ServiceContext.targetEPR");

        // EndpointReference myEPR
        myEPR = (EndpointReference) ObjectStateUtils.readObject(in, "ServiceContext.myEPR");

        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------

        HashMap tmpHashMap = ObjectStateUtils.readHashMap(in, "ServiceContext.properties");

        properties = new HashMap();
        if (tmpHashMap != null) {
            setProperties(tmpHashMap);
        }

        //---------------------------------------------------------
        // AxisService
        //---------------------------------------------------------

        // axisService is not usable until the meta data has been reconciled

        ObjectStateUtils.readString(in, "ServiceContext.axisService");

        boolean metaAxisServiceIsActive = in.readBoolean();

        if (metaAxisServiceIsActive == ObjectStateUtils.ACTIVE_OBJECT) {
            metaAxisService = (MetaDataEntry) ObjectStateUtils
                    .readObject(in, "ServiceContext.metaAxisService");
        } else {
            metaAxisService = null;
        }

        //---------------------------------------------------------
        // parent 
        //---------------------------------------------------------

        // ServiceGroupContext is not usable until it has been activated 

        metaParent = (ServiceGroupContext) ObjectStateUtils
                .readObject(in, "ServiceContext.parent ServiceGroupContext");

        //---------------------------------------------------------
        // other
        //---------------------------------------------------------

        // currently not saving this object
        lastOperationContext = null;

        //---------------------------------------------------------
        // done
        //---------------------------------------------------------
    }


    /**
     * This method checks to see if additional work needs to be
     * done in order to complete the object reconstitution.
     * Some parts of the object restored from the readExternal()
     * cannot be completed until we have a configurationContext
     * from the active engine. The configurationContext is used
     * to help this object to plug back into the engine's
     * configuration and deployment objects.
     *
     * @param cc The configuration context object representing the active configuration
     */
    public void activate(ConfigurationContext cc) {
        // see if there's any work to do
        if (!needsToBeReconciled) {
            // return quick
            return;
        }

        // use the supplied configuration context
        configContext = cc;

        // get the axis configuration 
        AxisConfiguration axisConfig = cc.getAxisConfiguration();

        // We previously saved metaAxisService; restore it
        axisService = null;

        if (metaAxisService != null) {
            axisService = ObjectStateUtils.findService(axisConfig, metaAxisService.getClassName(),
                                                       metaAxisService.getQNameAsString());
        }

        // the parent ServiceGroupContext object was saved
        // either use the restored object or sync up with 
        // an existing ServiceGroupContext object
        if (metaParent != null) {
            // find out if a copy of the ServiceGroupContext object exists on this
            // engine where this ServiceContext is being restored/activated
            // if so, use that object instead of the restored object
            // in order to make sure that future changes to group-level 
            // properties are preserved for future operations
            String groupName = metaParent.getId();

            ServiceGroupContext existingSGC = null;

            ServiceGroupContext sgc = cc.getServiceGroupContext(groupName);

            if (existingSGC == null) {
                // could not find an existing ServiceGroupContext
                // use the restored object
                metaParent.activate(cc);

                // set parent 
                this.setParent(metaParent);
            } else {
                // switch over to the existing object
                this.setParent(existingSGC);

                // do a copy of the properties from the restored object
                // to the existing ServiceContext
                // Should the copy be a non-destructive one?  That is,
                // if the key already exists in the properties table of the
                // existing object, should the value be overwritten from the 
                // restored ojbect? For now, the decision is that the state
                // that has been preserved for a saved context object is
                // is important to be restored.
                metaParent.putContextProperties(existingSGC);
            }

        } else {
            // set parent to null
            this.setParent(metaParent);
        }

        // this is another pointer to our parent object
        serviceGroupContext = (ServiceGroupContext) this.getParent();

        if (serviceGroupContext != null) {
            // add the service context to the table
            serviceGroupContext.addServiceContext(this);
        }

        //-------------------------------------------------------
        // done, reset the flag
        //-------------------------------------------------------
        needsToBeReconciled = false;

        // make sure this restored object is in the parent's list
        if (metaParent != null) {
            // make sure this restored object is in the parent's list
            metaParent.addServiceContext(this);
        }

    }

    /**
     * This will do a copy of the properties from this context object
     * to the properties of the specified context object.
     *
     * @param context            The ServiceContext object to hold the merged properties
     * @param doParentProperties Indicates whether to go up the context hierachy
     *                           copy the properties at each level
     */
    public void putContextProperties(ServiceContext context, boolean doParentProperties) {
        if (context != null) {
            // get the current properties on this context object
            Map props = getProperties();

            // copy them to the specified context object
            context.mergeProperties(props);

            if (doParentProperties) {
                ServiceGroupContext mySGC = null;

                if (serviceGroupContext != null) {
                    mySGC = serviceGroupContext;
                } else if (metaParent != null) {
                    mySGC = metaParent;
                }

                if (mySGC != null) {
                    ServiceGroupContext sgc = context.getServiceGroupContext();
                    mySGC.putContextProperties(sgc);
                }
            }
        }
    }


    /**
     * Compares key parts of the state from the current instance of
     * this class with the specified instance to see if they are
     * equivalent.
     * <p/>
     * This differs from the java.lang.Object.equals() method in
     * that the equals() method generally looks at both the
     * object identity (location in memory) and the object state
     * (data).
     * <p/>
     *
     * @param ctx
     * @return TRUE if this object is equivalent with the specified object
     *         that is, key fields match
     *         FALSE, otherwise
     */
    public boolean isEquivalent(ServiceContext ctx) {
        // NOTE: the input object is expected to exist (ie, be non-null)

        if (!this.axisService.equals(ctx.getAxisService())) {
            return false;
        }


        EndpointReference targetEPR2 = ctx.getTargetEPR();

        if ((this.targetEPR != null) && (targetEPR2 != null)) {
            if (!this.targetEPR.isEquivalent(targetEPR2)) {
                return false;
            }
        } else if ((this.targetEPR == null) && (targetEPR2 == null)) {
            // keep going
        } else {
            // one of the objects is null
            return false;
        }

        EndpointReference myEPR2 = ctx.getMyEPR();

        if ((this.myEPR != null) && (myEPR2 != null)) {
            if (!this.myEPR.isEquivalent(myEPR2)) {
                return false;
            }
        } else if ((this.myEPR == null) && (myEPR2 == null)) {
            // keep going
        } else {
            // one of the objects is null
            return false;
        }

        // TODO: consider checking the parent objects for equivalency

        // TODO: consider checking fields from the super class for equivalency

        return true;
    }


    /**
     * Get the ID associated with this object instance.
     *
     * @return A string that can be output to a log file as an identifier
     *         for this object instance.  It is suitable for matching related log
     *         entries.
     */
    public String getLogCorrelationIDString() {
        return logCorrelationIDString;
    }


    /**
     * Trace a warning message, if needed, indicating that this
     * object needs to be activated before accessing certain fields.
     *
     * @param methodname The method where the warning occurs
     */
    private void checkActivateWarning(String methodname) {
        if (needsToBeReconciled) {
            log.warn(logCorrelationIDString + ":" + methodname + "(): ****WARNING**** " +
                    myClassName + ".activate(configurationContext) needs to be invoked.");
        }
    }

    public ConfigurationContext getRootContext() {
        return configContext;
    }


}
