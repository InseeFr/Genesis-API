package fr.insee.genesis.domain.service;

import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.utils.DataVerifier;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;

import java.util.ArrayList;
import java.util.List;

/**
 * This service is used in the data verification process
 */
public class SurveyUnitVerificationService {
    public void verifySurveyUnits(List<SurveyUnitUpdateDto> suDtos, VariablesMap variablesMap) {
        DataVerifier.verifySurveyUnits(suDtos,variablesMap);
    }
}
