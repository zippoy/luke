@echo off
if not "%JFX_HOME%" == "" (
    set JAVA_OPTIONS=--module-path %JFX_HOME%\lib --add-modules=javafx.controls,javafx.fxml
)
start javaw %JAVA_OPTIONS% -jar .\target\luke-javafx-with-deps.jar
