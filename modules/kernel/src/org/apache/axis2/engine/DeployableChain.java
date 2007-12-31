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

package org.apache.axis2.engine;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

public class DeployableChain {
    /** The actual things (handlers or phases) */
    List chain = new ArrayList();

    Deployable first;
    Deployable last;

    Map activeConstraints = new HashMap();
    private Map deployed = new HashMap();

    Map activePhaseConstraints = new HashMap();

    /**
     * Deploy a Deployable into this chain.  Note that this does NOT order yet.  The idea
     * is to deploy everything first, then call rebuild() to generate the fully ordered chain.
     * This method will validate the Deployable, including confirming that it's compatible with
     * any previously deployed item of the same name.
     *
     * @param deployable Deployable to deploy.
     * @throws Exception if there is a deployment error.
     */
    public void deploy(Deployable deployable) throws Exception {
        String name = deployable.getName();

        if (deployable.isFirst()) {
            if (first != null) {
                throw new Exception("'" + first.getName() + "' is already first, can't deploy '" +
                    name + "' as first also.");
            }
            first = deployable;
        }

        if (deployable.isLast()) {
            if (last != null) {
                throw new Exception("'" + last.getName() + "' is already last, can't deploy '" +
                        name + "' as last also.");
            }
            last = deployable;
        }

        // Now check internal consistency
        Set mySuccessors = deployable.getSuccessors();
        Set myPredecessors = deployable.getPredecessors();

        if (mySuccessors != null && deployable.isLast()) {
            throw new Exception("Deploying '" + name +
                    "' - can't both be last and have successors!");
        }
        if (myPredecessors != null && deployable.isFirst()) {
            throw new Exception("Deploying '" + name +
                    "' - can't both be first and have predecessors!");
        }

        Deployable previous = (Deployable)deployed.get(name);
        if (previous == null) {
            deployed.put(name, deployable);
        } else {
            // If something by this name already exists, ensure it's compatible
            if (previous.isFirst() != deployable.isFirst()) {
                throw new Exception();
            }
            if (previous.isLast() != deployable.isLast()) {
                throw new Exception();
            }
            Object target = previous.getTarget();
            if (target != null) {
                if (deployable.getTarget() != null && !target.equals(deployable.getTarget())) {
                    throw new Exception();
                }
            } else {
                previous.setTarget(deployable.getTarget());
            }
        }

        if (mySuccessors != null && !mySuccessors.isEmpty()) {
            Set successors = (Set)activeConstraints.get(name);
            if (successors == null) {
                successors = new HashSet();
                activeConstraints.put(name, successors);
            }
            successors.addAll(mySuccessors);
        }

        if (myPredecessors != null) {
            for (Iterator i = myPredecessors.iterator(); i.hasNext();) {
                String predecessor = (String)i.next();
                // define relationship for each one
                addRelationship(predecessor, name);
            }
        }
    }

    /**
     * Find the correct (minimum) index for a given name.  Finds the active constraints
     * for the name, then walks that list, making sure that each one is already placed.
     * If a given successor isn't yet placed, we recursively call this method to place it,
     * passing a set of "what's left to do" and a set of "what we've seen".
     *
     * This results in chain containing the names (Strings).
     *
     * TODO: investigate optimizing this algorithm a bit
     *
     * @param name the target deployable's name
     * @param remaining a Set of the names of things we have yet to deploy
     * @param seen a Set of the names of things we've already seen (to detect loops)
     * @return an index >=0 or -1 if there was a dependency cycle
     * @throws Exception if there's a cyclic dependency
     */
    private int getMinIndex(String name, Set remaining, Set seen) throws Exception {
        if (seen.contains(name)) {
            // We return -1 here instead of throwing the Exception so we can build a better
            // error message below.
            return -1;
        }

        Collection successors = (Collection)activeConstraints.get(name);
        if (successors == null || successors.isEmpty()) {
            // Never put anything after the thing marked "last"...
            int index = (last == null) ? chain.size() : chain.size() - 1;
            chain.add(index, name);
            remaining.remove(name);
            return index;
        }

        int minIndex = -1;
        for (Iterator i = successors.iterator(); i.hasNext();) {
            String otherName = (String)i.next();
            int otherIdx = chain.indexOf(otherName);
            if (otherIdx > -1) {
                if ((minIndex == -1) || (minIndex > otherIdx)) {
                    minIndex = otherIdx;
                }
            } else {
                // This guy isn't in our list yet - put him in IF he's real (exists in
                // the deployed list) and we haven't already placed him (exists in
                // the remaining set).
                if ((deployed.get(otherName) != null) && remaining.contains(otherName)) {
                    seen.add(name);
                    // Not in list yet, go place it
                    int min = getMinIndex(otherName, remaining, seen);
                    if (minIndex == -1 || min < minIndex) {
                        minIndex = min;
                    }
                    if (minIndex == -1) {
                        throw new Exception("Trying to put '" + name + "' before '" +
                                otherName + "' - incompatible constraints!");
                    }
                }
            }
        }

        if (minIndex == -1) minIndex = 0;

        chain.add(minIndex, name);
        remaining.remove(name);

        return minIndex;
    }

    /**
     * Taking into account all the active constraints, order the list.  This blows
     * away the old order.  Could improve this slightly with a "dirty" flag.
     *
     * @throws Exception if there's an ordering conflict
     */
    public void rebuild() throws Exception {
        chain.clear();

        Set keys = new HashSet();
        keys.addAll(deployed.keySet());

        // First goes first.
        if (first != null) {
            chain.add(first.getName());
            keys.remove(first.getName());
        }

        // Last goes next, and everything else will get inserted before it if it exists.
        if (last != null) {
            Collection afterLast = (Collection)activeConstraints.get(last.getName());
            if (afterLast != null) {
                throw new Exception("Can't have anything which goes after '" + last.getName() +
                        "', which has been declared last.");
            }
            chain.add(last.getName());
            keys.remove(last.getName());
        }

        while (!keys.isEmpty()) {
            String name = (String)keys.iterator().next();
            getMinIndex(name, keys, new HashSet());
        }

        // Now we've got a chain of names.  Convert to actual things before we return.
        for (int i = 0; i < chain.size(); i++) {
            String name = (String)chain.get(i);
            chain.set(i, ((Deployable)deployed.get(name)).getTarget());
        }
    }

    public void addRelationship(String before, String after) {
        Set successors = (Set)activeConstraints.get(before);
        if (successors == null) {
            successors = new HashSet();
            activeConstraints.put(before, successors);
        }
        successors.add(after);
    }

    public List getChain() {
        return chain;
    }
}
