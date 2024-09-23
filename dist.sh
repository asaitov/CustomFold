#!/bin/sh
: ${ARDUINO_PATH?specify path to Arduino IDE installation directory}
rm -rf bin
find ./src/ -type f -name "*.java" | xargs javac -source 8 -target 8 -cp "$ARDUINO_PATH/lib/*" -d ./bin/classes
cd ./bin && mkdir -p CustomFold/tool && zip -r CustomFold/tool/CustomFold.jar ./classes/* && zip -r CustomFold.zip CustomFold && cd ..
