#!/bin/bash

JAVA_OPTIONS="-Xmx1024m -Xms512m -XX:MaxMetaspaceSize=256m"
JAR_FILE="target/luke-swing-with-deps.jar"
if [[ ! -d `echo $LUKE_PATH` ]]; then
  LUKE_PATH=$(cd $(dirname $0) && pwd)
  echo "Unable to find the LUKE_PATH environnement variable."
  echo "Set LUKE_PATH to $LUKE_PATH"
fi

cd ${LUKE_PATH}
nohup java ${JAVA_OPTIONS} -jar ${JAR_FILE} > ${HOME}/.luke.d/luke_out.log 2>&1 &