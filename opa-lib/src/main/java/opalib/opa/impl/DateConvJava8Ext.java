package opalib.opa.impl;

import opalib.opa.Opa.DataType;
import opalib.opa.impl.DateConv.IDateConvExt;

import java.lang.reflect.Field;
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
 * created on 2020.08.10
 */
public class DateConvJava8Ext implements IDateConvExt {
    private static DateConvJava8Ext INSTANCE = null;

    static {
        init();
    }

    public static void init() {
        synchronized (DateConvJava8Ext.class) {
            if (INSTANCE == null) {
                INSTANCE = new DateConvJava8Ext();
                DateConv.registerExtConverter(INSTANCE);
            }
        }
    }


    @Override
    public boolean isTypeOfDate(Class<?> type) {
        return Temporal.class.isAssignableFrom(type);
    }

    @Override
    public DataType guessAblType(Field field, DataType declaredDataType) {
        Class<?> type = field.getType();

        if (type == LocalDate.class)
            return DataType.DATE;
        if (type == LocalDateTime.class /*|| type == Instant.class*/)
            return DataType.DATETIME;
        if (type == OffsetDateTime.class /*||type == ZonedDateTime.class*/) // Zoned n/a in OE
            return DataType.DATETIMETZ;

        return null;
    }

    @Override
    public Object convProDate(Class<?> type, Date sqlDate) {
        if (type == LocalDate.class) {
            return Instant.ofEpochMilli(sqlDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    @Override
    public Object convProDateTime(Class<?> type, GregorianCalendar cal) {
        if (type == LocalDateTime.class) {
            return cal.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }

    @Override
    public Object convProDateTimeTz(Class<?> type, GregorianCalendar cal) {
        if (type == OffsetDateTime.class) {
            return cal.toZonedDateTime().toOffsetDateTime();
        }
        return null;
    }

    @Override
    public GregorianCalendar convToGregorian(Object value) {

        if (value instanceof LocalDate) {
            LocalDate ldt = (LocalDate) value;
            return GregorianCalendar.from(ldt.atStartOfDay(ZoneId.systemDefault()));
        }

        if (value instanceof LocalDateTime) {
            LocalDateTime ldtm = (LocalDateTime) value;
            return GregorianCalendar.from(ZonedDateTime.of(ldtm, ZoneId.systemDefault()));
        }

        if (value instanceof OffsetDateTime) {
            OffsetDateTime odtm = (OffsetDateTime) value;
            return GregorianCalendar.from(odtm.toZonedDateTime());
        }

        return null;
    }

}
