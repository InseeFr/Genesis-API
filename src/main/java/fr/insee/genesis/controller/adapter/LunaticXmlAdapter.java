package fr.insee.genesis.controller.adapter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.utils.LoopIdentifier;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.ExternalVariableDto;
import fr.insee.genesis.domain.dtos.Source;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableStateDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LunaticXmlAdapter {

    public static SurveyUnitUpdateDto convert(LunaticXmlSurveyUnit su, VariablesMap variablesMap, String idCampaign){
        SurveyUnitUpdateDto surveyUnitUpdateDto = SurveyUnitUpdateDto.builder()
                .idQuest(su.getQuestionnaireModelId())
                .idCampaign(idCampaign)
                .idUE(su.getId())
                .state(DataState.COLLECTED)
                .source(Source.WEB)
                .recordDate(LocalDateTime.now())
                .fileDate(su.getFileDate())
                .build();
        List<VariableStateDto> variablesUpdate = new ArrayList<>();

        /*For now we don't manage list of values, we always define a list of one element in values
        * To be updated when we will receive data from dynamic tables*/
        su.getData().getCollected().forEach(lunaticXmlCollectedData -> {
            for (int i =1;i<=lunaticXmlCollectedData.getCollected().size();i++) {
                List<String> variableValues = transformToList(lunaticXmlCollectedData.getCollected().get(i-1).getValue());
                if (!variableValues.isEmpty()) {
                    variablesUpdate.add(VariableStateDto.builder()
                            .idVar(lunaticXmlCollectedData.getVariableName())
                            .values(transformToList(lunaticXmlCollectedData.getCollected().get(i - 1).getValue()))
                            .idLoop(LoopIdentifier.getLoopIdentifier(lunaticXmlCollectedData.getVariableName(), variablesMap, i))
                            .idParent(LoopIdentifier.getParentGroupName(lunaticXmlCollectedData.getVariableName(), variablesMap))
                            .build());
                }
            }
        });
        surveyUnitUpdateDto.setVariablesUpdate(variablesUpdate);

        List<ExternalVariableDto> externalVariables = new ArrayList<>();
        su.getData().getExternal().forEach(lunaticXmlExternalData ->
            externalVariables.add(ExternalVariableDto.builder()
                    .idVar(lunaticXmlExternalData.getVariableName())
                    .values(transformToList(lunaticXmlExternalData.getValues().get(0).getValue()))
                    .build())
        );
        surveyUnitUpdateDto.setExternalVariables(externalVariables);
        return surveyUnitUpdateDto;
    }

    private static List<String> transformToList(String value) {
        if (value != null){
            List<String> values = new ArrayList<>();
            values.add(value);
            return values;
        }
        return List.of();
    }

}
