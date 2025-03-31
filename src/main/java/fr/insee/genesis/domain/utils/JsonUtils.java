package fr.insee.genesis.domain.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonUtils() {
        //Utility class
    }

    public static Map<String, Object> jsonToMap(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Map.class);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object obj) {
        return (Map<String, Object>) obj;
    }

    public static List<String> asStringList(Object obj) {
        if (obj instanceof List<?> list) {
            return list.stream()
                    .map(e -> e == null ? "" : String.valueOf(e))
                    .toList();
        }
        throw new IllegalArgumentException("Object is not a List");
    }
}
