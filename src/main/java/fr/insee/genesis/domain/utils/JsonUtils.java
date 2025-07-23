package fr.insee.genesis.domain.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UtilityClass
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

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

    public static Object readValue(JsonParser jsonParser) throws IOException {
        switch (jsonParser.currentToken()){
            case VALUE_STRING -> {
                return jsonParser.getText();
            }
            case VALUE_NUMBER_INT -> {
                return jsonParser.getIntValue();
            }
            case VALUE_NUMBER_FLOAT -> {
                return jsonParser.getDoubleValue();
            }
            case VALUE_TRUE, VALUE_FALSE -> {
                return jsonParser.getBooleanValue();
            }
            case VALUE_NULL -> {
                return null;
            }
            case START_ARRAY -> {
                return readArray(jsonParser);
            }
            case null, default -> throw new JsonParseException("Unexpected token %s on line %d".formatted(
                    jsonParser.currentToken(), jsonParser.currentLocation().getLineNr())
            );
        }
    }

    public static List<Object> readArray(JsonParser jsonParser) throws IOException {
        List<Object> list = new ArrayList<>();
        jsonParser.nextToken(); //Read [
        while(!jsonParser.currentToken().equals(JsonToken.END_ARRAY)){
            list.add(readValue(jsonParser));
            jsonParser.nextToken();
        }
        return list;
    }
}
