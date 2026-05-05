package fr.insee.genesis.domain.parser.rawdata;

import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class RawResponsePayloadParser {

    public Boolean getIsCapturedIndirectly(RawResponseModel rawResponseModel) {
        try {
            return rawResponseModel.payload().get("isCapturedIndirectly") == null
                    ? null
                    : Boolean.parseBoolean(rawResponseModel.payload().get("isCapturedIndirectly").toString());
        } catch (Exception e) {
            log.warn("Exception when parsing isCapturedIndirectly : {}", e.toString());
            return Boolean.FALSE;
        }
    }

    public LocalDateTime getValidationDate(RawResponseModel rawResponseModel) {
        try {
            return rawResponseModel.payload().get("validationDate") == null
                    ? null
                    : LocalDateTime.parse(
                    rawResponseModel.payload().get("validationDate").toString(),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
            );
        } catch (Exception e) {
            log.warn("Exception when parsing validation date : {}", e.toString());
            return null;
        }
    }

    public String getStringField(RawResponseModel rawResponseModel, String field) {
        try {
            return rawResponseModel.payload().get(field).toString();
        } catch (Exception e) {
            log.warn("Exception when parsing {} : {}", field, e.toString());
            return null;
        }
    }
}
