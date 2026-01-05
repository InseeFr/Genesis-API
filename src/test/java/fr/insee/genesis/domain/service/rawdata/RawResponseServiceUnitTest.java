package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.spi.QuestionnaireMetadataPersistencePort;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitQualityToolPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class RawResponseServiceUnitTest {
    static RawResponseService rawResponseService;

    static RawResponsePersistencePort rawResponsePersistencePort;

    static QuestionnaireMetadataService metadataService;

    @BeforeEach
    void init() {
        rawResponsePersistencePort = mock(RawResponsePersistencePort.class);
        metadataService = mock(QuestionnaireMetadataService.class);
        rawResponseService = new RawResponseService(
                new ControllerUtils(new FileUtils(new ConfigStub())),
                metadataService,
                mock(SurveyUnitService.class),
                mock(SurveyUnitQualityService.class),
                mock(SurveyUnitQualityToolPort.class),
                mock(DataProcessingContextService.class),
                new FileUtils(new ConfigStub()),
                new ConfigStub(),
                rawResponsePersistencePort
        );
    }

    @Test
    @SneakyThrows
    void getUnprocessedCollectionInstrumentIds_test() {
        //GIVEN
        List<String> collectionInstrumentIds = new ArrayList<>();
        collectionInstrumentIds.add("QUEST1");
        collectionInstrumentIds.add("QUEST2");
        doReturn(collectionInstrumentIds).when(rawResponsePersistencePort).getUnprocessedCollectionIds();
        doReturn(List.of(Mode.WEB)).when(rawResponsePersistencePort).findModesByCollectionInstrument(any());
        doReturn(new MetadataModel()).when(metadataService).loadAndSaveIfNotExists(any(), any(), any(), any(), any());


        //WHEN + THEN
        Assertions.assertThat(rawResponseService.getUnprocessedCollectionInstrumentIds())
                .containsExactlyInAnyOrder("QUEST1","QUEST2");
    }

    @Test
    @SneakyThrows
    void getUnprocessedCollectionInstrumentIds_shouldnt_return_if_no_spec() {
        //GIVEN
        List<String> questionnaireIds = new ArrayList<>();
        questionnaireIds.add("QUEST1"); //No spec
        questionnaireIds.add("TEST-TABLEAUX");
        doReturn(questionnaireIds).when(rawResponsePersistencePort).getUnprocessedCollectionIds();
        doReturn(List.of(Mode.WEB)).when(rawResponsePersistencePort).findModesByCollectionInstrument(any());
        //No mock for metadataservice this time
        metadataService = new QuestionnaireMetadataService(
                mock(QuestionnaireMetadataPersistencePort.class)
        );
        rawResponseService = new RawResponseService(
                new ControllerUtils(new FileUtils(new ConfigStub())),
                metadataService,
                mock(SurveyUnitService.class),
                mock(SurveyUnitQualityService.class),
                mock(SurveyUnitQualityToolPort.class),
                mock(DataProcessingContextService.class),
                new FileUtils(new ConfigStub()),
                new ConfigStub(),
                rawResponsePersistencePort
        );


        //WHEN + THEN
        Assertions.assertThat(rawResponseService.getUnprocessedCollectionInstrumentIds())
                .containsExactly("TEST-TABLEAUX");
    }
}