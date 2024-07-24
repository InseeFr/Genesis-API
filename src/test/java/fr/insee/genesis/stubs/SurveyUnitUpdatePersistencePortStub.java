package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
    public Stream<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtoList = new ArrayList<>();
        for(SurveyUnitUpdateDto surveyUnitUpdateDto : mongoStub){
            if(surveyUnitUpdateDto.getIdQuest().equals(idQuestionnaire))
                surveyUnitUpdateDtoList.add(surveyUnitUpdateDto);
        }

        return surveyUnitUpdateDtoList.stream();
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
    public List<SurveyUnitDto> findIdUEsByIdCampaign(String idCampaign) {
        List<SurveyUnitDto> surveyUnitDtoList = new ArrayList<>();
        for(SurveyUnitUpdateDto surveyUnitUpdateDto : mongoStub){
            if(surveyUnitUpdateDto.getIdCampaign().equals(idCampaign))
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

    @Override
    public long count() {
        return mongoStub.size();
    }

    @Override
    public List<String> findIdQuestionnairesByIdCampaign(String idCampaign) {
        Set<String> idQuestionnaireSet = new HashSet<>();
        for(SurveyUnitUpdateDto surveyUnitUpdateDto : mongoStub){
            if(surveyUnitUpdateDto.getIdCampaign().equals(idCampaign))
                idQuestionnaireSet.add(surveyUnitUpdateDto.getIdQuest());
        }

        return idQuestionnaireSet.stream().toList();
    }

    @Override
    public Set<String> findDistinctIdCampaigns() {
        Set<String> campaignIds = new HashSet<>();
        for(SurveyUnitUpdateDto surveyUnitUpdateDto : mongoStub){
            campaignIds.add(surveyUnitUpdateDto.getIdCampaign());
        }

        return campaignIds;
    }

    @Override
    public long countByIdCampaign(String idCampaign) {
        long count = 0;
        for(SurveyUnitUpdateDto ignored : mongoStub.stream().filter(
                surveyUnitUpdateDto -> surveyUnitUpdateDto.getIdCampaign().equals(idCampaign)).toList()
        ){
            count++;
        }
        return count;
    }

    @Override
    public Set<String> findDistinctIdQuestionnaires() {
        Set<String> questionnaireIds = new HashSet<>();
        for(SurveyUnitUpdateDto surveyUnitUpdateDto : mongoStub){
            questionnaireIds.add(surveyUnitUpdateDto.getIdQuest());
        }
        return questionnaireIds;
    }

    @Override
    public List<String> findIdCampaignsByIdQuestionnaire(String idQuestionnaire) {
        Set<String> idCampaignSet = new HashSet<>();
        for(SurveyUnitUpdateDto surveyUnitUpdateDto : mongoStub){
            if(surveyUnitUpdateDto.getIdQuest().equals(idQuestionnaire))
                idCampaignSet.add(surveyUnitUpdateDto.getIdCampaign());
        }

        return idCampaignSet.stream().toList();
    }
}
