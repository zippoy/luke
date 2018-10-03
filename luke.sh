#!/bin/bash

if [[ -n "$JFX_HOME" ]]; then
  JAVA_OPTIONS="$JAVA_OPTIONS --module-path $JFX_HOME/lib --add-modules=javafx.controls,javafx.fxml"
fi

if [[ -d `echo $LUKE_PATH` ]]; then
  nohup java $JAVA_OPTIONS -jar $LUKE_PATH/target/luke-javafx-with-deps.jar > /dev/null 2>&1 &
else
  echo "Unable to find the LUKE_PATH environnement variable..."
  echo "Assuming you're running from the root folder of luke..."
  nohup java $JAVA_OPTIONS -jar target/luke-javafx-with-deps.jar > /dev/null 2>&1 &
fi

