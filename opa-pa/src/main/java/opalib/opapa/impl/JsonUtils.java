package opalib.opapa.impl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

import java.io.IOException;

/**
 * @author Augustus
 *         created on 2017.07.11
 *
 *  for internal usage
 */
class JsonUtils {

	private static ObjectWriter prettyWriter = null;
	private static final ObjectMapper jsonMapper = newObjectMapper();

	public static String toJson(Object object) {
		if (object == null)
			return "";
		try {
			return jsonMapper.writeValueAsString(object);
		} catch (IOException e) {
			throw new RuntimeException("Error while creating json (class " + object.getClass().getName() + ")", e);
		}
	}

	public static String toPrettyJson(Object object) {
		if (object == null)
			return "";
		try {
			if (prettyWriter == null) // don't care about parallel, any of writer object is ok, use just for cache (?)
				prettyWriter = jsonMapper.writerWithDefaultPrettyPrinter();
			return prettyWriter.writeValueAsString(object);
		} catch (IOException e) {
			throw new RuntimeException("Error while creating json (class " + object.getClass().getName() + ")", e);
		}
	}

	public static <T> T fromJson(String json, Class<T> clazz) {
		if (json == null || json.isEmpty())
			return null;
		try {
			return jsonMapper.readValue(json, clazz);
		} catch (IOException e) {
			throw new RuntimeException("Error while parsing json (class " + clazz.getName() + ")", e);
		}
	}


	private static ObjectMapper newObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
//		SimpleDateFormat sdf = new SimpleDateFormat(MoConstants.JSON_DATETIME_FORMAT);
//		sdf.setTimeZone(MoConstants.SYSTEM_TIME_ZONE);
//		mapper.setDateFormat(sdf);
		mapper.setSerializationInclusion(Include.NON_NULL);

		mapper.registerModule(new Jdk8Module());
		mapper.registerModule(new JavaTimeModule());
		mapper.registerModule(new KotlinModule());

//		mapper.registerModule(JacksonDeciRegister.deciTypeModule());

		return mapper;
	}

}
