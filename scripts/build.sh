#!/usr/bin/env bash
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
#bin/sh

## whenever commit to the repository, do Compilation and Installation

if [ "$1" == "install" ]; then
    mvn apache-rat:check -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -B -Pit,distribution
    if [ $? != 0 ]; then
        echo "${red}Rat check failed.${reset}"
        exit 1
    fi

    mvn clean install -Dspotbugs.skip=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -B -Pdocker -Pdemo-run-release -Pjacoco -Pit -Pcoverage
    if [ $? == 0 ]; then
        echo "${green}Installation Success..${reset}"
    else
        echo "${red}Installation or Test Cases failed, please check the above logs for more details.${reset}"
        exit 1
    fi

    echo "Compilation and Installation Completed"
else
    echo "Not Implemented parameter"
    exit 1
fi 
