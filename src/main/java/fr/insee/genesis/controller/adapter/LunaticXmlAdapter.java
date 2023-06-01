package fr.insee.genesis.controller.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.ddi.Variable;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.sources.xml.ValueType;
import fr.insee.genesis.domain.dtos.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LunaticXmlAdapter {

    public static SurveyUnitUpdateDto convert(LunaticXmlSurveyUnit su, VariablesMap variablesMap){
        SurveyUnitUpdateDto surveyUnitUpdateDto = SurveyUnitUpdateDto.builder()
                .idQuest(su.getQuestionnaireModelId())
                .idCampaign("")
                .idUE(su.getId())
                .state(DataState.COLLECTED)
                .source(Source.WEB)
                .date(LocalDateTime.now())
                .build();
        List<VariableStateDto> variablesUpdate = new ArrayList<>();

        su.getData().getCollected().forEach(lunaticXmlCollectedData -> {
            for (int i =1;i<=lunaticXmlCollectedData.getCollected().size();i++) {
                variablesUpdate.add(VariableStateDto.builder()
                        .idVar(lunaticXmlCollectedData.getVariableName())
                        .values(getValuesFromValueTypes(lunaticXmlCollectedData.getCollected(),i-1))
                        .idLoop(getLoop(lunaticXmlCollectedData.getVariableName(), variablesMap,i))
                        .idParent(getParent(lunaticXmlCollectedData.getVariableName(), variablesMap))
                        .type(DataType.COLLECTED)
                        .build());
            }
        });
        surveyUnitUpdateDto.setVariablesUpdate(variablesUpdate);
        return surveyUnitUpdateDto;
    }

    private static List<String> getValuesFromValueTypes(List<ValueType> valueTypes, int index) {
        List<String> values = new ArrayList<>();
        values.add(valueTypes.get(index).getValue());
        return values;
    }

    private static String getLoop(String variableName, VariablesMap variablesMap, int index) {
        Variable variable = variablesMap.getVariable(variableName);
        if (variable == null) {
            log.warn("Variable {} not found in variablesMap and assigned in root group", variableName);
            return Constants.ROOT_GROUP_NAME;
        }
        if (variable.getGroup().isRoot()) {
            return variable.getGroup().getName();
        }
        return String.format("%s_%d", variable.getGroup().getName() ,index);
    }

    private static String getParent(String variableName, VariablesMap variablesMap) {
        Variable variable = variablesMap.getVariable(variableName);
        if ( variable == null || variable.getGroup().isRoot()) {
            return null;
        }
        return variable.getGroup().getParentName();
    }

}
