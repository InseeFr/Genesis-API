package fr.insee.genesis.controller.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.ddi.Variable;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.sources.xml.ValueType;
import fr.insee.genesis.controller.utils.LoopIdentifier;
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

        /*For now we don't manage list of values, we always define a list of one element in values
        * To be updated when we will receive data from dynamic tables*/
        su.getData().getCollected().forEach(lunaticXmlCollectedData -> {
            for (int i =1;i<=lunaticXmlCollectedData.getCollected().size();i++) {
                variablesUpdate.add(VariableStateDto.builder()
                        .idVar(lunaticXmlCollectedData.getVariableName())
                        .values(transformToList(lunaticXmlCollectedData.getCollected().get(i-1).getValue()))
                        .idLoop(LoopIdentifier.getLoopIdentifier(lunaticXmlCollectedData.getVariableName(), variablesMap,i))
                        .idParent(LoopIdentifier.getParent(lunaticXmlCollectedData.getVariableName(), variablesMap))
                        .type(DataType.COLLECTED)
                        .build());
            }
        });
        surveyUnitUpdateDto.setVariablesUpdate(variablesUpdate);
        return surveyUnitUpdateDto;
    }

    private static List<String> transformToList(String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        return values;
    }

}
