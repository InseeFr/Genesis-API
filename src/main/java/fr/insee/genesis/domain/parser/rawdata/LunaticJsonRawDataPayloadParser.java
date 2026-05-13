package fr.insee.genesis.domain.parser.rawdata;

import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class LunaticJsonRawDataPayloadParser {

    public LocalDateTime getValidationDate(LunaticJsonRawDataModel rawData) {
        try {
            return rawData.data().get("validationDate") == null
                    ? null
                    : LocalDateTime.parse(
                    rawData.data().get("validationDate").toString(),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
            );
        } catch (Exception e) {
            log.warn("Exception when parsing validation date : {}", e.toString());
            return null;
        }
    }

    public Boolean getIsCapturedIndirectly(LunaticJsonRawDataModel rawData) {
        try {
            return rawData.data().get("isCapturedIndirectly") == null
                    ? null
                    : Boolean.parseBoolean(rawData.data().get("isCapturedIndirectly").toString());
        } catch (Exception e) {
            log.warn("Exception when parsing isCapturedIndirectly : {}", e.toString());
            return Boolean.FALSE;
        }
    }
}