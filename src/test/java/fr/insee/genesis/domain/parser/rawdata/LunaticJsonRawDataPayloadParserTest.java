package fr.insee.genesis.domain.parser.rawdata;

import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LunaticJsonRawDataPayloadParserTest {

    private final LunaticJsonRawDataPayloadParser parser = new LunaticJsonRawDataPayloadParser();

    @Test
    void shouldReturnValidationDateWhenFieldContainsIsoOffsetDateTime() {
        LunaticJsonRawDataModel rawData = rawDataWith(Map.of(
                "validationDate", "2025-01-02T10:15:30Z"
        ));

        LocalDateTime validationDate = parser.getValidationDate(rawData);

        assertThat(validationDate).isEqualTo(LocalDateTime.of(2025, 1, 2, 10, 15, 30));
    }

    @Test
    void shouldReturnNullValidationDateWhenFieldIsMissing() {
        LunaticJsonRawDataModel rawData = rawDataWith(Map.of());

        LocalDateTime validationDate = parser.getValidationDate(rawData);

        assertThat(validationDate).isNull();
    }

    @Test
    void shouldReturnNullValidationDateWhenFieldIsNull() {
        LunaticJsonRawDataModel rawData = rawDataWith(new java.util.HashMap<>() {{
            put("validationDate", null);
        }});

        LocalDateTime validationDate = parser.getValidationDate(rawData);

        assertThat(validationDate).isNull();
    }

    @Test
    void shouldReturnNullValidationDateWhenFieldIsInvalid() {
        LunaticJsonRawDataModel rawData = rawDataWith(Map.of(
                "validationDate", "not-a-date"
        ));

        LocalDateTime validationDate = parser.getValidationDate(rawData);

        assertThat(validationDate).isNull();
    }

    @Test
    void shouldReturnTrueWhenIsCapturedIndirectlyIsTrue() {
        LunaticJsonRawDataModel rawData = rawDataWith(Map.of(
                "isCapturedIndirectly", "true"
        ));

        Boolean isCapturedIndirectly = parser.getIsCapturedIndirectly(rawData);

        assertThat(isCapturedIndirectly).isTrue();
    }

    @Test
    void shouldReturnFalseWhenIsCapturedIndirectlyIsFalse() {
        LunaticJsonRawDataModel rawData = rawDataWith(Map.of(
                "isCapturedIndirectly", "false"
        ));

        Boolean isCapturedIndirectly = parser.getIsCapturedIndirectly(rawData);

        assertThat(isCapturedIndirectly).isFalse();
    }

    @Test
    void shouldReturnNullWhenIsCapturedIndirectlyFieldIsMissing() {
        LunaticJsonRawDataModel rawData = rawDataWith(Map.of());

        Boolean isCapturedIndirectly = parser.getIsCapturedIndirectly(rawData);

        assertThat(isCapturedIndirectly).isNull();
    }

    @Test
    void shouldReturnNullWhenIsCapturedIndirectlyFieldIsNull() {
        LunaticJsonRawDataModel rawData = rawDataWith(new java.util.HashMap<>() {{
            put("isCapturedIndirectly", null);
        }});

        Boolean isCapturedIndirectly = parser.getIsCapturedIndirectly(rawData);

        assertThat(isCapturedIndirectly).isNull();
    }

    @Test
    void shouldReturnFalseWhenIsCapturedIndirectlyFieldIsInvalid() {
        LunaticJsonRawDataModel rawData = rawDataWith(Map.of(
                "isCapturedIndirectly", "not-a-boolean"
        ));

        Boolean isCapturedIndirectly = parser.getIsCapturedIndirectly(rawData);

        assertThat(isCapturedIndirectly).isFalse();
    }

    private static LunaticJsonRawDataModel rawDataWith(Map<String, Object> data) {
        LunaticJsonRawDataModel rawData = mock(LunaticJsonRawDataModel.class);
        when(rawData.data()).thenReturn(data);
        return rawData;
    }
}