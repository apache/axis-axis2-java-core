/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This is a utility class to make it easier/cleaner for user programming
 * model-level implementations (e.g. the Axis2 JAX-WS code) to invoke the
 * ThreadContextMigrators. 
 */
public class ThreadContextMigratorUtil {
  /**
   * Register a new ThreadContextMigrator.
   * 
   * @param configurationContext
   * @param threadContextMigratorListID The name of the property in the
   *                                    ConfigurationContext that contains
   *                                    the list of migrators.
   * @param migrator
   */
    public static void addThreadContextMigrator(ConfigurationContext configurationContext, String threadContextMigratorListID, ThreadContextMigrator migrator) {
    List migratorList = (List)configurationContext.getProperty(threadContextMigratorListID);

        if (migratorList == null) {
      migratorList = new LinkedList();
      configurationContext.setProperty(threadContextMigratorListID, migratorList);
    }
    
    migratorList.add(migrator);
  }
 
  /**
   * Activate any registered ThreadContextMigrators to move context info
   * to the thread of execution.
     *
   * @param threadContextMigratorListID The name of the property in the
   *                                    ConfigurationContext that contains
   *                                    the list of migrators.
   * @param msgContext
   * @throws AxisFault
   */
  public static void performMigrationToThread(String threadContextMigratorListID, MessageContext msgContext)
            throws AxisFault {
        if (msgContext == null) {
      return;
    }
      
    List migratorList = (List)msgContext.getConfigurationContext().getProperty(threadContextMigratorListID);
    
        if (migratorList != null) {
      ListIterator threadContextMigrators = migratorList.listIterator();
            while (threadContextMigrators.hasNext()) {
        ((ThreadContextMigrator)threadContextMigrators.next()).migrateContextToThread(msgContext);
      }
    }
  }

  /**
   * Activate any registered ThreadContextMigrators to remove information
   * from the thread of execution if necessary.
   * 
   * @param threadContextMigratorListID The name of the property in the
   *                                    ConfigurationContext that contains
   *                                    the list of migrators.
   * @param msgContext
   */
    public static void performThreadCleanup(String threadContextMigratorListID, MessageContext msgContext) {
        if (msgContext == null) {
      return;
    }
      
    List migratorList = (List)msgContext.getConfigurationContext().getProperty(threadContextMigratorListID);
    
        if (migratorList != null) {
      ListIterator threadContextMigrators = migratorList.listIterator();
            while (threadContextMigrators.hasNext()) {
        ((ThreadContextMigrator)threadContextMigrators.next()).cleanupThread(msgContext);
      }
    }
  }

  /**
   * Activate any registered ThreadContextMigrators to move info from the
   * thread of execution into the context.
   * 
   * @param threadContextMigratorListID The name of the property in the
   *                                    ConfigurationContext that contains
   *                                    the list of migrators.
   * @param msgContext
   * @throws AxisFault
   */
  public static void performMigrationToContext(String threadContextMigratorListID, MessageContext msgContext)
            throws AxisFault {
        if (msgContext == null) {
      return;
    }
      
    List migratorList = (List)msgContext.getConfigurationContext().getProperty(threadContextMigratorListID);

        if (migratorList != null) {
      ListIterator threadContextMigrators = migratorList.listIterator();
            while (threadContextMigrators.hasNext()) {
        ((ThreadContextMigrator)threadContextMigrators.next()).migrateThreadToContext(msgContext);
      }
    }
  }
  
  /**
   * Activate any registered ThreadContextMigrators to remove information from
   * the context if necessary.
   * 
   * @param threadContextMigratorListID The name of the property in the
   *                                    ConfigurationContext that contains
   *                                    the list of migrators.
   * @param msgContext
   */
    public static void performContextCleanup(String threadContextMigratorListID, MessageContext msgContext) {
        if (msgContext == null) {
      return;
    }
      
    List migratorList = (List)msgContext.getConfigurationContext().getProperty(threadContextMigratorListID);

        if (migratorList != null) {
      ListIterator threadContextMigrators = migratorList.listIterator();
            while (threadContextMigrators.hasNext()) {
        ((ThreadContextMigrator)threadContextMigrators.next()).cleanupContext(msgContext);
      }
    }
  }
}
