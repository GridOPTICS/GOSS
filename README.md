GridOPTICS Software System (GOSS) README

Installation

    1. Download Apache Karaf 3.0.0 
    2. Extract to a location of your choice (c:\apache-karaf-3.0.0\) KARAF_HOME
    3. Open a command line to KARAF_HOME\bin
    4. Execute karaf.(bat|sh) (arguments debug to start remote debugging capability on port 5005)
    5. Install cxf using the following commmands
        - feature:repo-add cxf 2.7.10
        - feature:install cxf
    6. Install activemq using the following commands
        - feature:repo-add activemq 5.9.0
        - feature:install activemq
        - (Optional) feature:install activemq-web-console
    7. Install mysql drivers
        - install mvn:mysql/mysql-connector-java/5.1.25

Deploy/Development

    1. Download from the git repository the source code to GOSS_HOME.
    2. Copy GOSS_HOME/goss.properties to your user directory.
    3. Modify connection strings and other parameters in the goss.properties file.
    4. Open terminal at GOSS_HOME
    5. Execute mvn install
    6. Install features
        - feature:repo-add mvn:pnnl.goss/goss-core-feature/0.1.0-SNAPSHOT/xml/features
        - feature:repo-add mvn:pnnl.goss/goss-powergrid-feature/0.0.1-SNAPSHOT/xml/features
        - feature:repo-add mvn:pnnl.goss/goss-gridmw-feature/0.0.1-SNAPSHOT/xml/features
        - feature:repo-add mvn:pnnl.goss/goss-fusiondb-feature/0.0.1-SNAPSHOT/xml/features
        - feature:repo-add mvn:pnnl.goss/goss-dsa-feature/0.0.1-SNAPSHOT/xml/features
        - feature:repo-add mvn:pnnl.goss/goss-kairosdb-feature/0.0.1-SNAPSHOT/xml/features
        - feature:repo-add mvn:pnnl.goss/goss-mdart-feature/0.0.1-SNAPSHOT/xml/features
        - feature:repo-add mvn:pnnl.goss/goss-tool-sharedperspective-feature/0.0.1-SNAPSHOT/xml/features
        - feature:repo-add mvn:pnnl.goss/goss-tool-gridpack-feature/0.0.1-SNAPSHOT/xml/features
        
        - feature:install goss-core-feature
        - feature:install goss-powergrid-feature goss-gridmw-feature goss-fusiondb-feature goss-dsa-feature
        - feature:install goss-mdart-feature goss-tool-sharedperspective-feature goss-tool-gridpack-feature
        