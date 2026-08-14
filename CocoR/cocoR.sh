#!/bin/bash

java -jar Coco.jar ConfigLang.ATG -package "me.molybdenum.ambience_mini.engine.shared.configuration.syntactic_analysis" -o "../engine/src/main/java/me/molybdenum/ambience_mini/engine/shared/configuration/syntactic_analysis"

[ -e "../engine/src/main/java/me/molybdenum/ambience_mini/engine/shared/configuration/syntactic_analysis/Parser.java.old" ] && rm "../engine/src/main/java/me/molybdenum/ambience_mini/engine/shared/configuration/syntactic_analysis/Parser.java.old"

[ -e "../engine/src/main/java/me/molybdenum/ambience_mini/engine/shared/configuration/syntactic_analysis/Scanner.java.old" ] && rm "../engine/src/main/java/me/molybdenum/ambience_mini/engine/shared/configuration/syntactic_analysis/Scanner.java.old"
