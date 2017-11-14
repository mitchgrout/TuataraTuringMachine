CC=javac
ROOT=src/tuataraTMSim

all: gui
gui:
	$(CC) $(ROOT)/*.java $(ROOT)/commands/*.java $(ROOT)/exceptions/*.java $(ROOT)/TM/*.java
	
run:
	cd src; java tuataraTMSim.MainWindow

.PHONY: clean
clean:
	rm $(ROOT)/*.class $(ROOT)/commands/*.class $(ROOT)/exceptions/*.class $(ROOT)/TM/*.class

