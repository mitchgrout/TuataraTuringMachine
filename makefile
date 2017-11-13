CC=javac

all: gui
gui:
	javac src/tuataraTMSim/*.java src/tuataraTMSim/gui/*.java

run:
	cd src; java tuataraTMSim.gui.MainWindow

.PHONY: clean
clean:
	rm src/tuataraTMSim/*.class src/tuataraTMSim/gui/*.class
