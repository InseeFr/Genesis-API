package fr.insee.genesis.domain.utils;

import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class LunaticJsonRawDataParser {

    private LunaticJsonRawDataParser(){}


    static LocalDateTime parseValidationDate(LunaticJsonRawDataModel rawData) {
        try {
            Object val = rawData.data().get("validationDate");
            return val == null ? null :
                    LocalDateTime.parse(val.toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            log.warn("Exception when parsing validation date : {}", e.toString());
            return null;
        }
    }

    static Boolean parseIsCapturedIndirectly(LunaticJsonRawDataModel rawData) {
        try {
            Object val = rawData.data().get("isCapturedIndirectly");
            return val == null ? null : Boolean.parseBoolean(val.toString());
        } catch (Exception e) {
            log.warn("Exception when parsing isCapturedIndirectly : {}", e.toString());
            return Boolean.FALSE;
        }
    }
}
