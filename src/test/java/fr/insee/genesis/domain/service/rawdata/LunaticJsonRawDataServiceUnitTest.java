package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.TestConstants;
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
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.Set;

import static fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService.getValueString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LunaticJsonRawDataServiceUnitTest {

    @Mock
    static LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort;

    @Mock
    static QuestionnaireMetadataService metadataService;

    static LunaticJsonRawDataService lunaticJsonRawDataService;

    @BeforeEach
    void init() {
        lunaticJsonRawDataService = new LunaticJsonRawDataService(
                lunaticJsonRawDataPersistencePort,
                new ControllerUtils(new FileUtils(TestConstants.getConfigStub())),
                metadataService,
                mock(SurveyUnitService.class),
                mock(SurveyUnitQualityService.class),
                new FileUtils(TestConstants.getConfigStub()),
                mock(DataProcessingContextService.class),
                mock(SurveyUnitQualityToolPort.class),
                TestConstants.getConfigStub(),
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
                new ControllerUtils(new FileUtils(TestConstants.getConfigStub())),
                metadataService,
                mock(SurveyUnitService.class),
                mock(SurveyUnitQualityService.class),
                new FileUtils(TestConstants.getConfigStub()),
                mock(DataProcessingContextService.class),
                mock(SurveyUnitQualityToolPort.class),
                TestConstants.getConfigStub(),
                mock(DataProcessingContextPersistancePort.class)
        );


        //WHEN + THEN
        Assertions.assertThat(lunaticJsonRawDataService.getUnprocessedDataQuestionnaireIds())
                .containsExactly("TEST-TABLEAUX");
    }

    @Test
    void getValueString_null_test(){
        Object stringObject = null;

        Assertions.assertThat(getValueString(stringObject)).isEqualTo("null");
    }

    @Test
    void getValueString_string_test(){
        Object stringObject = "test";

        Assertions.assertThat(getValueString(stringObject)).isEqualTo("test");
    }
    @Test
    void getValueString_int_test(){
        Object intObject = 10;

        Assertions.assertThat(getValueString(intObject)).isEqualTo("10");
    }

    @Test
    void getValueString_float_test(){
        Object floatObject = 10.111f;

        Assertions.assertThat(getValueString(floatObject)).isEqualTo("10.111");
    }

    @Test
    void getValueString_double_test(){
        Object doubleObject = 101010101010.111d;

        Assertions.assertThat(getValueString(doubleObject)).isEqualTo("101010101010.111");
    }
}