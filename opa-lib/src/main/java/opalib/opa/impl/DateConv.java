package opalib.opa.impl;

import com.progress.open4gl.DateHolder;
import opalib.api.DataType;
import opalib.api.OpaStructureException;
import opalib.opa.impl.TableUtils.ColDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Augustus
 *         created on 2018.12.22
 *
 * For internal usage only (is not part of api)
 *
 */
class DateConv {
	private final static Logger logger = LoggerFactory.getLogger(ProMap.class);

	private final static List<IDateConvExt> extConverters = new ArrayList<>();

	interface IDateConvExt {
		boolean isTypeOfDate(Class<?> type);
		DataType guessAblType(Field field, DataType declaredDataType);
		Object convProDate(Class<?> type, Date sqlDate);
		Object convProDateTime(Class<?> type, GregorianCalendar cal);
		Object convProDateTimeTz(Class<?> type, GregorianCalendar cal);
		GregorianCalendar convToGregorian(Object value);
	}

	static void registerExtConverter(IDateConvExt conv) {
		synchronized (extConverters) {
			for (IDateConvExt c : extConverters) {
				if (c.getClass().equals(conv.getClass()))
					return;
			}
			extConverters.add(conv);
		}
	}


	static boolean isTypeOfDate(Class<?> type) {
		if (Date.class.isAssignableFrom(type))
			return true;
		for (IDateConvExt conv : extConverters) {
			if (conv.isTypeOfDate(type))
				return true;
		}
		return false;
	}

	static DataType guessAblType(Field field, DataType declaredDataType) throws OpaStructureException {
		Class<?> type = field.getType();

		for (IDateConvExt conv : extConverters) {
			DataType res = conv.guessAblType(field, declaredDataType);
			if (res != null)
				return res;
		}

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

	static <T> void readProDate(ColDef colDef, T pojo, Date sqlDate) throws InvocationTargetException, IllegalAccessException {
		if (sqlDate == null) {
			colDef.setValue(pojo, null);
			return;
		}

		for (IDateConvExt conv : extConverters) {
			Object val = conv.convProDate(colDef.type, sqlDate);
			if (val != null) {
				colDef.setValue(pojo, val);
				return;
			}
		}

		// Date
		colDef.setValue(pojo, new Date(sqlDate.getTime()));

	}

	static <T> void readProDateTime(ColDef colDef, T pojo, GregorianCalendar cal) throws InvocationTargetException, IllegalAccessException {
		if (cal == null) {
			colDef.setValue(pojo, null);
			return;
		}

		for (IDateConvExt conv : extConverters) {
			Object val = conv.convProDateTime(colDef.type, cal);
			if (val != null) {
				colDef.setValue(pojo, val);
				return;
			}
		}

/*
		else if (colDef.type == LocalDateTime.class) {
			LocalDateTime ldtm = cal.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			colDef.setValue(pojo, ldtm);
		}
*/
		// Date
		colDef.setValue(pojo, cal.getTime());

	}

	static <T> void readProDateTimeTz(ColDef colDef, T pojo, GregorianCalendar cal) throws InvocationTargetException, IllegalAccessException {
		if (cal == null) {
			colDef.setValue(pojo, null);
			return;
		}
		for (IDateConvExt conv : extConverters) {
			Object val = conv.convProDateTimeTz(colDef.type, cal);
			if (val != null) {
				colDef.setValue(pojo, val);
				return;
			}
		}

		// Date
		colDef.setValue(pojo, cal.getTime());
	}

	// convert to GregorianCalendar
	static GregorianCalendar convToGregorian(Field field, Object bean) throws OpaStructureException, IllegalAccessException {
		Object value = field.get(bean);

		if (value == null)
			return null;

		for (IDateConvExt conv : extConverters) {
			GregorianCalendar val = conv.convToGregorian(value);
			if (val != null)
				return val;
		}

		if (value instanceof Date) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime((Date) value);
			return cal;
		}

		throw new OpaStructureException("Invalid field type (field=" + field.getName() +" type=" +field.getType().getSimpleName() + ")");
	}

	static <T> void readProDateInParam(Field field, T pojo, Object value) throws IllegalAccessException, OpaStructureException, InvocationTargetException {
		Class<?> type = field.getType();

		DateHolder h = new DateHolder();
		h.setValue(value);
		GregorianCalendar cal = h.getDateValue(); // do we really need to add to holders and then read from it?

		if (Date.class.isAssignableFrom(type)) {
			field.set(pojo, cal.getTime());
			return;
		}

		ColDef<T> colDef = new ColDef<>(field, (Class<T>) pojo.getClass(), true);

		switch (guessAblType(field, colDef.ablType)) {
			case DATE:
				readProDate(colDef, pojo, cal.getTime());
				return;
			case DATETIME:
				readProDateTime(colDef, pojo, cal);
				return;
			case DATETIMETZ:
				readProDateTimeTz(colDef, pojo, cal);
				return;
		}

		throw new OpaStructureException("Invalid field type (field=" + field.getName() + " type=" + field.getType().getSimpleName() + ")");

	}


}
