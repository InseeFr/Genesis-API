package fr.insee.genesis.infrastructure.adapter;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.infrastructure.document.metadata.QuestionnaireMetadataDocument;
import fr.insee.genesis.infrastructure.repository.QuestionnaireMetadataMongoDBRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionnaireMetadataMongoAdapter tests")
class QuestionnaireMetadataMongoAdapterTest {

    private static final String COLLECTION_INSTRUMENT_ID = "instrument-123";
    private static final Mode MODE = Mode.WEB;

    @Mock
    private QuestionnaireMetadataMongoDBRepository questionnaireMetadataMongoDBRepository;

    @InjectMocks
    private QuestionnaireMetadataMongoAdapter adapter;

    @Nested
    @DisplayName("find() tests")
    class FindTests {

        @Test
        @DisplayName("Should call both repository methods and return merged mapped models")
        void find_shouldCallBothRepositoryMethodsAndMergeResults() {
            // GIVEN
            QuestionnaireMetadataDocument doc1 = getDocument();
            QuestionnaireMetadataDocument doc2 = getDocument();
            when(questionnaireMetadataMongoDBRepository.findByQuestionnaireIdAndMode(COLLECTION_INSTRUMENT_ID, MODE))
                    .thenReturn(List.of(doc1));
            when(questionnaireMetadataMongoDBRepository.findByCollectionInstrumentIdAndMode(COLLECTION_INSTRUMENT_ID, MODE))
                    .thenReturn(List.of(doc2));

            // WHEN
            List<QuestionnaireMetadataModel> result = adapter.find(COLLECTION_INSTRUMENT_ID, MODE);

            // THEN
            verify(questionnaireMetadataMongoDBRepository).findByQuestionnaireIdAndMode(COLLECTION_INSTRUMENT_ID, MODE);
            verify(questionnaireMetadataMongoDBRepository).findByCollectionInstrumentIdAndMode(COLLECTION_INSTRUMENT_ID, MODE);
            assertThat(result).isNotNull().hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when both repository methods return empty lists")
        void find_bothEmpty_shouldReturnEmptyList() {
            // GIVEN
            when(questionnaireMetadataMongoDBRepository.findByQuestionnaireIdAndMode(COLLECTION_INSTRUMENT_ID, MODE))
                    .thenReturn(List.of());
            when(questionnaireMetadataMongoDBRepository.findByCollectionInstrumentIdAndMode(COLLECTION_INSTRUMENT_ID, MODE))
                    .thenReturn(List.of());

            // WHEN
            List<QuestionnaireMetadataModel> result = adapter.find(COLLECTION_INSTRUMENT_ID, MODE);

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return only results from findByQuestionnaireIdAndMode when findByCollectionInstrumentIdAndMode is empty")
        void find_onlyQuestionnaireIdResults_shouldReturnThoseOnly() {
            // GIVEN
            when(questionnaireMetadataMongoDBRepository.findByQuestionnaireIdAndMode(COLLECTION_INSTRUMENT_ID, MODE))
                    .thenReturn(List.of(getDocument()));
            when(questionnaireMetadataMongoDBRepository.findByCollectionInstrumentIdAndMode(COLLECTION_INSTRUMENT_ID, MODE))
                    .thenReturn(List.of());

            // WHEN
            List<QuestionnaireMetadataModel> result = adapter.find(COLLECTION_INSTRUMENT_ID, MODE);

            // THEN
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return only results from findByCollectionInstrumentIdAndMode when findByQuestionnaireIdAndMode is empty")
        void find_onlyCollectionInstrumentIdResults_shouldReturnThoseOnly() {
            // GIVEN
            when(questionnaireMetadataMongoDBRepository.findByQuestionnaireIdAndMode(COLLECTION_INSTRUMENT_ID, MODE))
                    .thenReturn(List.of());
            when(questionnaireMetadataMongoDBRepository.findByCollectionInstrumentIdAndMode(COLLECTION_INSTRUMENT_ID, MODE))
                    .thenReturn(List.of(getDocument()));

            // WHEN
            List<QuestionnaireMetadataModel> result = adapter.find(COLLECTION_INSTRUMENT_ID, MODE);

            // THEN
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should pass the same collectionInstrumentId and mode to both repository methods")
        void find_shouldPassSameArgumentsToBothMethods() {
            // GIVEN
            when(questionnaireMetadataMongoDBRepository.findByQuestionnaireIdAndMode(any(), any()))
                    .thenReturn(List.of());
            when(questionnaireMetadataMongoDBRepository.findByCollectionInstrumentIdAndMode(any(), any()))
                    .thenReturn(List.of());

            // WHEN
            adapter.find(COLLECTION_INSTRUMENT_ID, MODE);

            // THEN
            verify(questionnaireMetadataMongoDBRepository).findByQuestionnaireIdAndMode(COLLECTION_INSTRUMENT_ID, MODE);
            verify(questionnaireMetadataMongoDBRepository).findByCollectionInstrumentIdAndMode(COLLECTION_INSTRUMENT_ID, MODE);
        }
    }

    @Nested
    @DisplayName("save() tests")
    class SaveTests {

        @Test
        @DisplayName("save() should call remove() then repository.save() with a mapped document")
        void save_shouldRemoveThenPersist() {
            // GIVEN
            QuestionnaireMetadataModel model = buildModel();

            // WHEN
            adapter.save(model);

            // THEN
            verify(questionnaireMetadataMongoDBRepository).deleteByQuestionnaireIdAndMode(COLLECTION_INSTRUMENT_ID, MODE);
            verify(questionnaireMetadataMongoDBRepository).deleteByCollectionInstrumentIdIdAndMode(COLLECTION_INSTRUMENT_ID, MODE);
            verify(questionnaireMetadataMongoDBRepository).save(any(QuestionnaireMetadataDocument.class));
        }

        @Test
        @DisplayName("save() should persist a non-null document")
        void save_shouldPersistNonNullDocument() {
            // GIVEN
            QuestionnaireMetadataModel model = buildModel();

            // WHEN
            adapter.save(model);

            // THEN
            ArgumentCaptor<QuestionnaireMetadataDocument> captor =
                    ArgumentCaptor.forClass(QuestionnaireMetadataDocument.class);
            verify(questionnaireMetadataMongoDBRepository).save(captor.capture());
            assertThat(captor.getValue()).isNotNull();
        }

        @Test
        @DisplayName("save() should delete before inserting — delete is called before save")
        void save_deleteShouldBeCalledBeforeSave() {
            // GIVEN
            QuestionnaireMetadataModel model = buildModel();

            // WHEN
            adapter.save(model);

            // THEN
            org.mockito.InOrder inOrder = inOrder(questionnaireMetadataMongoDBRepository);
            inOrder.verify(questionnaireMetadataMongoDBRepository).deleteByQuestionnaireIdAndMode(COLLECTION_INSTRUMENT_ID, MODE);
            inOrder.verify(questionnaireMetadataMongoDBRepository).deleteByCollectionInstrumentIdIdAndMode(COLLECTION_INSTRUMENT_ID, MODE);
            inOrder.verify(questionnaireMetadataMongoDBRepository).save(any());
        }
    }

    @Nested
    @DisplayName("remove() tests")
    class RemoveTests {

        @Test
        @DisplayName("remove() should call both delete methods on the repository")
        void remove_shouldCallBothDeleteMethods() {
            // WHEN
            adapter.remove(COLLECTION_INSTRUMENT_ID, MODE);

            // THEN
            verify(questionnaireMetadataMongoDBRepository).deleteByQuestionnaireIdAndMode(COLLECTION_INSTRUMENT_ID, MODE);
            verify(questionnaireMetadataMongoDBRepository).deleteByCollectionInstrumentIdIdAndMode(COLLECTION_INSTRUMENT_ID, MODE);
            verifyNoMoreInteractions(questionnaireMetadataMongoDBRepository);
        }

        @Test
        @DisplayName("remove() should pass the same collectionInstrumentId and mode to both delete methods")
        void remove_shouldPassSameArgumentsToBothMethods() {
            // WHEN
            adapter.remove(COLLECTION_INSTRUMENT_ID, MODE);

            // THEN
            ArgumentCaptor<String> idCaptor1 = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> idCaptor2 = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Mode> modeCaptor1 = ArgumentCaptor.forClass(Mode.class);
            ArgumentCaptor<Mode> modeCaptor2 = ArgumentCaptor.forClass(Mode.class);

            verify(questionnaireMetadataMongoDBRepository)
                    .deleteByQuestionnaireIdAndMode(idCaptor1.capture(), modeCaptor1.capture());
            verify(questionnaireMetadataMongoDBRepository)
                    .deleteByCollectionInstrumentIdIdAndMode(idCaptor2.capture(), modeCaptor2.capture());

            assertThat(idCaptor1.getValue()).isEqualTo(COLLECTION_INSTRUMENT_ID);
            assertThat(idCaptor2.getValue()).isEqualTo(COLLECTION_INSTRUMENT_ID);
            assertThat(modeCaptor1.getValue()).isEqualTo(MODE);
            assertThat(modeCaptor2.getValue()).isEqualTo(MODE);
        }

        @Test
        @DisplayName("remove() should work for all known modes")
        void remove_shouldWorkForAllModes() {
            // WHEN / THEN
            for (Mode mode : Mode.values()) {
                adapter.remove(COLLECTION_INSTRUMENT_ID, mode);
                verify(questionnaireMetadataMongoDBRepository).deleteByQuestionnaireIdAndMode(COLLECTION_INSTRUMENT_ID, mode);
                verify(questionnaireMetadataMongoDBRepository).deleteByCollectionInstrumentIdIdAndMode(COLLECTION_INSTRUMENT_ID, mode);
            }
        }
    }

    // UTILS
    private QuestionnaireMetadataModel buildModel() {
        return new QuestionnaireMetadataModel(COLLECTION_INSTRUMENT_ID, MODE, null);
    }

    private static @NonNull QuestionnaireMetadataDocument getDocument() {
        return new QuestionnaireMetadataDocument(
                null,
                COLLECTION_INSTRUMENT_ID,
                Mode.WEB,
                new MetadataModel()
        );
    }
}