# Compiler to use
CC=javac

# Directory to find source files
SOURCE_DIR=src

# Directory to find class files
BUILD_DIR=build

# Directory to find documentation HTML
DOCS_DIR=docs

# Directory to find images
IMG_DIR=images

# Directory to find HTML files
HTML_DIR=help

# Name of the main file
MAIN_FILE=tuataraTMSim.MainWindow

# Name of the .JAR file
FILE_JAR=TuataraTuringMachine.jar


# Default behaviour for make
all: gui

# Compile everything such that the GUI can be run, but do not archive.
gui:
	mkdir -p $(BUILD_DIR)
	$(CC) -d $(BUILD_DIR) `find $(SOURCE_DIR) -name "*.java"`
	cp -r $(SOURCE_DIR)/tuataraTMSim/$(IMG_DIR) $(BUILD_DIR)/tuataraTMSim/$(IMG_DIR)
	cp -r $(SOURCE_DIR)/tuataraTMSim/$(HTML_DIR) $(BUILD_DIR)/tuataraTMSim/$(HTML_DIR)

# Compile everything such that the GUI can be run, and archive the $(BUILD_DIR) directory
jar:
	make gui
	cd $(BUILD_DIR) && jar cvfe $(FILE_JAR) $(MAIN_FILE) `find .`

# Generate only javadoc documentation for the project
docs:
	mkdir -p $(DOCS_DIR)
	javadoc -private -d $(DOCS_DIR) `find $(SOURCE_DIR) -name "*.java"`

# Run the project
run:
	cd $(BUILD_DIR) && java $(MAIN_FILE)

# Compile and run the project
compile-run:
	make gui
	make run

# Remove all .class files, .jar files
.PHONY: clean
clean:
	rm -rf $(BUILD_DIR)
