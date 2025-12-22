package fr.insee.genesis.domain.service.rawdata;

import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitQualityToolPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


class LunaticJsonRawDataServiceUnitTest {

    static LunaticJsonRawDataService lunaticJsonRawDataService;

    static LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort;

    @BeforeEach
    void init() {
        lunaticJsonRawDataPersistencePort = mock(LunaticJsonRawDataPersistencePort.class);
        lunaticJsonRawDataService = new LunaticJsonRawDataService(
                lunaticJsonRawDataPersistencePort,
                new ControllerUtils(new FileUtils(new ConfigStub())),
                mock(QuestionnaireMetadataService.class),
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
    void getUnprocessedDataQuestionnaireIds() {
        //GIVEN
        Set<String> questionnaireIds = new HashSet<>();
        questionnaireIds.add("QUEST1");
        questionnaireIds.add("QUEST2");
        doReturn(questionnaireIds).when(lunaticJsonRawDataPersistencePort).findDistinctQuestionnaireIdsByNullProcessDate();

        //WHEN + THEN
        Assertions.assertThat(lunaticJsonRawDataService.getUnprocessedDataQuestionnaireIds())
                .containsExactlyInAnyOrder("QUEST1","QUEST2");
    }
}