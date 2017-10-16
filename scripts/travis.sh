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
        fi
        echo "Installation Completed"
else
        if [ "$TAGGEDCOMMIT" ==   "true" ]; then
                echo "Decrypting the key"
		openssl aes-256-cbc -K $encrypted_acbbc88fb3ab_key -iv $encrypted_acbbc88fb3ab_iv -in gpg-sec.tar.enc -out gpg-sec.tar -d
		tar xvf gpg-sec.tar
		echo "Deploying Staging Release"
		mvn deploy -DskipTests -Prelease -Pdistribution -Ppassphrase --settings .travis.settings.xml
        else
		echo "Deploy a Non-Signed Staging Release"
		mvn deploy -DskipTests --settings .travis.settings.xml
                
        fi
	echo "Deployment Completed"
fi 
