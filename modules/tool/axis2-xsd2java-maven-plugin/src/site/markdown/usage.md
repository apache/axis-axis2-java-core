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

axis2-xsd2java-maven-plugin offers two goals: `generate-sources` and `generate-test-sources`.
Both read one or more XSD files and generate the corresponding ADB beans, but they differ in the phases in which they
expect to be executed. Use `generate-sources` if the generated classes should become part of the
artifact produced by the Maven module; use `generate-test-sources` if the generated code is only
used by the unit tests.

To run the plugin, add the following section to your POM:

    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.axis2.maven2</groupId>
          <artifactId>axis2-xsd2java-maven-plugin</artifactId>
          <executions>
            <execution>
              <goals>
                <goal>generate-sources</goal>
              </goals>
            </execution>
            <configuration>
              <xsdFiles>
                <xsdFile>src/main/resources/xsd/attribute.xsd</xsdFile>
              </xsdFiles>
              <namespaceMappings>
                <namespaceMapping>
                  <uri>http://www.example.org/schema/test</uri>
                  <packageName>org.example.schema.test</packageName>
                </namespaceMapping>
              </namespaceMappings>
            </configuration>
          </executions>
        </plugin>
      </plugins>
    </build>

The plugin will be invoked automatically in the generate-sources
phase.

It reads the specified XSD files and creates the matching Axis2 ADB Java bean classes.  The mapping from
XSD target-namespaces to Java packages is specified with the `namespaceMappings` configuration element above.

See the detailed documentation on [properties](generate-sources-mojo.html) for how to configure the goal.
