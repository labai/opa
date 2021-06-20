package opalib.opapa.impl;

import opalib.api.OpaParam;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Augustus
 * created on 2021.06.19
 */
class MiscUtils {

    static String trimLastSlash(String url) {
        if (url == null) return null;
        return (url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
    }

    static Map<Field, Class<?>> getOppTTFields(Object opp) {
        Map<Field, Class<?>> result = new LinkedHashMap<>();
        for (Field f : opp.getClass().getDeclaredFields()) {
            OpaParam op = f.getAnnotation(OpaParam.class);
            if (op == null || op.table() == Void.class)
                continue;
            result.put(f, op.table());
        }
        return result;
    }

    static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

}
