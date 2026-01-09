package fr.insee.genesis.domain.service.context;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.exceptions.GenesisException;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class DataProcessingContextServiceUnitTest {

    static DataProcessingContextService dataProcessingContextService;
    static DataProcessingContextPersistancePort dataProcessingContextPersistancePort;
    static SurveyUnitPersistencePort surveyUnitPersistencePort;

    @BeforeEach
    void setUp() {
        dataProcessingContextPersistancePort = mock(DataProcessingContextPersistancePort.class);
        surveyUnitPersistencePort = mock(SurveyUnitPersistencePort.class);
        dataProcessingContextService = new DataProcessingContextService(
                dataProcessingContextPersistancePort,
                surveyUnitPersistencePort
        );
    }

    @Test
    @SneakyThrows
    void getContext_test() {
        //GIVEN
        String collectionInstrumentId = TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID;
        String interrogationId = TestConstants.DEFAULT_INTERROGATION_ID;
        SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                        .interrogationId(interrogationId)
                        .collectionInstrumentId(collectionInstrumentId)
                        .build();
        doReturn(Collections.singletonList(surveyUnitModel))
                .when(surveyUnitPersistencePort)
                .findByInterrogationId(any());
        DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                .collectionInstrumentId(collectionInstrumentId)
                .withReview(true)
                .build();
        doReturn(dataProcessingContextModel).when(dataProcessingContextPersistancePort).findByCollectionInstrumentId(any());

        //WHEN
        DataProcessingContextModel returnedContext =
                dataProcessingContextService.getContext(interrogationId);

        //THEN
        Assertions.assertThat(returnedContext.getCollectionInstrumentId()).isEqualTo(collectionInstrumentId);
        Assertions.assertThat(returnedContext.isWithReview()).isTrue();
    }

    @Test
    @SneakyThrows
    void getContext_no_surveyUnit_exception_test() {
        //GIVEN
        doReturn(new ArrayList<>())
                .when(surveyUnitPersistencePort)
                .findByInterrogationId(any());
        //WHEN + THEN
        Assertions.assertThatThrownBy(() ->
                dataProcessingContextService.getContext(TestConstants.DEFAULT_INTERROGATION_ID))
                .isInstanceOf(GenesisException.class);
    }

    @Test
    @SneakyThrows
    void getContext_multiple_CollectionInstruementIds_test() {
        //GIVEN
        String collectionInstrumentId = TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID;
        String interrogationId = TestConstants.DEFAULT_INTERROGATION_ID;
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                .interrogationId(interrogationId)
                .collectionInstrumentId(collectionInstrumentId)
                .build();
        surveyUnitModelList.add(surveyUnitModel);
        SurveyUnitModel surveyUnitModel2 = SurveyUnitModel.builder()
                .interrogationId(interrogationId)
                .collectionInstrumentId(collectionInstrumentId + "2")
                .build();
        surveyUnitModelList.add(surveyUnitModel2);
        doReturn(surveyUnitModelList)
                .when(surveyUnitPersistencePort)
                .findByInterrogationId(any());
        DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                .collectionInstrumentId(collectionInstrumentId)
                .withReview(true)
                .build();
        doReturn(dataProcessingContextModel).when(dataProcessingContextPersistancePort).findByCollectionInstrumentId(any());

        //WHEN + THEN
        Assertions.assertThatThrownBy(() ->
                        dataProcessingContextService.getContext(TestConstants.DEFAULT_INTERROGATION_ID))
                .isInstanceOf(GenesisException.class);
    }

    @Test
    @SneakyThrows
    void getContext_no_CollectionInstruementIds_test() {
        //GIVEN
        //GIVEN
        String collectionInstrumentId = TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID;
        String interrogationId = TestConstants.DEFAULT_INTERROGATION_ID;
        SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                .interrogationId(interrogationId)
                .collectionInstrumentId(null)c
                .build();
        doReturn(Collections.singletonList(surveyUnitModel))
                .when(surveyUnitPersistencePort)
                .findByInterrogationId(any());
        DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                .collectionInstrumentId(collectionInstrumentId)
                .withReview(true)
                .build();
        doReturn(dataProcessingContextModel).when(dataProcessingContextPersistancePort).findByCollectionInstrumentId(any());

        //WHEN + THEN
        Assertions.assertThatThrownBy(() ->
                        dataProcessingContextService.getContext(TestConstants.DEFAULT_INTERROGATION_ID))
                .isInstanceOf(GenesisException.class);
    }
}