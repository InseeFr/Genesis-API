package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public class SurveyUnitPersistencePortStub implements SurveyUnitPersistencePort {
    List<SurveyUnitDto> mongoStub = new ArrayList<>();

    @Override
    public void saveAll(List<SurveyUnitDto> suList) {
        mongoStub.addAll(suList);
    }

    @Override
    public List<SurveyUnitDto> findByIds(String idUE, String idQuest) {
        List<SurveyUnitDto> SurveyUnitDtoList = new ArrayList<>();
        for(SurveyUnitDto SurveyUnitDto : mongoStub){
            if(SurveyUnitDto.getIdUE().equals(idUE) && SurveyUnitDto.getIdQuest().equals(idQuest))
                SurveyUnitDtoList.add(SurveyUnitDto);
        }

        return SurveyUnitDtoList;
    }

    @Override
    public List<SurveyUnitDto> findByIdUE(String idUE) {
        List<SurveyUnitDto> SurveyUnitDtoList = new ArrayList<>();
        for(SurveyUnitDto SurveyUnitDto : mongoStub){
            if(SurveyUnitDto.getIdUE().equals(idUE))
                SurveyUnitDtoList.add(SurveyUnitDto);
        }

        return SurveyUnitDtoList;
    }

    @Override
    public List<SurveyUnitDto> findByIdUEsAndIdQuestionnaire(List<SurveyUnitDto> idUEs, String idQuestionnaire) {
        List<SurveyUnitDto> SurveyUnitDtoList = new ArrayList<>();
        for(SurveyUnitDto surveyUnitDto : idUEs) {
            for (SurveyUnitDto document : mongoStub) {
                if (surveyUnitDto.getIdUE().equals(document.getIdUE()) && document.getIdQuest().equals(idQuestionnaire))
                    SurveyUnitDtoList.add(document);
            }
        }

        return SurveyUnitDtoList;
    }

    @Override
    public Stream<SurveyUnitDto> findByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitDto> SurveyUnitDtoList = new ArrayList<>();
        for(SurveyUnitDto SurveyUnitDto : mongoStub){
            if(SurveyUnitDto.getIdQuest().equals(idQuestionnaire))
                SurveyUnitDtoList.add(SurveyUnitDto);
        }

        return SurveyUnitDtoList.stream();
    }

    @Override
    public List<SurveyUnitDto> findIdUEsByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitDto> surveyUnitDtoList = new ArrayList<>();
        for(SurveyUnitDto SurveyUnitDto : mongoStub){
            if(SurveyUnitDto.getIdQuest().equals(idQuestionnaire))
                surveyUnitDtoList.add(
                        new SurveyUnitDto(SurveyUnitDto.getIdUE(),SurveyUnitDto.getMode())
                );
        }

        return surveyUnitDtoList;
    }

    @Override
    public List<SurveyUnitDto> findIdUEsByIdCampaign(String idCampaign) {
        List<SurveyUnitDto> surveyUnitDtoList = new ArrayList<>();
        for(SurveyUnitDto SurveyUnitDto : mongoStub){
            if(SurveyUnitDto.getIdCampaign().equals(idCampaign))
                surveyUnitDtoList.add(
                        new SurveyUnitDto(SurveyUnitDto.getIdUE(),SurveyUnitDto.getMode())
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
    public Set<String> findIdQuestionnairesByIdCampaign(String idCampaign) {
        Set<String> idQuestionnaireSet = new HashSet<>();
        for(SurveyUnitDto SurveyUnitDto : mongoStub){
            if(SurveyUnitDto.getIdCampaign().equals(idCampaign))
                idQuestionnaireSet.add(SurveyUnitDto.getIdQuest());
        }

        return idQuestionnaireSet;
    }

    @Override
    public Set<String> findDistinctIdCampaigns() {
        Set<String> campaignIds = new HashSet<>();
        for(SurveyUnitDto SurveyUnitDto : mongoStub){
            campaignIds.add(SurveyUnitDto.getIdCampaign());
        }

        return campaignIds;
    }

    @Override
    public long countByIdCampaign(String idCampaign) {
        long count = 0;
        for(SurveyUnitDto ignored : mongoStub.stream().filter(
                SurveyUnitDto -> SurveyUnitDto.getIdCampaign().equals(idCampaign)).toList()
        ){
            count++;
        }
        return count;
    }

    @Override
    public Set<String> findDistinctIdQuestionnaires() {
        Set<String> questionnaireIds = new HashSet<>();
        for(SurveyUnitDto SurveyUnitDto : mongoStub){
            questionnaireIds.add(SurveyUnitDto.getIdQuest());
        }
        return questionnaireIds;
    }

    @Override
    public Set<String> findIdCampaignsByIdQuestionnaire(String idQuestionnaire) {
        Set<String> idCampaignSet = new HashSet<>();
        for(SurveyUnitDto SurveyUnitDto : mongoStub){
            if(SurveyUnitDto.getIdQuest().equals(idQuestionnaire))
                idCampaignSet.add(SurveyUnitDto.getIdCampaign());
        }

        return idCampaignSet;
    }
}
