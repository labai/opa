package opalib.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter for ABL procedure
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface OpaParam {
    /**
     * parameter direction: IN, OUT, INOUT
     */
    IoDir io() default IoDir.IN;

    /**
     * as data-type
     */
    DataType dataType() default DataType.AUTO;

    Class<?> table() default Void.class; // default fake
}
