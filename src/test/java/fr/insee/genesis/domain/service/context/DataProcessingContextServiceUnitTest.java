package fr.insee.genesis.domain.service.context;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DataProcessingContextServiceUnitTest {
    @Mock
    private DataProcessingContextPersistancePort dataProcessingContextPersistancePort;

    @Mock
    private SurveyUnitPersistencePort surveyUnitPersistencePort;

    @InjectMocks
    private DataProcessingContextService dataProcessingContextService;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @SneakyThrows
    void saveContextByCollectionInstrumentId_test(boolean isNull) {
        //GIVEN
        if(!isNull){
            DataProcessingContextModel dataProcessingContextModel = new DataProcessingContextModel();
            doReturn(dataProcessingContextModel).when(dataProcessingContextPersistancePort)
                    .findByCollectionInstrumentId(any());
        }
        String collectionInstrumentId = "collectionInstrumentId";
        Boolean withReview = true;

        //WHEN
        dataProcessingContextService.saveContextByCollectionInstrumentId(collectionInstrumentId, withReview);

        //THEN
        verify(dataProcessingContextPersistancePort, times(1)).save(
                any(DataProcessingContextDocument.class)
        );
    }

    @Test
    void countContext_test(){
        //GIVEN
        long expected = 5;
        doReturn(expected).when(dataProcessingContextPersistancePort).count();

        //WHEN
        long actual = dataProcessingContextService.countContexts();

        //THEN
        Assertions.assertThat(actual).isEqualTo(expected);
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
    void getContext_multiple_CollectionInstrumentIds_test() {
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

        //WHEN + THEN
        Assertions.assertThatThrownBy(() ->
                        dataProcessingContextService.getContext(TestConstants.DEFAULT_INTERROGATION_ID))
                .isInstanceOf(GenesisException.class);
    }

    @Test
    @SneakyThrows
    void getContext_no_CollectionInstrumentIds_test() {
        //GIVEN
        String interrogationId = TestConstants.DEFAULT_INTERROGATION_ID;
        SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                .interrogationId(interrogationId)
                .collectionInstrumentId(null)
                .build();
        doReturn(Collections.singletonList(surveyUnitModel))
                .when(surveyUnitPersistencePort)
                .findByInterrogationId(any());

        //WHEN + THEN
        Assertions.assertThatThrownBy(() ->
                        dataProcessingContextService.getContext(TestConstants.DEFAULT_INTERROGATION_ID))
                .isInstanceOf(GenesisException.class);
    }

    @Test
    void getContextByCollectionInstrumentId_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        DataProcessingContextModel expected = new DataProcessingContextModel();
        doReturn(expected).when(dataProcessingContextPersistancePort).findByCollectionInstrumentId(any());

        //WHEN
        DataProcessingContextModel actual = dataProcessingContextService.getContextByCollectionInstrumentId(collectionInstrumentId);

        //THEN
        verify(dataProcessingContextPersistancePort, times(1))
                .findByCollectionInstrumentId(collectionInstrumentId);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getCollectionInstrumentIdsWithReview_test() {
        //GIVEN
        DataProcessingContextDocument dataProcessingContextDocument = new DataProcessingContextDocument();
        dataProcessingContextDocument.setCollectionInstrumentId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
        dataProcessingContextDocument.setWithReview(true);
        //null collectionInstrumentId
        DataProcessingContextDocument dataProcessingContextDocumentNull = new DataProcessingContextDocument();
        dataProcessingContextDocumentNull.setCollectionInstrumentId(null);
        dataProcessingContextDocumentNull.setWithReview(true);
        List<DataProcessingContextDocument> documents = List.of(
                dataProcessingContextDocument,
                dataProcessingContextDocumentNull
        );
        doReturn(documents).when(dataProcessingContextPersistancePort).findAllByReview(anyBoolean());

        //WHEN
        List<String> collectionInstrumentIds = dataProcessingContextService.getCollectionInstrumentIds(true);

        //THEN
        Assertions.assertThat(collectionInstrumentIds).containsExactly(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
    }

    @Test
    @SneakyThrows
    void getReviewByCollectionInstrumentId_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        boolean withReview = true;
        DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                .collectionInstrumentId(collectionInstrumentId)
                .withReview(withReview)
                .build();
        doReturn(dataProcessingContextModel).when(dataProcessingContextPersistancePort).findByCollectionInstrumentId(any());

        //WHEN
        boolean actual = dataProcessingContextService.getReviewByCollectionInstrumentId(collectionInstrumentId);

        //THEN
        verify(dataProcessingContextPersistancePort, times(1)).findByCollectionInstrumentId(collectionInstrumentId);
        Assertions.assertThat(actual).isEqualTo(withReview);
    }

    @Test
    void getReviewByCollectionInstrumentId_not_found_test() {
        //WHEN + THEN
        try{
            dataProcessingContextService.getReviewByCollectionInstrumentId("collectionInstrumentId");
            Assertions.fail();
        }catch (GenesisException ge){
            Assertions.assertThat(ge.getStatus()).isEqualTo(404);
        }
    }
}