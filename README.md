GridOPTICS Software System (GOSS)

The following instructions are to install the goss core modules to a system.  This will only install 
the core system.  The core system is capable of executing in an osgi environment (we use [Apache Karaf](http://karaf.apache.org/) exclusively) out of the box.  If
you would like to develop in standalone mode please see [GOSS-Server](https://github.com/GridOPTICS/GOSS-Server).

Installation Windows 7
  1. Clone the goss repository.
  2. Open command line to the repository root (i.e. git/GOSS folder)
  3. Execute gradlew build install publishToMavenLocal

Installation Linux
  1. Open terminal 
  2. Clone repository (https://github.com/GridOPTICS/GOSS.git)
  3. Change directory to goss (cd GOSS)
  4. Add execute to gradlew (chmod +x gradlew)
  5. Build core project (./gradlew build install publishToMavenLocal)

That's it the goss jars are now available to be used in the local maven repository and 
via karaf.  In addition the goss-core-feature will also be available in the repository.

Eclipse Integration (At the time of writing Luna is the version)
  1. Download latest java-ee eclippse (not java developer or other flavor of eclispe)
  2. Open eclipse (use default or specify your own workspace)
  3. Open Eclipse Marketplace (Menu: Help->Eclipse Marketplace ..)
  4. Search for Gradle (Install Gradle IDE Pack and Gradle Integration for Eclipse)
  5. Search for Groovy (Install Groovy/Grails Tool Suite)
  6. Import Gradle Project (Browse to root of the git repository and click Build Models)

Available Integreation Projects
  - [GOSS-Powergrid](https://github.com/GridOPTICS/GOSS-Powergrid)
