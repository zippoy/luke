#!/bin/bash

if [[ -d `echo $LUKE_PATH` ]]; then
  nohup java -jar $LUKE_PATH/target/luke-swing-with-deps.jar > /dev/null &
else
  echo "Unable to find the LUKE_PATH environnement variable..."
  echo "Assuming you're running from the root folder of luke..."
  nohup java -jar target/luke-swing-with-deps.jar > /dev/null &
fi
