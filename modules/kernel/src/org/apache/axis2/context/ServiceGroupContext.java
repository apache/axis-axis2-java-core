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

import org.apache.axis2.AxisFault;
import org.apache.axis2.cluster.ClusterManager;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.MetaDataEntry;
import org.apache.axis2.util.ObjectStateUtils;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServiceGroupContext extends AbstractContext implements Externalizable {

    /*
     * setup for logging
     */
    private static final Log log = LogFactory.getLog(ServiceGroupContext.class);

    private static final String myClassName = "ServiceGroupContext";

    /**
     * @serial The serialization version ID tracks the version of the class.
     * If a class definition changes, then the serialization/externalization
     * of the class is affected. If a change to the class is made which is
     * not compatible with the serialization/externalization of the class,
     * then the serialization version ID should be updated.
     * Refer to the "serialVer" utility to compute a serialization
     * version ID.
     */
    private static final long serialVersionUID = 9014471144479928885L;

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


    private transient AxisServiceGroup axisServiceGroup;
    private String id;
    private Map serviceContextMap;

    //----------------------------------------------------------------
    // MetaData for data to be restored in activate after readExternal
    //----------------------------------------------------------------
    
    /**
     * Indicates whether the message context has been reconstituted
     * and needs to have its object references reconciled
     */
    private transient boolean needsToBeReconciled = false;


    /**
     * The AxisServiceContext metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaAxisServiceGroup = null;
    

    //----------------------------------------------------------------
    // end MetaData section
    //----------------------------------------------------------------

    // simple constructor
    public ServiceGroupContext() {
        super(null);
        serviceContextMap = new HashMap();
    }


    public ServiceGroupContext(ConfigurationContext parent, AxisServiceGroup axisServiceGroup) {
        super(parent);
        this.axisServiceGroup = axisServiceGroup;
        serviceContextMap = new HashMap();
        // initially set the id to the axisServiceGroup
        if (axisServiceGroup != null) {
            setId(axisServiceGroup.getServiceGroupName());
        }
    }

    public AxisServiceGroup getDescription() {
        checkActivateWarning("getDescription");
        return axisServiceGroup;
    }

    public String getId() {
        return id;
    }

    /**
     * Gets a service context. Creates a new one from AxisService.
     * There is no need to store service context inside serviceGroup
     * context as well.
     *
     * @param service
     * @return Returns ServiceContext.
     * @throws AxisFault
     */
    public ServiceContext getServiceContext(AxisService service) throws AxisFault {
        AxisService axisService = axisServiceGroup.getService(service.getName());
        if (axisService == null) {
            throw new AxisFault(Messages.getMessage("invalidserviceinagroup",
                    service.getName(), axisServiceGroup.getServiceGroupName()));
        }
        if (serviceContextMap == null) {
            serviceContextMap = new HashMap();
        }
        ServiceContext serviceContext = (ServiceContext) serviceContextMap.get(service.getName());
        if (serviceContext == null) {
            serviceContext = new ServiceContext(service, this);
            serviceContextMap.put(service.getName(), serviceContext);
            
            ClusterManager clusterManager = axisService.getAxisConfiguration().getClusterManager();
            if (clusterManager!=null) {
                clusterManager.addContext(serviceContext);
            }
            
        }
        return serviceContext;
    }

    public Iterator getServiceContexts() {
        if (serviceContextMap == null) {
            serviceContextMap = new HashMap();
        }
        if (serviceContextMap.isEmpty()) {
            return null;
        }
        return serviceContextMap.values().iterator();
    }

    public void setId(String id) {
        this.id = id;
    }



    /**
     * Adds the specified service context object to the
     * lists of service contexts for this service group
     * context.
     * 
     * @param srvctx The ServiceContext object to add
     */
    public void addServiceContext(ServiceContext srvctx) {
        if (srvctx == null) {
            return;
        }

        AxisService axisService = srvctx.getAxisService();
        
        if (axisService == null) {
            return;
        }

        if (serviceContextMap == null) {
            serviceContextMap = new HashMap();
        }

        serviceContextMap.put(axisService.getName(), srvctx);

    }


    /**
     * Finds the service context object that corresponds
     * to the specified name from the list
     * of service contexts for this service group
     * context.
     * 
     * @param name The name associated with the ServiceContext
     * @return The ServiceContext associated with the name,
     *         or null, if none can be found
     */
    public ServiceContext findServiceContext(String name) {
        if (serviceContextMap == null) {
            return null;
        }

        // a map allows a null key
        return (ServiceContext) serviceContextMap.get(name);
    }


    /**
     * Finds the service context object that corresponds
     * to the specified AxisService from the list
     * of service contexts for this service group
     * context.
     * 
     * @return The ServiceContext associated with the AxisService
     *         or null, if none can be found
     */
    public ServiceContext findServiceContext(AxisService axisSrv) {
        if (axisSrv == null) {
            return null;
        }

        if (serviceContextMap == null) {
            return null;
        }

        return (ServiceContext) serviceContextMap.get(axisSrv.getName());
    }

    /**                         
     * This will do a copy of the properties from this context object 
     * to the properties of the specified context object.
     *
     * @param context The ServiceGroupContext object to hold the merged properties
     */
    public void putContextProperties(ServiceGroupContext context) {
        if (context != null) {
            // get the current properties on this context object
            Map props = getProperties();

            // copy them to the specified context object
            context.mergeProperties(props);
        }
    }


    /* ===============================================================
     * Externalizable support 
     * ===============================================================
     */
    

    /**
     * Save the contents of this object.
     * <p/>
     * NOTE: Transient fields and static fields are not saved.
     *       Also, objects that represent "static" data are
     *       not saved, except for enough information to be
     *       able to find matching objects when the message
     *       context is re-constituted.
     *
     * @param out    The stream to write the object contents to
     * @throws IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
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

        out.writeLong(getLastTouchedTime());

        if (id == null) {
            // generate an ID to use when this object is restored
            id = UUIDGenerator.getUUID();
        }

        ObjectStateUtils.writeString(out, id, "ServiceGroupContext.id");

        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------
        Map tmpMap = getProperties();

        HashMap tmpHashMap = null;

        if ((tmpMap != null) && (!tmpMap.isEmpty())) {
            tmpHashMap = new HashMap(tmpMap);
        }

        ObjectStateUtils.writeHashMap(out, tmpHashMap, "ServiceGroupContext.properties");


        //---------------------------------------------------------
        // AxisServiceGroup
        //---------------------------------------------------------

        String axisServGrpMarker = "ServiceGroupContext.axisServiceGroup";
        ObjectStateUtils.writeString(out, axisServGrpMarker, axisServGrpMarker);

        if (axisServiceGroup == null) {
            out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);
        } else {
            out.writeBoolean(ObjectStateUtils.ACTIVE_OBJECT);
            metaAxisServiceGroup = new MetaDataEntry(axisServiceGroup.getClass().getName(), axisServiceGroup.getServiceGroupName());
            ObjectStateUtils.writeObject(out, metaAxisServiceGroup, "ServiceGroupContext.metaAxisServiceGroup");
        }
        

        //---------------------------------------------------------
        // parent 
        //---------------------------------------------------------

        // the parent is the ConfigurationContext object, which
        // at this time, is not being saved.
        // instead, we will need to register this ServiceGroupObject
        // with the existing ConfigurationContext object when
        // this object is reconstituted

    }


    /**
     * Restore the contents of the object that was previously saved.
     * <p/>
     * NOTE: The field data must read back in the same order and type
     * as it was written.  Some data will need to be validated when 
     * resurrected.
     *
     * @param in    The stream to read the object contents from 
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // set the flag to indicate that the message context is being
        // reconstituted and will need to have certain object references 
        // to be reconciled with the current engine setup
        needsToBeReconciled = true;

        // trace point
        if (log.isTraceEnabled()) {
            log.trace(myClassName+":readExternal():  BEGIN  bytes available in stream ["+in.available()+"]  ");
        }

        // serialization version ID
        long suid = in.readLong();

        // revision ID
        int  revID = in.readInt();

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

        id = ObjectStateUtils.readString(in, "ServiceGroupContext.id");


        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------

        HashMap tmpHashMap = ObjectStateUtils.readHashMap(in,"ServiceGroupContext.properties");

        properties = new HashMap();
        if (tmpHashMap != null) {
            setProperties(tmpHashMap);
        }

        //---------------------------------------------------------
        // AxisServiceGroup
        //---------------------------------------------------------

        // axisServiceGroup is not usable until the meta data has been reconciled
        axisServiceGroup = null;

        ObjectStateUtils.readString(in, "ServiceGroupContext.axisServiceGroup");
        
        boolean metaAxisServiceGrpIsActive = in.readBoolean();

        if (metaAxisServiceGrpIsActive == ObjectStateUtils.ACTIVE_OBJECT) {
            metaAxisServiceGroup = (MetaDataEntry) ObjectStateUtils.readObject(in, "ServiceGroupContext.metaAxisServiceGroup");
        } else {
            metaAxisServiceGroup = null;
        }
        


        //---------------------------------------------------------
        // parent 
        //---------------------------------------------------------

        // the parent is the ConfigurationContext object, whic
        // at this time, is not being saved.
        // instead, we will need to register this ServiceGroupObject
        // with the existing ConfigurationContext object when
        // this object is reconstituted


        //---------------------------------------------------------
        // other
        //---------------------------------------------------------
        serviceContextMap = new HashMap();


        //---------------------------------------------------------
        // done
        //---------------------------------------------------------

    }

    
    /**                     
     * Some parts of the object restored from the 
     * readExternal deserialization work cannot be completed until
     * we have a configurationContext.  This method checks to see
     * if additional work needs to be done in order to complete
     * the object reconstitution.
     */
    public void activate(ConfigurationContext cc) {
        // see if there's any work to do
        if (!needsToBeReconciled) {
            // return quick
            return;
        }

        // get the axis configuration 
        AxisConfiguration axisConfig = cc.getAxisConfiguration();

        // We previously saved metaAxisServiceGroup; restore it
        if (metaAxisServiceGroup != null) {
            axisServiceGroup = ObjectStateUtils.findServiceGroup(axisConfig, metaAxisServiceGroup.getClassName(), metaAxisServiceGroup.getQNameAsString());
        } else {
            axisServiceGroup = null;
        }

        // set parent 
        this.setParent(cc);

        // register with the parent
        cc.registerServiceGroupContextintoSoapSessionTable(this);


        //-------------------------------------------------------
        // done, reset the flag
        //-------------------------------------------------------
        needsToBeReconciled = false;

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
     * @param ctx  The object to compare with
     * @return TRUE if this object is equivalent with the specified object
     *              that is, key fields match
     *         FALSE, otherwise
     */
    public boolean isEquivalent(ServiceGroupContext ctx) {
        // NOTE: the input object is expected to exist (ie, be non-null)

        if (!this.axisServiceGroup.equals(ctx.getDescription())) {
            return false;
        }

        String ctxid = ctx.getId();

        if ((this.id != null) && (ctxid != null)) {
            if (!this.id.equals(ctxid)) {
                return false;
            }

        } else if ((this.id == null) && (ctxid == null)) {
            // keep going
        } else {
            // mismatch
            return false;
        }



        // TODO: consider checking the parent objects for equivalency

        // TODO: consider checking fields from the super class for equivalency

        return true;
    }

    /**
     * Trace a warning message, if needed, indicating that this 
     * object needs to be activated before accessing certain fields.
     * 
     * @param methodname The method where the warning occurs
     */
    private void checkActivateWarning(String methodname) {
        if (needsToBeReconciled) {
            log.warn(myClassName+":"+methodname+"(): ****WARNING**** "+myClassName+".activate(configurationContext) needs to be invoked.");
        }
    }
    
    public ConfigurationContext getRootContext() {
        //parent of the ServiceGroupContext is the ConfigurationContext
        return (ConfigurationContext) this.getParent();
    }
}
