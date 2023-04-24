#!/bin/bash
#
# Deploy a jar, source jar, and javadoc jar to Sonatype Maven Central repo.
#
# Inspired from https://github.com/square/retrofit/blob/fccedbeb4d5181c926fff450cdf5c5116ef0eeaa/.buildscript/deploy_snapshot.sh

set -e

echo "Deploying ..."
# made with symmetric key encryption algorithm AES256
# reference: https://docs.github.com/en/actions/security-guides/encrypted-secrets#limits-for-secrets
gpg --quiet --batch --yes --decrypt --passphrase="$SPOONBOT_PARAPHRASE" --output spoonbot.gpg .buildscript/spoonbot.gpg.enc
gpg --fast-import spoonbot.gpg

# getting the previous version on Maven Central
# for some reasons, some versions don't get index by search.maven.org/solrsearch/, and it break the build
# so we have to roll our own: https://gist.github.com/monperrus/9ee373e2500e40b634f8daf707f6ad2a
PREVIOUS_MAVEN_CENTRAL_VERSION=`curl "http://search.maven.org/solrsearch/select?q=a:gumtree-spoon-ast-diff+g:fr.inria.gforge.spoon.labs&rows=20&wt=json" -L | jq -r ".response.docs[0].latestVersion" | egrep -o "[0-9]+$"`

# and incrementing it
mvn versions:set -DnewVersion=1.$((PREVIOUS_MAVEN_CENTRAL_VERSION+1))

mvn -Prelease deploy --settings .buildscript/settings.xml -Dmaven.test.skip=true
echo "Well deployed!"
