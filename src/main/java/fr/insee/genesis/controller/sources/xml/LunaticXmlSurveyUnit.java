package fr.insee.genesis.controller.sources.xml;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LunaticXmlSurveyUnit {

    private String id;
    private String questionnaireModelId;
    private LocalDateTime fileDate;
    private LunaticXmlData data;
}
