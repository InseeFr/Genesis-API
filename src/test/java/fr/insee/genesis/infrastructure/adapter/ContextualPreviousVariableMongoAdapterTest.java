package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.infrastructure.document.contextualprevious.ContextualPreviousVariableDocument;
import fr.insee.genesis.infrastructure.repository.ContextualPreviousVariableMongoDBRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContextualPreviousVariableMongoAdapter tests")
class ContextualPreviousVariableMongoAdapterTest {

    private static final String COLLECTION_INSTRUMENT_ID = "instrument-123";
    private static final String INTERROGATION_ID = "interrogation-456";
    private static final String BACKUP_COLLECTION = "editedPreviousResponses_instrument-123_backup";
    private static final String MAIN_COLLECTION = "editedPreviousResponses";

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private ContextualPreviousVariableMongoDBRepository repository;

    @InjectMocks
    private ContextualPreviousVariableMongoAdapter adapter;

    @Nested
    @DisplayName("backup() tests")
    class BackupTests {
        @Test
        @DisplayName("backup() should drop existing backup collection then run aggregation into backup collection")
        @SuppressWarnings("unchecked")
        void backup_shouldDeleteThenAggregate() {
            //GIVEN
            when(mongoTemplate.collectionExists(BACKUP_COLLECTION)).thenReturn(true);
            when(mongoTemplate.aggregate(any(Aggregation.class), eq(MAIN_COLLECTION), eq(ContextualPreviousVariableDocument.class)))
                    .thenReturn(mock(AggregationResults.class));

            //WHEN
            adapter.backup(COLLECTION_INSTRUMENT_ID);

            //THEN
            verify(mongoTemplate).collectionExists(BACKUP_COLLECTION);
            verify(mongoTemplate).dropCollection(BACKUP_COLLECTION);
            verify(mongoTemplate).aggregate(any(Aggregation.class), eq(MAIN_COLLECTION), eq(ContextualPreviousVariableDocument.class));
        }

        @Test
        @DisplayName("backup() should not drop collection if backup does not exist")
        @SuppressWarnings("unchecked")
        void backup_shouldSkipDropIfBackupMissing() {
            //GIVEN
            when(mongoTemplate.collectionExists(BACKUP_COLLECTION)).thenReturn(false);
            when(mongoTemplate.aggregate(any(Aggregation.class), eq(MAIN_COLLECTION), eq(ContextualPreviousVariableDocument.class)))
                    .thenReturn(mock(AggregationResults.class));

            //WHEN
            adapter.backup(COLLECTION_INSTRUMENT_ID);

            //THEN
            verify(mongoTemplate, never()).dropCollection(BACKUP_COLLECTION);
            verify(mongoTemplate).aggregate(any(Aggregation.class), eq(MAIN_COLLECTION), eq(ContextualPreviousVariableDocument.class));
        }
    }

    @Nested
    @DisplayName("deleteBackup() tests")
    class DeleteBackupTests {

        @Test
        @DisplayName("deleteBackup() should drop the backup collection when it exists")
        void deleteBackup_shouldDropCollectionWhenExists() {
            //GIVEN
            when(mongoTemplate.collectionExists(BACKUP_COLLECTION)).thenReturn(true);

            //WHEN
            adapter.deleteBackup(COLLECTION_INSTRUMENT_ID);

            //THEN
            verify(mongoTemplate).collectionExists(BACKUP_COLLECTION);
            verify(mongoTemplate).dropCollection(BACKUP_COLLECTION);
        }

        @Test
        @DisplayName("deleteBackup() should do nothing when backup collection does not exist")
        void deleteBackup_shouldDoNothingWhenMissing() {
            //GIVEN
            when(mongoTemplate.collectionExists(BACKUP_COLLECTION)).thenReturn(false);

            //WHEN
            adapter.deleteBackup(COLLECTION_INSTRUMENT_ID);

            //THEN
            verify(mongoTemplate).collectionExists(BACKUP_COLLECTION);
            verify(mongoTemplate, never()).dropCollection(any(String.class));
        }

        @Test
        @DisplayName("deleteBackup() should use correctly formatted collection name")
        void deleteBackup_shouldUseCorrectCollectionName() {
            //GIVEN
            when(mongoTemplate.collectionExists(BACKUP_COLLECTION)).thenReturn(true);

            //WHEN
            adapter.deleteBackup(COLLECTION_INSTRUMENT_ID);

            //THEN
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(mongoTemplate).dropCollection(captor.capture());
            assertThat(captor.getValue()).isEqualTo(BACKUP_COLLECTION);
        }
    }

    @Nested
    @DisplayName("restoreBackup() tests")
    class RestoreBackupTests {

        @Test
        @DisplayName("restoreBackup() should delete current data then aggregate from backup into main collection")
        @SuppressWarnings("unchecked")
        void restoreBackup_shouldDeleteThenAggregate() {
            //GIVEN
            when(mongoTemplate.aggregate(any(Aggregation.class), eq(BACKUP_COLLECTION), eq(ContextualPreviousVariableDocument.class)))
                    .thenReturn(mock(AggregationResults.class));

            //WHEN
            adapter.restoreBackup(COLLECTION_INSTRUMENT_ID);

            //THEN
            verify(repository).deleteByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);
            verify(repository).deleteByQuestionnaireId(COLLECTION_INSTRUMENT_ID);
            verify(mongoTemplate).aggregate(any(Aggregation.class), eq(BACKUP_COLLECTION), eq(ContextualPreviousVariableDocument.class));
        }

        @Test
        @DisplayName("restoreBackup() should aggregate from the correctly named backup collection")
        @SuppressWarnings("unchecked")
        void restoreBackup_shouldUseCorrectSourceCollection() {
            //GIVEN
            when(mongoTemplate.aggregate(any(Aggregation.class), eq(BACKUP_COLLECTION), eq(ContextualPreviousVariableDocument.class)))
                    .thenReturn(mock(AggregationResults.class));

            //WHEN
            adapter.restoreBackup(COLLECTION_INSTRUMENT_ID);

            //THEN
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(mongoTemplate).aggregate(any(Aggregation.class), captor.capture(), eq(ContextualPreviousVariableDocument.class));
            assertThat(captor.getValue()).isEqualTo(BACKUP_COLLECTION);
        }
    }

    @Nested
    @DisplayName("saveAll() tests")
    class SaveAllTests {

        @Test
        @DisplayName("saveAll() should call repository.saveAll() with a non-null list")
        void saveAll_shouldCallRepositorySaveAll() {
            //GIVEN
            adapter.saveAll(List.of(buildModel("id-1")));

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<ContextualPreviousVariableDocument>> captor = ArgumentCaptor.forClass(List.class);


            //WHEN
            verify(repository).saveAll(captor.capture());

            //THEN
            assertThat(captor.getValue()).isNotNull();
        }

        @Test
        @DisplayName("saveAll() with empty list should call repository with empty list")
        void saveAll_emptyList_shouldCallRepositoryWithEmptyList() {
            //GIVEN
            adapter.saveAll(List.of());

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<ContextualPreviousVariableDocument>> captor = ArgumentCaptor.forClass(List.class);

            //WHEN
            verify(repository).saveAll(captor.capture());

            //THEN
            assertThat(captor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("saveAll() should pass as many documents as models provided")
        void saveAll_shouldMapAllModels() {
            //GIVEN
            adapter.saveAll(List.of(buildModel("id-1"), buildModel("id-2"), buildModel("id-3")));

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<ContextualPreviousVariableDocument>> captor = ArgumentCaptor.forClass(List.class);

            //WHEN
            verify(repository).saveAll(captor.capture());

            //THEN
            assertThat(captor.getValue()).hasSize(3);
        }
    }

    // -------------------------------------------------------------------------
    // delete()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("delete() tests")
    class DeleteTests {

        @Test
        @DisplayName("delete() should call both deleteByCollectionInstrumentId and deleteByQuestionnaireId")
        void delete_shouldCallBothRepositoryDeleteMethods() {
            //WHEN
            adapter.delete(COLLECTION_INSTRUMENT_ID);

            //THEN
            verify(repository).deleteByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);
            verify(repository).deleteByQuestionnaireId(COLLECTION_INSTRUMENT_ID);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("delete() should pass the same id to both delete methods")
        void delete_shouldPassSameIdToBothMethods() {
            //WHEN
            adapter.delete(COLLECTION_INSTRUMENT_ID);

            //THEN
            ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class);
            verify(repository).deleteByCollectionInstrumentId(captor1.capture());
            verify(repository).deleteByQuestionnaireId(captor2.capture());
            assertThat(captor1.getValue()).isEqualTo(COLLECTION_INSTRUMENT_ID);
            assertThat(captor2.getValue()).isEqualTo(COLLECTION_INSTRUMENT_ID);
        }
    }

    @Nested
    @DisplayName("findByCollectionInstrumentIdAndInterrogationId() tests")
    class FindTests {

        @Test
        @DisplayName("Should return null when both repository methods return empty lists")
        void find_noResults_shouldReturnNull() {
            //GIVEN
            when(repository.findByQuestionnaireIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID))
                    .thenReturn(List.of());
            when(repository.findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID))
                    .thenReturn(List.of());

            //WHEN
            ContextualPreviousVariableModel result =
                    adapter.findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID);

            //THEN
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return a non-null model when one document is found via questionnaireId")
        void find_oneResultViaQuestionnaireId_shouldReturnModel() {
            //GIVEN
            when(repository.findByQuestionnaireIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID))
                    .thenReturn(List.of(new ContextualPreviousVariableDocument()));
            when(repository.findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID))
                    .thenReturn(List.of());

            //WHEN
            ContextualPreviousVariableModel result =
                    adapter.findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID);

            //THEN
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should return a non-null model when one document found via collectionInstrumentId")
        void find_oneResultViaCollectionInstrumentId_shouldReturnModel() {
            //GIVEN
            when(repository.findByQuestionnaireIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID))
                    .thenReturn(List.of());
            when(repository.findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID))
                    .thenReturn(List.of(new ContextualPreviousVariableDocument()));

            //WHEN
            ContextualPreviousVariableModel result =
                    adapter.findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID);

            //THEN
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should still return a result when more than one document is found")
        void find_moreThanOneResult_shouldReturnFirstAndNotNull() {
            //GIVEN
            when(repository.findByQuestionnaireIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID))
                    .thenReturn(List.of(new ContextualPreviousVariableDocument(), new ContextualPreviousVariableDocument()));
            when(repository.findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID))
                    .thenReturn(List.of());

            //WHEN
            ContextualPreviousVariableModel result =
                    adapter.findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID);

            //THEN
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should always call both repository find methods")
        void find_shouldAlwaysCallBothRepositoryMethods() {
            //GIVEN
            when(repository.findByQuestionnaireIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID))
                    .thenReturn(List.of());
            when(repository.findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID))
                    .thenReturn(List.of());

            //WHEN
            adapter.findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID);

            //THEN
            verify(repository).findByQuestionnaireIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID);
            verify(repository).findByCollectionInstrumentIdAndInterrogationId(COLLECTION_INSTRUMENT_ID, INTERROGATION_ID);
        }
    }

    //UTILS
    private ContextualPreviousVariableModel buildModel(String interrogationId) {
        return ContextualPreviousVariableModel.builder()
                .interrogationId(interrogationId)
                .collectionInstrumentId(COLLECTION_INSTRUMENT_ID)
                .build();
    }
}