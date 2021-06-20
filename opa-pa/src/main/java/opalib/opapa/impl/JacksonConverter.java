package opalib.opapa.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import opalib.api.DataType;
import opalib.api.IoDir;
import opalib.api.OpaField;
import opalib.api.OpaParam;
import opalib.api.OpaStructureException;
import opalib.api.OpaTable;
import opalib.api.OpaTransient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Augustus
 * created on 2021.06.19
 */
class JacksonConverter implements IJsonConverter {
    private final static Logger logger = LoggerFactory.getLogger(JacksonConverter.class);

    private static final String JSON_DATE_FORMAT = "yyyy-MM-dd";
    private static final String JSON_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final String JSON_DATETIMETZ_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final ThreadLocal<SimpleDateFormat> JSON_DATE_FORMATTER = ThreadLocal.withInitial(() -> new SimpleDateFormat(JSON_DATE_FORMAT));
    private static final ThreadLocal<SimpleDateFormat> JSON_DATETIME_FORMATTER = ThreadLocal.withInitial(() -> new SimpleDateFormat(JSON_DATETIME_FORMAT));
    private static final ThreadLocal<SimpleDateFormat> JSON_DATETIMETZ_FORMATTER = ThreadLocal.withInitial(() -> new SimpleDateFormat(JSON_DATETIMETZ_FORMAT));

    private final ObjectMapper mapper;
    private final ObjectWriter writer;


    @JsonFilter("opa-opaw-opp-filter")
    static class OppMixin {
    }

    @JsonFilter("opa-opaw-tt-filter")
    static class OpaTtMixin {
    }

    JacksonConverter() {
        ObjectMapper mapper = new ObjectMapper();
        FilterProvider filters = new SimpleFilterProvider()
                .addFilter("opa-opaw-opp-filter", new OpaProcPropertyFilter())
                .addFilter("opa-opaw-tt-filter", new OpaTablePropertyFilter());

        mapper.setPropertyNamingStrategy(new OpaFieldNamingStrategy());

        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(Visibility.ANY)
                .withGetterVisibility(Visibility.PUBLIC_ONLY)
                .withSetterVisibility(Visibility.PUBLIC_ONLY)
                .withCreatorVisibility(Visibility.NONE));

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        // todo - configurable?
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL); // we want "" to be as null (?)

        SimpleModule dateMod = new SimpleModule();
        dateMod.addSerializer(Date.class, new OpaDateSerializer());
        dateMod.addDeserializer(Date.class, new OpaDateDeserializer());
        mapper.registerModule(dateMod);

        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new KotlinModule());

        this.mapper = mapper;
        this.writer = mapper.writer(filters);
    }


    @Override
    public String getInputJson(Object opp) {
        if (mapper.findMixInClassFor(opp.getClass()) == null) {
            mapper.addMixIn(opp.getClass(), OppMixin.class);
            for (Field f : opp.getClass().getDeclaredFields()) {
                OpaParam op = f.getAnnotation(OpaParam.class);
                if (op == null || op.table() == Void.class)
                    continue;
                mapper.addMixIn(op.table(), OpaTtMixin.class);
            }
        }

        try {
            return writer.writeValueAsString(opp);
        } catch (JsonProcessingException e) {
            throw new OpaStructureException("Can't read opp structure (" + opp.getClass().getCanonicalName() + ")", e);
        }
    }


    @Override
    public void applyResponseJson(Object opp, String json) {
        ObjectReader objectReader = mapper.readerForUpdating(opp).withValueToUpdate(opp);
        try {
            objectReader.readValue(json);
        } catch (IOException e) {
            throw new OpaStructureException("Can't apply result to opp (" + opp.getClass().getCanonicalName() + ")", e);
        }

    }


    private static class OpaProcPropertyFilter extends SimpleBeanPropertyFilter {
        @Override
        protected boolean include(BeanPropertyWriter writer) {
            return include((PropertyWriter) writer);
        }

        @Override
        protected boolean include(PropertyWriter writer) {
            OpaParam op = writer.getAnnotation(OpaParam.class);
            return !(op == null || op.io() == IoDir.OUT);
        }
    }


    private static class OpaTablePropertyFilter extends SimpleBeanPropertyFilter {
        @Override
        protected boolean include(BeanPropertyWriter writer) {
            return include((PropertyWriter) writer);
        }

        @Override
        protected boolean include(PropertyWriter writer) {
            OpaTransient tr = writer.getAnnotation(OpaTransient.class);
            if (tr != null)
                return false;
            OpaTable tb = writer.getContextAnnotation(OpaTable.class);
            if (tb == null)
                return true;
            if (tb.allowOmitOpaField())
                return true;
            OpaField of = writer.getAnnotation(OpaField.class);
            return of != null;
        }
    }

    //
    //
    private static class OpaFieldNamingStrategy extends PropertyNamingStrategy {
        @Override
        public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
            OpaField p = field.getAnnotation(OpaField.class);
            return (p == null || p.name().isEmpty()) ? defaultName : p.name();
        }
    }


    // dates
    //
    private static class OpaDateSerializer extends StdSerializer<Date> implements ContextualSerializer {

        private final DataType formatType;

        public OpaDateSerializer() {
            this(DataType.DATETIME);
        }

        private OpaDateSerializer(DataType formatType) {
            super(Date.class);
            this.formatType = formatType;
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            String res = null;
            if (value != null) {
                SimpleDateFormat sdf = getDateFormatter(formatType);
                res = sdf.format(value);
            }
            gen.writeString(res);
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
            DataType tp = getDateDataType(property);
            return new OpaDateSerializer(tp);
        }
    }


    private static class OpaDateDeserializer extends StdDeserializer<Date> implements ContextualDeserializer {

        private final DataType formatType;

        public OpaDateDeserializer() {
            this(DataType.DATETIME);
        }

        private OpaDateDeserializer(DataType formatType) {
            super(Date.class);
            this.formatType = formatType;
        }

        @Override
        public Date deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
            String str = jp.readValueAs(String.class);
            Date res = null;
            if (str != null && !str.isEmpty()) {
                try {
                    SimpleDateFormat sdf = getDateFormatter(formatType);
                    res = sdf.parse(str);
                } catch (ParseException e) {
                    // try more formats, is unsuccessful ?
                    throw new OpaStructureException("Can't understand date format '" + str + "'");
                }
            }
            return res;
        }


        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
            DataType tp = getDateDataType(property);
            return new OpaDateDeserializer(tp);
        }
    }


    private static DataType getDateDataType(BeanProperty property) {
        OpaField of = property.getAnnotation(OpaField.class);
        DataType tp = DataType.AUTO;
        if (of != null) {
            tp = of.dataType();
        } else {
            OpaParam op = property.getAnnotation(OpaParam.class);
            if (op != null)
                tp = op.dataType();
        }
        return tp;
    }


    private static SimpleDateFormat getDateFormatter(DataType formatType) {
        switch (formatType) {
            case DATE:       return JSON_DATE_FORMATTER.get();
            case DATETIMETZ: return JSON_DATETIMETZ_FORMATTER.get();
            default:         return JSON_DATETIME_FORMATTER.get();
        }
    }


}
