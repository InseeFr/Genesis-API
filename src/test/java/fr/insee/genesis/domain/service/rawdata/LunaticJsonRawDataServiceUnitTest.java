package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.domain.ports.spi.QuestionnaireMetadataPersistencePort;
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

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


class LunaticJsonRawDataServiceUnitTest {

    static LunaticJsonRawDataService lunaticJsonRawDataService;

    static LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort;

    static QuestionnaireMetadataService metadataService;

    @BeforeEach
    void init() {
        lunaticJsonRawDataPersistencePort = mock(LunaticJsonRawDataPersistencePort.class);
        metadataService = mock(QuestionnaireMetadataService.class);
        lunaticJsonRawDataService = new LunaticJsonRawDataService(
                lunaticJsonRawDataPersistencePort,
                new ControllerUtils(new FileUtils(new ConfigStub())),
                metadataService,
                mock(SurveyUnitService.class),
                mock(SurveyUnitQualityService.class),
                new FileUtils(new ConfigStub()),
                mock(DataProcessingContextService.class),
                mock(SurveyUnitQualityToolPort.class),
                new ConfigStub(),
                mock(DataProcessingContextPersistancePort.class)
        );
    }

    @Test
    @SneakyThrows
    void getUnprocessedDataQuestionnaireIds() {
        //GIVEN
        Set<String> questionnaireIds = new HashSet<>();
        questionnaireIds.add("QUEST1");
        questionnaireIds.add("QUEST2");
        doReturn(questionnaireIds).when(lunaticJsonRawDataPersistencePort).findDistinctQuestionnaireIdsByNullProcessDate();
        doReturn(Set.of(Mode.WEB)).when(lunaticJsonRawDataPersistencePort).findModesByQuestionnaire(any());
        doReturn(new MetadataModel()).when(metadataService).loadAndSaveIfNotExists(any(), any(), any(), any(), any());


        //WHEN + THEN
        Assertions.assertThat(lunaticJsonRawDataService.getUnprocessedDataQuestionnaireIds())
                .containsExactlyInAnyOrder("QUEST1","QUEST2");
    }

    @Test
    @SneakyThrows
    void getUnprocessedDataQuestionnaireIds_shouldnt_return_if_no_spec() {
        //GIVEN
        Set<String> questionnaireIds = new HashSet<>();
        questionnaireIds.add("QUEST1"); //No spec
        questionnaireIds.add("TEST-TABLEAUX");
        doReturn(questionnaireIds).when(lunaticJsonRawDataPersistencePort).findDistinctQuestionnaireIdsByNullProcessDate();
        doReturn(Set.of(Mode.WEB)).when(lunaticJsonRawDataPersistencePort).findModesByQuestionnaire(any());
        //No mock for metadataservice this time
        metadataService = new QuestionnaireMetadataService(
                mock(QuestionnaireMetadataPersistencePort.class)
        );
        lunaticJsonRawDataService = new LunaticJsonRawDataService(
                lunaticJsonRawDataPersistencePort,
                new ControllerUtils(new FileUtils(new ConfigStub())),
                metadataService,
                mock(SurveyUnitService.class),
                mock(SurveyUnitQualityService.class),
                new FileUtils(new ConfigStub()),
                mock(DataProcessingContextService.class),
                mock(SurveyUnitQualityToolPort.class),
                new ConfigStub(),
                mock(DataProcessingContextPersistancePort.class)
        );


        //WHEN + THEN
        Assertions.assertThat(lunaticJsonRawDataService.getUnprocessedDataQuestionnaireIds())
                .containsExactly("TEST-TABLEAUX");
    }
}