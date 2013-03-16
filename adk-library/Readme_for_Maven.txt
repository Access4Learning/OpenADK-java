OpenADK Java library
====================
This Readme_for_Maven.txt holds important instructions on how to build the final OpenADK for Java. The build can be
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

Maven (http://maven.apache.org/)
--------------------------------
Maven must be installed on your machine for the instructions below. Please refer to the maven website for 
install instructions. If you want to use Maven with Eclipse please refer to instructions below.

------------------
Build Instructions
------------------
Note that the PROFILE may be one of AU, UK, or US.

> `mvn -P <PROFILE> install`

The ADK jar file (with data model built-in) is deposited in the target folder, or may be used in another maven project using the following dependency coordinates.

    <dependency>
        <groupId>openadk</groupId>
        <artifactId>openadk-library</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <classifier>UK</classifier>
    </dependency>

The classifier is required in order to resolve the artifact that contins the desired data model.

Eclipse IDE setup
-----------------

We`ve found that it is easier to use command line Maven with the Eclipse IDE. To prepare Eclipse for this kind of setup, the M2_REPO classpath variable must be configured first using the following steps.

1. Open Eclipse IDE
2. Open the Eclipse >> Preferences... dialog
3. Navigate to Java >> Build Path >> Classpath Variables
4. Click New...
5. Enter `M2_REPO` for the Name
6. Enter `<HOME>/.m2/repository` for the Path
7. Click OK in the Edit Variable Entry dialog
8. Clock OK in the Preferences dialog

Note: If you find that the M2_REPO variable is already set and unmodifiable, then you likely have m2eclipse installed. Ensure that it is using the correct local repository in this case.

The Maven Eclipse plugin may now be used to create an Eclipse project. From the command line perform the following steps.

1. Change directory to OpenADK-java/adk-library
2. Run `mvn -P <PROFILE> eclipse:eclipse`
3. Import the adk-library project into Eclipse as an existing project

Ensure that the `target\generated-sources` exists in the project as a source folder.

Intellij IDEA setup
-------------------

Ensure that the Maven plugins for IDEA are enabled before creating a module from the adk-library folder. To create a module in an existing project follow these steps.

1. Click the File >> New Module... menu option
2. Select `Import module from external model`
3. Click Next
4. Select OpenADK-java/adk-library as the root search directory
5. Deselect Import Maven project automatically
6. Click Next
7. Select the desirect data model profile (AU, UK, or US)
8. Click Next
9. Select opendadk:openadk-library project to import
10. Click Next

After the module is created in the project, the IDEA Maven Projects panel may be used to trigger Maven lifecycles (clean, compile, etc.).
