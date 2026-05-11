package fr.insee.genesis.domain.parser.rawdata;

import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RawResponsePayloadParserTest {

    private final RawResponsePayloadParser parser = new RawResponsePayloadParser();

    @Test
    void shouldReturnValidationDateWhenFieldContainsIsoOffsetDateTime() {
        RawResponseModel rawResponseModel = rawResponseWith(Map.of(
                "validationDate", "2025-01-02T10:15:30Z"
        ));

        LocalDateTime validationDate = parser.getValidationDate(rawResponseModel);

        assertThat(validationDate).isEqualTo(LocalDateTime.of(2025, 1, 2, 10, 15, 30));
    }

    @Test
    void shouldReturnNullValidationDateWhenFieldIsMissing() {
        RawResponseModel rawResponseModel = rawResponseWith(Map.of());

        LocalDateTime validationDate = parser.getValidationDate(rawResponseModel);

        assertThat(validationDate).isNull();
    }

    @Test
    void shouldReturnNullValidationDateWhenFieldIsNull() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("validationDate", null);

        RawResponseModel rawResponseModel = rawResponseWith(payload);

        LocalDateTime validationDate = parser.getValidationDate(rawResponseModel);

        assertThat(validationDate).isNull();
    }

    @Test
    void shouldReturnNullValidationDateWhenFieldIsInvalid() {
        RawResponseModel rawResponseModel = rawResponseWith(Map.of(
                "validationDate", "not-a-date"
        ));

        LocalDateTime validationDate = parser.getValidationDate(rawResponseModel);

        assertThat(validationDate).isNull();
    }

    @Test
    void shouldReturnTrueWhenIsCapturedIndirectlyIsTrue() {
        RawResponseModel rawResponseModel = rawResponseWith(Map.of(
                "isCapturedIndirectly", "true"
        ));

        Boolean isCapturedIndirectly = parser.getIsCapturedIndirectly(rawResponseModel);

        assertThat(isCapturedIndirectly).isTrue();
    }

    @Test
    void shouldReturnFalseWhenIsCapturedIndirectlyIsFalse() {
        RawResponseModel rawResponseModel = rawResponseWith(Map.of(
                "isCapturedIndirectly", "false"
        ));

        Boolean isCapturedIndirectly = parser.getIsCapturedIndirectly(rawResponseModel);

        assertThat(isCapturedIndirectly).isFalse();
    }

    @Test
    void shouldReturnNullWhenIsCapturedIndirectlyFieldIsMissing() {
        RawResponseModel rawResponseModel = rawResponseWith(Map.of());

        Boolean isCapturedIndirectly = parser.getIsCapturedIndirectly(rawResponseModel);

        assertThat(isCapturedIndirectly).isNull();
    }

    @Test
    void shouldReturnNullWhenIsCapturedIndirectlyFieldIsNull() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("isCapturedIndirectly", null);

        RawResponseModel rawResponseModel = rawResponseWith(payload);

        Boolean isCapturedIndirectly = parser.getIsCapturedIndirectly(rawResponseModel);

        assertThat(isCapturedIndirectly).isNull();
    }

    @Test
    void shouldReturnFalseWhenIsCapturedIndirectlyFieldIsInvalid() {
        RawResponseModel rawResponseModel = rawResponseWith(Map.of(
                "isCapturedIndirectly", "not-a-boolean"
        ));

        Boolean isCapturedIndirectly = parser.getIsCapturedIndirectly(rawResponseModel);

        assertThat(isCapturedIndirectly).isFalse();
    }

    @Test
    void shouldReturnStringFieldWhenFieldExists() {
        RawResponseModel rawResponseModel = rawResponseWith(Map.of(
                "majorModelVersion", 3
        ));

        String value = parser.getStringField(rawResponseModel, "majorModelVersion");

        assertThat(value).isEqualTo("3");
    }

    @Test
    void shouldReturnNullStringFieldWhenFieldIsMissing() {
        RawResponseModel rawResponseModel = rawResponseWith(Map.of());

        String value = parser.getStringField(rawResponseModel, "majorModelVersion");

        assertThat(value).isNull();
    }

    @Test
    void shouldReturnNullStringFieldWhenFieldIsNull() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("majorModelVersion", null);

        RawResponseModel rawResponseModel = rawResponseWith(payload);

        String value = parser.getStringField(rawResponseModel, "majorModelVersion");

        assertThat(value).isNull();
    }

    private static RawResponseModel rawResponseWith(Map<String, Object> payload) {
        RawResponseModel rawResponseModel = mock(RawResponseModel.class);
        when(rawResponseModel.payload()).thenReturn(payload);
        return rawResponseModel;
    }
}