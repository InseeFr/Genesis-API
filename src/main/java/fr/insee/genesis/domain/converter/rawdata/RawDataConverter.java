package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public abstract class RawDataConverter {

    @Autowired
    private final SurveyUnitService surveyUnitService;


    /**
     * @param questionnaireOrCollectionInstrumentId Questionnaire/Collection instrument id
     * @param interrogationIds list of interrogation ids
     * @return a Map containing latest survey unit models for each interrogation ids
     */
    protected Map<String, SurveyUnitModel> getLastSurveyUnitModels(
            String questionnaireOrCollectionInstrumentId,
            List<String> interrogationIds
    ) {
        Set<String> interrogationIdsSet = new HashSet<>(interrogationIds);

        return surveyUnitService.findLatestByInterrogationIds(
                questionnaireOrCollectionInstrumentId,
                interrogationIdsSet
        ).stream().collect(Collectors.toMap(
                SurveyUnitModel::getInterrogationId,
                surveyUnitModel -> surveyUnitModel
        ));
    }
}
