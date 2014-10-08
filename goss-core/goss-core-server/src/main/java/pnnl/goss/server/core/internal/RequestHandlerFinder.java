package pnnl.goss.server.core.internal;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import org.apache.xbean.finder.BundleAnnotationFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.UrlSet;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.server.annotations.RequestHandler;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;

public class RequestHandlerFinder {
	
	private static Logger log = LoggerFactory.getLogger(RequestHandlerFinder.class);
	private BundleContext context;
	
	public RequestHandlerFinder(BundleContext bc){
		this.context = bc;
	}
	
	
	public RequestHandlerFinder(GossRequestHandlerRegistrationService registrationService) throws IOException{
		
		Reflections ref = new Reflections();
		
		// Get the annotation classes that provide a requesthandler class.
		Set<Class<?>> refs = ref.getTypesAnnotatedWith(RequestHandler.class); //.getConstructorsAnnotatedWith(RequestHandler.class);
		
		// For each of the annotations we need to add all of the elements that are specified
		// in the requests parameter.
		for(Class r : refs){
			log.debug("Found handler: "+r.getName());
			for(Annotation a : r.getAnnotations()){
				for(Class p: ((RequestHandler)a).requests()){
					registrationService.addHandlerMapping(p, r);
				}
			}
		}
		
//		urlSet.excludeJavaHome();
//		// exclude some well known libs to go faster in real apps
//	      urlSet = urlSet.exclude(".*/activation(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/activeio-core(-[\\d.]+)?(-incubator)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/activemq-(core|ra)(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/annotations-api-6.[01].[\\d.]+.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/asm-(all|commons|util|tree)?[\\d.]+.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/avalon-framework(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/axis2-jaxws-api(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/backport-util-concurrent(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/bcprov-jdk15(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/catalina(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/cglib-(nodep-)?[\\d.]+.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/com\\.ibm\\.ws\\.[^/]*.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/commons-(logging|logging-api|cli|pool|lang|collections|dbcp|dbcp-all)(-[\\d.r-]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/cxf-bundle(-[\\d.]+)?(incubator)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/openejb-cxf-bundle(-[\\d.]+)?(incubator)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/derby(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/ejb31-api-experimental(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/geronimo-(connector|transaction)(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/geronimo-[^/]+_spec(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/geronimo-javamail_([\\d.]+)_mail(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/hibernate-(entitymanager|annotations)?(-[\\d.]+(ga)?)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/howl(-[\\d.-]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/hsqldb(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/idb(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/idea_rt.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/javaee-api(-embedded)?-[\\d.-]+.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/javassist[^/]*.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/jaxb-(impl|api)(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/jboss-[^/]*.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/jbossall-[^/]*.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/jbosscx-[^/]*.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/jbossjts-?[^/]*.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/jbosssx-[^/]*.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/jmdns(-[\\d.]+)?(-RC\\d)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/juli(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/junit(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/log4j(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/logkit(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/mail(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/neethi(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/org\\.eclipse\\.persistence\\.[^/]*.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/org\\.junit_.[^/]*.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/openjpa-(jdbc|kernel|lib|persistence|persistence-jdbc)(-5)?(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/openjpa(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/opensaml(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/quartz(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/saaj-impl(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/spring(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/serp(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/servlet-api(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/slf4j-api(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/slf4j-jdk14(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/stax-api(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/swizzle-stream(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/sxc-(jaxb|runtime)(-[\\d.]+)?(-SNAPSHOT)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/wsdl4j(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/wss4j(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/wstx-asl(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/xbean-(reflect|naming|finder)-(shaded-)?[\\d.]+.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/xmlParserAPIs(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/xmlunit(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/xmlsec(-[\\d.]+)?.jar(!/)?");
//	      urlSet = urlSet.exclude(".*/XmlSchema(-[\\d.]+)?.jar(!/)?");
//		
//		ClassFinder finder = new ClassFinder(loader, urlSet.getUrls());
//		
//		List<Class<?>> pkg = finder.findAnnotatedClasses(RequestHandler.class);
//		
//		for(Class clazz: pkg){
//			System.out.println(clazz.getName());
//			for(Annotation cls:clazz.getAnnotations()){
//				System.out.println(cls.getClass().getName());
//			}
//
//			RequestHandler hndlr = (RequestHandler) clazz.getAnnotation(RequestHandler.class);
//			for(Class r: hndlr.requests()){
//				
//				for (int i = 0; i<r.getClasses().length; i++){
//					System.out.println(r.getClasses()[i].getCanonicalName());
//				}
//				System.out.println(r.getCanonicalName());
//				
//				System.out.println(r.getName());
//			}
//			
//			//System.out.println(clazz.getName());
//		}
//		
		
	}
	
	
//	private BundleAnnotationFinder createBundleAnnotationFinder(Bundle bundle) {
//        ServiceReference sr = this.context.getServiceReference(PackageAdmin.class.getName());
//        PackageAdmin pa = (PackageAdmin) this.context.getService(sr);
//        BundleAnnotationFinder baf = null;
//        try {
//            baf = new BundleAnnotationFinder(pa, bundle);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        this.context.ungetService(sr);
//        
//        return baf;
//    }

}
