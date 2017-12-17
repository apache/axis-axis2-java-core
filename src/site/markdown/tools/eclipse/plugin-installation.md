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

Eclipse plugin installation
===========================

Introduction
------------

This document provides instructions for installating of the two Eclipse plugins provided by Axis2
([Service Archive Generator](servicearchiver-plugin.html) and [Code Generator Wizard](wsdl2java-plugin.html)).
The two plugins should work on Eclipse version 3.1 and upwards, and require at least Java 1.5.
The plugins have dependencies on bundles that are installed by default in the Eclipse IDE for Java EE Developers
edition, but not in the Eclipse IDE for Java Developers edition. It is therefore recommended
to use the EE edition.
The installation procedure is the same for both plugins, but depends on the Eclipse
version being used. To determine which procedure to use, check if there is a
`dropins` folder in the Eclipse installation directory. This folder is used
by the p2 provisioning system introduced in recent Eclipse version. It should be
present starting with Eclipse version 3.4.

Installation using the dropins directory
----------------------------------------

If your Eclipse version uses p2, use the following procedure to install the
Axis2 plugins:

1.  [Download](../../download.html) the ZIP file for the plugin you want to install.

2.  Extract the content of the `plugins` folder in the ZIP archive into the
    `dropins` folder (i.e. do **not** create a `plugins` folder under `dropins`).

As explained [here](http://wiki.eclipse.org/Equinox_p2_Getting_Started#Dropins),
it is possible to use other directory layouts in the `dropins` folder.

Installation on older Eclipse versions
--------------------------------------

If you have an older Eclipse version that doesn't support p2 yet, use the following
procedure to install the Axis2 plugins:

1.  [Download](../../download.html) the ZIP file for the plugin you want to install.

2.  Extract the content of the ZIP archive into the Eclipse installation directory.
    This should add one or more JAR files and/or directories to the existing `plugins`
    folder.

Debugging
---------

If a plugin doesn't show up in the Eclipse UI, use the following debugging procedure:

1.  Start Eclipse with the `-console` option.

2.  In the console, use `ss axis2` to check if the plugin has been installed and to
    identify its bundle ID.

3.  If the plugin has not been installed, use the `install` command (with a `file:` URL
    pointing to the plugin) to force its installation.

4.  Use the `start` command (with the bundle ID as argument) to attempt to start the
    bundle. If the plugin doesn't show up in the UI, then this command will typically
    fail with an error message explaining the reason.

Please use this procedure before opening a bug report.
