package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.extraction.json.LastJsonExtractionModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.extraction.json.LastJsonExtractionDocument;
import fr.insee.genesis.infrastructure.repository.LastJsonExtractionMongoDBRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LastJsonExtractionMongoAdapter tests")
class LastJsonExtractionMongoAdapterTest {

    private static final String COLLECTION_INSTRUMENT_ID = "instrument-123";
    private static final Mode MODE = Mode.WEB;
    // Expected id format: "{collectionInstrumentId}_{mode}"
    private static final String EXPECTED_ID = "instrument-123_WEB";

    @Mock
    private LastJsonExtractionMongoDBRepository extractionRepository;

    @InjectMocks
    private LastJsonExtractionMongoAdapter adapter;

    @Nested
    @DisplayName("save() tests")
    class SaveTests {

        @Test
        @DisplayName("save() should call repository.save() with a non-null document")
        void save_shouldCallRepositorySave() {
            //GIVEN
            LastJsonExtractionModel model = buildModel();

            //WHEN
            adapter.save(model);

            //THEN
            verify(extractionRepository).save(any(LastJsonExtractionDocument.class));
            verifyNoMoreInteractions(extractionRepository);
        }

        @Test
        @DisplayName("save() should map the model to a document before persisting")
        void save_shouldMapModelToDocument() {
            //GIVEN
            LastJsonExtractionModel model = buildModel();

            //WHEN
            adapter.save(model);

            //THEN
            ArgumentCaptor<LastJsonExtractionDocument> captor = ArgumentCaptor.forClass(LastJsonExtractionDocument.class);
            verify(extractionRepository).save(captor.capture());
            assertThat(captor.getValue()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getLastExecutionDate() tests")
    class GetLastExecutionDateTests {

        @Test
        @DisplayName("Should return mapped model when document is found")
        void getLastExecutionDate_found_shouldReturnModel() throws GenesisException {
            //GIVEN
            LastJsonExtractionDocument doc = new LastJsonExtractionDocument();
            when(extractionRepository.findById(EXPECTED_ID)).thenReturn(Optional.of(doc));

            //WHEN
            LastJsonExtractionModel result = adapter.getLastExecutionDate(COLLECTION_INSTRUMENT_ID, MODE);

            //THEN
            assertThat(result).isNotNull();
            verify(extractionRepository).findById(EXPECTED_ID);
        }

        @Test
        @DisplayName("Should throw GenesisException with 404 status when document is not found")
        void getLastExecutionDate_notFound_shouldThrow404() {
            //GIVEN
            when(extractionRepository.findById(EXPECTED_ID)).thenReturn(Optional.empty());

            //WHEN + THEN
            assertThatThrownBy(() -> adapter.getLastExecutionDate(COLLECTION_INSTRUMENT_ID, MODE))
                    .isInstanceOf(GenesisException.class)
                    .satisfies(ex -> {
                        GenesisException genesisException = (GenesisException) ex;
                        assertThat(genesisException.getStatus()).isEqualTo(404);
                    });
        }

        @Test
        @DisplayName("Should include collectionInstrumentId in exception message when not found")
        void getLastExecutionDate_notFound_exceptionMessageShouldContainInstrumentId() {
            //GIVEN
            when(extractionRepository.findById(EXPECTED_ID)).thenReturn(Optional.empty());

            //WHEN + THEN
            assertThatThrownBy(() -> adapter.getLastExecutionDate(COLLECTION_INSTRUMENT_ID, MODE))
                    .isInstanceOf(GenesisException.class)
                    .hasMessageContaining(COLLECTION_INSTRUMENT_ID);
        }

        @Test
        @DisplayName("Should include mode name in exception message when not found")
        void getLastExecutionDate_notFound_exceptionMessageShouldContainModeName() {
            //GIVEN
            when(extractionRepository.findById(EXPECTED_ID)).thenReturn(Optional.empty());

            //WHEN + THEN
            assertThatThrownBy(() -> adapter.getLastExecutionDate(COLLECTION_INSTRUMENT_ID, MODE))
                    .isInstanceOf(GenesisException.class)
                    .hasMessageContaining(MODE.getModeName());
        }

        @Test
        @DisplayName("Should query repository with id formatted as '{collectionInstrumentId}_{mode}'")
        void getLastExecutionDate_shouldUseCorrectIdFormat() throws GenesisException {
            //GIVEN
            LastJsonExtractionDocument doc = new LastJsonExtractionDocument();
            when(extractionRepository.findById(EXPECTED_ID)).thenReturn(Optional.of(doc));

            //WHEN
            adapter.getLastExecutionDate(COLLECTION_INSTRUMENT_ID, MODE);

            //THEN
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(extractionRepository).findById(captor.capture());
            assertThat(captor.getValue()).isEqualTo(EXPECTED_ID);
        }

        @Test
        @DisplayName("Should handle null mode — id should be '{collectionInstrumentId}_null'")
        void getLastExecutionDate_nullMode_shouldUseNullInId() {
            //GIVEN
            String expectedIdWithNullMode = "instrument-123_null";
            when(extractionRepository.findById(expectedIdWithNullMode)).thenReturn(Optional.empty());

            //WHEN + THEN
            assertThatThrownBy(() -> adapter.getLastExecutionDate(COLLECTION_INSTRUMENT_ID, null))
                    .isInstanceOf(GenesisException.class)
                    .satisfies(ex -> assertThat(((GenesisException) ex).getStatus()).isEqualTo(404));

            verify(extractionRepository).findById(expectedIdWithNullMode);
        }

        @Test
        @DisplayName("Exception message should contain 'null' for mode name when mode is null")
        void getLastExecutionDate_nullMode_exceptionMessageShouldContainNull() {
            //GIVEN
            String expectedIdWithNullMode = "instrument-123_null";
            when(extractionRepository.findById(expectedIdWithNullMode)).thenReturn(Optional.empty());

            //WHEN + THEN
            assertThatThrownBy(() -> adapter.getLastExecutionDate(COLLECTION_INSTRUMENT_ID, null))
                    .isInstanceOf(GenesisException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("delete() tests")
    class DeleteTests {

        @Test
        @DisplayName("delete() should call repository.deleteById() with the correct id")
        void delete_shouldCallDeleteById() {
            //WHEN
            adapter.delete(COLLECTION_INSTRUMENT_ID, MODE);

            //THEN
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(extractionRepository).deleteById(captor.capture());
            assertThat(captor.getValue()).isEqualTo(EXPECTED_ID);
            verifyNoMoreInteractions(extractionRepository);
        }

        @Test
        @DisplayName("delete() with null mode should use 'null' in the id")
        void delete_nullMode_shouldUseNullInId() {
            //WHEN
            adapter.delete(COLLECTION_INSTRUMENT_ID, null);

            //THEN
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(extractionRepository).deleteById(captor.capture());
            assertThat(captor.getValue()).isEqualTo("instrument-123_null");
        }

        @Test
        @DisplayName("delete() id format should match getLastExecutionDate() id format")
        void delete_idFormatShouldMatchGetLastExecutionDate() throws GenesisException {
            //GIVEN
            // Ensure both methods build the id the same way
            LastJsonExtractionDocument doc = new LastJsonExtractionDocument();
            when(extractionRepository.findById(any())).thenReturn(Optional.of(doc));

            //WHEN
            adapter.getLastExecutionDate(COLLECTION_INSTRUMENT_ID, MODE);
            adapter.delete(COLLECTION_INSTRUMENT_ID, MODE);

            //THEN
            ArgumentCaptor<String> findCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> deleteCaptor = ArgumentCaptor.forClass(String.class);
            verify(extractionRepository).findById(findCaptor.capture());
            verify(extractionRepository).deleteById(deleteCaptor.capture());

            assertThat(findCaptor.getValue()).isEqualTo(deleteCaptor.getValue());
        }

        @Test
        @DisplayName("delete() should work for all known modes")
        void delete_shouldWorkForAllModes() {
            for (Mode mode : Mode.values()) {
                //GIVEN
                String expectedId = String.format("%s_%s", COLLECTION_INSTRUMENT_ID, mode);

                //WHEN
                adapter.delete(COLLECTION_INSTRUMENT_ID, mode);

                //THEN
                verify(extractionRepository).deleteById(expectedId);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private LastJsonExtractionModel buildModel() {
        return LastJsonExtractionModel.builder()
                .collectionInstrumentId(COLLECTION_INSTRUMENT_ID)
                .mode(MODE)
                .build();
    }
}