# GridOPTICS Software System (GOSS)

Current GOSS build status: ![GOSS build status](https://travis-ci.org/GridOPTICS/GOSS.svg?branch=master)

### Pre-Requisite
 1. JAVA 8 SDK

### Installing GOSS

 1. Clone the repository: `git clone https://github.com/GridOPTICS/GOSS.git`
 1. Open terminal to the root of the cloned repository
 1. Execute `gradlew check`. This will run the integration tests located in pnnl.goss.core.itest folder.There should be no failures.
 1. Execute `gradlew export`. This builds a runnable jar file.
 1. Copy the conf folder from pnnl.goss.core.runner to pnnl.goss.core.runner/generated/distribution/executable
 1. Change the current directory to pnnl.goss.core.runner/generated/distribution/executable
 1. Execute java -jar goss-core.jar
 
The framework should be started now.  Default commands that goss uses are:

    gs:listDataSources   - Lists the known datasources that have been registered with the server.
    gs:listHandlers      - Lists the known request handlers that have been registered with the server. 
   
Extending the framework with your own handlers and security options are covered in the [wiki](https://github.com/GridOPTICS/GOSS/wiki).
