package org.apache.axis.deployment;

import java.io.InputStream;

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
 *         Nov 2, 2004
 *         4:54:06 PM
 *
 */


public interface DeployManger {

    /**
     * This method can used to deploy any model of ws like J2EE or JWS
     * That is if it want to deploy ws as .aar file or .jws it can handle throgh this
     * @param wsin
     */
    void deployWS(InputStream wsin, String fileName);

    /**
     * This method is to undeploy ws from the system , when it undeploy the correspondance file
     * will permently remove from the syetm
     * @param wsName
     */
    void undeployeWS(String wsName);

    /**
     * This method is to list all the available ws in the system
     */
    void listAllWS();
}
