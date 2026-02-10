package fr.insee.genesis.domain.service.metadata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.spi.QuestionnaireMetadataPersistencePort;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class QuestionnaireMetadataServiceTest {
    QuestionnaireMetadataPersistencePort questionnaireMetadataPersistencePort;
    QuestionnaireMetadataService questionnaireMetadataService;

    @Captor
    ArgumentCaptor<QuestionnaireMetadataModel> questionnaireMetadataModelArgumentCaptor;

    @BeforeEach
    void setUp() {
        questionnaireMetadataPersistencePort = mock(QuestionnaireMetadataPersistencePort.class);
        questionnaireMetadataService = new QuestionnaireMetadataService(
                questionnaireMetadataPersistencePort
        );

        questionnaireMetadataModelArgumentCaptor = ArgumentCaptor.forClass(QuestionnaireMetadataModel.class);
    }

    @Test
    @SneakyThrows
    void find_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        Mode mode = Mode.WEB;
        MetadataModel metadataModel = new MetadataModel();
        QuestionnaireMetadataModel questionnaireMetadataModel = QuestionnaireMetadataModel.builder()
                .metadataModel(metadataModel)
                .build();
        doReturn(List.of(questionnaireMetadataModel)).when(questionnaireMetadataPersistencePort).find(
                any(), any()
        );

        //WHEN
        MetadataModel actual = questionnaireMetadataService.find(collectionInstrumentId, mode);

        //THEN
        verify(questionnaireMetadataPersistencePort, times(1))
                .find(collectionInstrumentId, mode);
        Assertions.assertThat(actual).isEqualTo(metadataModel);
    }

    @Test
    void find_not_found_test() {
        //GIVEN
        doReturn(new ArrayList<>()).when(questionnaireMetadataPersistencePort).find(
                any(), any()
        );

        try{
            //WHEN
            questionnaireMetadataService.find("test", Mode.WEB);
            Assertions.fail();
        }catch (GenesisException ge){
            //THEN
            Assertions.assertThat(ge.getStatus()).isEqualTo(404);
        }
    }

    @Test
    @SneakyThrows
    void loadAndSaveIfNotExists_not_exists_test() {
        //GIVEN
        String collectionInstrumentId = TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID;
        Mode mode = Mode.WEB;
        doReturn(new ArrayList<>()).when(questionnaireMetadataPersistencePort).find(
                any(), any()
        );
        FileUtils fileUtils = new FileUtils(TestConstants.getConfigStub());
        ArrayList<GenesisError> errors = new ArrayList<>();

        //WHEN
        MetadataModel actual = questionnaireMetadataService.loadAndSaveIfNotExists(null,
                collectionInstrumentId,
                mode,
                fileUtils,
                errors
        );

        //THEN
        verify(questionnaireMetadataPersistencePort, times(1))
                .find(collectionInstrumentId.toUpperCase(), mode);
        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.getVariables()).isNotNull();
        Assertions.assertThat(actual.getVariables().getVariables()).isNotNull().isNotEmpty();
        verify(questionnaireMetadataPersistencePort, times(1))
                .save(questionnaireMetadataModelArgumentCaptor.capture());
        Assertions.assertThat(questionnaireMetadataModelArgumentCaptor.getValue()).isNotNull();
        Assertions.assertThat(questionnaireMetadataModelArgumentCaptor.getValue().collectionInstrumentId())
                .isEqualTo(collectionInstrumentId);
        Assertions.assertThat(questionnaireMetadataModelArgumentCaptor.getValue().mode())
                .isEqualTo(mode);
        Assertions.assertThat(questionnaireMetadataModelArgumentCaptor.getValue().metadataModel())
                .isEqualTo(actual);
        Assertions.assertThat(errors).isEmpty();
    }

    @Test
    @SneakyThrows
    void loadAndSaveIfNotExists_already_exists_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        Mode mode = Mode.WEB;
        MetadataModel metadataModel = new MetadataModel();
        metadataModel.getVariables().putVariable(new Variable(
                "testVar",
                metadataModel.getRootGroup(),
                VariableType.STRING
        ));
        QuestionnaireMetadataModel questionnaireMetadataModel = QuestionnaireMetadataModel.builder()
                .metadataModel(metadataModel)
                .build();
        doReturn(List.of(questionnaireMetadataModel)).when(questionnaireMetadataPersistencePort).find(
                any(), any()
        );
        ArrayList<GenesisError> errors = new ArrayList<>();

        //WHEN
        MetadataModel actual = questionnaireMetadataService.loadAndSaveIfNotExists(null,
                collectionInstrumentId,
                mode,
                mock(FileUtils.class),
                new ArrayList<>()
        );

        //THEN
        verify(questionnaireMetadataPersistencePort, times(1))
                .find(collectionInstrumentId.toUpperCase(), mode);
        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.getVariables()).isNotNull();
        Assertions.assertThat(actual.getVariables().getVariables()).isNotNull().isNotEmpty();
        verify(questionnaireMetadataPersistencePort, never())
                .save(any());
        Assertions.assertThat(errors).isEmpty();
    }

    @Test
    void remove_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        Mode mode = Mode.WEB;

        //WHEN
        questionnaireMetadataService.remove(collectionInstrumentId, mode);

        //THEN
        verify(questionnaireMetadataPersistencePort, times(1))
                .remove(collectionInstrumentId, mode);
    }

    @Test
    void save_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        Mode mode = Mode.WEB;
        MetadataModel metadataModel = new MetadataModel();

        //WHEN
        questionnaireMetadataService.save(collectionInstrumentId, mode, metadataModel);

        //THEN
        verify(questionnaireMetadataPersistencePort, times(1))
                .save(questionnaireMetadataModelArgumentCaptor.capture());
        Assertions.assertThat(questionnaireMetadataModelArgumentCaptor.getValue()).isNotNull();
        Assertions.assertThat(questionnaireMetadataModelArgumentCaptor.getValue().collectionInstrumentId())
                .isEqualTo(collectionInstrumentId);
        Assertions.assertThat(questionnaireMetadataModelArgumentCaptor.getValue().mode())
                .isEqualTo(mode);
        Assertions.assertThat(questionnaireMetadataModelArgumentCaptor.getValue().metadataModel())
                .isEqualTo(metadataModel);
    }
}