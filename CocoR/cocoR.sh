#!/bin/bash

java -jar Coco.jar Dims.ATG -package "gsto.ambience_mini.music.loader.syntactic_analysis" -o "../src/main/java/gsto/ambience_mini/music/loader/syntactic_analysis"

[ -e "../src/main/java/gsto/ambience_mini/music/loader/syntactic_analysis/Parser.java.old" ] && rm "../src/main/java/gsto/ambience_mini/music/loader/syntactic_analysis/Parser.java.old"

[ -e "../src/main/java/gsto/ambience_mini/music/loader/syntactic_analysis/Scanner.java.old" ] && rm "../src/main/java/gsto/ambience_mini/music/loader/syntactic_analysis/Scanner.java.old"
