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

package org.apache.axis2.clustering.tribes;

import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TribesUtil {

    private static Log log = LogFactory.getLog(TribesUtil.class);

    public static void printMembers(Member[] members) {


        if (members != null) {
            int length = members.length;
            if (length > 0) {
                log.info("Members of current Tribes...");
                for (int i = 0; i < length; i++) {
                    byte[] hostBts = members[i].getHost();
                    String host = null;
                    if (hostBts != null) {
                        for (int j = 0; j < hostBts.length; j++) {
                            host = host == null ? ("" + hostBts[j]) : (host + "." + hostBts[j]);
                        }
                    }

                    String port = "" + members[i].getPort();
                    log.info("Member " + (i + 1) + " NAME:" + members[i].getName() + " HOST:"
                             + host + "  PORT:" + port);

                }
            } else {
                log.info("No members in current Tribe");
            }
        }
    }

}
