OpenADK - Data Model Class Library Generator (Java & .Net)
==========================================================

This Readme.txt holds important instructions on how to build the data model classes for a particular 
locale (US, AU, UK). These classes will be required to build the final OpenADK libraries for Java and .Net. 

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
The generation of the Java/.Net data model classes requires Ant. All versions from Ant 1.6 upwards should work. 
Please refer to the Ant documentation for installation instructions of Ant. All instructions within this file assume 
that Ant is installed and the the ANT_HOME environment variable is set and points to the Ant root directory.

You can run the required ant targets from either the command line or from within your desired IDE. The instructions 
below assume a command line.

------------------
Build Instructions
------------------
Before you can build the data model classes you must ensure that you are in the directory adk-generator (this directory).
You should also see a file called build.xml in this directory. If you don't then you are not in the correct directory. 
All instructions below assume that you type the given instruction in a command line tool and run it. You can also run the
appropriate ant target from within your IDE, assuming your IDE supports ant or has an ant plugin.

------
Step 1: Clean up previous builds
-------
c:\adk-generator>ant clean

-------
Step 2: Build data model classes for Java or .Net for a particular locale (AU, US, UK)
-------
Generate Data Model Classes for Java for AU (Australia)
c:\adk-generator>ant AU

The above will build the java classes for AU. If you wish to build them for .Net then type the following ant instruction
on the command line: 

c:\adk-generator>ant dotnet.au

etc.

The generated classes can be found in the directory adk-generator\target\adkgen\generated-sources\openadk\library
If you change data model configuration files in the adk-generator\datadef directory you need to re-run the ant target
from Step 2.



