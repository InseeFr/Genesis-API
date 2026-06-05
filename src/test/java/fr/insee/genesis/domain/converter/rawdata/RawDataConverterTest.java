package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class RawDataConverterTest {

    @Mock
    private SurveyUnitService surveyUnitService;

    @InjectMocks
    private final RawDataConverter rawDataConverterTestImpl = new RawDataConverter(surveyUnitService) {
        @Override
        protected Map<String, Map<DataState, SurveyUnitModel>> getLastSurveyUnitModels(String questionnaireOrCollectionInstrumentId, List<String> interrogationIds) {
            return super.getLastSurveyUnitModels(questionnaireOrCollectionInstrumentId, interrogationIds);
        }
    };


    @Nested
    @DisplayName("getLastSurveyUnitModels tests")
    class getLastSurveyUnitModelsTests{
        @Test
        @DisplayName("Should return map of map with interrogation ids and states as keys")
        void getLastSurveyUnitModelsTest() {
            //GIVEN
            String questionnaireId = "questionnaire";
            Set<String> interrogationIds = Set.of("Interrogation1", "Interrogation2");

            List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();
            for(String interrogationId : interrogationIds){
                surveyUnitModels.add(
                        SurveyUnitModel.builder()
                                .collectionInstrumentId(questionnaireId)
                                .state(DataState.COLLECTED)
                                .interrogationId(interrogationId)
                                .build()
                );
            }

            Mockito.when(surveyUnitService.findLatestByInterrogationIds(questionnaireId, interrogationIds)
            ).thenReturn(surveyUnitModels);

            //WHEN

            Map<String, Map<DataState, SurveyUnitModel>> resultMap = rawDataConverterTestImpl.getLastSurveyUnitModels(
                questionnaireId,
                interrogationIds.stream().toList()
            );


            //THEN
            //Service call with good arguments
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Set<String>> setArgumentCaptor = ArgumentCaptor.forClass(Set.class);
            Mockito.verify(surveyUnitService, Mockito.times(1)).findLatestByInterrogationIds(
                    eq(questionnaireId),
                    setArgumentCaptor.capture()
            );
            Assertions.assertThat(setArgumentCaptor.getValue()).containsExactlyInAnyOrder(
                    "Interrogation1","Interrogation2"
            );

            //Check resulted map key and values content
            Assertions.assertThat(resultMap).containsOnlyKeys(interrogationIds);
            for(String interrogationId : interrogationIds){
                Map<DataState, SurveyUnitModel> surveyUnitModelsOfInterrogationId = resultMap.get(interrogationId);
                Assertions.assertThat(surveyUnitModelsOfInterrogationId).containsOnlyKeys(DataState.COLLECTED);
                Assertions.assertThat(
                        surveyUnitModelsOfInterrogationId.get(DataState.COLLECTED).getInterrogationId()
                ).isEqualTo(interrogationId);
            }
        }
    }
}