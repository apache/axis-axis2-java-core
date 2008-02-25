/**
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
package org.apache.axis2.jaxws.catalog.impl;

import java.util.logging.Logger;

import org.apache.axis2.jaxws.catalog.JAXWSCatalogManager;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;

/**
 *  OASISCatalogManager provides an interface to the catalog properties.
 *  The primary difference between this and the 
 *  org.apache.xml.resolver.CatalogManger is that the staticCatalog is not
 *  a static data member in the OASISCatalogManger class.  This enables the use
 *  of a static catalog per CatalogManager.  
 */
public class OASISCatalogManager extends CatalogManager implements JAXWSCatalogManager {
    public static final String DEFAULT_CATALOG_NAME = "WEB-INF/jax-ws-catalog.xml";
    public static final String CATALOG_DEBUG_KEY = "OASISCatalogManager.catalog.debug.level";

    private static final Logger LOG =
    	Logger.getLogger(OASISCatalogManager.class.getName());
    private static final String DEBUG_LEVEL = System.getProperty(CATALOG_DEBUG_KEY);

    /** The static catalog used by this manager. */
    private Catalog staticCatalog = null;

    /**
     * Default constructor with no arguments.
     * This constructor will use the defaults specified for Axis2 in the
     * acceptDefaults method.
     */
    public OASISCatalogManager() {
    	super();
    	this.acceptDefaults();
    	if (DEBUG_LEVEL != null) {
    		this.debug.setDebug(Integer.parseInt(DEBUG_LEVEL)); 
    	}
    }

    /**
     * Constructor that specifies an explicit property file.
     * @param propertyFileName
     */
    public OASISCatalogManager(String propertyFileName) {
    	super(propertyFileName);
        if (DEBUG_LEVEL != null) {
            this.debug.setDebug(Integer.parseInt(DEBUG_LEVEL));            
        }
    }
    
    private void acceptDefaults() {
    	this.setUseStaticCatalog(true);
    	this.setIgnoreMissingProperties(true);
    	this.setCatalogFiles(DEFAULT_CATALOG_NAME);
    }
    
    /**
     * Get a catalog instance.
     *
     * If this manager uses static catalogs, the same static catalog will
     * always be returned. Otherwise a new catalog will be returned.
     */
    public Catalog getCatalog() {
        Catalog catalog = staticCatalog;

        if (catalog == null || !super.getUseStaticCatalog()) {
            catalog = getPrivateCatalog();
        }
        return catalog;
    }
    
    /**
     * Get a new catalog instance.
     *
     * This method returns an instance of the underlying catalog class.
     */
    public Catalog getPrivateCatalog() {
        Catalog catalog = staticCatalog;
        boolean useStatic = super.getUseStaticCatalog();

        if (catalog == null || !useStatic) {
            try {
  	            String catalogClassName = getCatalogClassName();
  	            if (catalogClassName == null) {
  	                catalog = new Catalog();
  	            } else {
  	                try {
  	                    catalog = (Catalog) Class.forName(catalogClassName).newInstance();
  	                } catch (ClassNotFoundException cnfe) {
  	                    debug.message(1,"Catalog class named '"
  			                          + catalogClassName
  			                          + "' could not be found. Using default.");
  	                    catalog = new Catalog();
  	                } catch (ClassCastException cnfe) {
  	                    debug.message(1,"Class named '"
  			                          + catalogClassName
  			                          + "' is not a Catalog. Using default.");
  	                    catalog = new Catalog();
  	                }
  	            }

  	            catalog.setCatalogManager(this);
  	            catalog.setupReaders();
  	            catalog.loadSystemCatalogs();
            } catch (Exception ex) {
  	            ex.printStackTrace();
            }

  	        staticCatalog = catalog;
        }

        return catalog;
    }
    
    /**
     * Set the list of catalog files.
     * This method will reset the staticCatalog for this CatalogManager.
     */
    public void setCatalogFiles(String fileList) {
    	staticCatalog = null;
    	super.setCatalogFiles(fileList);
    }
}
