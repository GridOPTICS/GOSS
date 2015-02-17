package pnnl.goss.core.server.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import pnnl.goss.core.Request;
import pnnl.goss.security.core.authorization.AbstractAccessControlHandler;
import pnnl.goss.security.core.authorization.basic.AccessControlHandlerAllowAll;

/**
 * The RequestItem annotation interface allows the developer to explicitly set
 * up handlers on there classes.
 * 
 * @author Craig Allwardt
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestItem {
	
	/**
	 * The class must have extended the base Request class for 
	 * it to be allowed.
	 * 
	 * @return
	 */
	Class<? extends Request> value();
	
	/**
	 * The class must have extended AbstractAccessControlHandler in order to
	 * be able to participate.
	 * 
	 * @return
	 */
	Class<? extends AbstractAccessControlHandler> access() default AccessControlHandlerAllowAll.class;
}
