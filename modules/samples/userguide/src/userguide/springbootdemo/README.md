<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

# springbootdemo

The original Spring Boot + Axis2 JSON demo from the
[JSON and Spring Boot User's Guide](https://axis.apache.org/axis2/java/core/docs/json-springboot-userguide.html),
deployed as an exploded WAR to an external application server (no embedded container).
It builds against Spring Boot 3.5.16 and Java 17.

```bash
mvn clean package
```

The output is `target/deploy/axis2-json-api.war/`, an exploded WAR directory named with the
`.war` suffix because WildFly takes the deployment name from it.

For new work, prefer the maintained variants, which share their Java source and carry
container-specific READMEs:

- [`../springbootdemo-tomcat11`](../springbootdemo-tomcat11/README.md) — Tomcat 11
- [`../springbootdemo-wildfly`](../springbootdemo-wildfly/README.md) — WildFly 32 / 41
