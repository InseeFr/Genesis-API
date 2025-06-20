package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public class SurveyUnitPersistencePortStub implements SurveyUnitPersistencePort {
    List<SurveyUnitModel> mongoStub = new ArrayList<>();

    @Override
    public void saveAll(List<SurveyUnitModel> suList) {
        mongoStub.addAll(suList);
    }

    @Override
    public List<SurveyUnitModel> findByIds(String interrogationId, String questionnaireId) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getInterrogationId().equals(interrogationId) && SurveyUnitModel.getQuestionnaireId().equals(questionnaireId))
                surveyUnitModelList.add(SurveyUnitModel);
        }

        return surveyUnitModelList;
    }

    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    public List<SurveyUnitModel> findBySetOfIdsAndQuestionnaireIdAndMode(String questionnaireId, String mode, List<String> interrogationIdSet) {
        //TODO : TO BE IMPLEMENTED
        return new ArrayList<SurveyUnitModel>();
    }
    //========= OPTIMISATIONS PERFS (START) ==========


    @Override
    public List<SurveyUnitModel> findByInterrogationId(String interrogationId) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getInterrogationId().equals(interrogationId))
                surveyUnitModelList.add(SurveyUnitModel);
        }

        return surveyUnitModelList;
    }

    @Override
    public List<SurveyUnitModel> findByInterrogationIdsAndQuestionnaireId(List<SurveyUnitModel> interrogationIds, String questionnaireId) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel surveyUnitModel : interrogationIds) {
            for (SurveyUnitModel document : mongoStub) {
                if (surveyUnitModel.getInterrogationId().equals(document.getInterrogationId()) && document.getQuestionnaireId().equals(questionnaireId))
                    surveyUnitModelList.add(document);
            }
        }

        return surveyUnitModelList;
    }

    @Override
    public Stream<SurveyUnitModel> findByQuestionnaireId(String questionnaireId) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getQuestionnaireId().equals(questionnaireId))
                surveyUnitModelList.add(SurveyUnitModel);
        }

        return surveyUnitModelList.stream();
    }

    @Override
    public List<SurveyUnitModel> findInterrogationIdsByQuestionnaireId(String questionnaireId) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getQuestionnaireId().equals(questionnaireId))
                surveyUnitModelList.add(
                        new SurveyUnitModel(SurveyUnitModel.getInterrogationId(), SurveyUnitModel.getMode())
                );
        }

        return surveyUnitModelList;
    }


    //======== OPTIMISATIONS PERFS (START) ========
    /**
     * @author Adrien Marchal
     */
    public long countInterrogationIdsByQuestionnaireId(String questionnaireId) {
        return mongoStub.size();
    }

    /**
     * @author Adrien Marchal
     */
    public List<SurveyUnitModel> findPageableInterrogationIdsByQuestionnaireId(String questionnaireId, Long skip, Long limit) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        if(skip < mongoStub.size()) {

        }
        return surveyUnitModelList;
    }


    @Override
    public List<SurveyUnitModel> findModesByCampaignIdV2(String campaignId) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getCampaignId().equals(campaignId))
                surveyUnitModelList.add(
                        new SurveyUnitModel(SurveyUnitModel.getInterrogationId(), SurveyUnitModel.getMode())
                );
        }

        return surveyUnitModelList;
    }

    @Override
    public List<SurveyUnitModel> findModesByQuestionnaireIdV2(String questionnaireId) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getQuestionnaireId().equals(questionnaireId))
                surveyUnitModelList.add(
                        new SurveyUnitModel(SurveyUnitModel.getInterrogationId(), SurveyUnitModel.getMode())
                );
        }

        return surveyUnitModelList;
    }
    //======= OPTIMISATIONS PERFS (END) =========


    @Override
    public List<SurveyUnitModel> findInterrogationIdsByCampaignId(String campaignId) {
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getCampaignId().equals(campaignId))
                surveyUnitModelList.add(
                        new SurveyUnitModel(SurveyUnitModel.getInterrogationId(), SurveyUnitModel.getMode())
                );
        }

        return surveyUnitModelList;
    }

    @Override
    public Long deleteByQuestionnaireId(String questionnaireId) {
        return null;
    }

    @Override
    public long count() {
        return mongoStub.size();
    }

    @Override
    public Set<String> findQuestionnaireIdsByCampaignId(String campaignId) {
        Set<String> questionnaireIdSet = new HashSet<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getCampaignId().equals(campaignId))
                questionnaireIdSet.add(SurveyUnitModel.getQuestionnaireId());
        }

        return questionnaireIdSet;
    }

    //========= OPTIMISATIONS PERFS (START) ==========
    @Override
    public Set<String> findQuestionnaireIdsByCampaignIdV2(String campaignId) {
        //This stub is explicitally the same as method "findQuestionnaireIdsByCampaignId()"
        return findQuestionnaireIdsByCampaignId(campaignId);
    }
    //========= OPTIMISATIONS PERFS (END) ==========

    @Override
    public Set<String> findDistinctCampaignIds() {
        Set<String> campaignIds = new HashSet<>();
        for(SurveyUnitModel surveyUnitModel : mongoStub){
            campaignIds.add(surveyUnitModel.getCampaignId());
        }
        return campaignIds;
    }

    @Override
    public long countByCampaignId(String campaignId) {
        return mongoStub.stream().filter(
                surveyUnitModel -> surveyUnitModel.getCampaignId().equals(campaignId)).toList().size();
    }

    @Override
    public Set<String> findDistinctQuestionnaireIds() {
        Set<String> questionnaireIds = new HashSet<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            questionnaireIds.add(SurveyUnitModel.getQuestionnaireId());
        }
        return questionnaireIds;
    }

    @Override
    public Set<String> findCampaignIdsByQuestionnaireId(String questionnaireId) {
        Set<String> campaignIdSet = new HashSet<>();
        for(SurveyUnitModel SurveyUnitModel : mongoStub){
            if(SurveyUnitModel.getQuestionnaireId().equals(questionnaireId))
                campaignIdSet.add(SurveyUnitModel.getCampaignId());
        }

        return campaignIdSet;
    }
}
