package opalib.opa.impl;

import opalib.api.DataType;
import opalib.api.OpaStructureException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Augustus
 * created on 2020.11.22
 *
 * for internal usage
 */
public class _OpaDataConvUtils {

    private final static Map<Class<?>, Map<DataType, ExtConv<?>>> extConverters = new HashMap<>();

    private static class ExtConv<T> {
        final DataType ablType;
        final Class<T> fieldType;
        final Function<String, T> convToPojo;
        final Function<T, ?> convToAbl;

        ExtConv(DataType ablType, Class<T> fieldType, Function<String, T> convToPojo, Function<T, ?> convToAbl) {
            this.ablType = ablType;
            this.fieldType = fieldType;
            this.convToPojo = convToPojo;
            this.convToAbl = convToAbl;
        }
    }

    public static <T> void registerTypeConverter(
            DataType ablType,
            Class<T> fieldType,
            Function<String, T> convToPojoField,
            Function<T, ?> convToAblField
    ) {
        synchronized (extConverters) {
            Map<DataType, ExtConv<?>> map2 = extConverters.get(fieldType);
            if (map2 == null)
                map2 = new HashMap<>();
            if (map2.get(ablType) != null)
                return; // already exists
            ExtConv<T> extConv = new ExtConv<>(ablType, fieldType, convToPojoField, convToAblField);
            map2.put(ablType, extConv);
            extConverters.put(fieldType, map2);
        }
    }

    static <T> Function<String, T> getConverterToPojoField(Class<T> fieldType, DataType dataType) {
        ExtConv<T> extConv = findConverter(fieldType, dataType);
        return extConv == null ? null : extConv.convToPojo;
    }

    static <T> Function<?, ?> getConverterToAblColumn(Class<T> fieldType, DataType dataType) {
        ExtConv<T> extConv = findConverter(fieldType, dataType);
        return extConv == null ? null : extConv.convToAbl;
    }

    static <T> DataType getAblType(Class<T> fieldType, DataType declaredDataType) {
        ExtConv<T> extConv;
        try {
            extConv = findConverter(fieldType, DataType.AUTO);
        } catch (OpaStructureException e) {
            return declaredDataType; // found more than 1 - use declared
        }
        return extConv == null ? null : extConv.ablType;
    }

    private static <T> ExtConv<T> findConverter(Class<T> fieldType, DataType dataType) {
        Map<DataType, ExtConv<?>> map = extConverters.get(fieldType);
        if (map == null)
            return null;
        if (dataType == null || dataType == DataType.AUTO) {
            if (map.size() != 1)
                throw new OpaStructureException("There are registered few converters for fieldType " + fieldType.getCanonicalName() + ", please provide dataType parameter in annotation");
            return (ExtConv<T>) map.values().stream().findFirst().get();
        }
        return (ExtConv<T>) map.get(dataType);
    }



}
