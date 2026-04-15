package fr.insee.genesis.domain.service.volumetry;

import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.stubs.ConfigStub;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class VolumetryLogServiceUnitTest {

    private static VolumetryLogService volumetryLogService;
    private static final Config config = new ConfigStub();
    private static final Path logFilePath = Path.of(config.getLogFolder()).resolve(Constants.VOLUMETRY_FOLDER_NAME);

    @BeforeEach
    @SneakyThrows
    void setUp() {
        volumetryLogService = new VolumetryLogService(
                config
        );
        if(Files.notExists(logFilePath)){
            Files.createDirectories(logFilePath);
        }
    }

    @Test
    @SneakyThrows
    void writeVolumetries_test() {
        //GIVEN
        SurveyUnitApiPort surveyUnitApiPort = mock(SurveyUnitApiPort.class);
        long exampleResponseCount = 5;
        doReturn(Set.of(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID))
                .when(surveyUnitApiPort).findDistinctQuestionnairesAndCollectionInstrumentIds();
        doReturn(exampleResponseCount).when(surveyUnitApiPort).countResponsesByCollectionInstrumentId(any());

        //WHEN
        Map<String, Long> responseVolumetricsMap = volumetryLogService.writeVolumetries(surveyUnitApiPort);

        //THEN
        Assertions.assertThat(Files.list(logFilePath)).hasSize(1);
        Assertions.assertThat(responseVolumetricsMap)
                .containsOnlyKeys(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
        Assertions.assertThat(responseVolumetricsMap.get(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID))
                .isEqualTo(exampleResponseCount);
    }

    @Test
    @SneakyThrows
    void writeVolumetries_questionnaireId_test() {
        //GIVEN
        SurveyUnitApiPort surveyUnitApiPort = mock(SurveyUnitApiPort.class);
        long exampleResponseCount = 5;
        long exampleResponseWithQuestionnaireIdCount = 3;
        doReturn(Set.of(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID))
                .when(surveyUnitApiPort).findDistinctQuestionnairesAndCollectionInstrumentIds();
        doReturn(exampleResponseCount).when(surveyUnitApiPort).countResponsesByCollectionInstrumentId(any());
        doReturn(exampleResponseWithQuestionnaireIdCount).when(surveyUnitApiPort).countResponsesByQuestionnaireId(any());

        //WHEN
        Map<String, Long> responseVolumetricsMap = volumetryLogService.writeVolumetries(surveyUnitApiPort);

        //THEN
        Assertions.assertThat(Files.list(logFilePath)).hasSize(1);
        Assertions.assertThat(responseVolumetricsMap)
                .containsOnlyKeys(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
        Assertions.assertThat(responseVolumetricsMap.get(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID))
                .isEqualTo(exampleResponseCount + exampleResponseWithQuestionnaireIdCount);
    }

    @ParameterizedTest
    @CsvSource({"false, true", "true, false", "true, true", "false, false"})
    @SneakyThrows
    void writeRawDataVolumetries_test(boolean hasOldRawData, boolean hasRawResponses) {
        //GIVEN
        LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort = mock(LunaticJsonRawDataApiPort.class);
        doReturn(hasOldRawData ? Set.of(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID) : Set.of())
                .when(lunaticJsonRawDataApiPort).findDistinctQuestionnaireIds();
        long oldRawDataCount = hasOldRawData ? 5 : 0;
        doReturn(oldRawDataCount).when(lunaticJsonRawDataApiPort).countRawResponsesByQuestionnaireId(any());

        RawResponseApiPort rawResponseApiPort = mock(RawResponseApiPort.class);
        doReturn(hasRawResponses ? Set.of(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID) : Set.of())
                .when(rawResponseApiPort).getDistinctCollectionInstrumentIds();
        long rawResponseCount = hasRawResponses ? 3 : 0;
        doReturn(rawResponseCount).when(rawResponseApiPort).countByCollectionInstrumentId(any());

        //WHEN
        Map<String, Map<String, Long>> responseVolumetricsMap = volumetryLogService.writeRawDataVolumetries(
                lunaticJsonRawDataApiPort, rawResponseApiPort
        );

        //THEN
        Assertions.assertThat(Files.list(logFilePath)).hasSize(1);
        Assertions.assertThat(responseVolumetricsMap)
                .containsOnlyKeys(
                        Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME,
                        Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME,
                        Constants.VOLUMETRY_RAW_TOTAL
                );

        if (hasOldRawData) {
            Assertions.assertThat(
                    responseVolumetricsMap.get(Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME)
            ).containsEntry(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, oldRawDataCount);
        }
        if (hasRawResponses) {
            Assertions.assertThat(
                    responseVolumetricsMap.get(Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME)
            ).containsEntry(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, rawResponseCount);
        }

        if(hasOldRawData || hasRawResponses) {
            Assertions.assertThat(
                    responseVolumetricsMap.get(Constants.VOLUMETRY_RAW_TOTAL)
            ).containsEntry(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, oldRawDataCount + rawResponseCount);
        } else {
            Assertions.assertThat(
                    responseVolumetricsMap.get(Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME)
            ).doesNotContainKey(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
            Assertions.assertThat(
                    responseVolumetricsMap.get(Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME)
            ).doesNotContainKey(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
            Assertions.assertThat(
                    responseVolumetricsMap.get(Constants.VOLUMETRY_RAW_TOTAL)
            ).doesNotContainKey(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @SneakyThrows
    void cleanOldFiles(boolean isRawVolumetricFile) {
        //GIVEN
        String volumetricFileSuffix = isRawVolumetricFile ? Constants.VOLUMETRY_RAW_FILE_SUFFIX : Constants.VOLUMETRY_FILE_SUFFIX;
        //Recent file to not delete
        LocalDateTime recentDateTime = LocalDateTime.now();
        Files.createFile(logFilePath.resolve(
                recentDateTime.format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                        + volumetricFileSuffix
                        + ".csv"
        ));
        //Old file to delete
        LocalDateTime oldDateTime = LocalDateTime.now().minusYears(5);
        Files.createFile(logFilePath.resolve(
                oldDateTime.format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                        + volumetricFileSuffix
                        + ".csv"
        ));

        //WHEN
        volumetryLogService.cleanOldFiles();

        //THEN
        try(Stream<Path> pathStream = Files.list(logFilePath)) {
            List<Path> pathList = pathStream.toList();
            Assertions.assertThat(pathList).hasSize(1);
            Assertions.assertThat(pathList.getFirst().getFileName().toString())
                    .contains(recentDateTime.format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT)));
        }
    }

    @AfterEach
    @SneakyThrows
    void tearDown() {
        FileSystemUtils.deleteRecursively(logFilePath);
    }
}