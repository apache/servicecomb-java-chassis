## ---------------------------------------------------------------------------
## Licensed to the Apache Software Foundation (ASF) under one or more
## contributor license agreements.  See the NOTICE file distributed with
## this work for additional information regarding copyright ownership.
## The ASF licenses this file to You under the Apache License, Version 2.0
## (the "License"); you may not use this file except in compliance with
## the License.  You may obtain a copy of the License at
##
##      http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.
## ---------------------------------------------------------------------------

import os

try:
    import xml.etree.cElementTree as ET
except:
    import xml.etree.ElementTree as ET

dir_path = os.path.dirname(os.path.realpath(__file__))
project_dir = os.path.dirname(dir_path)
VERSION = os.getenv("VERSION")


def change_pom(pom_path):
    print('changing' + pom_path)
    tree = ET.parse(pom_path)
    ET.register_namespace('', "http://maven.apache.org/POM/4.0.0")
    root = tree.getroot()
    POM_NS = "{http://maven.apache.org/POM/4.0.0}"
    print 'replace first level vesion'
    for child in tree.iterfind('%sversion' % (POM_NS)):
        print 'change from ' + child.text + " to " + VERSION
        child.text = VERSION
    print 'replace second level vesion'
    for child in tree.iterfind('%sparent' % (POM_NS)):
        if child.find('%sgroupId' % POM_NS).text == "org.apache.servicecomb":
            if child.find('%sversion' % POM_NS) is not None:
                print 'change module version from ' + child.find('%sversion' % POM_NS).text + " to " + VERSION
                child.find('%sversion' % POM_NS).text = VERSION
    print 'replace cse dependency version'
    for child in tree.iter('%sdependency' % (POM_NS)):
        if child.find('%sgroupId' % POM_NS).text == "org.apache.servicecomb":
            if child.find('%sversion' % POM_NS) is not None:
                print 'change module version from ' + child.find('%sversion' % POM_NS).text + " to " + VERSION
                child.find('%sversion' % POM_NS).text = VERSION
    tree.write(pom_path)


if __name__ == "__main__":
    for root, dirs, files in os.walk(project_dir):
        for file in files:
            if file.endswith("pom.xml"):
                change_pom(os.path.join(root, file))
