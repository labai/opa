package opalib.opa.impl;

import com.progress.open4gl.Parameter;
import com.progress.open4gl.dynamicapi.ParameterSet;
import opalib.api.DataType;
import opalib.api.IoDir;

/**
 * @author Augustus
 *
 */
class ProMap {

	static int getProgressId (IoDir ioDir) {
		switch (ioDir) {
			case IN: return ParameterSet.INPUT;
			case OUT: return ParameterSet.OUTPUT;
			case INOUT: return ParameterSet.INPUT_OUTPUT;
			default: throw new IllegalStateException("Invalid ioDir " + ioDir);
		}
	}

	static int getProgressId (DataType dataType) {
		switch (dataType) {
			case CHARACTER: return Parameter.PRO_CHARACTER;
			case INTEGER: return Parameter.PRO_INTEGER;
			case INT64: return Parameter.PRO_INT64;
			case DECIMAL: return Parameter.PRO_DECIMAL;
			case DATE: return Parameter.PRO_DATE;
			case LOGICAL: return Parameter.PRO_LOGICAL;
			case DATETIME: return Parameter.PRO_DATETIME;
			case DATETIMETZ: return Parameter.PRO_DATETIMETZ;
			case ROWID: return Parameter.PRO_ROWID;
			case RECID: return Parameter.PRO_RECID;
			case BLOB: return Parameter.PRO_BLOB;
			case CLOB: return Parameter.PRO_CLOB;
			case LONGCHAR: return -1;
			case MEMPTR: return -1;
			default: return -1;
		}
	}

}
