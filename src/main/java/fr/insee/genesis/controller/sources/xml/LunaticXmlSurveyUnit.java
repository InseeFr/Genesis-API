package fr.insee.genesis.controller.sources.xml;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LunaticXmlSurveyUnit {

    private String id;
    private String questionnaireModelId;
    /**
     * @deprecated use {@link #rawRecordDate} instead.
     * This field is kept temporarily for backward compatibility and will be removed in a future version.
     */
    @Deprecated(since = "2026-05-11")
    private LocalDateTime fileDate;
    private LocalDateTime rawRecordDate;
    private LunaticXmlData data;
}
