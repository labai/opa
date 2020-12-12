package com.github.labai.opa.sys;

import com.github.labai.opa.Opa.DataType;
import com.github.labai.opa.Opa.IoDir;
import com.github.labai.opa.Opa.OpaParam;
import com.github.labai.opa.Opa.OpaProc;
import com.progress.open4gl.*;
import com.progress.open4gl.dynamicapi.ParameterSet;
import com.github.labai.opa.sys.Exceptions.OpaStructureException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Augustus
 *
 * various functions to work with OPA procedures parameters
 *
 * For internal usage only (is not part of api)
 *
*/
class ParamUtils {

	/**
	 * Put values from bean to parameters for procedure execution
	 */
	static ParameterSet beanToParam (Object bean) throws Open4GLException, IllegalAccessException, OpaStructureException {

		List<Field> fields = getOpaFields(bean.getClass());

		int iPos = 1;
		ParameterSet paramSet = new ParameterSet(fields.size());

		for (Field field : fields) {
			fieldToParam(bean, field, paramSet, iPos);
			iPos++;
		}

		return paramSet;
	}

	private static void fieldToParam(Object bean, Field field, ParameterSet paramSet, int iPos) throws Open4GLException, IllegalAccessException {
		OpaParam pp = field.getAnnotation(OpaParam.class);
		Class<?> type = field.getType();
		int ioId = pp.io().progressId;

		if (String.class.isAssignableFrom(type)) {
			String str = null;
			if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT)
				str = (String)field.get(bean);
			switch(pp.dataType()){
				case LONGCHAR:
					paramSet.setLongcharParameter(iPos, str, ioId);
					break;
				default: // CHARACTER
					paramSet.setStringParameter(iPos, str, ioId);
			}
			return;
		}
		if (BigDecimal.class.isAssignableFrom(type)) {
			if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT)
				paramSet.setDecimalParameter(iPos, (BigDecimal)field.get(bean), ioId);
			else
				paramSet.setDecimalParameter(iPos, null, ioId);
			return;
		}
		if (Integer.class.isAssignableFrom(type) || type == int.class) {
			if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT)
				paramSet.setIntegerParameter(iPos, (Integer) field.get(bean), ioId);
			else
				paramSet.setIntegerParameter(iPos, null, ioId);
			return;
		}
		if (Long.class.isAssignableFrom(type) || type == long.class) {
			if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT)
				paramSet.setInt64Parameter(iPos, (Long)field.get(bean), ioId);
			else
				paramSet.setInt64Parameter(iPos, null, ioId);
			return;
		}
		if (DateConv.isTypeOfDate(type)) {
			GregorianCalendar cal = null;
			if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT) {
				cal = DateConv.convToProDate(field, bean);
			}
			switch(DateConv.guessAblType(field, pp.dataType())){
				case DATETIMETZ:
					paramSet.setDateTimeTzParameter(iPos, cal, ioId);
					break;
				case DATE:
					paramSet.setDateParameter(iPos, cal, ioId);
					break;
				default: // DATETIME
					paramSet.setDateTimeParameter(iPos, cal, ioId);
					break;
			}
			return;
		}
		if (Boolean.class.isAssignableFrom(type) || type == boolean.class) {
			if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT)
				paramSet.setBooleanParameter(iPos, (Boolean) field.get(bean), ioId);
			else
				paramSet.setBooleanParameter(iPos, null, ioId);
			return;
		}
		if (type.isEnum()) {
			if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT)
				paramSet.setStringParameter(iPos, field.get(bean) == null ? null : field.get(bean).toString(), ioId);
			else
				paramSet.setStringParameter(iPos, null, ioId);
			return;
		}
		if (Rowid.class.isAssignableFrom(type)) {
			if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT)
				paramSet.setRowidParameter(iPos, (Rowid) field.get(bean), ioId);
			else
				paramSet.setRowidParameter(iPos, null, ioId);
			return;
		}
		if (type.getSimpleName().equals("byte[]")) {
			if (pp.dataType() == DataType.MEMPTR) {
				if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT) {
					byte[] data = (byte[]) field.get(bean);
					paramSet.setMemptrParameter(iPos, (data == null ? null : new Memptr(data)), ioId);
				} else {
					paramSet.setMemptrParameter(iPos, null, ioId);
				}
			} else {
				throw new OpaStructureException("dataType for byte[] in params must be exclusively set to MEMPTR for field " + field.getName());
			}
			return;
		}
		if (List.class.isAssignableFrom(type)) {
			List<?> list = (List<?>) field.get(bean);
			paramSet.setResultSetParameter(iPos, TableUtils.listToResultSet(list, pp.table()), ioId);
			return;
		}

		throw new OpaStructureException("Unknown data type: " + type.getCanonicalName());
	}

	/**
	 * Read parameters after execution and push output values to bean.
	 * If there are temp-tables, their ResultSetHolder will be returned as map for later processing
	 * (in this stage we will not process temp-tables yet)
	 */
	static Map<Field, ResultSetHolder> paramToBean(ParameterSet paramSet, Object bean) throws IllegalAccessException, Open4GLException, OpaStructureException, InvocationTargetException {

		Map<Field, ResultSetHolder> rsMap = new LinkedHashMap<>();

		List<Field> fields = getOpaFields(bean.getClass());

		int iPos = 0; // start from 0+1 (will add 1 before use)

		for (Field field : fields) {
			OpaParam pp = field.getAnnotation(OpaParam.class);
			if (pp == null) continue; // ignore free fields
			iPos++;
			if (pp.io() == IoDir.IN) continue; // ignore input params

			Object val = paramSet.getOutputParameter(iPos);

			Class<?> type = field.getType();

			if (List.class.isAssignableFrom(type)) {
				ResultSetHolder rsh = new ResultSetHolder();
				rsh.setValue(val);
				rsMap.put(field, rsh);
				continue;
			}

			paramValueToField(bean, field, val);

		}

		return rsMap;
	}

	//
	private static void paramValueToField(Object bean, Field field, Object val) throws IllegalAccessException, InvocationTargetException {
		Class<?> type = field.getType();
		if (val == null) {
			if (List.class.isAssignableFrom(type) == false)
				return; // will handle later
			if (type == int.class || type == long.class)
				field.set(bean, 0);
			else if (type == boolean.class)
				field.set(bean, false);
			else
				field.set(bean, null);
			return;
		}

		if (String.class.isAssignableFrom(type)) {
			// field.set(bean, val.toString());
			StringHolder h = new StringHolder();
			h.setValue(val);
			field.set(bean, h.getStringValue());
			return;
		}
		if (BigDecimal.class.isAssignableFrom(type)) {
			BigDecimalHolder h = new BigDecimalHolder();
			h.setValue(val);
			field.set(bean, h.getBigDecimalValue());
			return;
		}
		if (Integer.class.isAssignableFrom(type) || type == int.class) {
			IntHolder h = new IntHolder();
			h.setValue(val);
			field.set(bean, h.getIntValue());
			return;
		}
		if (Long.class.isAssignableFrom(type) || type == long.class) {
			LongHolder h = new LongHolder();
			h.setValue(val);
			field.set(bean, h.getLongValue());
			return;
		}
		if (DateConv.isTypeOfDate(type)) {
			DateConv.readProDateInParam(field, bean, val);
			return;
		}
		if (Boolean.class.isAssignableFrom(type) || type == boolean.class) {
			BooleanHolder h = new BooleanHolder();
			h.setValue(val);
			field.set(bean, h.getBooleanValue());
			return;
		}
		if (type.isEnum()) {
			String sval = val == null ? null : val.toString();
			if (sval == null || "".equals(sval))
				field.set(bean, null);
			else
				field.set(bean, Enum.valueOf((Class<Enum>)type, sval));
			return;
		}
		if (Rowid.class.isAssignableFrom(type)) {
			RowidHolder h = new RowidHolder();
			h.setValue(val);
			field.set(bean, h.getRowidValue());
			return;
		}
		if (type.getSimpleName().equals("byte[]")) {
			MemptrHolder h = new MemptrHolder();
			h.setValue(val);
			field.set(bean, h.getMemptrValue() == null ? null : h.getMemptrValue().getBytes());
			return;
		}
		throw new OpaStructureException("Unknown data type: " + type.getCanonicalName());

	}



	static String getProcName(Class<?> clazz) {
		OpaProc ap = (OpaProc) clazz.getAnnotation(OpaProc.class);
		return ap.proc();
	}

	private static List<Field> getOpaFields(Class<?> clazz) {
		List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
				.filter(field -> field.getAnnotation(OpaParam.class) != null) // ignore free fields
				.collect(Collectors.toList());
		fields.forEach(field -> field.setAccessible(true));
		return fields;
	}


}
