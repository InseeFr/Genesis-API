package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class RawDataConverterTest {

    @Mock
    private SurveyUnitService surveyUnitService;

    @InjectMocks
    private final RawDataConverter rawDataConverterTestImpl = new RawDataConverter() {
        @Override
        protected Map<String, SurveyUnitModel> getLastSurveyUnitModels(String questionnaireOrCollectionInstrumentId, List<String> interrogationIds) {
            return super.getLastSurveyUnitModels(questionnaireOrCollectionInstrumentId, interrogationIds);
        }
    };


    @Nested
    @DisplayName("getLastSurveyUnitModels tests")
    class getLastSurveyUnitModelsTests{
        @Test
        @DisplayName("Should return map with interrogation ids as key")
        void getLastSurveyUnitModelsTest() {
            //GIVEN
            String questionnaireId = "questionnaire";
            List<SurveyUnitModel> surveyUnitModels =
                    List.of(
                            SurveyUnitModel.builder()
                                    .collectionInstrumentId(questionnaireId)
                                    .interrogationId("Interrogation1")
                                    .build(),
                            SurveyUnitModel.builder()
                                    .collectionInstrumentId(questionnaireId)
                                    .interrogationId("Interrogation2")
                                    .build()
                    );


            Mockito.when(surveyUnitService.findLatestByInterrogationIds(
                    eq(questionnaireId), anySet())
            ).thenReturn(surveyUnitModels);

            //WHEN
            Map<String, SurveyUnitModel> resultMap = rawDataConverterTestImpl.getLastSurveyUnitModels(
                questionnaireId,
                    List.of("Interrogation1","Interrogation2")
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
            Assertions.assertThat(resultMap).containsOnlyKeys("Interrogation1","Interrogation2");
            Assertions.assertThat(resultMap.get("Interrogation1").getInterrogationId()).isEqualTo("Interrogation1");
            Assertions.assertThat(resultMap.get("Interrogation2").getInterrogationId()).isEqualTo("Interrogation2");
        }
    }
}