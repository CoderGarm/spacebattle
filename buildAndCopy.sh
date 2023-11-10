#!/bin/bash

source version.sh
version=$(getVersion)

echo "Start building and uploading the application."
echo "Did you upgraded the version in the pom?"
echo "Current is $version"
echo "Please type 'yes' if you upgraded the version already - otherwise abort with any other input:"
read input
echo "You have chosen $input"
if [ "$input" = "yes" ]; then
    echo "Proceed..."
else
    echo "Aborted."
    exit 0
fi

versionFile="version.txt"
echo "$version" > $versionFile

mvn clean install;
check=$?
if [ "$check" -eq 0 ]; then
    echo 'build successful'
else
    echo 'build failure'; exit 1;
fi

# fixme check scp scripts/startSB.sh medusa:spacebattle/
scp $versionFile medusa:spacebattle/
scp scripts/deployBackend.sh medusa:
rsync -a --ignore-existing --progress data/sql/delta/ medusa:/home/karsten/uploadTarget/sql/delta/
scp "target/spacebattle-$version.jar" medusa:uploadTarget/
rm $versionFile
