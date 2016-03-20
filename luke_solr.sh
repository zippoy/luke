#!/usr/bin/bash
if [[ -d `echo $LUKE_PATH` ]]; then
  java -XX:MaxPermSize=512m -Dluke.ext.decoder.loader=org.apache.lucene.luke.ext.SolrDecoderLoader -jar target/pivot-luke-with-deps.jar
else
  echo "Unable to find the LUKE_PATH environnement variable..."
  echo "Assuming you're running from the root folder of luke..."
  java -XX:MaxPermSize=512m -Dluke.ext.decoder.loader=org.apache.lucene.luke.ext.SolrDecoderLoader -jar target/pivot-luke-with-deps.jar
fi
#
# In order to start luke with your custom analyzer class extending org.apache.lucene.analysis.Analyzer run:
# java -XX:MaxPermSize=512m -cp target/pivot-luke-with-deps.jar:/path/to/custom_analyzer.jar org.apache.lucene.luke.ui.LukeApplication
# your analyzer should appear in the drop-down menu with analyzers on the Search tab
#java -XX:MaxPermSize=512m -cp target/pivot-luke-with-deps.jar:/home/dmitry/projects/github/suggestinganalyzer/target/suggestinganalyzer-1.0-SNAPSHOT.jar org.apache.lucene.luke.ui.LukeApplication
