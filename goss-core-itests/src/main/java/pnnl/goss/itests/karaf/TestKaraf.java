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
	
	@Inject
	protected GossRequestHandlerRegistrationService registrationService;
	
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
				.artifactId("goss-core-feature")
				.versionAsInProject()
				.classifier("features")
				.type("xml");
		
		
		return new Option[] {
				// KarafDistributionOption.debugConfiguration("5005", true),
				karafDistributionConfiguration()
				.frameworkUrl(karafUrl)
				.unpackDirectory(new File("target/exam"))
				.useDeployFolder(false),
				keepRuntimeFolder(),
				KarafDistributionOption.features(karafStandardRepo, "scr")
//				,
//				KarafDistributionOption.features(gossCoreRepo, "goss-core-feature")		
		};
	}
	
	public static String karafVersion() {
		ConfigurationManager cm = new ConfigurationManager();
		String karafVersion = cm.getProperty("pax.exam.karaf.version", "3.0.2");
		return karafVersion;
	}
	
	@Test
	public void testRun(){
		Assert.assertTrue(true);
	}
}
