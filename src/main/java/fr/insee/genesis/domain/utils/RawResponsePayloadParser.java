package fr.insee.genesis.domain.utils;

import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public final class RawResponsePayloadParser {

    private RawResponsePayloadParser() {}

    public static Boolean getIsCapturedIndirectly(RawResponseModel rawResponseModel) {
        try {
            Object val = rawResponseModel.payload().get("isCapturedIndirectly");
            return val == null ? null : Boolean.parseBoolean(val.toString());
        } catch (Exception e) {
            log.warn("Exception when parsing isCapturedIndirectly : {}", e.toString());
            return Boolean.FALSE;
        }
    }

    public static LocalDateTime getValidationDate(RawResponseModel rawResponseModel) {
        try {
            Object val = rawResponseModel.payload().get("validationDate");
            return val == null ? null :
                    LocalDateTime.parse(val.toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            log.warn("Exception when parsing validation date : {}", e.toString());
            return null;
        }
    }

    public static String getStringField(RawResponseModel rawResponseModel, String field) {
        try {
            return rawResponseModel.payload().get(field).toString();
        } catch (Exception e) {
            log.warn("Exception when parsing {} : {}", field, e.toString());
            return null;
        }
    }
}
