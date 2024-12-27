package fr.insee.genesis.domain.service.surveyunit;

import fr.insee.genesis.controller.dto.VariableDto;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.utils.DataVerifier;
import fr.insee.bpm.metadata.model.VariablesMap;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * This service is used to ensure data quality processes
 */
@Service
public class SurveyUnitQualityService {
    public void verifySurveyUnits(List<SurveyUnitModel> surveyUnitModels, VariablesMap variablesMap) {
        DataVerifier.verifySurveyUnits(surveyUnitModels,variablesMap);
    }

    /**
     * Checks
     * @param variableDtos input variables to check
     * @param variablesMap BPM Questionnaire metadata to use for checking
     * @return A list of variables that are <strong>absent</strong> in metadata
     */
    public List<String> checkVariablesPresentInMetadata(List<VariableDto> variableDtos, VariablesMap variablesMap) {
        List<String> absentVariableNames = new ArrayList<>();

        for(VariableDto variableDto : variableDtos){
            if(!variablesMap.hasVariable(variableDto.getVariableName())){
                absentVariableNames.add(variableDto.getVariableName());
            }
        }

        return absentVariableNames;
    }
}
