package opalib.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class is an entity (temp-table)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface OpaTable {
    /**
     * all entity fields will be consider as OpaField (even annotation OpaField is missing)
     */
    boolean allowOmitOpaField() default true; // changing default! was false;
}
