package fr.insee.genesis.domain.service.surveyunit;

import fr.insee.genesis.domain.utils.DataVerifier;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnit;
import fr.insee.bpm.metadata.model.VariablesMap;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This service is used to ensure data quality processes
 */
@Service
public class SurveyUnitQualityService {
    public void verifySurveyUnits(List<SurveyUnit> suDtos, VariablesMap variablesMap) {
        DataVerifier.verifySurveyUnits(suDtos,variablesMap);
    }
}
