JFLAGS = -g
JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
		  Tema2.java

build: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

#run:
#	java main