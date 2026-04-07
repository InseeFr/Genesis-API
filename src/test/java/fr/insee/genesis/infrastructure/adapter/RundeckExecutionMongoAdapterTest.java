package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.rundeck.RundeckExecution;
import fr.insee.genesis.infrastructure.document.rundeck.RundeckExecutionDocument;
import fr.insee.genesis.infrastructure.repository.RundeckExecutionDBRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RundeckExecutionMongoAdapterTest {

    @Mock
    private RundeckExecutionDBRepository rundeckExecutionDBRepository;

    @InjectMocks
    private RundeckExecutionMongoAdapter adapter;

    @Nested
    @DisplayName("save() tests")
    class SaveTests {

        @Test
        @DisplayName("save() should call repository.insert() with a non-null document")
        void save_shouldCallRepositoryInsertWithNonNullDocument() {
            // GIVEN
            RundeckExecution execution = buildExecution();

            // WHEN
            adapter.save(execution);

            // THEN
            ArgumentCaptor<RundeckExecutionDocument> captor = ArgumentCaptor.forClass(RundeckExecutionDocument.class);
            verify(rundeckExecutionDBRepository).insert(captor.capture());
            assertThat(captor.getValue()).isNotNull();
        }

        @Test
        @DisplayName("save() should use insert() and NOT save() to preserve insert-only semantics")
        void save_shouldUseInsertNotSave() {
            // GIVEN
            RundeckExecution execution = buildExecution();

            // WHEN
            adapter.save(execution);

            // THEN
            verify(rundeckExecutionDBRepository, never()).save(any());
            verify(rundeckExecutionDBRepository).insert(any(RundeckExecutionDocument.class));
        }

        @Test
        @DisplayName("save() should interact with repository exactly once")
        void save_shouldInteractWithRepositoryExactlyOnce() {
            // GIVEN
            RundeckExecution execution = buildExecution();

            // WHEN
            adapter.save(execution);

            // THEN
            verify(rundeckExecutionDBRepository, times(1)).insert(any(RundeckExecutionDocument.class));
            verifyNoMoreInteractions(rundeckExecutionDBRepository);
        }
    }

    //UTILS
    private RundeckExecution buildExecution() {
        return new RundeckExecution();
    }
}