# AsgredLists
This tool automatically generates all the lists and generates and executes pyraf scripts for spectra reduction using asgred task for IRAF.
It is capable of listing images' properties, read from imhead, generating all the lists needed in all the steps of the reduction, generate one or multiple PyRAF scripts and executing them, auto-resolve errors, auto-recognize standard stars.

The latter is valid only for standard stars in the Asiago Astrophisical Observatory list (http://www.astro.unipd.it/~t120/stanlis.htm), for this tool is thought to be used only for images taken from this observatory.

The goal of this tool is NOT to substitute the operator but just to relieve him/her from any work that can be done just as good from a computer.

In particular, it still is care of the operator to make the wavelenght mask for lamps and the actual spectrum extraction done using apall.

# Pre-requirements
AsgredLists requires: IRAF, PyRAF, python (>=2.7), JDK (>=1.8).

# Installation
No installation is needed: you'll find the executable jar file in dist/ directory, just download it and run it.
