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

The XSD2Java plugin offers a single goal:

* xsd2java (default): Reads one or more XSD-files class and generates the corresponding Java Axis2 ADB beans.

To run the plugin, add the following section to your POM:

    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.axis2.maven2</groupId>
          <artifactId>axis2-xsd2java-maven-plugin</artifactId>
          <executions>
            <execution>
              <goals>
                <goal>xsd2java</goal>
              </goals>
            </execution>
            <configuration>
              <outputFolder>${project.basedir}/target/generated-sources/java</outputFolder>
              <xsdFiles>
                  <xsdFile>${project.basedir}/src/main/resources/xsd/attribute.xsd</xsdFile>
              </xsdFiles>
              <namespace2Packages>
                  <namespace2Package>http://www.example.org/schema/test=org.example.schema.test</namespace2Package>
              </namespace2Packages>
            </configuration>
          </executions>
        </plugin>
      </plugins>
    </build>

The plugin will be invoked automatically in the generate-sources
phase. You can also invoke it directly from the command line by
running the command

    mvn xsd2java:xsd2java

# The XSD2Java Goal

The plugin reads the specified XSD files and creates the matching Axis2 ADB Java bean classes.  The mapping from
XSD target-namespaces to Java packages is specified with the `namespace2Packages` configuration element above.

See the detailed documentation on [properties](xsd2java-mojo.html) for how to configure the goal.
