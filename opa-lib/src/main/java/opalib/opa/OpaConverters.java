package opalib.opa;

import opalib.opa.impl._OpaDataConvUtils;

import java.util.function.Function;

/**
 * @author Augustus
 * created on 2020.11.28
 *
 * can register own java type
 * works only for temp-tables yet
 *
 * in future maybe changed
 *
 */
public class OpaConverters {

    public static <T> void registerTypeConverter(Opa.DataType dataType, Class<T> pojoFieldClass, Function<String, T> convToPojo, Function <T, ?> convToAbl) {
    	_OpaDataConvUtils.registerTypeConverter(dataType, pojoFieldClass, convToPojo, convToAbl);
    }

}
