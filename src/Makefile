# ======== Environment ========
BUILD_DIR = ../build
JAVAC_FLAGS = -d ${BUILD_DIR}
JAR_FLAGS = -cf flispcc.jar *

JAVAC = javac ${JAVAC_FLAGS}
JAVA = java ${JAVA_FLAGS}
JAR = ${JAR} ${JAR_FLAGS}

PARSER=${JAVA} ${JAVA_FLAGS} org.antlr.v4.Tool
PARSER_FLAGS=-lib C -package C
LEXER=${JAVA} ${JAVA_FLAGS} org.antlr.v4.Tool
LEXER_FLAGS=-lib C -package C
# =============================

.PHONY: default generate build clean 

default: generate  C/CLexer.java C/CParser.java Main.class

generate:
	bnfc --java --antlr4 C.cf

# Files goals below:
Compiler.class: compiler/Compiler.java
	${JAVAC} $<

TypeChecker.class: typechecker/TypeChecker.java
	${JAVAC} $<

Main.class: Main.java TypeChecker.class Compiler.class
	${JAVAC} $<

# =========== Rules needed to create parser =============
# Create parser source via bnfc (dependency C.cf needs to be first).
C/CLexer.java : C/CLexer.g4
	${LEXER} ${LEXER_FLAGS} C/CLexer.g4
	-cp TypeExp.java C/Absyn/Exp.java

C/CParser.java : C/CParser.g4
	${PARSER} ${PARSER_FLAGS} C/CParser.g4

C/CLexer.class : C/CLexer.java C/CParser.java

C/CParser.class : C/CParser.java

C/PrettyPrinter.class : C/PrettyPrinter.java
# default rule
%.class: %.java
	${javac} $<

# Build into jar
build: default
	${jar} 




# Cleans directories from class files
clean: 
	-rm -f *.class
	-rm -f typechecker/*.class
	-rm -f compiler/*.class
	-rm -rf C/
