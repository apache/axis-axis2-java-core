/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test the setting of properties on an AbstractContext instance.  Note that MessageContext is used
 * in most of the tests as the concrete implementation of AbstractContext.  Some tests also test
 * the HashTableUpdateLockable collection used in AbstractContext.proeprties directly. 
 */
public class ContextPropertiesTest extends TestCase {
    
    /**
     * Test to ensure using an Iterator from getPropertyNames() after the properties have been 
     * updated does not cause a ConcurrentModificationException.  The CME does not happen because
     * the Iterator is over a copy of the key names, not the actual collection (which would cause
     * a CME in this test).
     */
    public void testPropertyNamesForCME() {
        MessageContext mc = new MessageContext();

        mc.setProperty("key1", "value1");
        mc.setProperty("key2", "value2");
        mc.setProperty(null, "value3_nullkey");
        mc.setProperty("key4_nullvalue", null);

        // Get an iterator over the key names, then add a new element.
        // This simulates two threads accessing the context.  This should not cause
        // a ConcurrentModificationException (it will if a live iterator over the collection
        // key set is returned by getPropertyNames())
        Iterator propNamesIterator = mc.getPropertyNames();
        mc.setProperty("newKey", "newValue");
        while (propNamesIterator.hasNext()) {
            // Verify 
            // (1) that calling next() on the iterator does not cause a CME, 
            // (2) that the keys are as expected and 
            // (3) those keys can be used to access the entries in the collection.
            String checkKey = (String) propNamesIterator.next();
            Iterator concurrentIterator = mc.getPropertyNames();
            while (concurrentIterator.hasNext()) {
                String concurrentKey = (String) concurrentIterator.next();
            }
        }
    }
    
    /**
     * Test that using an iterator of propertyNames does not cause a CME.  This is degenerate
     * test of the test for CME that updates the collection before using the iterator.
     * @see #testPropertyNamesForCME()
     */
    public void testPropertyNamesForCME_NoUpdate() {
        MessageContext mc = new MessageContext();

        mc.setProperty("key1", "value1");
        mc.setProperty("key2", "value2");
        mc.setProperty(null, "value3_nullkey");
        mc.setProperty("key4_nullvalue", null);

        Iterator propNamesIterator = mc.getPropertyNames();
        while (propNamesIterator.hasNext()) {
            Iterator concurrentIterator = mc.getPropertyNames();
            // Verify 
            // (1) that calling next() on the iterator does not cause a CME, 
            // (2) that the keys are as expected and 
            // (3) those keys can be used to access the entries in the collection.
            String checkKey = (String) propNamesIterator.next();
        }
    }
    
    /**
     * Test some aspects of the Concurrent collection directly, such as creating with a Map. 
     */
    public void testHashMapUpdateLockable() {
        
        HashMapUpdateLockable testMap = new HashMapUpdateLockable();
        testMap.put("key", "value");
        testMap.put(1, 2);
        
        Map map = new HashMap();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put(null, "v3NullKey");
        map.put("k3_null", null);
        HashMapUpdateLockable testCtorMap = new HashMapUpdateLockable(map);
        assertEquals("v1", testCtorMap.get("k1"));
        assertEquals("v2", testCtorMap.get("k2"));
        assertEquals("v3NullKey", testCtorMap.get(null));
        assertNull(testCtorMap.get("k3_null"));
        
        // put returns the previous value if there was one, or null
        assertEquals("v1", testCtorMap.put("k1", "newK1Value"));
        assertNull(testCtorMap.put("k3_null", "newK3Value"));
        assertNull(testCtorMap.put("noSuchKey-put", "value6"));
        
        // remove returns the value if there was an entry with that key, or null
        assertNull(testCtorMap.remove("noSuchKey-remove"));
        testCtorMap.put("key7", null);
        assertNull(testCtorMap.remove("key7"));

    }
    
    // =============================================================================================
    // The following tests are multithreaded tests to test the locking, blocking, and releasing
    // of threads by the HashTableUpdateLockable collection.
    // =============================================================================================

    /**
     * Methods on HashTableUpdateLockable to be tested.  The update methods are tested to
     * verify they block while the updateLock is held.  Some non-update methods are tested to
     * verify they do not block if the updateLock is held. 
     */
    private enum MethodToCheck {checkLock_wait, checkLock_nowait, put, putAll, remove, get};
    
    /**
     * Test HashTableUpdateLockable.remove method which overrides the HashTable method.
     * Tests the HashTableUpdateLockable collection in a multithreaded environment to ensure that
     * a thread trying to update the collection will block as long as another thread has the
     * collection locked for update.  Note that UpdateRunnable performs the actual test of the
     * method.
     * @see UpdateRunnable#run()
     * 
     */
    public void testMultithreadUpdateLock_remove() {
        MultithreadUpdateLockMonitor testMonitor = new MultithreadUpdateLockMonitor();
        HashMapUpdateLockable testMap = new HashMapUpdateLockable();
        testMap.put("testKey1", "value1");
        testMap.put("removeKey", "value2");
        startupTestThreads(testMonitor, testMap, MethodToCheck.remove);

        assertNoThreadErrors(testMonitor);            

        // Make sure the update thread blocked, i.e. the lockThread was released before the update thread.
        assertTrue(testMonitor.lockThreadReleaseTime <= testMonitor.updateThreadReleaseTime);
    }

    /**
     * Test HashTableUpdateLockable.putAll method which overrides the HashTable method.
     * Tests the HashTableUpdateLockable collection in a multithreaded environment to ensure that
     * a thread trying to update the collection will block as long as another thread has the
     * collection locked for update.  Note that UpdateRunnable performs the actual test of the
     * method.
     * @see UpdateRunnable#run()
     */
    public void testMultithreadUpdateLock_putAll() {
        MultithreadUpdateLockMonitor testMonitor = new MultithreadUpdateLockMonitor();
        HashMapUpdateLockable testMap = new HashMapUpdateLockable();
        startupTestThreads(testMonitor, testMap, MethodToCheck.putAll);

        assertNoThreadErrors(testMonitor);            

        // Make sure the update thread blocked, i.e. the lockThread was released before the update thread.
        assertTrue(testMonitor.lockThreadReleaseTime <= testMonitor.updateThreadReleaseTime);
    }

    /**
     * Test HashTableUpdateLockable.put method which overrides the HashTable method.
     * Tests the HashTableUpdateLockable collection in a multithreaded environment to ensure that
     * a thread trying to update the collection will block as long as another thread has the
     * collection locked for update.  Note that UpdateRunnable performs the actual test of the
     * method.
     * @see UpdateRunnable#run()
     */
    public void testMultithreadUpdateLock_put() {
        MultithreadUpdateLockMonitor testMonitor = new MultithreadUpdateLockMonitor();
        HashMapUpdateLockable testMap = new HashMapUpdateLockable();
        startupTestThreads(testMonitor, testMap, MethodToCheck.put);

        assertNoThreadErrors(testMonitor);            

        // Make sure the update thread blocked, i.e. the lockThread was released before the update thread.
        assertTrue(testMonitor.lockThreadReleaseTime <= testMonitor.updateThreadReleaseTime);
    }

    /**
     * Test HashTableUpdateLockable.put method which overrides the HashTable method.
     * Tests the HashTableUpdateLockable collection in a multithreaded environment to ensure that
     * a thread trying to update the collection does not block as long as another thread has the
     * collection locked for update.  Note that UpdateRunnable performs the actual test of the
     * method.
     * @see UpdateRunnable#run()
     */
    public void testMultithreadUpdateLock_get() {
        MultithreadUpdateLockMonitor testMonitor = new MultithreadUpdateLockMonitor();
        HashMapUpdateLockable testMap = new HashMapUpdateLockable();
        testMap.put("key1", "value1");
        testMap.put("key2", "value2");
        testMap.put("getKey", "value3");
        startupTestThreads(testMonitor, testMap, MethodToCheck.get);

        assertNoThreadErrors(testMonitor);

        // The update thread shouldn't block since we said to not wait.
        assertTrue(testMonitor.updateThreadReleaseTime <= testMonitor.lockThreadReleaseTime);
    }

    /**
     * Test HashTableUpdateLockable.checkUpdateLock method.
     * Tests the HashTableUpdateLockable collection in a multithreaded environment to ensure that
     * a thread trying to update the collection will block as long as another thread has the
     * collection locked for update.  Note that UpdateRunnable performs the actual test of the
     * method.
     * @see UpdateRunnable#run()
     */
    public void testMultithreadUpdateLock_checkLock_wait() {
        MultithreadUpdateLockMonitor testMonitor = new MultithreadUpdateLockMonitor();
        HashMapUpdateLockable testMap = new HashMapUpdateLockable();
        startupTestThreads(testMonitor, testMap, MethodToCheck.checkLock_wait);

        assertNoThreadErrors(testMonitor);            

        // Make sure the update thread blocked, i.e. the lockThread was released before the update thread.
        assertTrue(testMonitor.lockThreadReleaseTime <= testMonitor.updateThreadReleaseTime);
        // Make sure the return value indicates the table was not locked.
        assertFalse(((Boolean) testMonitor.methodToTestReturnValue).booleanValue());
    }

    /**
     * Test HashTableUpdateLockable.checkUpdateLock method.
     * Tests the HashTableUpdateLockable collection in a multithreaded environment to ensure that
     * a thread trying to update the collection will not block as long as another thread has the
     * collection locked for update.  Note that UpdateRunnable performs the actual test of the
     * method.
     * @see UpdateRunnable#run()
     */
    public void testMultithreadUpdateLock_checkLock_nowait() {
        MultithreadUpdateLockMonitor testMonitor = new MultithreadUpdateLockMonitor();
        HashMapUpdateLockable testMap = new HashMapUpdateLockable();
        startupTestThreads(testMonitor, testMap, MethodToCheck.checkLock_nowait);

        assertNoThreadErrors(testMonitor);

        // The update thread shouldn't block since we said to not wait.
        assertTrue(testMonitor.updateThreadReleaseTime <= testMonitor.lockThreadReleaseTime);
        // Make sure the return value indicates the table was locked.
        assertTrue(((Boolean) testMonitor.methodToTestReturnValue).booleanValue());
    }
    
    // Amount of time the testcase should wait on the test threads before timing out
    private static int THREAD_TIMEOUT = 90000;
    // Amount of time the thread holding the update lock should sleep.  This needs to be long
    // enough so that comparisons between the time the locking thread is released and the update
    // thread is released can be reliably compared as an indication of whether the update thread
    // was blocked.
    private static int LOCK_THREAD_SLEEP = 5000;
    
    /**
     * Assert there were no errors in the basic running of the threads.
     * @param testMonitor contains information about each of the test threads.
     */
    private void assertNoThreadErrors(MultithreadUpdateLockMonitor testMonitor) {
        // Make sure both threads were released (i.e. release time != 0) and that the 
        // that there were no exceptions encountered
        
        assertNull(testMonitor.lockThreadException);
        assertTrue(testMonitor.lockThreadReleaseTime != 0);
        
        assertNull(testMonitor.updateThreadException);
        assertTrue(testMonitor.updateThreadReleaseTime != 0);
    }

    /**
     * Configures the test threads with the common object to store information about each one, the 
     * collection to be tested and the method to be tested.  Two threads will be started:
     * (1) A thread that will lock the collection, sleep, then unlock the collection
     * (2) A thread that will try to update the collection while it is locked.
     * Thread (2) should block if the method to be tested is one that would update the table, 
     * otherwise it should not block.
     * 
     * @param testMonitor Common object used to communicate between the test method and the two
     * test threads
     * @param testMap Instance of HashTableUpdateLockable to test
     * @param methodToTest The method on HashTableUpdateLockable to be tested.
     */
    private void startupTestThreads(MultithreadUpdateLockMonitor testMonitor, 
                                    HashMapUpdateLockable testMap,
                                    MethodToCheck methodToTest) {
        LockingRunnable lockingRunnable = new LockingRunnable();
        lockingRunnable.testMonitor = testMonitor;
        lockingRunnable.testMap = testMap;
        lockingRunnable.methodToTest = methodToTest;
        
        UpdateRunnable updateRunnable = new UpdateRunnable();
        updateRunnable.testMonitor = testMonitor;
        updateRunnable.testMap = testMap;
        updateRunnable.methodToTest = methodToTest;

        Thread lockingThread = new Thread(lockingRunnable, "Locking");
        Thread updateThread = new Thread(updateRunnable, "Updating");

        // To eliminate a timing window where the locking thread starts and runs to completion
        // before the update thread gets started, start the update thread first.  It will block
        // waiting on the locking thread to complete.
        updateThread.start();
        lockingThread.start();
        
        // Join the threads to wait for their completion, specifying a timeout to prevent 
        // a testcase hang if something goes wrong with the threads.
        try {
            lockingThread.join(THREAD_TIMEOUT);
            updateThread.join(THREAD_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Unable to join to testing threads");
        }
    }
    
    /**
     * Object used to communicate information between the testcase and the two test threads.
     */
    class MultithreadUpdateLockMonitor {
        long lockThreadReleaseTime = 0;
        boolean lockSetupComplete = false;
        RuntimeException lockThreadException = null;

        long updateThreadReleaseTime = 0;
        RuntimeException updateThreadException = null;

        Object methodToTestReturnValue = null;
    }
    
    /**
     * Abstract superclass of the two test threads.
     */
    abstract class UpdateLockableTestRunnable implements Runnable {
        MultithreadUpdateLockMonitor testMonitor = null;
        MethodToCheck methodToTest = null;
        HashMapUpdateLockable testMap = null;
    }
    
    
    /**
     * Test thread that will lock the collection, sleep, then unlock the collection.
     */
    class LockingRunnable extends UpdateLockableTestRunnable {

        public void run() {
            try {
                // Lock the table, then relese the "lockSetupComplete" waiters, which will
                // release the update thread.
                // NOTE that the lock is done inside a try block and the unlock is done in 
                // a finaly block so that it will always be executed.
                testMap.lockForUpdate();
                synchronized (testMonitor) {
                    testMonitor.lockThreadReleaseTime = 0;
                    testMonitor.lockSetupComplete = true;
                    testMonitor.notifyAll();
                }
                // Sleep for a while to verify that the update thread is being blocked while 
                // the table is locked by this thread.  Then unlock the table.
                try {
                    Thread.sleep(LOCK_THREAD_SLEEP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    testMonitor.lockThreadException = new RuntimeException(e);
                    throw testMonitor.lockThreadException;
            }
            } finally {
                // Get the current time before releasing the lock.  On some systems, as soon as the 
                // notify is done, the waiting thread will immediately get control.
                testMonitor.lockThreadReleaseTime = System.currentTimeMillis();
                testMap.unlockForUpdate();
            }
        }
    }
    
    /**
     * Thread that will run the methods to be tested on the collection while it is locked for 
     * update by another thread.
     */
    class UpdateRunnable extends UpdateLockableTestRunnable  {
        public void run() {
            // Setup whatever we'll need to test laser on
            Map putAllMap = new HashMap();
            putAllMap.put("k1", "v1");
            putAllMap.put("k2", "v2");
            putAllMap.put("k3", "v3");
            
            // Wait till the locking thread is setup
            synchronized(testMonitor) {
                testMonitor.updateThreadReleaseTime = 0;
                while(!testMonitor.lockSetupComplete) {
                    try {
                        testMonitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        testMonitor.updateThreadException = new RuntimeException(e);
                        throw testMonitor.updateThreadException;
                    }
                }
            }

            // Test the update method specified
            switch(methodToTest) {
            case checkLock_wait:
                boolean retValWait = testMap.checkUpdateLock(true);
                testMonitor.methodToTestReturnValue = new Boolean(retValWait);
                break;
            case checkLock_nowait:
                boolean retValNoWait = testMap.checkUpdateLock(false);
                testMonitor.methodToTestReturnValue = new Boolean(retValNoWait);
                break;
            case put:
                testMap.put("newKey", "newValue");
                break;
            case putAll:
                testMap.putAll(putAllMap);
                break;
            case remove:
                testMap.remove("removeKey");
                break;
            case get:
                testMap.get("getKey");
                break;
            default:
                testMonitor.updateThreadException =
                    new UnsupportedOperationException("method to test not recognized: " + methodToTest); 
                throw testMonitor.updateThreadException;
            }
            testMonitor.updateThreadReleaseTime = System.currentTimeMillis();
        }
    }
}
