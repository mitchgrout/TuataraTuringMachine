# Tuatara Turing Machine
Tuatarata Turing Machine is a Java program used for the simulation of
finite-state auttomata. Currently the simulation of both deterministic and
nondeterministic Turing Machines and finite-state acceptors is supported. This
is a fork of the original Tuatara Turing Machine written by Jimmy Foulds, which
patches issues present in the original release, adds new features, and provides
a more generic framework to allow the easy addition of new functionality.

## Getting Started
Precompiled releases in the form of JAR archives are available under *Releases*.
Running Tuatara Turing Machine requires the Java JRE (version 1.5 or later). To
run Tuatara, either double-click the JAR archive, or run 

`java -jar TuataraTuringMachine.jar` 

from the command line. To compile Tuatara Turing Machine, the following tools
are needed:
* `git`
* `make`
* `javac`

To compile, clone the repo and run `make`:

```
git clone https://github.com/mitchgrout/TuatararaTuringMachine.git
cd TuataraTuringMachine
make
```

This will compile the project into .class files. To produce a JAR archive, instead run:

`make jar`

This will produce a JAR archive in the build directory.

JavaDoc documentation can also be produced by running:

`make docs`

## Authors
* **Jimmy Foulds** - Initial design and implementation of Tuatara Turing Machine
* **Mitchell Grout** - Redesign and rewrite of existing code, extended functionality
* **Justin Bedggood** - Redesign of all sprites used in the program
