package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public abstract class RawDataConverter {
    private SurveyUnitService surveyUnitService;

    @Autowired
    public RawDataConverter(SurveyUnitService surveyUnitService) {
        this.surveyUnitService = surveyUnitService;
    }

    /**
     * @param questionnaireOrCollectionInstrumentId Questionnaire/Collection instrument id
     * @param interrogationIds list of interrogation ids
     * @return a Map containing latest survey unit models for each interrogation ids
     */
    protected Map<String, Map<DataState, SurveyUnitModel>> getLastSurveyUnitModels(
            String questionnaireOrCollectionInstrumentId,
            List<String> interrogationIds
    ) {
        Set<String> interrogationIdsSet = new HashSet<>(interrogationIds);

        List<SurveyUnitModel> surveyUnitModels = surveyUnitService.findLatestByInterrogationIds(
                questionnaireOrCollectionInstrumentId,
                interrogationIdsSet
        );

        Map<String, Map<DataState, SurveyUnitModel>> surveyUnitModelsByInterrogationIdAndState = new HashMap<>();

        for (String interrogationId : interrogationIdsSet){
            List<SurveyUnitModel> surveyUnitModelsForInterrogationId = surveyUnitModels.stream().filter(
                    surveyUnitModel -> surveyUnitModel.getInterrogationId().equals(interrogationId)
            ).toList();
            if(surveyUnitModelsForInterrogationId.isEmpty()){
                continue;
            }
            addSurveyUnitsOfInterrogationByState(
                    interrogationId,
                    surveyUnitModelsForInterrogationId,
                    surveyUnitModelsByInterrogationIdAndState
            );
        }

        return surveyUnitModelsByInterrogationIdAndState;
    }

    private static void addSurveyUnitsOfInterrogationByState(
            String interrogationId,
            List<SurveyUnitModel> surveyUnitModelsForInterrogationId,
            Map<String, Map<DataState, SurveyUnitModel>> surveyUnitModelsByInterrogationIdAndState
    ) {
        surveyUnitModelsByInterrogationIdAndState.put(interrogationId, new HashMap<>());
        for(SurveyUnitModel surveyUnitOfInterrogation : surveyUnitModelsForInterrogationId){
            if(surveyUnitOfInterrogation.getState() == null){
                continue;
            }
            surveyUnitModelsByInterrogationIdAndState.get(interrogationId).put(
                    surveyUnitOfInterrogation.getState(),
                    surveyUnitOfInterrogation
            );
        }
    }
}
