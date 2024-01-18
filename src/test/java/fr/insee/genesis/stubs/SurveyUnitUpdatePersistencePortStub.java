package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SurveyUnitUpdatePersistencePortStub implements SurveyUnitUpdatePersistencePort {
    List<SurveyUnitUpdateDto> mongoStub = new ArrayList<>();

    @Override
    public void saveAll(List<SurveyUnitUpdateDto> suList) {
        mongoStub.addAll(suList);
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIds(String idUE, String idQuest) {
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtoList = new ArrayList<>();
        for(SurveyUnitUpdateDto surveyUnitUpdateDto : mongoStub){
            if(surveyUnitUpdateDto.getIdUE().equals(idUE) && surveyUnitUpdateDto.getIdQuest().equals(idQuest))
                surveyUnitUpdateDtoList.add(surveyUnitUpdateDto);
        }

        return surveyUnitUpdateDtoList;
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIdUE(String idUE) {
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtoList = new ArrayList<>();
        for(SurveyUnitUpdateDto surveyUnitUpdateDto : mongoStub){
            if(surveyUnitUpdateDto.getIdUE().equals(idUE))
                surveyUnitUpdateDtoList.add(surveyUnitUpdateDto);
        }

        return surveyUnitUpdateDtoList;
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIdUEsAndIdQuestionnaire(List<SurveyUnitDto> idUEs, String idQuestionnaire) {
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtoList = new ArrayList<>();
        for(SurveyUnitDto surveyUnitDto : idUEs) {
            for (SurveyUnitUpdateDto document : mongoStub) {
                if (surveyUnitDto.getIdUE().equals(document.getIdUE()) && document.getIdQuest().equals(idQuestionnaire))
                    surveyUnitUpdateDtoList.add(document);
            }
        }

        return surveyUnitUpdateDtoList;
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtoList = new ArrayList<>();
        for(SurveyUnitUpdateDto surveyUnitUpdateDto : mongoStub){
            if(surveyUnitUpdateDto.getIdQuest().equals(idQuestionnaire))
                surveyUnitUpdateDtoList.add(surveyUnitUpdateDto);
        }

        return surveyUnitUpdateDtoList;
    }

    @Override
    public List<SurveyUnitDto> findIdUEsByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitDto> surveyUnitDtoList = new ArrayList<>();
        for(SurveyUnitUpdateDto surveyUnitUpdateDto : mongoStub){
            if(surveyUnitUpdateDto.getIdQuest().equals(idQuestionnaire))
                surveyUnitDtoList.add(
                        new SurveyUnitDto(surveyUnitUpdateDto.getIdUE(),surveyUnitUpdateDto.getMode())
                );
        }

        return surveyUnitDtoList;
    }

    @Override
    public Long deleteByIdQuestionnaire(String idQuestionnaire) {
        return null;
    }
}
