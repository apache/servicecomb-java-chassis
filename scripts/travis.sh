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

##Check if the commit is tagged commit or not
TAGGEDCOMMIT=$(git tag -l --contains HEAD)
if [ "$TAGGEDCOMMIT" == "" ]; then
        TAGGEDCOMMIT=false
else
        TAGGEDCOMMIT=true
fi
echo $TAGGEDCOMMIT


if [ "$1" == "install" ]; then
        if [ "$TAGGEDCOMMIT" == "true" ]; then
              	echo "Skipping the installation as it is tagged commit"
        else
                mvn clean install -Ddocker.showLogs -Pdocker -Pjacoco -Pit -Pcoverage coveralls:report
		if [ $? == 0 ]; then
			echo "${green}Installation Success..${reset}"
		else
			echo "${red}Installation or Test Cases failed, please check the above logs for more details.${reset}"
			exit 1
		fi
        fi
        echo "Installation Completed"
else
        if [ "$TAGGEDCOMMIT" ==   "true" ]; then
                echo "Decrypting the key"
		openssl aes-256-cbc -K $encrypted_acbbc88fb3ab_key -iv $encrypted_acbbc88fb3ab_iv -in gpg-sec.tar.enc -out gpg-sec.tar -d
		tar xvf gpg-sec.tar
		echo "Deploying Staging Release"
		mvn deploy -DskipTests -Prelease -Pdistribution -Ppassphrase --settings .travis.settings.xml
		if [ $? == 0 ]; then
			echo "${green}Staging Deployment is Success, please log on to Nexus Repo to see the staging release..${reset}"
		else
			echo "${red}Staging Release deployment failed.${reset}"
			exit 1
		fi
        else
		echo "Deploy a Non-Signed Staging Release"
		mvn deploy -DskipTests --settings .travis.settings.xml
		if [ $? == 0 ]; then
			echo "${green}Snapshot Deployment is Success, please log on to Nexus Repo to see the snapshot release..${reset}"
		else
			echo "${red}Snapshot deployment failed.${reset}"
			# No need to exit 1 here as the snapshot depoyment will fail for private builds as decryption of password is allowed for ServiceComb repo and not forked repo's.
		fi
                
        fi
	echo "Deployment Completed"
fi 
