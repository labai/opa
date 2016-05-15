package com.github.labai.opa.sys;

import com.progress.open4gl.*;
import com.progress.open4gl.dynamicapi.ParameterSet;
import com.github.labai.opa.Opa.DataType;
import com.github.labai.opa.Opa.IoDir;
import com.github.labai.opa.Opa.OpaParam;
import com.github.labai.opa.Opa.OpaProc;
import com.github.labai.opa.sys.Exceptions.OpaStructureException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/*
 * For internal usage only (is not part of api)
 *
 * @author Augustas Mickus
 *
*/
class ParamUtils {

	//private static void wrlog(String msg) { System.out.println(msg);}

	/**
	 * Put values from bean to parameters for procedure execution
	 */
	static ParameterSet beanToParam (Object bean) throws Open4GLException, IllegalAccessException, OpaStructureException {

		Class<?> clazz = bean.getClass();

		// count fields
		int iCount = 0;
		for (Field field : clazz.getDeclaredFields()) {
			OpaParam pp = field.getAnnotation(OpaParam.class);
			if (pp == null) continue; // ignore free fields
			iCount++;
		}

		// fill fields
		//
		int iPos = 1;
		ParameterSet paramSet = new ParameterSet(iCount);

		for (Field field : clazz.getDeclaredFields()) {
			OpaParam pp = field.getAnnotation(OpaParam.class);
			if (pp == null) continue; // ignore free fields
			field.setAccessible(true);
			Class type = field.getType();
			//String tp = field.getType().getSimpleName();
			int ioId = pp.io().progressId;
			if (type.isAssignableFrom(String.class)) {
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
			} else if (type.isAssignableFrom(BigDecimal.class)) {
				if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT)
					paramSet.setDecimalParameter(iPos, (BigDecimal)field.get(bean), ioId);
				else
					paramSet.setDecimalParameter(iPos, null, ioId);
			} else if (type.isAssignableFrom(Integer.class) || type == int.class) {
				if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT)
					paramSet.setIntegerParameter(iPos, (Integer) field.get(bean), ioId);
				else
					paramSet.setIntegerParameter(iPos, null, ioId);
			} else if (type.isAssignableFrom(Long.class) || type == long.class) {
				if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT)
					paramSet.setInt64Parameter(iPos, (Long)field.get(bean), ioId);
				else
					paramSet.setInt64Parameter(iPos, null, ioId);
			} else if (type.isAssignableFrom(Date.class)) {
				GregorianCalendar cal = null;
				if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT) {
					Date dt = (Date) field.get(bean);
					if (dt != null) {
						cal = new GregorianCalendar();
						cal.setTime(dt);
					}
				}
				switch(pp.dataType()){
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

			} else if (type.isAssignableFrom(Boolean.class) || type == boolean.class) {
				if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT)
					paramSet.setBooleanParameter(iPos, (Boolean) field.get(bean), ioId);
				else
					paramSet.setBooleanParameter(iPos, null, ioId);
			} else if (type.isEnum()) {
				if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT)
					paramSet.setStringParameter(iPos, field.get(bean) == null ? null : field.get(bean).toString(), ioId);
				else
					paramSet.setStringParameter(iPos, null, ioId);
			} else if (type.isAssignableFrom(Rowid.class)) {
				if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT)
					paramSet.setRowidParameter(iPos, (Rowid) field.get(bean), ioId);
				else
					paramSet.setRowidParameter(iPos, null, ioId);
			} else if (type.getSimpleName().equals("byte[]")) {
				if (pp.dataType() == DataType.MEMPTR) {
					if (pp.io() == IoDir.IN || pp.io() == IoDir.INOUT) {
						byte[] data = (byte[]) field.get(bean);
						paramSet.setMemptrParameter(iPos, new Memptr(data), ioId);
					} else {
						paramSet.setMemptrParameter(iPos, null, ioId);
					}
				} else {
					throw new OpaStructureException("dataType for byte[] in params must be exclusively set to MEMPTR for field " + field.getName());
				}
			} else if (type.isAssignableFrom(List.class)) {
				List<?> list = (List<?>) field.get(bean);
				paramSet.setResultSetParameter(iPos, TableUtils.listToResultSet(list, pp.table()), ioId);
			} else {
				throw new OpaStructureException("Unknown data type: " + type.getCanonicalName());
			}
			iPos++;
		}

		return paramSet;
	}

	/**
	 * Read parameters after execution and push output values to bean.
	 * If there are temp-tables, their ResultSetHolder will be returned as map for later processing
	 * (in this stage we will not process temp-tables yet)
	 */
	static Map<Field, ResultSetHolder> paramTobean (ParameterSet paramSet, Object bean) throws IllegalAccessException, Open4GLException, OpaStructureException {
		Class<?> clazz = bean.getClass();

		Map<Field, ResultSetHolder> rsMap = new LinkedHashMap<Field, ResultSetHolder>();

		// count fields
		int iCount = 0;
		for (Field field : clazz.getDeclaredFields()) {
			OpaParam pp = field.getAnnotation(OpaParam.class);
			if (pp == null) continue; // ignore free fields
			iCount++;
		}

		// fill fields
		//
		int iPos = 0; // start from 0+1 (will add 1 before use)

		for (Field field : clazz.getDeclaredFields()) {
			OpaParam pp = field.getAnnotation(OpaParam.class);

			if (pp == null) continue; // ignore free fields
			iPos++;
			if (pp.io() == IoDir.IN) continue; // ignore input params

			field.setAccessible(true);

			Object val = paramSet.getOutputParameter(iPos);
			//String tp = field.getType().getSimpleName();
			Class type = field.getType();
			if (val == null) {
				if (type.isAssignableFrom(List.class) == false) continue; // will handle later
				if (type == int.class || type == long.class)
					field.set(bean, 0);
				else if (type == boolean.class)
					field.set(bean, false);
				else
					field.set(bean, null);
				continue;
			}

			if (type.isAssignableFrom(String.class)) {
				// field.set(bean, val.toString());
				StringHolder h = new StringHolder();
				h.setValue(val);
				field.set(bean, h.getStringValue());
			} else if (type.isAssignableFrom(BigDecimal.class)) {
				// BigDecimal d = new BigDecimal(val.toString());
				BigDecimalHolder h = new BigDecimalHolder();
				h.setValue(val);
				field.set(bean, h.getBigDecimalValue());
			} else if (type.isAssignableFrom(Integer.class) || type == int.class) {
				//Integer n = Integer.parseInt(val.toString());
				IntHolder h = new IntHolder();
				h.setValue(val);
				field.set(bean, h.getIntValue());
			} else if (type.isAssignableFrom(Long.class) || type == long.class) {
				//Long n = Long.parseLong(val.toString());
				LongHolder h = new LongHolder();
				h.setValue(val);
				field.set(bean, h.getLongValue());
			} else if (type.isAssignableFrom(Date.class)) {
				//GregorianCalendar cal = (GregorianCalendar) val;
				DateHolder h = new DateHolder();
				h.setValue(val);
				GregorianCalendar cal = h.getDateValue(); // do we really need to add to holders and then read from it?
				field.set(bean, cal.getTime());
			} else if (type.isAssignableFrom(Boolean.class) || type == boolean.class) {
				//Boolean b = Boolean.parseBoolean(val.toString());
				BooleanHolder h = new BooleanHolder();
				h.setValue(val);
				field.set(bean, h.getBooleanValue());
			} else if (type.isEnum()) {
				String sval = val == null ? null : val.toString();
				if (sval == null || "".equals(sval))
					field.set(bean, null);
				else
					field.set(bean, Enum.valueOf((Class<Enum>)type, sval));
			} else if (type.isAssignableFrom(Rowid.class)) {
				RowidHolder h = new RowidHolder();
				h.setValue(val);
				field.set(bean, h.getRowidValue());
			} else if (type.getSimpleName().equals("byte[]")) {
				MemptrHolder h = new MemptrHolder();
				h.setValue(val);
				field.set(bean, h.getMemptrValue() == null ? null : h.getMemptrValue().getBytes());
			} else if (type.isAssignableFrom(List.class)) {
				ResultSetHolder rsh = new ResultSetHolder();
				rsh.setValue(val);
				rsMap.put(field, rsh);
			} else {
				throw new OpaStructureException("Unknown data type: " + type.getCanonicalName());
			}

		}

		return rsMap;
	}

	public static String getProcName(Class<?> clazz) {
		OpaProc ap = (OpaProc) clazz.getAnnotation(OpaProc.class);
		return ap.proc();
	}


}
