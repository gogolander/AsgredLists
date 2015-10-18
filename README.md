# AsgredLists
This tool automatically generates all the lists and generates and executes pyraf scripts for spectra reduction using asgred package for IRAF.
It is capable of listing images' properties, read from imhead, generating all the lists needed in all the steps of the reduction, generate one or multiple PyRAF scripts and executing them, auto-resolve conflicts and errors, auto-recognize standard stars.

The latter is valid only for standard stars included in the Asiago Astrophisical Observatory list (http://www.astro.unipd.it/~t120/stanlis.htm), for this tool is explicitly thought to be used on images taken from this observatory.

The goal of this tool is NOT to substitute the operator but just to relieve him/her from any work that can be done just as good from a computer.

In particular, it still is care of the operator to make the wavelenght mask for lamps and the actual spectrum extraction, done using the apall task.

# Pre-requirements
AsgredLists requires: IRAF, PyRAF, asgred, python (>=2.7), JDK (>=1.8).

# Installation
No installation is needed: you'll find the executable jar file in dist/ directory, just download it and run it.