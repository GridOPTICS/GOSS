# GOSS
GridOPTICS Software System
## GridOPTICS Software System (GOSS)

Current GOSS build status: ![GOSS build status](https://travis-ci.org/GridOPTICS/GOSS.svg?branch=master)

The following instructions are for installing GOSS as a base core developer.

 1. Clone the repository (git clone https://github.com/GridOPTICS/GOSS.git or the latest in the 2.x branch git clone https://github.com/GridOPTICS/GOSS.git -b 2.x)
 1. Open command prompt to the root of the cloned repostiory
 1. Execute gradlew check (This will run the integration tests located in pnnl.goss.core.itest folder)  There should be no failures.
 1. Execute gradlew export (Builds a runnable jar file)
 1. Copy the conf folder from pnnl.goss.core.runner to pnnl.goss.core.runner/generated/distribution/executable
 1. cd to pnnl.goss.core.runner/generated/distribution/executable
 1. Execute java -jar goss-core.jar
 
The framework should be started now.  Default commands that goss uses are:

    gs:listDataSources   - Lists the known datasources that have been registered with the server.
    gs:listHandlers      - Lists the known request handlers that have been registered with the server. 
   
Extending the framework with your own handlers and security options are covered in the [wiki](https://github.com/GridOPTICS/GOSS/wiki).
