OpenADK Java library
====================
This Readme_for_Ant.txt holds important instructions on how to build the final OpenADK for Java. The build can be
performed for any locale (US, AU, UK). 

-------------
Pre-Requisite
-------------

Java
----
You must have a Java JDK v1.6+ (not jre) installed on your machine and the JAVA_HOME variable is set and points to the JDK
root directory. The data model class generator is a Java application and therefore requires Java. Also the Ant build tool
(see next paragraph) is a Java based build tool.

Ant (https://ant.apache.org)
-------------------------------------------
The build instructions covered in this file of the OpenADK library requires Ant. All versions from Ant 1.6 upwards 
should work. Please refer to the Ant documentation for installation instructions of Ant. All instructions within this 
file assume that Ant is installed and the the ANT_HOME environment variable is set and points to the Ant root directory.

You can run the required ant targets from either the command line or from within your desired IDE. The instructions 
below assume a command line.

------------------
Build Instructions
------------------
Before you can build the OpenADK library you must ensure that you are in the directory adk-library (this directory).
You should also see a file called ant.properties and build.xml in this directory. If you don't then you are not in the 
correct directory. All instructions below assume that you type the given instruction in a command line tool and run it.

-------
Step 1: Clean up previous builds
-------
c:\adk-library>ant clean

-------
Step 2: Build OpenADK library for a particular locale (AU, US, UK)
-------
In the ant.properties file you must set the property generator.locale to either 'AU', 'UK' or 'US' 
(without the single quote but in uppercase!). This will ensure that the data model classes for the selected locale will
be part of the final OpenADK library. Save changes and then type on the command line the following ant instruction to 
build OpenADK library:

c:\adk-library>ant

The above will build the OpenADK for the selected locale. If you wish to build it for another locale simply change the
generator.locale property in the ant.properties accordingly.

The final OpenADK library can be found in the following directory adk-library\target. You will notice that there are 2 jar 
files in that directory. They are identical but have different names. The generic name of openADK-<version>.jar is simply 
there for eclipse setup of other projects within the OpenADK-java parent project. The second jar file has the name based 
on the other properties in the ant.properties file. This is the library that should be used for your Agents.

