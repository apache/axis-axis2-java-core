/**
 * 
 */
package javax.jws;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target (ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebResult {
	String name() default "";
	String targetNamespace() default "";
	boolean header() default false;
	String partName() default "";
}
