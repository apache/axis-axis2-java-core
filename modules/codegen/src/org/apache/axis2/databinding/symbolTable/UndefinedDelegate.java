/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.axis2.databinding.symbolTable;

import java.io.IOException;
import java.util.Vector;

/**
 * This UndefinedDelegate class implements the common functions of UndefinedType and UndefinedElement.
 */
public class UndefinedDelegate implements Undefined {

    /** Field list */
    private Vector list;

    /** Field undefinedType */
    private TypeEntry undefinedType;

    /**
     * Constructor
     * 
     * @param te 
     */
    UndefinedDelegate(TypeEntry te) {
        list = new Vector();
        undefinedType = te;
    }

    /**
     * Register referrant TypeEntry so that
     * the code can update the TypeEntry when the Undefined Element or Type is defined
     * 
     * @param referrant 
     */
    public void register(TypeEntry referrant) {
        list.add(referrant);
    }

    /**
     * Call update with the actual TypeEntry.  This updates all of the
     * referrant TypeEntry's that were registered.
     * 
     * @param def 
     * @throws IOException 
     */
    public void update(TypeEntry def) throws IOException {

        boolean done = false;

        while (!done) {
            done = true;             // Assume this is the last pass

            // Call updatedUndefined for all items on the list
            // updateUndefined returns true if the state of the te TypeEntry
            // is changed.  The outer loop is traversed until there are no more
            // state changes.
            for (int i = 0; i < list.size(); i++) {
                TypeEntry te = (TypeEntry) list.elementAt(i);

                if (te.updateUndefined(undefinedType, def)) {
                    done = false;    // Items still undefined, need another pass
                }
            }
        }

        // It is possible that the def TypeEntry depends on an Undefined type.
        // If so, register all of the entries with the undefined type.
        TypeEntry uType = def.getUndefinedTypeRef();

        if (uType != null) {
            for (int i = 0; i < list.size(); i++) {
                TypeEntry te = (TypeEntry) list.elementAt(i);

                ((Undefined) uType).register(te);
            }
        }
    }
}
