package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.repository.DataProcessingContextMongoDBRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataProcessingContextMongoAdapter tests")
class DataProcessingContextMongoAdapterTest {

    private static final String COLLECTION_INSTRUMENT_ID = "instrument-456";

    @Mock
    private DataProcessingContextMongoDBRepository dataProcessingContextMongoDBRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private DataProcessingContextMongoAdapter adapter;

    @Nested
    @DisplayName("findByCollectionInstrumentId() tests")
    class FindByCollectionInstrumentIdTests {

        @Test
        @DisplayName("Should return null when repository returns empty list")
        void findByCollectionInstrumentId_noDocument_shouldReturnNull() {
            //GIVEN
            when(dataProcessingContextMongoDBRepository.findByCollectionInstrumentIdList(List.of(COLLECTION_INSTRUMENT_ID)))
                    .thenReturn(List.of());

            //WHEN
            DataProcessingContextModel result = adapter.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

            //THEN
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return a non-null model when repository returns a document")
        void findByCollectionInstrumentId_withDocument_shouldReturnModel() {
            //GIVEN
            DataProcessingContextDocument doc = new DataProcessingContextDocument();
            when(dataProcessingContextMongoDBRepository.findByCollectionInstrumentIdList(List.of(COLLECTION_INSTRUMENT_ID)))
                    .thenReturn(List.of(doc));

            //WHEN
            DataProcessingContextModel result = adapter.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

            //THEN
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should pass collectionInstrumentId wrapped in a singleton list")
        void findByCollectionInstrumentId_shouldWrapIdInSingletonList() {
            //GIVEN
            when(dataProcessingContextMongoDBRepository.findByCollectionInstrumentIdList(any()))
                    .thenReturn(List.of());

            //WHEN
            adapter.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

            //THEN
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
            verify(dataProcessingContextMongoDBRepository).findByCollectionInstrumentIdList(captor.capture());
            assertThat(captor.getValue()).containsExactly(COLLECTION_INSTRUMENT_ID);
        }
    }

    @Nested
    @DisplayName("findByCollectionInstrumentIds() tests")
    class FindByCollectionInstrumentIdsTests {

        @Test
        @DisplayName("Should delegate to repository with the full list")
        void findByCollectionInstrumentIds_shouldCallRepository() {
            //GIVEN
            List<String> ids = List.of("id1", "id2");
            when(dataProcessingContextMongoDBRepository.findByCollectionInstrumentIdList(ids))
                    .thenReturn(List.of());

            //WHEN
            List<DataProcessingContextModel> result = adapter.findByCollectionInstrumentIds(ids);

            //THEN
            verify(dataProcessingContextMongoDBRepository).findByCollectionInstrumentIdList(ids);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should return empty list when repository returns no documents")
        void findByCollectionInstrumentIds_noDocuments_shouldReturnEmptyList() {
            //GIVEN
            when(dataProcessingContextMongoDBRepository.findByCollectionInstrumentIdList(any()))
                    .thenReturn(List.of());

            //WHEN
            List<DataProcessingContextModel> result = adapter.findByCollectionInstrumentIds(List.of("id1"));

            //THEN
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save() tests")
    class SaveTests {

        @Test
        @DisplayName("save() should delegate to repository.save()")
        void save_shouldCallRepositorySave() {
            //GIVEN
            DataProcessingContextDocument doc = new DataProcessingContextDocument();

            //WHEN
            adapter.save(doc);

            //THEN
            verify(dataProcessingContextMongoDBRepository).save(doc);
            verifyNoMoreInteractions(dataProcessingContextMongoDBRepository);
        }
    }

    @Nested
    @DisplayName("saveAll() tests")
    class SaveAllTests {

        @Test
        @DisplayName("saveAll() should delegate the full list to repository.saveAll()")
        void saveAll_shouldCallRepositorySaveAll() {
            //GIVEN
            List<DataProcessingContextDocument> docs = List.of(
                    new DataProcessingContextDocument(),
                    new DataProcessingContextDocument()
            );

            //WHEN
            adapter.saveAll(docs);

            //THEN
            verify(dataProcessingContextMongoDBRepository).saveAll(docs);
            verifyNoMoreInteractions(dataProcessingContextMongoDBRepository);
        }

        @Test
        @DisplayName("saveAll() with empty list should call repository with empty list")
        void saveAll_emptyList_shouldCallRepositoryWithEmptyList() {
            //WHEN
            adapter.saveAll(List.of());

            //THEN
            verify(dataProcessingContextMongoDBRepository).saveAll(List.of());
        }
    }

    @Nested
    @DisplayName("findAll() tests")
    class FindAllTests {

        @Test
        @DisplayName("findAll() should return what the repository returns")
        void findAll_shouldDelegateToRepository() {
            //GIVEN
            List<DataProcessingContextDocument> docs = List.of(new DataProcessingContextDocument());
            when(dataProcessingContextMongoDBRepository.findAll()).thenReturn(docs);

            //WHEN
            List<DataProcessingContextDocument> result = adapter.findAll();

            //THEN
            assertThat(result).isEqualTo(docs);
        }

        @Test
        @DisplayName("findAll() should return empty list when repository is empty")
        void findAll_empty_shouldReturnEmptyList() {
            //GIVEN
            when(dataProcessingContextMongoDBRepository.findAll()).thenReturn(List.of());

            //WHEN + THEN
            assertThat(adapter.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("count() tests")
    class CountTests {

        @Test
        @DisplayName("count() should return the value from repository")
        void count_shouldReturnRepositoryValue() {
            //GIVEN
            when(dataProcessingContextMongoDBRepository.count()).thenReturn(42L);

            //WHEN + THEN
            assertThat(adapter.count()).isEqualTo(42L);
        }

        @Test
        @DisplayName("count() should return 0 when repository is empty")
        void count_empty_shouldReturnZero() {

            //GIVEN
            when(dataProcessingContextMongoDBRepository.count()).thenReturn(0L);

            //WHEN + THEN
            assertThat(adapter.count()).isZero();
        }
    }

    @Nested
    @DisplayName("removeExpiredSchedules() tests")
    class RemoveExpiredSchedulesTests {

        @Test
        @DisplayName("Should return empty list when no schedule is expired")
        void removeExpiredSchedules_noExpired_shouldReturnEmptyList() throws IOException {
            //GIVEN
            KraftwerkExecutionSchedule future = buildSchedule(LocalDateTime.now().plusDays(1));
            DataProcessingContextModel model = buildModelWithSchedules(List.of(future));

            //WHEN
            List<KraftwerkExecutionSchedule> removed = adapter.removeExpiredSchedules(model);

            //THEN
            assertThat(removed).isEmpty();
            verifyNoInteractions(mongoTemplate);
        }

        @Test
        @DisplayName("Should return expired schedules and call updateMulti by collectionInstrumentId when set")
        void removeExpiredSchedules_withCollectionInstrumentId_shouldUpdateByCollectionInstrumentId() throws IOException {
            //GIVEN
            KraftwerkExecutionSchedule expired = buildSchedule(LocalDateTime.now().minusDays(1));
            DataProcessingContextModel model = buildModelWithSchedules(List.of(expired));

            //WHEN
            List<KraftwerkExecutionSchedule> removed = adapter.removeExpiredSchedules(model);

            //THEN
            assertThat(removed).hasSize(1).containsExactly(expired);

            ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
            verify(mongoTemplate).updateMulti(
                    queryCaptor.capture(),
                    any(Update.class),
                    eq(Constants.MONGODB_SCHEDULE_COLLECTION_NAME)
            );
            // The outer query must filter by collectionInstrumentId
            assertThat(queryCaptor.getValue().toString()).contains("collectionInstrumentId");
        }

        @Test
        @DisplayName("Should remove only expired schedules from a mixed list")
        void removeExpiredSchedules_mixedList_shouldRemoveOnlyExpired() throws IOException {
            //GIVEN
            KraftwerkExecutionSchedule expired = buildSchedule(LocalDateTime.now().minusDays(1));
            KraftwerkExecutionSchedule future = buildSchedule(LocalDateTime.now().plusDays(1));
            DataProcessingContextModel model = buildModelWithSchedules(List.of(expired, future));

            //WHEN
            List<KraftwerkExecutionSchedule> removed = adapter.removeExpiredSchedules(model);

            //THEN
            assertThat(removed).hasSize(1).containsExactly(expired);
            // updateMulti called once, only for the expired one
            verify(mongoTemplate, times(1)).updateMulti(any(), any(), any(String.class));
        }

        @Test
        @DisplayName("Should call updateMulti once per expired schedule")
        void removeExpiredSchedules_multipleExpired_shouldCallUpdateMultiForEach() throws IOException {
            //GIVEN
            KraftwerkExecutionSchedule expired1 = buildSchedule(LocalDateTime.now().minusDays(1));
            KraftwerkExecutionSchedule expired2 = buildSchedule(LocalDateTime.now().minusDays(2));
            DataProcessingContextModel model = buildModelWithSchedules(List.of(expired1, expired2));

            //WHEN
            List<KraftwerkExecutionSchedule> removed = adapter.removeExpiredSchedules(model);

            //THEN
            assertThat(removed).hasSize(2);
            verify(mongoTemplate, times(2)).updateMulti(any(), any(), any(String.class));
        }

        @Test
        @DisplayName("Should return empty list when schedule list is empty")
        void removeExpiredSchedules_emptyScheduleList_shouldReturnEmptyList() throws IOException {
            //GIVEN
            DataProcessingContextModel model = buildModelWithSchedules(List.of());

            //WHEN
            List<KraftwerkExecutionSchedule> removed = adapter.removeExpiredSchedules(model);

            //THEN
            assertThat(removed).isEmpty();
            verifyNoInteractions(mongoTemplate);
        }
    }
    @Nested
    @DisplayName("findAllByReview() tests")
    class FindAllByReviewTests {

        @Test
        @DisplayName("Should return only documents with withReview=true when requested")
        void findAllByReview_true_shouldReturnOnlyWithReviewDocs() {
            //GIVEN
            DataProcessingContextDocument withReview = buildDocWithReview(true);
            DataProcessingContextDocument withoutReview = buildDocWithReview(false);
            when(dataProcessingContextMongoDBRepository.findAll()).thenReturn(List.of(withReview, withoutReview));

            //WHEN
            List<DataProcessingContextDocument> result = adapter.findAllByReview(true);

            //THEN
            assertThat(result).containsExactly(withReview);
        }

        @Test
        @DisplayName("Should return only documents with withReview=false when requested")
        void findAllByReview_false_shouldReturnOnlyWithoutReviewDocs() {
            //GIVEN
            DataProcessingContextDocument withReview = buildDocWithReview(true);
            DataProcessingContextDocument withoutReview = buildDocWithReview(false);
            when(dataProcessingContextMongoDBRepository.findAll()).thenReturn(List.of(withReview, withoutReview));

            //WHEN
            List<DataProcessingContextDocument> result = adapter.findAllByReview(false);

            //THEN
            assertThat(result).containsExactly(withoutReview);
        }

        @Test
        @DisplayName("Should return empty list when no document matches the review flag")
        void findAllByReview_noMatch_shouldReturnEmptyList() {
            //GIVEN
            DataProcessingContextDocument withoutReview = buildDocWithReview(false);
            when(dataProcessingContextMongoDBRepository.findAll()).thenReturn(List.of(withoutReview));

            //WHEN
            List<DataProcessingContextDocument> result = adapter.findAllByReview(true);

            //THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return all documents when all match the review flag")
        void findAllByReview_allMatch_shouldReturnAll() {
            //GIVEN
            DataProcessingContextDocument d1 = buildDocWithReview(true);
            DataProcessingContextDocument d2 = buildDocWithReview(true);
            when(dataProcessingContextMongoDBRepository.findAll()).thenReturn(List.of(d1, d2));

            //WHEN
            List<DataProcessingContextDocument> result = adapter.findAllByReview(true);

            //THEN
            assertThat(result).hasSize(2).containsExactly(d1, d2);
        }

        @Test
        @DisplayName("Should return empty list when repository is empty")
        void findAllByReview_emptyRepository_shouldReturnEmptyList() {
            //GIVEN
            when(dataProcessingContextMongoDBRepository.findAll()).thenReturn(List.of());

            //WHEN + THEN
            assertThat(adapter.findAllByReview(true)).isEmpty();
            assertThat(adapter.findAllByReview(false)).isEmpty();
        }
    }

    //UTILS
    private KraftwerkExecutionSchedule buildSchedule(LocalDateTime endDate) {
        return new KraftwerkExecutionSchedule(
                null,
                "0 10 * * *",
                ServiceToCall.GENESIS,
                LocalDateTime.now(),
                endDate,
                null
        );
    }

    private DataProcessingContextModel buildModelWithSchedules(List<KraftwerkExecutionSchedule> schedules) {
        return DataProcessingContextModel.builder()
                .collectionInstrumentId(DataProcessingContextMongoAdapterTest.COLLECTION_INSTRUMENT_ID)
                .kraftwerkExecutionScheduleList(schedules)
                .build();
    }

    private DataProcessingContextDocument buildDocWithReview(boolean withReview) {
        DataProcessingContextDocument doc = new DataProcessingContextDocument();
        doc.setWithReview(withReview);
        return doc;
    }
}