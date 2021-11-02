package opalib.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field is an entity field
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface OpaField {
    DataType dataType() default DataType.AUTO;

    /**
     * set OE tt field name if differs from Java entity field name
     */
    String name() default "";
}
