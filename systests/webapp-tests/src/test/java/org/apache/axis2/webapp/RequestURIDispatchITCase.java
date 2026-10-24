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
package org.apache.axis2.webapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.Test;

/**
 * The service and operation a request is dispatched to come from the request
 * path, which is what the container matched its URL patterns against. A service
 * path carried in a query value, or appended after the real service name, must
 * not select anything.
 */
public class RequestURIDispatchITCase {
    private static String get(String pathAndQuery) throws Exception {
        int port = Integer.getInteger("jetty.httpPort", 8080);
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + "/axis2" + pathAndQuery)).GET().build();
        return HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    @Test
    public void testServiceIsTakenFromThePathOnly() throws Exception {
        // Control: the operation answers when the path names it.
        assertThat(get("/services/Version/getVersion")).contains("getVersionResponse");
        // The path names no deployed service; neither the query string nor a
        // repeated service path after it may route the request to Version.
        assertThat(get("/services/NoSuchService?next=/axis2/services/Version/getVersion"))
                .doesNotContain("getVersionResponse");
        assertThat(get("/services/NoSuchService/axis2/services/Version/getVersion"))
                .doesNotContain("getVersionResponse");
    }
}
