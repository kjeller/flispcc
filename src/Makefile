# ======== Environment ========
BIN_DIR = ../bin
JAVAC = javac
JAVAC_FLAGS = -sourcepath .
JAVA = java

javac = ${JAVAC} ${JAVAC_FLAGS}
java = ${JAVA} ${JAVA_FLAGS}

CUPFILE = C/_cup.cup
# =============================

.PHONY: default clean

default: Main.class

# Files goals below:
Compiler.class: compiler/Compiler.java
	${javac} $<

TypeChecker.class: typechecker/TypeChecker.java
	${javac} $<

Main.class: Main.java C/Test.class TypeChecker.class Compiler.class
	${javac} $<

# =========== Rules needed to create parser =============
# Create parser source via bnfc (dependency C.cf needs to be first).
# Patch C/Absyn/Exp.java
C/Yylex $(CUPFILE) C/Test.java : C.cf
	bnfc --java $<
	-cp TypeExp.java C/Absyn/Exp.java
 
# Create parser and move it to the correct location.
C/parser.java C/sym.java : $(CUPFILE)
	$(java) java_cup.Main -package C $<
	mv parser.java sym.java C/

# Create lexer.
C/Yylex.java : C/Yylex
	$(java) JLex.Main $<

# Compile lexer.
C/Yylex.class : C/Yylex.java C/sym.class
	$(javac) $<

# Create parser test.
C/Test.class : C/Test.java C/parser.class C/sym.class C/Yylex.class
	$(javac) $<

# default rule
%.class: %.java
	${javac} $<

clean: 
	-rm -f *.class
	-rm -f typechecker/*.class
	-rm -f compiler/*.class
	-rm -rf C/
