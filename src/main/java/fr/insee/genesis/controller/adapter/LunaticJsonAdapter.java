package fr.insee.genesis.controller.adapter;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import fr.insee.genesis.controller.sources.json.LunaticJsonSurveyUnit;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.Source;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;

@Service
public class LunaticJsonAdapter {

    public SurveyUnitUpdateDto convert(LunaticJsonSurveyUnit su){
        return SurveyUnitUpdateDto.builder()
                .idQuest(su.getIdQuest())
                .idCampaign("")
                .idUE(su.getIdUE())
                .state(DataState.COLLECTED)
                .source(Source.WEB)
                .recordDate(LocalDateTime.now())
                .build();
    }




}
