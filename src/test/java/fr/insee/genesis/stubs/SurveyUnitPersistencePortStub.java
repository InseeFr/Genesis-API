package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnit;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public class SurveyUnitPersistencePortStub implements SurveyUnitPersistencePort {
    List<SurveyUnit> mongoStub = new ArrayList<>();

    @Override
    public void saveAll(List<SurveyUnit> suList) {
        mongoStub.addAll(suList);
    }

    @Override
    public List<SurveyUnit> findByIds(String idUE, String idQuest) {
        List<SurveyUnit> surveyUnitList = new ArrayList<>();
        for(SurveyUnit SurveyUnit : mongoStub){
            if(SurveyUnit.getIdUE().equals(idUE) && SurveyUnit.getIdQuest().equals(idQuest))
                surveyUnitList.add(SurveyUnit);
        }

        return surveyUnitList;
    }

    @Override
    public List<SurveyUnit> findByIdUE(String idUE) {
        List<SurveyUnit> surveyUnitList = new ArrayList<>();
        for(SurveyUnit SurveyUnit : mongoStub){
            if(SurveyUnit.getIdUE().equals(idUE))
                surveyUnitList.add(SurveyUnit);
        }

        return surveyUnitList;
    }

    @Override
    public List<SurveyUnit> findByIdUEsAndIdQuestionnaire(List<SurveyUnit> idUEs, String idQuestionnaire) {
        List<SurveyUnit> surveyUnitList = new ArrayList<>();
        for(SurveyUnit surveyUnit : idUEs) {
            for (SurveyUnit document : mongoStub) {
                if (surveyUnit.getIdUE().equals(document.getIdUE()) && document.getIdQuest().equals(idQuestionnaire))
                    surveyUnitList.add(document);
            }
        }

        return surveyUnitList;
    }

    @Override
    public Stream<SurveyUnit> findByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnit> surveyUnitList = new ArrayList<>();
        for(SurveyUnit SurveyUnit : mongoStub){
            if(SurveyUnit.getIdQuest().equals(idQuestionnaire))
                surveyUnitList.add(SurveyUnit);
        }

        return surveyUnitList.stream();
    }

    @Override
    public List<SurveyUnit> findIdUEsByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnit> surveyUnitList = new ArrayList<>();
        for(SurveyUnit SurveyUnit : mongoStub){
            if(SurveyUnit.getIdQuest().equals(idQuestionnaire))
                surveyUnitList.add(
                        new SurveyUnit(SurveyUnit.getIdUE(), SurveyUnit.getMode())
                );
        }

        return surveyUnitList;
    }

    @Override
    public List<SurveyUnit> findIdUEsByIdCampaign(String idCampaign) {
        List<SurveyUnit> surveyUnitList = new ArrayList<>();
        for(SurveyUnit SurveyUnit : mongoStub){
            if(SurveyUnit.getIdCampaign().equals(idCampaign))
                surveyUnitList.add(
                        new SurveyUnit(SurveyUnit.getIdUE(), SurveyUnit.getMode())
                );
        }

        return surveyUnitList;
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
        for(SurveyUnit SurveyUnit : mongoStub){
            if(SurveyUnit.getIdCampaign().equals(idCampaign))
                idQuestionnaireSet.add(SurveyUnit.getIdQuest());
        }

        return idQuestionnaireSet;
    }

    @Override
    public Set<String> findDistinctIdCampaigns() {
        Set<String> campaignIds = new HashSet<>();
        for(SurveyUnit SurveyUnit : mongoStub){
            campaignIds.add(SurveyUnit.getIdCampaign());
        }

        return campaignIds;
    }

    @Override
    public long countByIdCampaign(String idCampaign) {
        long count = 0;
        for(SurveyUnit ignored : mongoStub.stream().filter(
                SurveyUnitDto -> SurveyUnitDto.getIdCampaign().equals(idCampaign)).toList()
        ){
            count++;
        }
        return count;
    }

    @Override
    public Set<String> findDistinctIdQuestionnaires() {
        Set<String> questionnaireIds = new HashSet<>();
        for(SurveyUnit SurveyUnit : mongoStub){
            questionnaireIds.add(SurveyUnit.getIdQuest());
        }
        return questionnaireIds;
    }

    @Override
    public Set<String> findIdCampaignsByIdQuestionnaire(String idQuestionnaire) {
        Set<String> idCampaignSet = new HashSet<>();
        for(SurveyUnit SurveyUnit : mongoStub){
            if(SurveyUnit.getIdQuest().equals(idQuestionnaire))
                idCampaignSet.add(SurveyUnit.getIdCampaign());
        }

        return idCampaignSet;
    }
}
