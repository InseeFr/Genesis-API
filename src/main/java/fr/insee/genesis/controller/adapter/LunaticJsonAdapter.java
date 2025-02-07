package fr.insee.genesis.controller.adapter;

import fr.insee.genesis.controller.sources.json.LunaticJsonSurveyUnit;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LunaticJsonAdapter {

    public SurveyUnitModel convert(LunaticJsonSurveyUnit su){
        return SurveyUnitModel.builder()
                .questionnaireId(su.getQuestionnaireId())
                .campaignId("")
                .interrogationId(su.getInterrogationId())
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .build();
    }




}
