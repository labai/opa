package com.github.labai.opa.sys;

import com.progress.open4gl.DateHolder;
import com.github.labai.opa.Opa;
import com.github.labai.opa.Opa.DataType;
import com.github.labai.opa.sys.Exceptions.OpaStructureException;
import com.github.labai.opa.sys.TableUtils.ColDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Augustus
 *         created on 2018.12.22
 *
 * For internal usage only (is not part of api)
 *
 */
class DateConv {
	private final static Logger logger = LoggerFactory.getLogger(Opa.class);


	static boolean isTypeOfDate(Class<?> type) {
		return Temporal.class.isAssignableFrom(type) || Date.class.isAssignableFrom(type);
	}

	static DataType guessAblType(Field field, DataType declaredDataType) throws OpaStructureException {
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

	static <T> void readProDate(ColDef colDef, T pojo, Date sqlDate) throws InvocationTargetException, IllegalAccessException {
		if (sqlDate == null) {
			colDef.setValue(pojo, null);
		} else if (colDef.type == LocalDate.class) {
			LocalDate localDate = Instant.ofEpochMilli(sqlDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
			colDef.setValue(pojo, localDate);
		} else { // Date
			colDef.setValue(pojo, new Date(sqlDate.getTime()));
		}
	}

	static <T> void readProDateTime(ColDef colDef, T pojo, GregorianCalendar cal) throws InvocationTargetException, IllegalAccessException {
		if (cal == null) {
			colDef.setValue(pojo, null);
		} else if (colDef.type == LocalDateTime.class) {
			LocalDateTime ldtm = cal.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			colDef.setValue(pojo, ldtm);
//		} else if (colDef.type == Instant.class) {
//			colDef.setValue(pojo, cal.toInstant());
		} else { // Date
			colDef.setValue(pojo, cal.getTime());
		}
	}

	static <T> void readProDateTimeTz(ColDef colDef, T pojo, GregorianCalendar cal) throws InvocationTargetException, IllegalAccessException {
		if (cal == null) {
			colDef.setValue(pojo, null);
//		} else if (colDef.type == ZonedDateTime.class) { -- n/a in OE
//			colDef.setValue(pojo, cal.toZonedDateTime());
		} else if (colDef.type == OffsetDateTime.class) {
			colDef.setValue(pojo, cal.toZonedDateTime().toOffsetDateTime());
		} else { // Date
			colDef.setValue(pojo, cal.getTime());
		}
	}

	// convert to GregorianCalendar
	static GregorianCalendar convToProDate(Field field, Object bean) throws OpaStructureException, IllegalAccessException {
		Object value = field.get(bean);

		if (value == null)
			return null;

		//Class<?> type = field.getType();
		if (value instanceof LocalDate) {
			LocalDate ldt = (LocalDate) value;
			return GregorianCalendar.from(ldt.atStartOfDay(ZoneId.systemDefault()));
		}

		if (value instanceof LocalDateTime) {
			LocalDateTime ldtm = (LocalDateTime) value;
			return GregorianCalendar.from(ZonedDateTime.of(ldtm, ZoneId.systemDefault()));
		}

//		if (value instanceof Instant) {
//			Instant instant = (Instant) value;
//			return GregorianCalendar.from(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()));
//		}

//		if (value instanceof ZonedDateTime) { -- n/a in OE
//			return GregorianCalendar.from((ZonedDateTime) value);
//		}

		if (value instanceof OffsetDateTime) {
			OffsetDateTime odtm = (OffsetDateTime) value;
			return GregorianCalendar.from(odtm.toZonedDateTime());
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

		ColDef<T> colDef = new ColDef<>(field, (Class<T>)pojo.getClass(), true);

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
