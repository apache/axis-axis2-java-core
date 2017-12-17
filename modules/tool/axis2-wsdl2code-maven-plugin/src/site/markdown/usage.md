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

Usage
-----

axis2-wsdl2code-maven-plugin offers to goals: `generate-sources` and `generate-test-sources`.
Both read a given WSDL and generate source code, but they differ in the phases in which they
expect to be executed. Use `generate-sources` if the generated classes should become part of the
artifact produced by the Maven module; use `generate-test-sources` if the generated code is only
used by the unit tests.

To run the plugin, add the following section to your POM:

    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.axis2</groupId>
          <artifactId>axis2-wsdl2code-maven-plugin</artifactId>
          <executions>
            <execution>
              <goals>
                <goal>generate-sources</goal>
              </goals>
              <configuration>
                <packageName>com.foo.myservice</packageName>
                <wsdlFile>src/main/wsdl/myservice.wsdl</wsdlFile>
                <databindingName>xmlbeans</databindingName>
              </configuration>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>

The plugin will be invoked automatically in the generate-sources
phase.

See the detailed documentation on [properties](wsdl2code-mojo.html) for
how to configure the goal.
