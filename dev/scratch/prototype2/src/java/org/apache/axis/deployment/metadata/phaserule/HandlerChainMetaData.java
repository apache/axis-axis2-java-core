package org.apache.axis.deployment.metadata.phaserule;

import org.apache.axis.deployment.metadata.HandlerMetaData;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Nov 8, 2004
 *         1:54:04 PM
 */
public interface HandlerChainMetaData {

    void addHandler(HandlerMetaData handler) throws PhaseException;

    HandlerMetaData[] getOrderdHandlers() throws PhaseException;
}
