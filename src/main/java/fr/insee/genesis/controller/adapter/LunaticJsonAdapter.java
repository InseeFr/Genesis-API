package fr.insee.genesis.controller.adapter;

import fr.insee.genesis.controller.sources.json.LunaticJsonSurveyUnit;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LunaticJsonAdapter {

    public SurveyUnitUpdateDto convert(LunaticJsonSurveyUnit su){
        return SurveyUnitUpdateDto.builder()
                .idQuest(su.getIdQuest())
                .idCampaign("")
                .idUE(su.getIdUE())
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .build();
    }




}
