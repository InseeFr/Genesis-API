package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService.getValueString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LunaticJsonRawDataServiceUnitTest {

    @Mock
    private LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort;

    @Mock
    private QuestionnaireMetadataService metadataService;

    @Mock
    private DataProcessingContextService dataProcessingContextService;

    @Mock
    private ControllerUtils controllerUtils;

    @Mock
    private SurveyUnitQualityService surveyUnitQualityService;


    @Captor
    ArgumentCaptor<List<SurveyUnitModel>> listArgumentCaptor;

    private LunaticJsonRawDataService lunaticJsonRawDataService;

    @BeforeEach
    void init() {
        lunaticJsonRawDataService = new LunaticJsonRawDataService(
                lunaticJsonRawDataPersistencePort,
                new ControllerUtils(new FileUtils(TestConstants.getConfigStub())),
                metadataService,
                mock(SurveyUnitService.class),
                surveyUnitQualityService,
                new FileUtils(TestConstants.getConfigStub()),
                dataProcessingContextService,
                mock(SurveyUnitQualityToolPort.class),
                TestConstants.getConfigStub(),
                mock(DataProcessingContextPersistancePort.class)
        );
    }

    @Test
    @SneakyThrows
    void save_test() {
        //GIVEN
        LunaticJsonRawDataModel lunaticJsonRawDataModel = LunaticJsonRawDataModel.builder().build();

        //WHEN
        lunaticJsonRawDataService.save(lunaticJsonRawDataModel);

        //THEN
        verify(lunaticJsonRawDataPersistencePort, times(1))
                .save(lunaticJsonRawDataModel);
    }

    @Test
    @SneakyThrows
    void getRawDataByInterrogationId_test() {
        //GIVEN
        String interrogationId = "test";
        LunaticJsonRawDataModel lunaticJsonRawDataModel = LunaticJsonRawDataModel.builder().build();

        doReturn(List.of(lunaticJsonRawDataModel))
                .when(lunaticJsonRawDataPersistencePort)
                .findRawDataByInterrogationId(any());

        //WHEN
        List<LunaticJsonRawDataModel> lunaticJsonRawDataModelList = lunaticJsonRawDataService
                .getRawDataByInterrogationId(interrogationId);

        //THEN
        verify(lunaticJsonRawDataPersistencePort, times(1))
                .findRawDataByInterrogationId(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataModelList).containsExactly(lunaticJsonRawDataModel);
    }
    @Test
    @SneakyThrows
    void processRawData_test(){
        //GIVEN
        String questionnaireId = "test";
        String interrogationId = "testInterrogation";
        DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                .withReview(true)
                .build();
        doReturn(dataProcessingContextModel).when(dataProcessingContextService)
                .getContextByCollectionInstrumentId(any());
        doReturn(List.of(Mode.WEB)).when(controllerUtils).getModesList(anyString(), any());
        Set<String> interrogationIds = Set.of(interrogationId);
        doReturn(interrogationIds).when(lunaticJsonRawDataPersistencePort)
                .findUnprocessedInterrogationIdsByCollectionInstrumentId(any());

        //TODO
        doReturn(List.of(lunaticJsonRawDataModel)).when(lunaticJsonRawDataPersistencePort).findRawDataByQuestionnaireId(any(), any(),
                any());

        //WHEN
        lunaticJsonRawDataService.processRawData(questionnaireId);

        //THEN
        verify(surveyUnitQualityService, times(1))
                .verifySurveyUnits(listArgumentCaptor.capture(), any());
        Assertions.assertThat(listArgumentCaptor.getValue()).isNotNull().hasSize(1);

        List<SurveyUnitModel> surveyUnitModels = listArgumentCaptor.getValue();
        Assertions.assertThat(surveyUnitModels).hasSize(1);

        SurveyUnitModel surveyUnitModel = surveyUnitModels.getFirst();
        Assertions.assertThat(surveyUnitModel.getCollectionInstrumentId()).isEqualTo(questionnaireId);
        Assertions.assertThat(surveyUnitModel.getInterrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(surveyUnitModel.getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitModel.getMode()).isEqualTo(Mode.WEB);

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

    @Test
    @SneakyThrows
    void findRawDataByQuestionnaireId_test() {
        //GIVEN
        String questionnaireId = "test";
        LunaticJsonRawDataModel lunaticJsonRawDataModel = LunaticJsonRawDataModel.builder().build();
        Pageable pageable = PageRequest.of(0, 10);

        doReturn(new PageImpl<>(List.of(lunaticJsonRawDataModel), pageable, 1))
                .when(lunaticJsonRawDataPersistencePort)
                .findRawDataByQuestionnaireId(any(), any());

        //WHEN
        Page<LunaticJsonRawDataModel> page = lunaticJsonRawDataService
                .findRawDataByQuestionnaireId(questionnaireId, pageable);

        //THEN
        verify(lunaticJsonRawDataPersistencePort, times(1))
                .findRawDataByQuestionnaireId(questionnaireId, pageable);
        Assertions.assertThat(page.getTotalElements()).isEqualTo(1);
        Optional<LunaticJsonRawDataModel> lunaticJsonRawDataModelOptional = page.get().findFirst();
        Assertions.assertThat(lunaticJsonRawDataModelOptional).isPresent().contains(lunaticJsonRawDataModel);
    }
}