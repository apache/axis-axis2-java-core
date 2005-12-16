/*
* Copyright 2001, 2002,2004 The Apache Software Foundation.
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


package org.apache.axis2.transport.jms;

import org.apache.axis2.AxisFault;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Iterator;

/**
 * JMSConnectorManager manages a pool of connectors and works with the
 * vendor adapters to support the reuse of JMS connections.
 */
public class JMSConnectorManager {
    protected static Log log =
            LogFactory.getLog(JMSConnectorManager.class.getName());
    private static JMSConnectorManager s_instance = new JMSConnectorManager();
    private static HashMap vendorConnectorPools = new HashMap();
    private int DEFAULT_WAIT_FOR_SHUTDOWN = 90000;    // 1.5 minutes

    private JMSConnectorManager() {
    }

    /**
     * Adds a JMSConnector to the appropriate vendor pool
     */
    public void addConnectorToPool(JMSConnector conn) {
        if (log.isDebugEnabled()) {
            log.debug("Enter: JMSConnectorManager::addConnectorToPool");
        }

        ShareableObjectPool vendorConnectors = null;

        synchronized (vendorConnectorPools) {
            String vendorId = conn.getVendorAdapter().getVendorId();

            vendorConnectors = getVendorPool(vendorId);

            // it's possible the pool does not yet exist (if, for example, the connector
            // is created before invoking the call/JMSTransport, as is the case with
            // SimpleJMSListener)
            if (vendorConnectors == null) {
                vendorConnectors = new ShareableObjectPool();
                vendorConnectorPools.put(vendorId, vendorConnectors);
            }
        }

        synchronized (vendorConnectors) {
            vendorConnectors.addObject(conn);
        }

        if (log.isDebugEnabled()) {
            log.debug("Exit: JMSConnectorManager::addConnectorToPool");
        }
    }

    /**
     * Closes JMSConnectors in all pools
     */
    void closeAllConnectors() {
        if (log.isDebugEnabled()) {
            log.debug("Enter: JMSConnectorManager::closeAllConnectors");
        }

        synchronized (vendorConnectorPools) {
            Iterator iter = vendorConnectorPools.values().iterator();

            while (iter.hasNext()) {

                // close all connectors in the vendor pool
                ShareableObjectPool pool = (ShareableObjectPool) iter.next();

                synchronized (pool) {
                    Object[] elements = pool.getElements().toArray();

                    for (int i = 0; i < elements.length; i++) {
                        JMSConnector conn = (JMSConnector) elements[i];

                        try {

                            // shutdown automatically decrements the ref count of a connector before closing it
                            // call reserve() to simulate the checkout
                            reserve(conn);
                            closeConnector(conn);
                        } catch (Exception e) {
                        }    // ignore. the connector is already being deactivated
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Exit: JMSConnectorManager::closeAllConnectors");
        }
    }

    private void closeConnector(JMSConnector conn) {
        conn.stop();
        conn.shutdown();
    }

    /**
     * Closes JMS connectors that match the specified endpoint address
     */
    void closeMatchingJMSConnectors(HashMap connectorProps, HashMap cfProps, String username,
                                    String password, JMSVendorAdapter vendorAdapter) {
        if (log.isDebugEnabled()) {
            log.debug("Enter: JMSConnectorManager::closeMatchingJMSConnectors");
        }

        try {
            String vendorId = vendorAdapter.getVendorId();

            // get the vendor-specific pool of connectors
            ShareableObjectPool vendorConnectors = null;

            synchronized (vendorConnectorPools) {
                vendorConnectors = getVendorPool(vendorId);
            }

            // it's possible that there is no pool for that vendor
            if (vendorConnectors == null) {
                return;
            }

            synchronized (vendorConnectors) {

                // close any matched connectors
                JMSConnector connector = null;

                while ((vendorConnectors.size() > 0)
                        && (connector =
                        JMSConnectorFactory.matchConnector(vendorConnectors.getElements(),
                                connectorProps, cfProps, username, password,
                                vendorAdapter)) != null) {
                    closeConnector(connector);
                }
            }
        } catch (Exception e) {
            log.warn(Messages.getMessage("failedJMSConnectorShutdown"), e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Exit: JMSConnectorManager::closeMatchingJMSConnectors");
        }
    }

    /**
     * Performs a non-exclusive checkin of the JMSConnector
     */
    public void release(JMSConnector connector) {
        ShareableObjectPool pool = null;

        synchronized (vendorConnectorPools) {
            pool = getVendorPool(connector.getVendorAdapter().getVendorId());
        }

        if (pool != null) {
            pool.release(connector);
        }
    }

    /**
     * Removes a JMSConnector from the appropriate vendor pool
     */
    public void removeConnectorFromPool(JMSConnector conn) {
        if (log.isDebugEnabled()) {
            log.debug("Enter: JMSConnectorManager::removeConnectorFromPool");
        }

        ShareableObjectPool vendorConnectors = null;

        synchronized (vendorConnectorPools) {
            vendorConnectors = getVendorPool(conn.getVendorAdapter().getVendorId());
        }

        if (vendorConnectors == null) {
            return;
        }

        synchronized (vendorConnectors) {

            // first release, to decrement the ref count (it is automatically incremented when
            // the connector is matched)
            vendorConnectors.release(conn);
            vendorConnectors.removeObject(conn);
        }

        if (log.isDebugEnabled()) {
            log.debug("Exit: JMSConnectorManager::removeConnectorFromPool");
        }
    }

    /**
     * Performs a non-exclusive checkout of the JMSConnector
     */
    public void reserve(JMSConnector connector) throws Exception {
        ShareableObjectPool pool = null;

        synchronized (vendorConnectorPools) {
            pool = getVendorPool(connector.getVendorAdapter().getVendorId());
        }

        if (pool != null) {
            pool.reserve(connector);
        }
    }

    /**
     * Retrieves a JMSConnector that satisfies the provided connector criteria
     */
    public JMSConnector getConnector(HashMap connectorProperties,
                                     HashMap connectionFactoryProperties, String username,
                                     String password, JMSVendorAdapter vendorAdapter)
            throws AxisFault {
        JMSConnector connector = null;

        try {

            // check for a vendor-specific pool, and create if necessary
            ShareableObjectPool vendorConnectors = getVendorPool(vendorAdapter.getVendorId());

            if (vendorConnectors == null) {
                synchronized (vendorConnectorPools) {
                    vendorConnectors = getVendorPool(vendorAdapter.getVendorId());

                    if (vendorConnectors == null) {
                        vendorConnectors = new ShareableObjectPool();
                        vendorConnectorPools.put(vendorAdapter.getVendorId(), vendorConnectors);
                    }
                }
            }

            // look for a matching JMSConnector among existing connectors
            synchronized (vendorConnectors) {
                try {
                    connector = JMSConnectorFactory.matchConnector(vendorConnectors.getElements(),
                            connectorProperties, connectionFactoryProperties, username, password,
                            vendorAdapter);
                } catch (Exception e) {
                }    // ignore. a new connector will be created if no match is found

                if (connector == null) {
                    connector = JMSConnectorFactory.createClientConnector(connectorProperties,
                            connectionFactoryProperties, username, password, vendorAdapter);
                    connector.start();
                }
            }
        } catch (Exception e) {
            log.error(Messages.getMessage("cannotConnectError"), e);

            if (e instanceof AxisFault) {
                throw(AxisFault) e;
            }

            throw new AxisFault("cannotConnect", e);
        }

        return connector;
    }

    public static JMSConnectorManager getInstance() {
        return s_instance;
    }

    /**
     * Returns the pool of JMSConnectors for a particular vendor
     */
    public ShareableObjectPool getVendorPool(String vendorId) {
        return (ShareableObjectPool) vendorConnectorPools.get(vendorId);
    }

    /**
     * A simple non-blocking pool impl for objects that can be shared.
     * Only a ref count is necessary to prevent collisions at shutdown.
     * Todo: max size, cleanup stale connections
     */
    public class ShareableObjectPool {
        private int m_numElements = 0;

        // maps object to ref count wrapper
        private java.util.HashMap m_elements;

        // holds objects which should no longer be leased (pending removal)
        private java.util.HashMap m_expiring;

        public ShareableObjectPool() {
            m_elements = new java.util.HashMap();
            m_expiring = new java.util.HashMap();
        }

        /**
         * Adds the object to the pool, if not already added
         */
        public void addObject(Object obj) {
            ReferenceCountedObject ref = new ReferenceCountedObject(obj);

            synchronized (m_elements) {
                if (!m_elements.containsKey(obj) && !m_expiring.containsKey(obj)) {
                    m_elements.put(obj, ref);
                }
            }
        }

        /**
         * Decrements the connector's reference count
         */
        public void release(Object obj) {
            synchronized (m_elements) {
                ReferenceCountedObject ref = (ReferenceCountedObject) m_elements.get(obj);

                ref.decrement();
            }
        }

        public void removeObject(Object obj) {
            removeObject(obj, DEFAULT_WAIT_FOR_SHUTDOWN);
        }

        /**
         * Removes the object from the pool.  If the object is reserved,
         * waits the specified time before forcibly removing
         * Todo: check expirations with the next request instead of holding up the current request
         */
        public void removeObject(Object obj, long waitTime) {
            ReferenceCountedObject ref = null;

            synchronized (m_elements) {
                ref = (ReferenceCountedObject) m_elements.get(obj);

                if (ref == null) {
                    return;
                }

                m_elements.remove(obj);

                if (ref.count() == 0) {
                    return;
                } else {

                    // mark the object for expiration
                    m_expiring.put(obj, ref);
                }
            }

            // connector is now marked for expiration. wait for the ref count to drop to zero
            long expiration = System.currentTimeMillis() + waitTime;

            while (ref.count() > 0) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }    // ignore

                if (System.currentTimeMillis() > expiration) {
                    break;
                }
            }

            // also clear from the expiring list
            m_expiring.remove(obj);
        }

        /**
         * Marks the connector as in use by incrementing the connector's reference count
         */
        public void reserve(Object obj) throws Exception {
            synchronized (m_elements) {
                if (m_expiring.containsKey(obj)) {
                    throw new Exception("resourceUnavailable");
                }

                ReferenceCountedObject ref = (ReferenceCountedObject) m_elements.get(obj);

                ref.increment();
            }
        }

        public synchronized int size() {
            return m_elements.size();
        }

        public synchronized java.util.Set getElements() {
            return m_elements.keySet();
        }

        /**
         * Wrapper to track the use count of an object
         */
        public class ReferenceCountedObject {
            private Object m_object;
            private int m_refCount;

            public ReferenceCountedObject(Object obj) {
                m_object = obj;
                m_refCount = 0;
            }

            public synchronized int count() {
                return m_refCount;
            }

            public synchronized void decrement() {
                if (m_refCount > 0) {
                    m_refCount--;
                }
            }

            public synchronized void increment() {
                m_refCount++;
            }
        }
    }
}
