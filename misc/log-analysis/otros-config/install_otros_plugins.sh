#!/bin/bash

export OTROS_HOME=/installDir/olv-1.4.0

echo Installs AEM-related OTROS plugins to $OTROS_HOME
echo Deletes pre-installed example plugins

rm $OTROS_HOME/plugins/logimporters/log4j-1.pattern
rm $OTROS_HOME/plugins/logimporters/selenium.pattern
rm $OTROS_HOME/plugins/markers/example-regex.marker
rm $OTROS_HOME/plugins/markers/example-string.marker

cd plugins

cp -R * $OTROS_HOME/plugins
