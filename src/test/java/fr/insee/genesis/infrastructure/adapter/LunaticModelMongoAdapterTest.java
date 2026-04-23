package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.infrastructure.document.lunaticmodel.LunaticModelDocument;
import fr.insee.genesis.infrastructure.repository.LunaticModelMongoDBRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LunaticModelMongoAdapter tests")
class LunaticModelMongoAdapterTest {

    private static final String COLLECTION_INSTRUMENT_ID = "instrument-123";

    @Mock
    private LunaticModelMongoDBRepository repository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private LunaticModelMongoAdapter adapter;

    @Nested
    @DisplayName("save() tests")
    class SaveTests {

        // save() relies on a MongoTemplate fluent chain:
        // mongoTemplate.update(...).matching(...).apply(...).upsert()
        // We use a dedicated adapter with RETURNS_DEEP_STUBS to stub the chain.

        private MongoTemplate deepStubMongoTemplate;
        private LunaticModelMongoAdapter localAdapter;

        @BeforeEach
        void setUp() {
            deepStubMongoTemplate = mock(MongoTemplate.class, Answers.RETURNS_DEEP_STUBS);
            localAdapter = new LunaticModelMongoAdapter(repository, deepStubMongoTemplate);
        }

        @Test
        @DisplayName("save() should call mongoTemplate fluent update chain and upsert")
        void save_shouldCallUpsertOnMongoTemplate() {
            // GIVEN
            LunaticModelModel model = buildModel();

            // WHEN
            localAdapter.save(model);

            // THEN
            verify(deepStubMongoTemplate).update(LunaticModelDocument.class);
        }

        @Test
        @DisplayName("save() should not interact with the repository")
        void save_shouldNotTouchRepository() {
            // GIVEN
            LunaticModelModel model = buildModel();

            // WHEN
            localAdapter.save(model);

            // THEN
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("save() should work without throwing for a valid model")
        void save_validModel_shouldNotThrow() {
            // GIVEN
            LunaticModelModel model = buildModel();

            // WHEN + THEN
            org.assertj.core.api.Assertions.assertThatCode(() -> localAdapter.save(model))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("find() tests")
    class FindTests {

        @Test
        @DisplayName("Should call both repository methods and merge results")
        void find_shouldCallBothRepositoryMethods() {
            // GIVEN
            LunaticModelDocument doc1 = buildDoc("doc-1");
            LunaticModelDocument doc2 = buildDoc("doc-2");
            when(repository.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID)).thenReturn(List.of(doc1));
            when(repository.findByQuestionnaireId(COLLECTION_INSTRUMENT_ID)).thenReturn(List.of(doc2));

            // WHEN
            List<LunaticModelDocument> result = adapter.find(COLLECTION_INSTRUMENT_ID);

            // THEN
            verify(repository).findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);
            verify(repository).findByQuestionnaireId(COLLECTION_INSTRUMENT_ID);
            assertThat(result).containsExactlyInAnyOrder(doc1, doc2);
        }

        @Test
        @DisplayName("Should return empty list when both repository methods return empty lists")
        void find_bothEmpty_shouldReturnEmptyList() {
            // GIVEN
            when(repository.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID)).thenReturn(List.of());
            when(repository.findByQuestionnaireId(COLLECTION_INSTRUMENT_ID)).thenReturn(List.of());

            // WHEN
            List<LunaticModelDocument> result = adapter.find(COLLECTION_INSTRUMENT_ID);

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return only documents from findByCollectionInstrumentId when findByQuestionnaireId returns empty")
        void find_onlyCollectionInstrumentIdResults_shouldReturnThoseOnly() {
            // GIVEN
            LunaticModelDocument doc = buildDoc("doc-1");
            when(repository.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID)).thenReturn(List.of(doc));
            when(repository.findByQuestionnaireId(COLLECTION_INSTRUMENT_ID)).thenReturn(List.of());

            // WHEN
            List<LunaticModelDocument> result = adapter.find(COLLECTION_INSTRUMENT_ID);

            // THEN
            assertThat(result).containsExactly(doc);
        }

        @Test
        @DisplayName("Should return only documents from findByQuestionnaireId when findByCollectionInstrumentId returns empty")
        void find_onlyQuestionnaireIdResults_shouldReturnThoseOnly() {
            // GIVEN
            LunaticModelDocument doc = buildDoc("doc-1");
            when(repository.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID)).thenReturn(List.of());
            when(repository.findByQuestionnaireId(COLLECTION_INSTRUMENT_ID)).thenReturn(List.of(doc));

            // WHEN
            List<LunaticModelDocument> result = adapter.find(COLLECTION_INSTRUMENT_ID);

            // THEN
            assertThat(result).containsExactly(doc);
        }

        @Test
        @DisplayName("Should deduplicate when the same document is returned by both repository methods")
        void find_sameDocumentFromBothMethods_shouldDeduplicate() {
            // GIVEN
            LunaticModelDocument doc = buildDoc("doc-shared");
            when(repository.findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID)).thenReturn(List.of(doc));
            when(repository.findByQuestionnaireId(COLLECTION_INSTRUMENT_ID)).thenReturn(List.of(doc));

            // WHEN
            List<LunaticModelDocument> result = adapter.find(COLLECTION_INSTRUMENT_ID);

            // THEN
            assertThat(result).hasSize(1).containsExactly(doc);
        }

        @Test
        @DisplayName("Should pass the same collectionInstrumentId to both repository methods")
        void find_shouldPassSameIdToBothMethods() {
            // GIVEN
            when(repository.findByCollectionInstrumentId(any())).thenReturn(List.of());
            when(repository.findByQuestionnaireId(any())).thenReturn(List.of());

            // WHEN
            adapter.find(COLLECTION_INSTRUMENT_ID);

            // THEN
            verify(repository).findByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);
            verify(repository).findByQuestionnaireId(COLLECTION_INSTRUMENT_ID);
        }

        @Test
        @DisplayName("Should not interact with mongoTemplate")
        void find_shouldNotTouchMongoTemplate() {
            // GIVEN
            when(repository.findByCollectionInstrumentId(any())).thenReturn(List.of());
            when(repository.findByQuestionnaireId(any())).thenReturn(List.of());

            // WHEN
            adapter.find(COLLECTION_INSTRUMENT_ID);

            // THEN
            verifyNoInteractions(mongoTemplate);
        }
    }

    //UTILS
    private LunaticModelModel buildModel() {
        return new LunaticModelModel(LunaticModelMongoAdapterTest.COLLECTION_INSTRUMENT_ID, null, LocalDateTime.now());
    }

    private LunaticModelDocument buildDoc(String id) {
        return new LunaticModelDocument(
                null,
                id,
                new HashMap<>(),
                LocalDateTime.now()
        );
    }
}