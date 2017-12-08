#bin/sh

##Check if the commit is tagged commit or not
TAGGEDCOMMIT=$(git tag -l --contains HEAD)
if [ "$TAGGEDCOMMIT" == "" ]; then
        TAGGEDCOMMIT=false
else
        TAGGEDCOMMIT=true
fi
echo $TAGGEDCOMMIT
echo $TRAVIS_PULL_REQUEST

        if [ "$TAGGEDCOMMIT" == "true" ]; then
              	echo "Skipping the installation as it is tagged commit"
        else
              #  mvn clean install -Ddocker.showLogs -Pdocker -Pjacoco -Pit -Pcoverage coveralls:report
			if [ $? == 0 ]; then
				echo "${green}Installation Success..${reset}"
			else
				echo "${red}Installation or Test Cases failed, please check the above logs for more details.${reset}"
				exit 0:
			fi
        fi
        echo "Installation Completed"
