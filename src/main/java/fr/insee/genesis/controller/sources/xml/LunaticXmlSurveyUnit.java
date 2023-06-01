package fr.insee.genesis.controller.sources.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LunaticXmlSurveyUnit {

    private String id;
    private String questionnaireModelId;
    private LunaticXmlData data;
}
