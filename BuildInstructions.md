# Build Instructions #

omeglespy-z is currently organized into 3 main components:

  * the core library on top of which the applications are built (omeglespy-z-core)
  * the desktop application (omeglespy-z-desktop)
  * the command line application (omeglespy-z-cl)

## Prerequisites ##

No matter which component(s) of omeglespy-z you desire to build the prerequisites are the same:

  * JDK 5 or higher (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * Maven 2 or higher (http://maven.apache.org/download.html if you use maven 3 and run into an issue, please fill out an issue)
  * A subversion client

Please note that you can also run the build by using Eclipse Helios or higher with the latest version of m2e installed. But if you're smart enough to get that installed, you're probably smart enough to not need this guide.

## Details ##

First, build the omeglespy-z-core and install it into your local repository (BuildInstructionsCore).

Second, decide which application you want to build (if any) and build it/them (BuildInstructionsDesktop and BuildInstructionsCl).