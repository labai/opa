package opalib.opapa.impl;

import opalib.api.DataType;
import opalib.api.IoDir;
import opalib.api.OpaField;
import opalib.api.OpaParam;
import opalib.api.OpaStructureException;
import opalib.api.OpaTable;
import opalib.api.OpaTransient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Augustus
 *
 * various functions to work with OPA procedures parameters
 *
 * For internal usage only (is not part of api)
 *
*/
public class OppParamReader implements IOppParamReader {
	private final static Logger logger = LoggerFactory.getLogger(OppParamReader.class);

	private final static Map<DataType, String> shortMap = new HashMap<>();
	static {
		shortMap.put(DataType.AUTO, 	 	"");
		shortMap.put(DataType.CHARACTER,  	"s");
		shortMap.put(DataType.INTEGER, 	 	"i");
		shortMap.put(DataType.INT64, 	 	"ii");
		shortMap.put(DataType.DECIMAL, 	 	"d");
		shortMap.put(DataType.DATE, 	 	"dt");
		shortMap.put(DataType.LOGICAL, 	 	"b");
		shortMap.put(DataType.DATETIME,  	"tm");
		shortMap.put(DataType.DATETIMETZ, 	"tz");
		shortMap.put(DataType.ROWID, 	 	"ro");
		shortMap.put(DataType.RECID, 	 	"re");
		shortMap.put(DataType.BLOB, 	 	"m");
		shortMap.put(DataType.CLOB, 	 	"ss");
		shortMap.put(DataType.LONGCHAR,  	"ss");
		shortMap.put(DataType.MEMPTR, 	 	"m");
	}


	@Override
	public String readOppParams(Class<?> oppClass) {
		List<Field> fields = getOpaFields(oppClass);
		List<String> params = new ArrayList<>();
		for (Field field : fields) {
			String entry = fieldToParamMeta(field);
			params.add(entry);
		}
		return String.join(";", params);
	}

	private String fieldToParamMeta(Field field) throws OpaStructureException {
		OpaParam pp = field.getAnnotation(OpaParam.class);
		String io = (pp.io() == IoDir.IN) ? "i" : "o";
		String tp = fieldTypeCode(field, pp.dataType());
		String ttmeta = "";
		if ("t".equals(tp) && "i".equals(io)) {
			ttmeta = "[" + readTTMeta(field) + "]";
		}
		return io + "," + tp + ttmeta + ("i".equals(io) ? "" : "," + field.getName());
	}


	private String fieldTypeCode(Field field, DataType dataType) throws OpaStructureException {
		Class<?> type = field.getType();

		if (String.class.isAssignableFrom(type)) {
			if (dataType == DataType.LONGCHAR) // params
				return "ss";
			if (dataType == DataType.CLOB) // tt
				return "ss";
			if (dataType == DataType.ROWID)
				return "ro";
			return "s";
		}
		if (BigDecimal.class.isAssignableFrom(type)) {
			return "d";
		}
		if (Integer.class.isAssignableFrom(type) || type == int.class) {
			return "i";
		}
		if (Long.class.isAssignableFrom(type) || type == long.class) {
			if (dataType == DataType.RECID)
				return "re";
			return "ii";
		}
		if (isTypeOfDate(type)) {
			switch(getDateAblType(field, dataType)){
				case DATETIMETZ: return "tz";
				case DATE: return "dt";
				default: return "tm";
			}
		}
		if (Boolean.class.isAssignableFrom(type) || type == boolean.class) {
			return "b";
		}
		if (type.isEnum()) {
			return "s";
		}
		if (type.isAssignableFrom(byte[].class)) {
			if (dataType == DataType.MEMPTR) // param
				return "m";
			if (dataType == DataType.BLOB) // tt
				return "m";
			throw new OpaStructureException("dataType for byte[] in params must be exclusively set to MEMPTR (parameter) or BLOB (table field) for field " + field.getName());
		}
// TODO extensions
//		DataType dataType2 = _OpaDataConvUtils.getAblType(type, dataType);
//		if (dataType2 != null)
//			return shortMap.get(dataType2);

		if (List.class.isAssignableFrom(type)) {
			return "t";
		}
		throw new OpaStructureException("Unknown data type: " + type.getCanonicalName());
	}

	String readTTMeta(Field paramField) throws OpaStructureException {
		OpaParam opaParam = paramField.getAnnotation(OpaParam.class);
		if (opaParam == null || opaParam.table() == Void.class)
			throw new OpaStructureException("OpaParam should contain the 'table' attribute for tables (lists)");
		Class<?> tableClass = opaParam.table();
		OpaTable opaTable = tableClass.getAnnotation(OpaTable.class);
		boolean allowOmitOpaField = opaTable == null || opaTable.allowOmitOpaField();

		List<String> fieldMetas = new ArrayList<>();
		for (Field f : opaParam.table().getDeclaredFields()) {
			if (f.getAnnotation(OpaTransient.class) != null)
				continue;
			OpaField opaField = f.getAnnotation(OpaField.class);
			if (!allowOmitOpaField && opaField == null)
				continue;
			f.setAccessible(true);
			// fill meta
			DataType dataType = (opaField != null) ? opaField.dataType() : DataType.AUTO;
			String name = (opaField != null && !opaField.name().isEmpty()) ? opaField.name() : f.getName();
			String tp = fieldTypeCode(f, dataType);
			String meta = tp + ":" + name;
			fieldMetas.add(meta);
		}

		return String.join("/", fieldMetas);
	}

	// return null if it is not OpaField, and name of field, if it is OpaField
	private String getOpaName (Field field, boolean allowOmitOpaField) {
		OpaField af = field.getAnnotation(OpaField.class);
		if (allowOmitOpaField) {
			if (field.getAnnotation(OpaTransient.class) != null)
				return null;
		} else {
			if (af == null)
				return null;
		}
		return af == null || "".equals(af.name()) ? field.getName() : af.name();
	}


	private static List<Field> getOpaFields(Class<?> clazz) {
		List<Field> result = new ArrayList<>();
		for (Field f : clazz.getDeclaredFields()) {
			if (f.getAnnotation(OpaParam.class) == null) continue;
			f.setAccessible(true);
			result.add(f);
		}
		return result;
	}


	static DataType getDateAblType(Field field, DataType declaredDataType) throws OpaStructureException {
		Class<?> type = field.getType();

		if (type == LocalDate.class)
			return DataType.DATE;
		if (type == LocalDateTime.class /*|| type == Instant.class*/)
			return DataType.DATETIME;
		if (type == OffsetDateTime.class /*||type == ZonedDateTime.class*/) // Zoned n/a in OE
			return DataType.DATETIMETZ;

		if (Date.class.isAssignableFrom(type)) {
			switch(declaredDataType){
				case DATETIMETZ: return DataType.DATETIMETZ;
				case DATETIME: return DataType.DATETIME;
				case DATE: return DataType.DATE;
				default: {
					logger.warn("DataType.DATE* is missing in OpaField annotation for 'Date' type field '"+ field.getName() +"'. Will use default DATETIME");
					return DataType.DATETIME;
				}
			}
		}
		throw new OpaStructureException("Invalid field type (field=" + field.getName() +" type=" +field.getType().getSimpleName() + ")");
	}

	static boolean isTypeOfDate(Class<?> type) {
		if (Date.class.isAssignableFrom(type))
			return true;
		if (Temporal.class.isAssignableFrom(type))
			return true;
		return false;
	}

}
