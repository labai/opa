package com.github.labai.opa;

import com.progress.open4gl.Parameter;
import com.progress.open4gl.dynamicapi.ParameterSet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Augustus
 *
 * OpenEdge Procedure Adapter.
 * Enums, annotations, config and etc.
 *
 */
public class Opa {

	private static final int BOTH = 1;
	private static final int PARAM = 2;
	private static final int TT = 3;

	/**
	 * Procedure's parameter direction
	 */
	public enum IoDir {
		IN(ParameterSet.INPUT), // 1
		OUT(ParameterSet.OUTPUT), // 2

		// @Deprecated - is it really good style to have input-output parameter?
		INOUT(ParameterSet.INPUT_OUTPUT); // 3

		// id in OE javaProxy libraries
		final public int progressId;

		IoDir(int progressId) {
			this.progressId = progressId;
		}
	}

	/**
	 * Progress data types
	 */
	public enum DataType {
		// Pseudo type for annotations - recognize progress type by java type and use default from matched
		AUTO(-1, BOTH),

		// allowed in params and temp-tables
		CHARACTER(Parameter.PRO_CHARACTER, BOTH), // 1
		INTEGER(Parameter.PRO_INTEGER, BOTH), // 4
		INT64(Parameter.PRO_INT64, BOTH), // 41
		DECIMAL(Parameter.PRO_DECIMAL, BOTH), // 5
		DATE(Parameter.PRO_DATE, BOTH), // 2
		LOGICAL(Parameter.PRO_LOGICAL, BOTH), // 3
		DATETIME(Parameter.PRO_DATETIME, BOTH), // 34
		DATETIMETZ(Parameter.PRO_DATETIMETZ, BOTH), // 40
		ROWID(Parameter.PRO_ROWID, BOTH), // 13
		//RAW(Parameter.PRO_RAW, BOTH), // 8

		// allowed in temp-table only
		RECID(Parameter.PRO_RECID, TT), // 7
		BLOB(Parameter.PRO_BLOB, TT), // 18
		CLOB(Parameter.PRO_CLOB, TT), // 19

		// allowed in params only
		LONGCHAR(-1, PARAM),
		MEMPTR(-1, PARAM);

		// id in OE javaProxy libraries
		final public int progressId;
		// is this data type allowed in temp-tables
		final boolean allowInTT;
		final boolean allowInParam;

		DataType(int progressId, int allow) {
			this.progressId = progressId;
			this.allowInTT = allow == BOTH || allow == TT;
			this.allowInParam = allow == BOTH || allow == PARAM;
		}

	}

	/**
	 * Class is ABL procedure parameters class
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@Documented
	public @interface OpaProc {
		/**
		 * ABL procedure name with path
		 */
		String proc() default "";
	}

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
		boolean allowOmitOpaField() default false;
	}

	/**
	 * Field is an entity field
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@Documented
	public @interface OpaField {
		DataType dataType() default DataType.AUTO;
		/** set OE tt field name if differs from Java entity field name */
		String name() default "";
	}

	/**
	 * Mark entity field as free (non Opa) field (in case of allowOmitOpaField)
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@Documented
	public @interface OpaTransient {
	}

}
