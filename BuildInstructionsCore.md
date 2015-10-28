# Building omeglespy-z-core #

omeglespy-z-core is the communication library on which all of the omeglespy-z applications are built.

## Details ##

First, make certain you meet the prerequisites (BuildInstructions).

Second, you will check out the code from SVN.

Third, ???.

Fourth, you will invoke the maven build.

Fifth, profit.

### Check out the code from subversion ###

If you have a subversion commandline client, just cd to your preferred directory and invoke

```
svn checkout http://omeglespy-z.googlecode.com/svn/trunk/omeglespy-z-core omeglespy-z-read-only
```

If you have one of those new fangled GUI-based subversion clients, you're on your own :P.

Note that you can also find source that corresponds to the various releases of omeglespy-z-core at http://omeglespy-z.googlecode.com/svn/tags/omeglespy-z-core

### ??? ###

Maybe you modify the code? Or something?

### Invoke the maven build ###

There are a number of maven tasks available, but I'd recommend just running the package task. It's reasonably quick, and it gives you everything you need.

cd into the project root (you'll know you're there because you'll see pom.xml in the directory listing). Then invoke

```
mvn package
```

If you modified the code and/or plan on using the omeglespy-z-core in your own mavenized Java project, you may want to install the omeglespy-z-core build artifact in your local maven repository. Simply invoke

```
mvn install
```

### Profit ###

Everything you'd ever want should be in the /target directory. Enjoy!