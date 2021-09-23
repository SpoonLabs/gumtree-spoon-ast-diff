#!/bin/bash
#
# Deploy a jar, source jar, and javadoc jar to Sonatype Maven Central repo.
#
# Inspired from https://github.com/square/retrofit/blob/fccedbeb4d5181c926fff450cdf5c5116ef0eeaa/.buildscript/deploy_snapshot.sh

set -e

echo "Deploying ..."
# made with "travis encrypt-file .buildscript/codesigning.asc -r SpoonLabs/gumtree-spoon-ast-diff --add"
openssl aes-256-cbc -K $encrypted_9809c3ea697e_key -iv $encrypted_9809c3ea697e_iv -in .buildscript/codesigning.asc.enc -out codesigning.asc -d
gpg --fast-import codesigning.asc

# getting the previous version on Maven Central
# for some reasons, some versions don't get index by search.maven.org/solrsearch/, and it break the build
# so we have to roll our own: https://gist.github.com/monperrus/9ee373e2500e40b634f8daf707f6ad2a
# PREVIOUS_MAVEN_CENTRAL_VERSION=`curl "http://search.maven.org/solrsearch/select?q=a:gumtree-spoon-ast-diff+g:fr.inria.gforge.spoon.labs&rows=20&wt=json" | jq -r .response.docs[0].latestVersion | egrep -o "[0-9]+$"`
PREVIOUS_MAVEN_CENTRAL_VERSION=`curl "https://www.monperrus.net/martin/last-version-maven.py?groupId=fr.inria.gforge.spoon.labs&artifactId=gumtree-spoon-ast-diff" | egrep -o "[0-9]+$"`

# and incrementing it
mvn versions:set -DnewVersion=1.$((PREVIOUS_MAVEN_CENTRAL_VERSION+1))

mvn -Prelease deploy --settings .buildscript/settings.xml -Dmaven.test.skip=true
echo "Well deployed!"
