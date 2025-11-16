#!/bin/bash

java -jar Coco.jar Amb.ATG -package "me.molybdenum.ambience_mini.engine.loader.syntactic_analysis" -o "../engine/src/main/java/me/molybdenum/ambience_mini/engine/loader/syntactic_analysis"

[ -e "../engine/src/main/java/me/molybdenum/ambience_mini/engine/loader/syntactic_analysis/Parser.java.old" ] && rm "../engine/src/main/java/me/molybdenum/ambience_mini/engine/loader/syntactic_analysis/Parser.java.old"

[ -e "../engine/src/main/java/me/molybdenum/ambience_mini/engine/loader/syntactic_analysis/Scanner.java.old" ] && rm "../engine/src/main/java/me/molybdenum/ambience_mini/engine/loader/syntactic_analysis/Scanner.java.old"
