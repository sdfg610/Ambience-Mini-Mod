java -jar Coco.jar Amb.ATG -package "me.molybdenum.ambience_mini.engine.configuration.syntactic_analysis" -o "../engine/src/main/java/me/molybdenum/ambience_mini/engine/configuration/syntactic_analysis"

@ECHO OFF

if exist "..\engine\src\main\java\me\molybdenum\ambience_mini\engine\configuration\syntactic_analysis\Parser.java.old" del "..\engine\src\main\java\me\molybdenum\ambience_mini\engine\configuration\syntactic_analysis\Parser.java.old"

if exist "..\engine\src\main\java\me\molybdenum\ambience_mini\engine\configuration\syntactic_analysis\Scanner.java.old" del "..\engine\src\main\java\me\molybdenum\ambience_mini\engine\configuration\syntactic_analysis\Scanner.java.old"

