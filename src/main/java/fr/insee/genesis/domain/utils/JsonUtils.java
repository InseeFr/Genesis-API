package fr.insee.genesis.domain.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, Object> jsonToMap(String json) throws Exception {
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
