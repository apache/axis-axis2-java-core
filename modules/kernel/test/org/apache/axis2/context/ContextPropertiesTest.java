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
import java.util.Set;

import junit.framework.TestCase;

/**
 * Test the setting of properties on an AbstractContext instance.  Originally the properties
 * collection was a HashMap, but was changed to a Concurrent collection because multiple threads
 * could be accessing the properties at the same time for aysnc wire flows, when processing
 * responses.
 * 
 * The intent is to retain the original semantics of the HashMap which allows null keys and null 
 * values, since areas of the code seem to make use of those semantics.  Note that most of the 
 * tests below use MessageContext as the concreate implemntation of the abstract
 * AbstractContext.
 */
public class ContextPropertiesTest extends TestCase {
    
    /**
     * Test basic property setting, including using null keys and values.
     */
    public void testProperties() {
        MessageContext mc = new MessageContext();

        // Test getting a non-existent key returns a null value
        assertNull(mc.getProperty("NonexistenKey"));
        
        // Test setting a null property value;
        mc.setProperty("testProperty1", null);
        assertNull(mc.getProperty("testProperty1"));
        
        // Test setting a null property key
        mc.setProperty(null, "value");
        Object value = mc.getProperty(null);
        assertEquals("value", (String) value);
        
        // Test setting a null key and value
        mc.setProperty(null, null);
        assertNull(mc.getProperty(null));

        // Test setting a null value after a valid value; the next get should return null
        String property2 = "testProperty2";
        String testValue2 = "value2";
        mc.setProperty(property2, testValue2);
        Object value2 = mc.getProperty(property2);
        assertEquals(testValue2, value2);
        mc.setProperty(property2, null);
        assertNull(mc.getProperty(property2));
        
    }
    
    /**
     * Test setting the properties based on an input Map.  NOTE that MessageContext has logic
     * in setProperties(Map) that always performs a copy by setting a COPY_PROPERTIES flag.  
     * There there is an additional test below that uses a mock object implemntation of
     * AbstractContext to make sure the collection remains a concurrent one.
     * @see #testPropertiesAssignment_MockAbstractContext()  
     */
    public void testPropertiesAssignment() {
        MessageContext mc = new MessageContext();

        Map map = new HashMap();
        map.put("keyFromMap1", "valueFromMap1");
        map.put("keyFromMap2", "valueFromMap2");
        map.put("keyFromMap3", null);
        map.put(null, "valueFromMapNullKey");
        
        
        mc.setProperties(map);
        assertEquals("valueFromMap1", mc.getProperty("keyFromMap1"));
        assertEquals("valueFromMap2", mc.getProperty("keyFromMap2"));
        assertEquals(null, mc.getProperty("keyFromMap3"));
        assertEquals("valueFromMapNullKey", mc.getProperty(null));
    }

    /**
     * Test setting the properties based on an input Map.  Make sure that the resulting collection
     * on the AbstractContext is a Concurrent collection.
     * @see #testPropertiesAssignment()
     */
    public void testPropertiesAssignment_MockAbstractContext() {
        MockAbstractContext mc = new MockAbstractContext();
        assertTrue(mc.getProperties() instanceof ConcurrentHashMapNullSemantics);
        
        Map map = new HashMap();
        map.put("keyFromMap1", "valueFromMap1");
        map.put("keyFromMap2", "valueFromMap2");
        map.put("keyFromMap3", null);
        map.put(null, "valueFromMapNullKey");
        
        mc.setProperties(map);
        assertEquals("valueFromMap1", mc.getProperty("keyFromMap1"));
        assertEquals("valueFromMap2", mc.getProperty("keyFromMap2"));
        assertEquals(null, mc.getProperty("keyFromMap3"));
        assertEquals("valueFromMapNullKey", mc.getProperty(null));
        assertTrue(mc.getProperties() instanceof ConcurrentHashMapNullSemantics);
        
    }
    
    /**
     * Test removing elements from the collection.
     */
    public void testPropertiesRemove() {
        MessageContext mc = new MessageContext();
        // Remove null key not previously added, make sure it is gone
        mc.removeProperty(null);
        
        // Remove null key previously added
        mc.setProperty(null, "ValueForNullKey");
        assertEquals("ValueForNullKey", mc.getProperty(null));
        mc.removeProperty(null);
        assertNull(mc.getProperty(null));

        // Remove non-existent key
        assertNull(mc.getProperty("NonexistentKey"));
        mc.removeProperty("NonexistentKey");
        assertNull(mc.getProperty("NonexistentKey"));
        
        // Remove non-null key, non-null value make sure it is gone
        mc.setProperty("nonNullKey1", "nonNullValue1");
        assertEquals("nonNullValue1", mc.getProperty("nonNullKey1"));
        mc.removeProperty("nonNullKey1");
        assertNull(mc.getProperty("nonNullKey1"));
        
        // Remove non-null key, null value & make sure it is gone
        mc.setProperty("nonNullKey2", null);
        assertEquals(null, mc.getProperty("nonNullKey2"));
        mc.removeProperty("nonNullKey2");
        assertNull(mc.getProperty("nonNullKey2"));
        
    }
    
    public void testCollectionEntrySet() {
        MockAbstractContext mc = new MockAbstractContext();
        mc.setProperty("key1", "value1");
        mc.setProperty("key2", "value2");
        mc.setProperty("key3", null);
        mc.setProperty(null, "value4-nullkey");
        mc.setProperty("key5", "value5");
        
        Map checkProperties = mc.getProperties(); 
        Set entrySet = checkProperties.entrySet();
        Iterator entryIterator = entrySet.iterator();
        int correctEntries = 0;
        while (entryIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) entryIterator.next();
            // The collection uses Object instances to represent nulls in the collection.  If 
            // the conversion back to nulls hasn't occured, that will cause a CastClassException
            // when it is casted to a (String).
            try {
                String checkKey = (String) entry.getKey();
                String checkValue = (String) entry.getValue();
                if ("key1".equals(checkKey) && "value1".equals(checkValue)) {
                    correctEntries++;
                } else if ("key2".equals(checkKey) && "value2".equals(checkValue)) {
                    correctEntries++;
                }  else if ("key3".equals(checkKey) && (checkValue == null)) {
                    correctEntries++;
                } else if ((checkKey == null) && "value4-nullkey".equals(checkValue)) {
                    correctEntries++;
                }  else if ("key5".equals(checkKey) && "value5".equals(checkValue)) {
                    correctEntries++;
                } else {
                    fail("Invalid entry: key: " + checkKey + ", value: " + checkValue);
                }
            } catch (ClassCastException e) {
                e.printStackTrace();
                fail("Caught ClassCastException " + e.toString());
            }
        }
        assertEquals(5, correctEntries);
    }

    public void testCollectionKeySet() {
        MockAbstractContext mc = new MockAbstractContext();
        mc.setProperty("key1", "value1");
        mc.setProperty("key2", "value2");
        mc.setProperty("key3", null);
        mc.setProperty(null, "value4-nullkey");
        mc.setProperty("key5", "value5");
        
        Map checkProperties = mc.getProperties(); 
        Set keySet = checkProperties.keySet();
        Iterator keyIterator = keySet.iterator();
        int correctEntries = 0;
        while (keyIterator.hasNext()) {
            // The collection uses Object instances to represent nulls in the collection.  If 
            // the conversion back to nulls hasn't occured, that will cause a CastClassException
            // when it is casted to a (String).
            try {
                String checkKey = (String) keyIterator.next();
                if ("key1".equals(checkKey)) {
                    correctEntries++;
                } else if ("key2".equals(checkKey)) {
                    correctEntries++;
                }  else if ("key3".equals(checkKey)) {
                    correctEntries++;
                } else if ((checkKey == null)) {
                    correctEntries++;
                }  else if ("key5".equals(checkKey)) {
                    correctEntries++;
                } else {
                    fail("Invalid entry: key: " + checkKey);
                }
            } catch (ClassCastException e) {
                fail("Caught ClassCastException " + e.toString());
            }
        }
        assertEquals(5, correctEntries);
    }
    
    public void testCollectionContainsKey() {
        MockAbstractContext mc = new MockAbstractContext();
        mc.setProperty("key1", "value1");
        mc.setProperty("key2", "value2");
        mc.setProperty("key3", null);
        mc.setProperty(null, "value4-nullkey");
        mc.setProperty("key5", "value5");
        
        Map checkCollection = mc.getProperties();

        assertTrue(checkCollection.containsKey("key1"));
        assertTrue(checkCollection.containsKey(null));
        assertFalse(checkCollection.containsKey("notHere"));
    }

    /**
     * Test some aspects of the Concurrent collection directly, such as creating with and without
     * generics, and creating with a Map. 
     */
    public void testConcurrentHashMapNullSemantics() {
        
        ConcurrentHashMapNullSemantics noGenerics = new ConcurrentHashMapNullSemantics();
        noGenerics.put("key", "value");
        noGenerics.put(1, 2);
        
        ConcurrentHashMapNullSemantics<String, String> stringGenerics = new ConcurrentHashMapNullSemantics<String, String>();
        stringGenerics.put("key", "value");
        
        ConcurrentHashMapNullSemantics<Integer, Integer> integerGenerics = new ConcurrentHashMapNullSemantics<Integer, Integer>();
        integerGenerics.put(new Integer(1), new Integer(2));
        
        Map<String, String> map = new HashMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put(null, "v3NullKey");
        map.put("k3_null", null);
        ConcurrentHashMapNullSemantics<String, String> useMapGenerics = new ConcurrentHashMapNullSemantics<String, String>(map);
        assertEquals("v1", useMapGenerics.get("k1"));
        assertEquals("v2", useMapGenerics.get("k2"));
        assertEquals("v3NullKey", useMapGenerics.get(null));
        assertNull(useMapGenerics.get("k3_null"));
        
        // put returns the previous value if there was one, or null
        assertEquals("v1", useMapGenerics.put("k1", "newK1Value"));
        assertNull(useMapGenerics.put("k3_null", "newK3Value"));
        assertNull(useMapGenerics.put("noSuchKey-put", "value6"));
        
        // remove returns the value if there was an entry with that key, or null
        assertNull(useMapGenerics.remove("noSuchKey-remove"));
        useMapGenerics.put("key7", null);
        assertNull(useMapGenerics.remove("key7"));
        
        ConcurrentHashMapNullSemantics useMapNoGenerics = new ConcurrentHashMapNullSemantics(map);
        assertEquals("v1", useMapNoGenerics.get("k1"));
        assertEquals("v2", useMapNoGenerics.get("k2"));

    }

}

class MockAbstractContext extends AbstractContext {

    MockAbstractContext() {
        properties = new ConcurrentHashMapNullSemantics();
    }
    
    @Override
    public ConfigurationContext getRootContext() {
        return null;
    }
    
    public Map getProperties() {
        return properties;
    }
    
}
