package fr.insee.genesis.controller.sources.xml;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LunaticXmlCampaign {

    private String idCampaign;
    private String label;
    private List<LunaticXmlSurveyUnit> surveyUnits;




}
