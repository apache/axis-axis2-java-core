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
public @interface SOAPBinding {
	public enum Style{DOCUMENT, RPC};
	public enum Use {LITERAL, ENCODED};
	public enum ParameterStyle{BARE, WRAPPED};
	Style style() default Style.DOCUMENT;
	Use use() default Use.LITERAL;
	ParameterStyle parameterStyle() default ParameterStyle.WRAPPED;
}
