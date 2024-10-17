JAVAC=javac
JAVA=java

ANTLR=java -jar /usr/local/lib/antlr-complete.jar	
GRUN=java org.antlr.v4.gui.TestRig

GRAMMAR=simpleC.g4
SOURCES=simpleC*.java CFGBuilder.java

OUTPUT=cfg.out
INPUT=example.c

all: cfa

simpleCParser.java: $(GRAMMAR)
	$(ANTLR) -no-listener -visitor $(GRAMMAR)

compile: simpleCParser.java
	$(JAVAC) $(SOURCES)

grun: compile
	$(GRUN) simpleC program -tree < $(INPUT)

cfa: compile
	$(JAVA) CFGBuilder > $(OUTPUT)

clean:
	rm -f simpleC*.java simpleC*.class CFGBuilder.class $(OUTPUT)

.PHONY: all compile grun cfa clean