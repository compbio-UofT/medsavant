CLASSPATH=../../..
SOURCEPATH=../../..
JFLAGS=-classpath $(CLASSPATH)
JDFLAGS=-classpath $(CLASSPATH) -sourcepath $(SOURCEPATH)
JAVAC=javac $(JFLAGS) 
OPTIMIZE=-g:none
JAVA=java $(JFLAGS)
JAVADOC=javadoc $(JDFLAGS)
CVS=cvs -q
BTE=bte
ANT=classic-ant

.SUFFIXES:
.SUFFIXES: .lex .java
.SUFFIXES: .java .class

all:
	@$(MAKE) -s --no-print-directory junkclean
	@$(MAKE) -s --no-print-directory spell
	@$(MAKE) -s --no-print-directory neaten
	@$(MAKE) -s --no-print-directory compile
	@$(MAKE) -s --no-print-directory build
	@$(MAKE) -s --no-print-directory javadoc
	@$(MAKE) -s --no-print-directory htmlfiles
	@$(MAKE) -s --no-print-directory htmlsource

spell: *.bte *.java
	@echo Make: Running spell check.$?
	@./spell.sh $?
	@touch spell

.PHONY : compile
compile: javafiles classes

neaten: *.java
	@./neaten.sh $?
	@touch neaten
	
.PHONY: htmlfiles
htmlfiles: *.bte
	@echo Make: Compiling web pages.
	@bte .

.PHONY: htmlfilesclean
htmlfilesclean: 
	@echo Make: Removing generated html files.
	@rm -f `ls *.html | grep -v package` 
	
LEXFILES=$(wildcard *.lex)
.PHONY: javafiles
javafiles: $(LEXFILES:.lex=.java)
	@# Write a bash script that will compile the files in the todo list
	@echo "#!/bin/bash" > tempCommand	
	@# If the todo list doesn't exist, don't compile anything
	@echo "if [ -e tempChangedLexFileList ]" >> tempCommand
	@echo "then" >> tempCommand
	@# Make sure each file is only on the todo list once.
	@echo "sort tempChangedLexFileList | uniq  > tempChangedLexFileListUniq" >> tempCommand
	@echo "FILES=\`cat tempChangedLexFileListUniq\`" >> tempCommand
	@# Compile the files.
	@echo "echo Make: Compiling: $$ FILES" >> tempCommand
	@echo "$(JLEX) $$ FILES" >> tempCommand
	@echo "for file in $$ FILES" >> tempCommand
	@echo "do" >> tempCommand
	@# Each generated java file needs to be compiled by the java compiler.
	@echo "echo \"$$ {file%.lex}.java\" >> tempChangedJavaFileList" >> tempCommand
	@echo "done" >> tempCommand
	@echo "fi" >> tempCommand
	@# Remove extra spaces in the script that follow the dollar signs.
	@sed "s/\$$ /\$$/" tempCommand > tempCommand.sh
	@# Make the script executable.
	@chmod +x tempCommand.sh
	@# Call the script
	@./tempCommand.sh
	@rm -f tempCommand tempCommand.sh tempChangedLexFileList tempChangedLexFileListUniq *~

.lex.java:
	@#for each changed lex file, add it to the todo list.
	@echo "$<" >> tempChangedLexFileList

.PHONY: javafilesclean
javafilesclean: 
	@echo Make: Removing generated java files.
	@rm -f *Lexer.java

JAVAFILES=$(wildcard *.java)
.PHONY: classes
classes: javafiles $(JAVAFILES:.java=.class)
	@# Write a bash script that will compile the files in the todo list
	@echo "#!/bin/bash" > tempCommand	
	@# If the todo list doesn't exist, don't compile anything
	@echo "if [ -e tempChangedJavaFileList ]" >> tempCommand
	@echo "then" >> tempCommand
	@# Make sure each file is only on the todo list once.
	@echo "sort tempChangedJavaFileList | uniq  > tempChangedJavaFileListUniq" >> tempCommand
	@echo "FILES=\`cat tempChangedJavaFileListUniq\`" >> tempCommand
	@# Compile the files.
	@echo "echo Make: Compiling: $$ FILES" >> tempCommand
	@echo "$(JAVAC) $$ FILES" >> tempCommand
	@echo "fi" >> tempCommand
	@# Remove extra spaces in the script that follow the dollar signs.
	@sed "s/\$$ /\$$/" tempCommand > tempCommand.sh
	@# Make the script executable.
	@chmod +x tempCommand.sh
	@# Call the script
	@./tempCommand.sh
	@rm -f tempCommand tempCommand.sh tempChangedJavaFileList tempChangedJavaFileListUniq

.java.class:
	@#for each changed java file, add it to the todo list.
	@echo "$<" >> tempChangedJavaFileList

.PHONY: classesclean
classesclean: junkclean
	@echo Make: Removing generated class files
	@rm -f *.class

.PHONY: junkclean	        
junkclean:
	@echo Make: Removing utilites detritus.
	@rm -rf *~ ~* temp* utils_*.jar out.txt *.bak CSVTest.txt CircularBufferTestResults.txt com/ gnu/ srcbuild/

.PHONY: buildclean	        
buildclean: junkclean
	@echo Make: Removing generated jar files.
	@rm -f utils.jar randpass.jar
        
.PHONY: javadocclean	        
javadocclean: junkclean
	@echo Make: Removing generated documentation.
	@rm -rf doc/ javadoc

.PHONY: htmlsourceclean	        
htmlsourceclean: junkclean
	@echo Make: Removing generated html source.
	@rm -rf src/ htmlsource

.PHONY: clean	        
clean: buildclean javadocclean htmlsourceclean htmlfilesclean
	@echo Make: Removing generated class files.
	@rm -f *.class

.PHONY: allclean        
allclean: clean javafilesclean
	@echo Make: Removing all files not in CVS.
	@rm -rf neaten spell release javadoc htmlsource src/
	
.PHONY: cleanall        
cleanall: allclean

javadoc: *.java
	@echo Make: Generating javadoc
	@rm -rf doc
	@mkdir doc
	@$(JAVADOC) \
		-bottom '<p>Copyright (c) 2001-2004 by <a href="http://ostermiller.org/contact.pl?regarding=Java+Utilities">Stephen Ostermiller</a></p>' \
		-header "<h1><a target=\"_top\" href="http://ostermiller.org/utils/">com.Ostermiller.util</a> Java Utilities</h1>" \
		-link http://java.sun.com/j2se/1.4.2/docs/api/ -d doc/ \
		com.Ostermiller.util > /dev/null
	@touch javadoc

.PHONY: build
build: utils.jar randpass.jar

randpass.jar: *RandPass*.class *RandPass*.properties *.TXT 
	@echo Make: Building  randpass.jar.
	@mkdir -p com/Ostermiller/util
	@cp *RandPass*.class *RandPass*.properties *.TXT com/Ostermiller/util/
	@jar cfv randpass.jar com/ > /dev/null
	@rm -rf com/
	
utils.jar: *.java *.class *.sh *.lex *.properties *.txt *.TXT *.csv *.bte *.dict Makefile *.xml ../../../gnu/getopt/*.*
	@echo Make: Building jar file.
	@$(ANT) dist > /dev/null

.PHONY: test
test: 
	$(JAVA) com.Ostermiller.util.TokenizerTests
	$(JAVA) com.Ostermiller.util.CSVLexer CSVRegressionTest.csv > out.txt
	@diff out.txt CSVRegressionTestResults.txt
	$(JAVA) com.Ostermiller.util.ExcelCSVLexer ExcelCSVRegressionTest.csv > out.txt
	@diff out.txt ExcelCSVRegressionTestResults.txt
	$(JAVA) com.Ostermiller.util.CSVTests
	$(JAVA) com.Ostermiller.util.CircularBufferTests
	$(JAVA) com.Ostermiller.util.UberPropertiesTests
	$(JAVA) com.Ostermiller.util.LabeledCSVParserTests
	$(JAVA) com.Ostermiller.util.Base64Tests
	$(JAVA) com.Ostermiller.util.MD5Tests
	$(JAVA) com.Ostermiller.util.SizeLimitInputStreamTests
	$(JAVA) com.Ostermiller.util.SignificantFiguresTests
	$(JAVA) com.Ostermiller.util.ConcatTests

.PHONY: update
update: 
	@$(CVS) update -RPd .
        
.PHONY: commit
commit: 
	@$(CVS) commit

release: *.html src/* utils.jar randpass.jar .htaccess install.sh doc/
	@./release.sh $?
	@touch release

.PHONY: install
install:
	@./install.sh

htmlsource: *.java *.properties *.lex
	@echo Make: Generating colored html source: $?
	@rm -rf srcbuild/
	@mkdir srcbuild
	@cp $? src.bte srcbuild
	@rm -f srcbuild/*Lexer.java
	@touch srcbuild/tempdummy.java srcbuild/tempdummy.lex srcbuild/tempdummy.properties
	@echo "cd srcbuild" > srcbuild/temp.sh
	@echo "$(JAVA)/.. com.Ostermiller.util.Tabs -s 4 *.java" >> srcbuild/temp.sh
	@echo "$(JAVA)/.. com.Ostermiller.Syntax.ToHTML -t src.bte -i whitespace *.lex *.java *.properties" >> srcbuild/temp.sh
	@echo "rm -rf tempdummy*" >> srcbuild/temp.sh	
	@chmod +x srcbuild/temp.sh
	@srcbuild/temp.sh
	@mkdir -p src/
	@mv srcbuild/*.*.html srcbuild/*.css src
	@rm -rf srcbuild
	@cp source.sh cleansource.sh src/
	@echo "cd src" > src/temp.sh
	@echo "./cleansource.sh" >> src/temp.sh
	@echo "./source.sh" >> src/temp.sh
	@chmod +x src/temp.sh
	@src/temp.sh
	@rm -f src/*.sh
	@touch htmlsource
