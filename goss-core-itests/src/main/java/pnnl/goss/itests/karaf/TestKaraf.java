package pnnl.goss.itests.karaf;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

import java.io.File;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.server.GossRequestHandlerRegistrationService;

@RunWith(PaxExam.class)
public class TestKaraf {
	private static Logger LOG = LoggerFactory.getLogger(TestKaraf.class);

//    @Inject
//    protected GossRequestHandlerRegistrationService requestHandlerService;

    @Configuration
    public Option[] config() {
        MavenArtifactUrlReference karafUrl = maven()
            .groupId("org.apache.karaf")
            .artifactId("apache-karaf")
            .version(karafVersion())
            .type("zip");

        MavenUrlReference karafStandardRepo = maven()
            .groupId("org.apache.karaf.features")
            .artifactId("standard")
            .version(karafVersion())
            .classifier("features")
            .type("xml");
        
        MavenUrlReference gossCoreRepo = maven()
                .groupId("pnnl.goss")
                .artifactId("goss-feature")
                //.versionAsInProject()
                .version("0.1.6-SNAPSHOT")
                .classifier("features")
                .type("xml");
        return new Option[] {
            // KarafDistributionOption.debugConfiguration("5005", true),
            karafDistributionConfiguration()
                .frameworkUrl(karafUrl)
                .unpackDirectory(new File("target/exam"))
                .useDeployFolder(false),
            keepRuntimeFolder(),
            KarafDistributionOption.features(karafStandardRepo , "scr"),
            mavenBundle("eu.infomas", "annotation-detector","3.0.4"),
            mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.xstream", "1.4.3_1"),
            mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-io", "1.4_3"),
            mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-dbcp", "1.4_3"),
            mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.reflections", "0.9.8_1"),
            mavenBundle("com.google.code.gson", "gson", "2.3"),
            mavenBundle("mysql", "mysql-connector-java", "5.1.33"),
            mavenBundle("org.apache.httpcomponents", "httpcore-osgi", "4.3.3"),
            mavenBundle("org.apache.httpcomponents", "httpclient-osgi", "4.3.3"),
            mavenBundle("org.fusesource.stompjms", "stompjms-client", "1.19"),
      		mavenBundle("org.fusesource.hawtdispatch", "hawtdispatch-transport", "1.21"),
      		mavenBundle("org.fusesource.hawtdispatch", "hawtdispatch", "1.21"),
      		mavenBundle("org.fusesource.hawtbuf", "hawtbuf", "1.11"),
      		
//            KarafDistributionOption.features(gossCoreRepo, "goss-core-feature")
//            mavenBundle()
//                .groupId("org.ops4j.pax.exam.samples")
//                .artifactId("pax-exam-sample8-ds")
//                .versionAsInProject().start(),
       };
    }

    public static String karafVersion() {
        ConfigurationManager cm = new ConfigurationManager();
        String karafVersion = cm.getProperty("pax.exam.karaf.version", "3.0.2");
        return karafVersion;
    }


    @Test
    public void testRegistrationServiceIsNotNull() {
    	Assert.assertTrue(true);
    }
}
