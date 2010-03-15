#!/bin/bash

if [ $# -lt 1 ] ; then
      echo "Syntax: $0 GRIFFIN_HOME"
      exit 0
fi

echo "Installing griffin into $1..."
mkdir -p $1
mkdir -p $1/logs
cp griffin_full.jar $1
cp griffin-ctx.xml $1
sed "s@GRIFFIN_HOME@$1@g" log4j.properties > log4j.p
mv log4j.p $1/log4j.properties
cp griffin /etc/init.d/
rm -f /etc/default/griffin
touch /etc/default/griffin
echo "APP_HOME=$1" >> /etc/default/griffin
echo "JAVA_OPTIONS=\"-Dlog4j.configuration=file:$1/log4j.properties -server -Xms128m -Xmx384m\"" >> /etc/default/griffin
echo "Done."
exit 0
