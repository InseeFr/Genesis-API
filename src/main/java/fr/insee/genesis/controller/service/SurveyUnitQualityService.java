package fr.insee.genesis.controller.service;

import fr.insee.genesis.controller.utils.DataVerifier;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.metadataparserlib.metadata.model.VariablesMap;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This service is used to ensure data quality processes
 */
@Service
public class SurveyUnitQualityService {
    public void verifySurveyUnits(List<SurveyUnitUpdateDto> suDtos, VariablesMap variablesMap) {
        DataVerifier.verifySurveyUnits(suDtos,variablesMap);
    }
}
