CC=javac
DOCC=javadoc
FILE_JAVA=`find . -name "*.java"`
FILE_CLASS=`find . -name "*.class"`
FILE_HTML=`find . -name "*.html" | grep -v "help/"`
FILE_JAR=TuataraTuringMachine.jar


# Default behaviour for $ make
all: gui

# Compile everything such that the GUI can be run, but do not archive.
gui:
	$(CC) $(FILE_JAVA)

# Compile everything such that the GUI can be run, archive, and move the archive to this folder.
jar:
	make gui &&
	cd src   &&
	jar cvfe $(FILE_JAR) tuataraTMSim.MainWindow $(FILE_CLASS) &&
	mv TuataraTuringMachine.jar ..

# Generate only javadoc documentation for the project
docs:
	$(DOCC) $(FILE_JAVA)

# Compile and run the project
run:
	make gui &&
	cd src   && 
	java tuataraTMSim.MainWindow

# Remove all intermediate files, i.e. .class files, .jar files, and javadoc .html files
.PHONY: clean
clean:
	rm $(FILE_CLASS) $(FILE_HTML) $(FILE_JAR)

