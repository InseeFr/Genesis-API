package fr.insee.genesis.domain.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
class JsonUtilsTest {

    @Test
    void jsonToMap_shouldConvertValidJsonToMap() throws Exception {
        String json = "{\"key1\": \"value1\", \"key2\": 42}";
        Map<String, Object> result = JsonUtils.jsonToMap(json);

        Assertions.assertThat(result)
                .isNotNull()
                .containsEntry("key1", "value1")
                .containsEntry("key2", 42);
    }

    @Test
    void jsonToMap_shouldThrowExceptionForInvalidJson() {
        String invalidJson = "{key1: value1, key2: }";

        assertThatThrownBy(() -> JsonUtils.jsonToMap(invalidJson))
                .isInstanceOf(JsonProcessingException.class);
    }

    @Test
    void asMap_shouldConvertObjectToMap() {
        Object obj = Map.of("key", "value");
        Map<String, Object> result = JsonUtils.asMap(obj);

        Assertions.assertThat(result)
                .isNotNull()
                .containsEntry("key", "value");
    }

    @Test
    void asMap_shouldThrowExceptionIfNotMap() {
        Object obj = "Not a map";

        assertThatThrownBy(() -> JsonUtils.asMap(obj))
                .isInstanceOf(ClassCastException.class);
    }

    @Test
    void asMap_ifNull(){
        Object obj = null;
        assertNull(JsonUtils.asMap(obj));
        log.info("Arret");
    }

    @Test
    void asStringList_shouldConvertValidList() {
        Object obj = Arrays.asList(1, "text", null);
        log.info("test");
        List<String> result = JsonUtils.asStringList(obj);

        Assertions.assertThat(result).containsExactly("1", "text", null);
    }

    @Test
    void asStringList_shouldThrowExceptionIfNotList() {
        Object obj = "Not a list";

        assertThatThrownBy(() -> JsonUtils.asStringList(obj))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Object is not a List");
    }


}