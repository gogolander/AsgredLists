# AsgredLists
This tool automatically generates all the lists and generates and executes pyraf scripts for spectra reduction using asgred package for IRAF.
It is capable of listing images' properties, read from imhead, generating all the lists needed in all the steps of the reduction, generate one or multiple PyRAF scripts and executing them, auto-resolve conflicts and errors, auto-recognize standard stars.

The latter is valid only for standard stars included in the Asiago Astrophisical Observatory list (http://www.astro.unipd.it/~t120/stanlis.htm), for this tool is explicitly thought to be used on images taken from this observatory.

The goal of this tool is NOT to substitute the operator but just to relieve him/her from any work that can be done just as good from a computer.

In particular, it still is care of the operator to make the wavelenght mask for lamps and the actual spectrum extraction, done using the apall task.

# Pre-requirements
AsgredLists requires:
 * IRAF;
 * PyRAF;
 * asgred;
 * python (>=3.0);
 * JDK (>=1.8).

### Pre-requirements installation for Ubuntu/Debian
To install the JDK we will add the <a href="http://ppa.webupd8.org/">WebUpd8</a> PPA repository: it contains the compiled version of JAVA ready to be installed.
##### How to install JDK for Debian
In Debian systems, run the following commands in your terminal:
```sh
su -
echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list
echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886
apt-get update
apt-get install oracle-java8-installer
exit
```
##### How to install JDK for Ubuntu
In Ubuntu systems, run the following commands in your terminal:
```sh
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
```
##### How to install python
Now install python using the following command:
```sh
sudo apt-get install python3
```
##### How to install IRAF and PyRAF
Download <a href="http://www.astro.uson.mx/favilac/downloads/ubuntu-iraf/iso/">this image</a> containing both IRAF and PyRAF.
Then mount it and install it.

# Installation
No installation is needed: you can either download the executable jar file in the release section (https://github.com/gogolander/AsgredLists/releases) or download the source code and compile it.
##### Download sources via git
The best way to download the sources is via the git tool. Install it with the following command:
```sh
sudo apt-get install git
```
and download the repository using this command:
```sh
git clone https://github.com/gogolander/AsgredLists.git
```
##### Compilation
In order to compile AsgredLists you need the Maven compilator. Get it with the following command:
```sh
sudo apt-get install maven
```
Then open a terminal in the source folder and run the following command to launch the compilation:
```sh
mvn clean license:format install prepare-package package
```
If the compilation ends successfully, you will find the executable file ```AsgredLists.jar``` in the subfolder
```
AsgredLists/target
```
##### Warning
When started, the Maven compilation needs to download more than 10Mb of required JAR libraries. Keep it in mind if you are using a metered connection.

# Execution
![Image of Launch with Java Runtime](https://github.com/gogolander/AsgredLists/wiki/images/default_application.png)
You have two choises:
 * use the command ```java -jar asgredLists.jar```
 * since the JAR file is an executable file, you can grant execution priviledge to it with the command ```chmod +x asgredLists.jar``` and associate it with the Java Runtime Machine in this way:
  1. right-click on the JAR, open ```Properties``` and the go to the ```Open with``` tab;
  2. scroll the list until you find ```Oracle Java 8 Runtime```: select it and then click on ```Set as default```
  3. now you can run asgredLists just double-clicking it.
  
# Documentation

For a full documentation about AsgredLists, how it works, how it done, please read our wiki-page: https://github.com/gogolander/AsgredLists/wiki
 
