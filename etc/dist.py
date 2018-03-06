#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

from os import *
from os.path import *
from shutil import copyfile
from shutil import rmtree
from subprocess import call
from xml.etree.ElementTree import parse

root_dir = realpath(join(dirname(__file__), ".."))
pom = parse(join(root_dir, "pom.xml"))
release = pom.getroot().find("{http://maven.apache.org/POM/4.0.0}version").text
dist_root = join(root_dir, "target", "dist")
dist_dir = join(dist_root, release)

if exists(dist_root):
    rmtree(dist_root)
call(["svn", "checkout", "https://dist.apache.org/repos/dist/dev/axis/axis2/java/core/", dist_root])
mkdir(dist_dir)
for suffix in [ "zip", "zip.asc", "zip.sha512" ]:
    for classifier in [ "bin", "src", "war" ]:
        file = "axis2-" + release + "-" + classifier + "." + suffix
        copyfile(join(root_dir, "modules", "distribution", "target", file), join(dist_dir, file))
    for tool in [ "codegen", "service" ]:
        copyfile(join(root_dir, "modules", "tool", "axis2-eclipse-" + tool + "-plugin", "target", "axis2.eclipse." + tool + ".plugin-" + release + "-dist." + suffix),
                 join(dist_dir, "axis2-eclipse-" + tool + "-plugin-" + release + "." + suffix))
    file = "axis2-idea-plugin-" + release + "." + suffix
    copyfile(join(root_dir, "modules", "tool", "axis2-idea-plugin", "target", file), join(dist_dir, file))

call(["svn", "add", dist_dir])
if release.endswith("-SNAPSHOT"):
    print "Skipping commit because version is a snapshot."
else:
    call(["svn", "commit", dist_dir])
