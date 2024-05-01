#!/bin/bash
#
# Deploy a jar, source jar, and javadoc jar to Sonatype Maven Central repo.
#
# Inspired from https://github.com/square/retrofit/blob/fccedbeb4d5181c926fff450cdf5c5116ef0eeaa/.buildscript/deploy_snapshot.sh

set -e

echo "Deploying ..."
# $GPG_SECRET_KEY is an env secret containing a gog secret key without passphrase
echo "$GPG_SECRET_KEY" | gpg --fast-import 

# getting the previous version on Maven Central
# does not work: PREVIOUS_MAVEN_CENTRAL_VERSION=`curl "http://search.maven.org/solrsearch/select?q=a:gumtree-spoon-ast-diff+g:fr.inria.gforge.spoon.labs&rows=20&wt=json" -L | jq -r ".response.docs[0].latestVersion" | egrep -o "[0-9]+$"`
# for some reasons, some versions don't get index by search.maven.org/solrsearch/, and it break the build
# so we have to host our own
PREVIOUS_MAVEN_CENTRAL_VERSION=`curl "https://www.monperrus.net/martin/last-version-maven.py?groupId=fr.inria.gforge.spoon.labs&artifactId=gumtree-spoon-ast-diff"  | egrep -o "[0-9]+$"`


# and incrementing it
mvn versions:set -DnewVersion=1.$((PREVIOUS_MAVEN_CENTRAL_VERSION+1))

mvn -Prelease deploy --settings .buildscript/settings.xml -Dmaven.test.skip=true
echo "Well deployed!"
