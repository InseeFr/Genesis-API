package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.GroupedInterrogation;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.GroupedInterrogationDocument;
import fr.insee.genesis.infrastructure.repository.LunaticJsonMongoDBRepository;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static fr.insee.genesis.TestConstants.DEFAULT_INTERROGATION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LunaticJsonRawDataMongoAdapterTest {

    private static final String QUESTIONNAIRE_ID = "questionnaire-123";
    private static final String CAMPAIGN_ID = "campaign-456";
    private static final String INTERROGATION_ID = "interrogation-789";
    private static final Mode MODE = Mode.WEB;

    @Mock
    private LunaticJsonMongoDBRepository repository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private LunaticJsonRawDataMongoAdapter adapter;

    @Nested
    @DisplayName("save() tests")
    class SaveTests {

        @Test
        @DisplayName("save() should call repository.insert() with a non-null document")
        void save_shouldCallRepositoryInsert() {
            //GIVEN
            LunaticJsonRawDataModel model = buildModel();

            //WHEN
            adapter.save(model);

            //THEN
            verify(repository).insert(any(LunaticJsonRawDataDocument.class));
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("save() should use insert() and NOT save() to avoid upsert behaviour")
        void save_shouldUseInsertNotSave() {
            //WHEN
            adapter.save(buildModel());

            //THEN
            verify(repository, never()).save(any());
            verify(repository).insert(any(LunaticJsonRawDataDocument.class));
        }
    }

    @Nested
    @DisplayName("getAllUnprocessedData() tests")
    class GetAllUnprocessedDataTests {

        @Test
        @DisplayName("Should return mapped models from repository")
        void getAllUnprocessedData_shouldReturnMappedModels() {
            //GIVEN
            when(repository.findByNullProcessDate()).thenReturn(List.of(getDocument()));

            //WHEN
            List<LunaticJsonRawDataModel> result = adapter.getAllUnprocessedData();

            //THEN
            assertThat(result).isNotNull().hasSize(1);
            verify(repository).findByNullProcessDate();
        }

        @Test
        @DisplayName("Should return empty list when repository returns no documents")
        void getAllUnprocessedData_noDocuments_shouldReturnEmptyList() {
            //GIVEN
            when(repository.findByNullProcessDate()).thenReturn(List.of());

            //WHEN + THEN
            assertThat(adapter.getAllUnprocessedData()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findDistinctQuestionnaireIdsByNullProcessDate() tests")
    class FindDistinctQuestionnaireIdsByNullProcessDateTests {

        @Test
        @DisplayName("Should return a Set of ids from repository")
        void findDistinct_shouldReturnSet() {
            //GIVEN
            when(repository.findDistinctQuestionnaireIdByProcessDateIsNull())
                    .thenReturn(List.of("id1", "id2", "id3"));

            //WHEN
            Set<String> result = adapter.findDistinctQuestionnaireIdsByNullProcessDate();

            //THEN
            assertThat(result).containsExactlyInAnyOrder("id1", "id2", "id3");
        }

        @Test
        @DisplayName("Should deduplicate ids when repository returns duplicates")
        void findDistinct_duplicates_shouldDeduplicate() {
            //GIVEN
            when(repository.findDistinctQuestionnaireIdByProcessDateIsNull())
                    .thenReturn(List.of("id1", "id1", "id2"));

            //WHEN
            Set<String> result = adapter.findDistinctQuestionnaireIdsByNullProcessDate();

            //THEN
            assertThat(result).hasSize(2).containsExactlyInAnyOrder("id1", "id2");
        }

        @Test
        @DisplayName("Should return empty set when repository returns empty list")
        void findDistinct_empty_shouldReturnEmptySet() {
            //GIVEN
            when(repository.findDistinctQuestionnaireIdByProcessDateIsNull()).thenReturn(List.of());

            //WHEN + THEN
            assertThat(adapter.findDistinctQuestionnaireIdsByNullProcessDate()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findModesByQuestionnaire() tests")
    class FindModesByQuestionnaireTests {

        @Test
        @DisplayName("Should return modes as a Set for the given questionnaireId")
        void findModes_shouldReturnSet() {
            //GIVEN
            when(repository.findModesByQuestionnaireId(QUESTIONNAIRE_ID))
                    .thenReturn(List.of(Mode.WEB, Mode.TEL));

            //WHEN
            Set<Mode> result = adapter.findModesByQuestionnaire(QUESTIONNAIRE_ID);

            //THEN
            assertThat(result).containsExactlyInAnyOrder(Mode.WEB, Mode.TEL);
            verify(repository).findModesByQuestionnaireId(QUESTIONNAIRE_ID);
        }

        @Test
        @DisplayName("Should deduplicate modes when repository returns duplicates")
        void findModes_duplicates_shouldDeduplicate() {
            //GIVEN
            when(repository.findModesByQuestionnaireId(QUESTIONNAIRE_ID))
                    .thenReturn(List.of(Mode.WEB, Mode.WEB, Mode.TEL));

            //WHEN + THEN
            assertThat(adapter.findModesByQuestionnaire(QUESTIONNAIRE_ID)).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty set when no modes found")
        void findModes_noModes_shouldReturnEmptySet() {
            //GIVEN
            when(repository.findModesByQuestionnaireId(QUESTIONNAIRE_ID)).thenReturn(List.of());

            //WHEN + THEN
            assertThat(adapter.findModesByQuestionnaire(QUESTIONNAIRE_ID)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findRawDataByQuestionnaireId(questionnaireId, mode, interrogationIds) tests")
    class FindRawDataByQuestionnaireIdWithModeTests {

        @Test
        @DisplayName("Should delegate to repository and return mapped models")
        void findRawData_shouldCallRepositoryAndReturnModels() {
            //GIVEN
            List<String> interrogationIds = List.of("i1");
            when(repository.findByQuestionnaireModeAndInterrogations(QUESTIONNAIRE_ID, MODE, interrogationIds))
                    .thenReturn(List.of(getDocument()));

            //WHEN
            List<LunaticJsonRawDataModel> result =
                    adapter.findRawDataByQuestionnaireId(QUESTIONNAIRE_ID, MODE, interrogationIds);

            //THEN
            assertThat(result).isNotNull().hasSize(1);
            verify(repository).findByQuestionnaireModeAndInterrogations(QUESTIONNAIRE_ID, MODE, interrogationIds);
        }

        @Test
        @DisplayName("Should return empty list when repository returns no documents")
        void findRawData_noDocuments_shouldReturnEmptyList() {
            //GIVEN
            when(repository.findByQuestionnaireModeAndInterrogations(any(), any(), any()))
                    .thenReturn(List.of());

            //WHEN + THEN
            assertThat(adapter.findRawDataByQuestionnaireId(QUESTIONNAIRE_ID, MODE, List.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("findRawDataByQuestionnaireId(questionnaireId, pageable) tests")
    class FindRawDataByQuestionnaireIdPagedTests {

        @Test
        @DisplayName("Should return a Page of mapped models")
        void findRawDataPaged_shouldReturnPage() {
            //GIVEN
            Pageable pageable = PageRequest.of(0, 10);
            LunaticJsonRawDataDocument doc = getDocument();
            Page<LunaticJsonRawDataDocument> docPage = new PageImpl<>(List.of(doc), pageable, 1);
            when(repository.findByQuestionnaireId(QUESTIONNAIRE_ID, pageable)).thenReturn(docPage);

            //WHEN
            Page<LunaticJsonRawDataModel> result = adapter.findRawDataByQuestionnaireId(QUESTIONNAIRE_ID, pageable);

            //THEN
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should preserve pagination metadata from repository page")
        void findRawDataPaged_shouldPreservePaginationMetadata() {
            //GIVEN
            Pageable pageable = PageRequest.of(2, 5);
            Page<LunaticJsonRawDataDocument> docPage = new PageImpl<>(List.of(), pageable, 42);
            when(repository.findByQuestionnaireId(QUESTIONNAIRE_ID, pageable)).thenReturn(docPage);

            //WHEN
            Page<LunaticJsonRawDataModel> result = adapter.findRawDataByQuestionnaireId(QUESTIONNAIRE_ID, pageable);

            //THEN
            assertThat(result.getTotalElements()).isEqualTo(42);
            assertThat(result.getPageable()).isEqualTo(pageable);
        }

        @Test
        @DisplayName("Should return empty page when repository returns empty page")
        void findRawDataPaged_emptyPage_shouldReturnEmptyPage() {
            //GIVEN
            Pageable pageable = PageRequest.of(0, 10);
            when(repository.findByQuestionnaireId(QUESTIONNAIRE_ID, pageable))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));

            //WHEN
            Page<LunaticJsonRawDataModel> result = adapter.findRawDataByQuestionnaireId(QUESTIONNAIRE_ID, pageable);

            //THEN
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("findRawDataByInterrogationId() tests")
    class FindRawDataByInterrogationIdTests {

        @Test
        @DisplayName("Should delegate to repository and return mapped models")
        void findRawData_shouldReturnMappedModels() {
            //GIVEN
            when(repository.findByInterrogationId(INTERROGATION_ID))
                    .thenReturn(List.of(getDocument()));

            //WHEN
            List<LunaticJsonRawDataModel> result = adapter.findRawDataByInterrogationId(INTERROGATION_ID);

            //THEN
            assertThat(result).isNotNull().hasSize(1);
            verify(repository).findByInterrogationId(INTERROGATION_ID);
        }

        @Test
        @DisplayName("Should return empty list when no document found")
        void findRawData_noDocument_shouldReturnEmptyList() {
            //GIVEN
            when(repository.findByInterrogationId(INTERROGATION_ID)).thenReturn(List.of());

            //WHEN + THEN
            assertThat(adapter.findRawDataByInterrogationId(INTERROGATION_ID)).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateProcessDates() tests")
    class UpdateProcessDatesTests {

        @Test
        @DisplayName("Should call mongoTemplate.updateMulti() on the correct collection")
        void updateProcessDates_shouldCallUpdateMultiOnCorrectCollection() {
            //GIVEN
            Set<String> ids = Set.of("i1", "i2");

            //WHEN
            adapter.updateProcessDates(QUESTIONNAIRE_ID, ids);

            //THEN
            verify(mongoTemplate).updateMulti(
                    any(Query.class),
                    any(Update.class),
                    eq(Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME)
            );
        }

        @Test
        @DisplayName("Should call updateMulti() exactly once")
        void updateProcessDates_shouldCallUpdateMultiOnce() {
            //GIVEN
            adapter.updateProcessDates(QUESTIONNAIRE_ID, Set.of("i1"));

            //WHEN + THEN
            verify(mongoTemplate, times(1)).updateMulti(any(), any(), any(String.class));
        }

        @Test
        @DisplayName("Should not call repository — only mongoTemplate")
        void updateProcessDates_shouldNotTouchRepository() {
            //GIVEN
            adapter.updateProcessDates(QUESTIONNAIRE_ID, Set.of("i1"));

            //WHEN + THEN
            verifyNoInteractions(repository);
        }
    }

    @Nested
    @DisplayName("findDistinctQuestionnaireIds() tests")
    class FindDistinctQuestionnaireIdsTests {
        private MongoTemplate deepStubMongoTemplate;
        private LunaticJsonRawDataMongoAdapter localAdapter;

        @BeforeEach
        void setUp() {
            deepStubMongoTemplate = mock(MongoTemplate.class, Answers.RETURNS_DEEP_STUBS);
            localAdapter = new LunaticJsonRawDataMongoAdapter(repository, deepStubMongoTemplate);
        }

        @Test
        @DisplayName("Should return non-blank ids as a Set")
        void findDistinctIds_shouldReturnNonBlankIds() {
            //GIVEN
            when(deepStubMongoTemplate.query(LunaticJsonRawDataDocument.class)
                    .distinct("questionnaireId")
                    .as(String.class)
                    .all())
                    .thenReturn(List.of("id1", "id2", "id3"));

            //WHEN + THEN
            assertThat(localAdapter.findDistinctQuestionnaireIds())
                    .containsExactlyInAnyOrder("id1", "id2", "id3");
        }

        @Test
        @DisplayName("Should filter out null ids")
        void findDistinctIds_shouldFilterNulls() {
            //GIVEN
            when(deepStubMongoTemplate.query(LunaticJsonRawDataDocument.class)
                    .distinct("questionnaireId")
                    .as(String.class)
                    .all())
                    .thenReturn(List.of("id1", "id2"));

            //WHEN + THEN
            assertThat(localAdapter.findDistinctQuestionnaireIds())
                    .containsExactlyInAnyOrder("id1", "id2");
        }

        @Test
        @DisplayName("Should filter out blank ids")
        void findDistinctIds_shouldFilterBlankIds() {
            //GIVEN
            when(deepStubMongoTemplate.query(LunaticJsonRawDataDocument.class)
                    .distinct("questionnaireId")
                    .as(String.class)
                    .all())
                    .thenReturn(List.of("id1", "  ", "", "id2"));

            //WHEN + THEN
            assertThat(localAdapter.findDistinctQuestionnaireIds())
                    .containsExactlyInAnyOrder("id1", "id2");
        }

        @Test
        @DisplayName("Should return empty set when all ids are blank or null")
        void findDistinctIds_allBlankOrNull_shouldReturnEmptySet() {
            //GIVEN
            when(deepStubMongoTemplate.query(LunaticJsonRawDataDocument.class)
                    .distinct("questionnaireId")
                    .as(String.class)
                    .all())
                    .thenReturn(List.of("", " "));

            //WHEN + THEN
            assertThat(localAdapter.findDistinctQuestionnaireIds()).isEmpty();
        }

        @Test
        @DisplayName("Should deduplicate ids")
        void findDistinctIds_duplicates_shouldDeduplicate() {
            //GIVEN
            when(deepStubMongoTemplate.query(LunaticJsonRawDataDocument.class)
                    .distinct("questionnaireId")
                    .as(String.class)
                    .all())
                    .thenReturn(List.of("id1", "id1", "id2"));

            //WHEN + THEN
            assertThat(localAdapter.findDistinctQuestionnaireIds()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findByCampaignIdAndDate() tests")
    class FindByCampaignIdAndDateTests {
        @Test
        @DisplayName("Should return a Page of mapped models")
        void findByCampaignIdAndDate_shouldReturnPage() {
            //GIVEN
            Pageable pageable = PageRequest.of(0, 10);
            Instant start = Instant.now().minusSeconds(3600);
            Instant end = Instant.now();
            Page<LunaticJsonRawDataDocument> docPage =
                    new PageImpl<>(List.of(getDocument()), pageable, 1);

            when(repository.findByCampaignIdAndRecordDateBetween(CAMPAIGN_ID, start, end, pageable))
                    .thenReturn(docPage);

            //WHEN
            Page<LunaticJsonRawDataModel> result = adapter.findByCampaignIdAndDate(CAMPAIGN_ID, start, end, pageable);

            //THEN
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should preserve pagination metadata")
        void findByCampaignIdAndDate_shouldPreserveMetadata() {
            //GIVEN
            Pageable pageable = PageRequest.of(1, 5);
            Instant start = Instant.EPOCH;
            Instant end = Instant.now();
            when(repository.findByCampaignIdAndRecordDateBetween(CAMPAIGN_ID, start, end, pageable))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 100));

            //WHEN
            Page<LunaticJsonRawDataModel> result = adapter.findByCampaignIdAndDate(CAMPAIGN_ID, start, end, pageable);

            //THEN
            assertThat(result.getTotalElements()).isEqualTo(100);
            assertThat(result.getPageable()).isEqualTo(pageable);
        }
    }

    @Nested
    @DisplayName("countRawResponsesByQuestionnaireId() tests")
    class CountRawResponsesTests {

        @Test
        @DisplayName("Should return the count from repository")
        void count_shouldReturnRepositoryValue() {
            //GIVEN
            when(repository.countByQuestionnaireId(QUESTIONNAIRE_ID)).thenReturn(7L);

            //WHEN + THEN
            assertThat(adapter.countRawResponsesByQuestionnaireId(QUESTIONNAIRE_ID)).isEqualTo(7L);
        }

        @Test
        @DisplayName("Should return 0 when repository returns 0")
        void count_zero_shouldReturnZero() {
            //GIVEN
            when(repository.countByQuestionnaireId(QUESTIONNAIRE_ID)).thenReturn(0L);

            //WHEN + THEN
            assertThat(adapter.countRawResponsesByQuestionnaireId(QUESTIONNAIRE_ID)).isZero();
        }
    }

    @Nested
    @DisplayName("findProcessedIdsGroupedByQuestionnaireSince() tests")
    class FindProcessedIdsGroupedTests {

        @Test
        @DisplayName("Should call repository with the given 'since' date and return mapped models")
        void findProcessedIds_shouldCallRepository() {
            //GIVEN
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            when(repository.aggregateRawGrouped(since)).thenReturn(List.of());

            //WHEN
            List<GroupedInterrogation> result = adapter.findProcessedIdsGroupedByQuestionnaireSince(since);

            //THEN
            assertThat(result).isNotNull();
            verify(repository).aggregateRawGrouped(since);
        }

        @Test
        @DisplayName("Should return empty list when repository returns empty list")
        void findProcessedIds_empty_shouldReturnEmptyList() {
            //GIVEN
            when(repository.aggregateRawGrouped(any())).thenReturn(List.of());

            //WHEN + THEN
            assertThat(adapter.findProcessedIdsGroupedByQuestionnaireSince(LocalDateTime.now())).isEmpty();
        }
    }

    @Nested
    @DisplayName("findUnprocessedIds() tests")
    class FindUnprocessedIdsTests {

        @Test
        @DisplayName("Should delegate to repository and return mapped models")
        void findUnprocessedIds_shouldCallRepository() {
            //GIVEN
            when(repository.aggregateRawGroupedWithNullProcessDate()).thenReturn(List.of());

            //WHEN
            List<GroupedInterrogation> result = adapter.findUnprocessedIds();

            //THEN
            assertThat(result).isNotNull();
            verify(repository).aggregateRawGroupedWithNullProcessDate();
        }
    }

    @Nested
    @DisplayName("findUnprocessedInterrogationIdsByCollectionInstrumentId() tests")
    class FindUnprocessedInterrogationIdsByCollectionInstrumentIdTests {

        @Test
        @DisplayName("Should return empty set when repository returns no grouped documents")
        void findUnprocessed_noDocuments_shouldReturnEmptySet() {
            //GIVEN
            when(repository.aggregateRawGroupedWithNullProcessDate(QUESTIONNAIRE_ID)).thenReturn(List.of());

            //WHEN
            Set<String> result = adapter.findUnprocessedInterrogationIdsByCollectionInstrumentId(QUESTIONNAIRE_ID);

            //THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should aggregate all interrogation ids from grouped documents")
        void findUnprocessed_withDocuments_shouldAggregateAllIds() {
            //GIVEN
            var doc1 = mock(GroupedInterrogationDocument.class);
            var doc2 = mock(GroupedInterrogationDocument.class);
            when(doc1.getInterrogationIds()).thenReturn(List.of("i1", "i2"));
            when(doc2.getInterrogationIds()).thenReturn(List.of("i3"));
            when(repository.aggregateRawGroupedWithNullProcessDate(QUESTIONNAIRE_ID))
                    .thenReturn(List.of(doc1, doc2));

            //WHEN
            Set<String> result = adapter.findUnprocessedInterrogationIdsByCollectionInstrumentId(QUESTIONNAIRE_ID);

            //THEN
            assertThat(result).containsExactlyInAnyOrder("i1", "i2", "i3");
        }

        @Test
        @DisplayName("Should deduplicate interrogation ids across grouped documents")
        void findUnprocessed_duplicateIds_shouldDeduplicate() {
            //GIVEN
            var doc = mock(GroupedInterrogationDocument.class);
            when(doc.getInterrogationIds()).thenReturn(List.of("i1", "i1", "i2"));
            when(repository.aggregateRawGroupedWithNullProcessDate(QUESTIONNAIRE_ID))
                    .thenReturn(List.of(doc));

            //WHEN
            Set<String> result = adapter.findUnprocessedInterrogationIdsByCollectionInstrumentId(QUESTIONNAIRE_ID);

            //THEN
            assertThat(result).hasSize(2).containsExactlyInAnyOrder("i1", "i2");
        }
    }

    @Nested
    @DisplayName("existsByInterrogationId() tests")
    class ExistsByInterrogationIdTests {

        @Test
        @DisplayName("Should return true when repository finds a match")
        void exists_found_shouldReturnTrue() {
            //GIVEN
            when(repository.existsByInterrogationId(INTERROGATION_ID)).thenReturn(true);

            //WHEN + THEN
            assertThat(adapter.existsByInterrogationId(INTERROGATION_ID)).isTrue();
        }

        @Test
        @DisplayName("Should return false when repository finds no match")
        void exists_notFound_shouldReturnFalse() {
            //GIVEN
            when(repository.existsByInterrogationId(INTERROGATION_ID)).thenReturn(false);

            //WHEN + THEN
            assertThat(adapter.existsByInterrogationId(INTERROGATION_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("countDistinctInterrogationIdsByQuestionnaireId() tests")
    class CountDistinctInterrogationIdsTests {

        @Test
        @DisplayName("Should return the count from repository")
        void countDistinct_shouldReturnRepositoryValue() {
            //GIVEN
            when(repository.countDistinctInterrogationIdsByQuestionnaireId(QUESTIONNAIRE_ID)).thenReturn(15L);

            //WHEN + THEN
            assertThat(adapter.countDistinctInterrogationIdsByQuestionnaireId(QUESTIONNAIRE_ID)).isEqualTo(15L);
        }

        @Test
        @DisplayName("Should return 0 when repository returns null (null-safety)")
        void countDistinct_nullFromRepository_shouldReturnZero() {
            //GIVEN
            when(repository.countDistinctInterrogationIdsByQuestionnaireId(QUESTIONNAIRE_ID)).thenReturn(null);

            //WHEN + THEN
            assertThat(adapter.countDistinctInterrogationIdsByQuestionnaireId(QUESTIONNAIRE_ID)).isZero();
        }

        @Test
        @DisplayName("Should return 0 when repository returns 0")
        void countDistinct_zero_shouldReturnZero() {
            //GIVEN
            when(repository.countDistinctInterrogationIdsByQuestionnaireId(QUESTIONNAIRE_ID)).thenReturn(0L);

            //WHEN + THEN
            assertThat(adapter.countDistinctInterrogationIdsByQuestionnaireId(QUESTIONNAIRE_ID)).isZero();
        }
    }

    @Test
    void existsByInterrogationId_shouldReturnTrue_whenRepositoryReturnsTrue() {
        // Given
        when(repository.existsByInterrogationId(DEFAULT_INTERROGATION_ID)).thenReturn(true);

        // When
        boolean exists = adapter.existsByInterrogationId(DEFAULT_INTERROGATION_ID);

        // Then
        Assertions.assertThat(exists).isTrue();
    }

    @Test
    void existsByInterrogationId_shouldReturnFalse_whenRepositoryReturnsFalse() {
        // When
        boolean exists = adapter.existsByInterrogationId("unknown-id");

        // Then
        Assertions.assertThat(exists).isFalse();
    }

    //UTILS
    private LunaticJsonRawDataModel buildModel() {
        return LunaticJsonRawDataModel.builder()
                .questionnaireId(QUESTIONNAIRE_ID)
                .interrogationId(INTERROGATION_ID)
                .mode(MODE)
                .build();
    }

    private static @NonNull LunaticJsonRawDataDocument getDocument() {
        return new LunaticJsonRawDataDocument(
                null,
                CAMPAIGN_ID,
                QUESTIONNAIRE_ID,
                INTERROGATION_ID,
                "IDUE",
                Mode.WEB,
                new HashMap<>(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}