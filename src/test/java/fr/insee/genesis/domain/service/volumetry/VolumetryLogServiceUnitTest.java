package fr.insee.genesis.domain.service.volumetry;

import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    private static final Config config = TestConstants.getConfigStub();
    private static final Path logFilePath = Path.of(config.getLogFolder()).resolve(Constants.VOLUMETRY_FOLDER_NAME);

    @BeforeEach
    @SneakyThrows
    void setUp() {
        volumetryLogService = new VolumetryLogService(config);
        if (Files.notExists(logFilePath)) {
            Files.createDirectories(logFilePath);
        }
    }

    @Test
    @SneakyThrows
    void writeVolumetries_test() {
        SurveyUnitApiPort surveyUnitApiPort = mock(SurveyUnitApiPort.class);
        long exampleResponseCount = 5;
        doReturn(Set.of(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID))
                .when(surveyUnitApiPort).findDistinctQuestionnairesAndCollectionInstrumentIds();
        doReturn(exampleResponseCount).when(surveyUnitApiPort).countResponsesByCollectionInstrumentId(any());

        Map<String, Long> responseVolumetricsMap = volumetryLogService.writeVolumetries(surveyUnitApiPort);

        Assertions.assertThat(Files.list(logFilePath)).hasSize(1);
        Assertions.assertThat(responseVolumetricsMap)
                .containsOnlyKeys(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
        Assertions.assertThat(responseVolumetricsMap.get(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID))
                .isEqualTo(exampleResponseCount);
    }

    @Test
    @SneakyThrows
    void writeVolumetries_questionnaireId_test() {
        SurveyUnitApiPort surveyUnitApiPort = mock(SurveyUnitApiPort.class);
        long exampleResponseCount = 5;
        long exampleResponseWithQuestionnaireIdCount = 3;
        doReturn(Set.of(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID))
                .when(surveyUnitApiPort).findDistinctQuestionnairesAndCollectionInstrumentIds();
        doReturn(exampleResponseCount).when(surveyUnitApiPort).countResponsesByCollectionInstrumentId(any());
        doReturn(exampleResponseWithQuestionnaireIdCount).when(surveyUnitApiPort).countResponsesByQuestionnaireId(any());

        Map<String, Long> responseVolumetricsMap = volumetryLogService.writeVolumetries(surveyUnitApiPort);

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

        Map<String, Map<String, Long>> responseVolumetricsMap = volumetryLogService.writeRawDataVolumetries(
                lunaticJsonRawDataApiPort, rawResponseApiPort
        );

        Assertions.assertThat(Files.list(logFilePath)).hasSize(1);
        Assertions.assertThat(responseVolumetricsMap)
                .containsOnlyKeys(
                        Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME,
                        Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME,
                        Constants.VOLUMETRY_RAW_TOTAL
                );

        if (hasOldRawData) {
            Assertions.assertThat(responseVolumetricsMap.get(Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME))
                    .containsEntry(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, oldRawDataCount);
        }
        if (hasRawResponses) {
            Assertions.assertThat(responseVolumetricsMap.get(Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME))
                    .containsEntry(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, rawResponseCount);
        }

        if (hasOldRawData || hasRawResponses) {
            Assertions.assertThat(responseVolumetricsMap.get(Constants.VOLUMETRY_RAW_TOTAL))
                    .containsEntry(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, oldRawDataCount + rawResponseCount);
        } else {
            Assertions.assertThat(responseVolumetricsMap.get(Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME))
                    .doesNotContainKey(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
            Assertions.assertThat(responseVolumetricsMap.get(Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME))
                    .doesNotContainKey(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
            Assertions.assertThat(responseVolumetricsMap.get(Constants.VOLUMETRY_RAW_TOTAL))
                    .doesNotContainKey(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @SneakyThrows
    void cleanOldFiles(boolean isRawVolumetricFile) {
        String volumetricFileSuffix = isRawVolumetricFile ? Constants.VOLUMETRY_RAW_FILE_SUFFIX : Constants.VOLUMETRY_FILE_SUFFIX;
        LocalDateTime recentDateTime = LocalDateTime.now();
        Files.createFile(logFilePath.resolve(
                recentDateTime.format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                        + volumetricFileSuffix + ".csv"
        ));
        LocalDateTime oldDateTime = LocalDateTime.now().minusYears(5);
        Files.createFile(logFilePath.resolve(
                oldDateTime.format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                        + volumetricFileSuffix + ".csv"
        ));

        volumetryLogService.cleanOldFiles();

        try (Stream<Path> pathStream = Files.list(logFilePath)) {
            List<Path> pathList = pathStream.toList();
            Assertions.assertThat(pathList).hasSize(1);
            Assertions.assertThat(pathList.getFirst().getFileName().toString())
                    .contains(recentDateTime.format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT)));
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("writeVolumetries should erase existing file")
    void writeVolumetries_shouldOverwriteExistingFile() {
        // GIVEN
        SurveyUnitApiPort surveyUnitApiPort = mock(SurveyUnitApiPort.class);
        doReturn(Set.of(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID))
                .when(surveyUnitApiPort).findDistinctQuestionnairesAndCollectionInstrumentIds();
        doReturn(5L).when(surveyUnitApiPort).countResponsesByCollectionInstrumentId(any());
        volumetryLogService.writeVolumetries(surveyUnitApiPort);

        // WHEN
        doReturn(10L).when(surveyUnitApiPort).countResponsesByCollectionInstrumentId(any());
        Map<String, Long> result = volumetryLogService.writeVolumetries(surveyUnitApiPort);

        // THEN
        Assertions.assertThat(Files.list(logFilePath)).hasSize(1);
        Assertions.assertThat(result).containsEntry(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, 10L);
    }

    @Test
    @SneakyThrows
    @DisplayName("writeVolumetries should return empty map when no questionnaire")
    void writeVolumetries_shouldReturnEmptyMap_whenNoQuestionnaire() {
        // GIVEN
        SurveyUnitApiPort surveyUnitApiPort = mock(SurveyUnitApiPort.class);
        doReturn(Set.of()).when(surveyUnitApiPort).findDistinctQuestionnairesAndCollectionInstrumentIds();

        // WHEN
        Map<String, Long> result = volumetryLogService.writeVolumetries(surveyUnitApiPort);

        // THEN
        Assertions.assertThat(result).isEmpty();
        try (Stream<Path> pathStream = Files.list(logFilePath)) {
            List<Path> paths = pathStream.toList();
            Assertions.assertThat(paths).hasSize(1);
            String content = Files.readString(paths.getFirst());
            Assertions.assertThat(content).isEqualTo("campaign;volumetry\n");
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("writeVolumetries should manage multiple questionnaires")
    void writeVolumetries_shouldHandleMultipleQuestionnaires() {
        // GIVEN
        SurveyUnitApiPort surveyUnitApiPort = mock(SurveyUnitApiPort.class);
        String questId1 = "QUEST1";
        String questId2 = "QUEST2";
        doReturn(Set.of(questId1, questId2))
                .when(surveyUnitApiPort).findDistinctQuestionnairesAndCollectionInstrumentIds();
        doReturn(5L).when(surveyUnitApiPort).countResponsesByCollectionInstrumentId(questId1);
        doReturn(3L).when(surveyUnitApiPort).countResponsesByCollectionInstrumentId(questId2);
        doReturn(0L).when(surveyUnitApiPort).countResponsesByQuestionnaireId(any());

        // WHEN
        Map<String, Long> result = volumetryLogService.writeVolumetries(surveyUnitApiPort);

        // THEN
        Assertions.assertThat(result).containsOnlyKeys(questId1, questId2)
                .containsEntry(questId1, 5L).containsEntry(questId2, 3L);
    }

    @Test
    @SneakyThrows
    @DisplayName("writeVolumetries should create directory")
    void writeVolumetries_shouldCreateDirectories_whenNotExists() {
        // GIVEN
        FileSystemUtils.deleteRecursively(logFilePath);
        SurveyUnitApiPort surveyUnitApiPort = mock(SurveyUnitApiPort.class);
        doReturn(Set.of()).when(surveyUnitApiPort).findDistinctQuestionnairesAndCollectionInstrumentIds();

        // WHEN
        volumetryLogService.writeVolumetries(surveyUnitApiPort);

        // THEN
        Assertions.assertThat(Files.exists(logFilePath)).isTrue();
    }

    @Test
    @SneakyThrows
    @DisplayName("writeVolumetries write correct CSV")
    void writeVolumetries_shouldWriteCorrectCsvContent() {
        // GIVEN
        SurveyUnitApiPort surveyUnitApiPort = mock(SurveyUnitApiPort.class);
        doReturn(Set.of(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID))
                .when(surveyUnitApiPort).findDistinctQuestionnairesAndCollectionInstrumentIds();
        doReturn(7L).when(surveyUnitApiPort).countResponsesByCollectionInstrumentId(any());
        doReturn(0L).when(surveyUnitApiPort).countResponsesByQuestionnaireId(any());

        // WHEN
        volumetryLogService.writeVolumetries(surveyUnitApiPort);

        // THEN
        try (Stream<Path> pathStream = Files.list(logFilePath)) {
            String content = Files.readString(pathStream.toList().getFirst());
            Assertions.assertThat(content).startsWith("campaign;volumetry\n");
            Assertions.assertThat(content).contains(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID + ";7");
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("writeRawDataVolumetries should create directories")
    void writeRawDataVolumetries_shouldCreateDirectories_whenNotExists() {
        // GIVEN
        FileSystemUtils.deleteRecursively(logFilePath);
        LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort = mock(LunaticJsonRawDataApiPort.class);
        doReturn(Set.of()).when(lunaticJsonRawDataApiPort).findDistinctQuestionnaireIds();
        RawResponseApiPort rawResponseApiPort = mock(RawResponseApiPort.class);
        doReturn(Set.of()).when(rawResponseApiPort).getDistinctCollectionInstrumentIds();

        // WHEN
        volumetryLogService.writeRawDataVolumetries(lunaticJsonRawDataApiPort, rawResponseApiPort);

        // THEN
        Assertions.assertThat(Files.exists(logFilePath)).isTrue();
    }

    @Test
    @SneakyThrows
    @DisplayName("writeRawDataVolumetries should overwrite existing file")
    void writeRawDataVolumetries_shouldOverwriteExistingFile() {
        // GIVEN - premier appel
        LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort = mock(LunaticJsonRawDataApiPort.class);
        doReturn(Set.of(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID))
                .when(lunaticJsonRawDataApiPort).findDistinctQuestionnaireIds();
        doReturn(5L).when(lunaticJsonRawDataApiPort).countRawResponsesByQuestionnaireId(any());
        RawResponseApiPort rawResponseApiPort = mock(RawResponseApiPort.class);
        doReturn(Set.of()).when(rawResponseApiPort).getDistinctCollectionInstrumentIds();
        doReturn(0L).when(rawResponseApiPort).countByCollectionInstrumentId(any());
        volumetryLogService.writeRawDataVolumetries(lunaticJsonRawDataApiPort, rawResponseApiPort);

        // WHEN - second appel
        doReturn(9L).when(lunaticJsonRawDataApiPort).countRawResponsesByQuestionnaireId(any());
        Map<String, Map<String, Long>> result = volumetryLogService.writeRawDataVolumetries(
                lunaticJsonRawDataApiPort, rawResponseApiPort
        );

        // THEN
        Assertions.assertThat(Files.list(logFilePath)).hasSize(1);
        Assertions.assertThat(
                result.get(Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME))
                .containsEntry(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, 9L);
    }

    @Test
    @SneakyThrows
    @DisplayName("writeRawDataVolumetries should write correct CSV")
    void writeRawDataVolumetries_shouldWriteCorrectCsvContent() {
        // GIVEN
        LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort = mock(LunaticJsonRawDataApiPort.class);
        doReturn(Set.of(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID))
                .when(lunaticJsonRawDataApiPort).findDistinctQuestionnaireIds();
        doReturn(4L).when(lunaticJsonRawDataApiPort).countRawResponsesByQuestionnaireId(any());
        RawResponseApiPort rawResponseApiPort = mock(RawResponseApiPort.class);
        doReturn(Set.of()).when(rawResponseApiPort).getDistinctCollectionInstrumentIds();
        doReturn(2L).when(rawResponseApiPort).countByCollectionInstrumentId(any());

        // WHEN
        volumetryLogService.writeRawDataVolumetries(lunaticJsonRawDataApiPort, rawResponseApiPort);

        // THEN
        try (Stream<Path> pathStream = Files.list(logFilePath)) {
            String content = Files.readString(pathStream.toList().getFirst());
            Assertions.assertThat(content).contains(
                    Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME,
                    Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME,
                    Constants.VOLUMETRY_RAW_TOTAL
            );
            Assertions.assertThat(content).contains(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID + ";4;2;6"
            );
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("writeRawDataVolumetries should merge from old models and filiere")
    void writeRawDataVolumetries_shouldMergeQuestionnairesFromBothSources() {
        // GIVEN - QUEST1 dans les deux sources, QUEST2 seulement dans rawResponse
        LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort = mock(LunaticJsonRawDataApiPort.class);
        doReturn(Set.of("QUEST1")).when(lunaticJsonRawDataApiPort).findDistinctQuestionnaireIds();
        doReturn(5L).when(lunaticJsonRawDataApiPort).countRawResponsesByQuestionnaireId(any());

        RawResponseApiPort rawResponseApiPort = mock(RawResponseApiPort.class);
        doReturn(Set.of("QUEST1", "QUEST2")).when(rawResponseApiPort).getDistinctCollectionInstrumentIds();
        doReturn(3L).when(rawResponseApiPort).countByCollectionInstrumentId(any());

        // WHEN
        Map<String, Map<String, Long>> result = volumetryLogService.writeRawDataVolumetries(
                lunaticJsonRawDataApiPort, rawResponseApiPort
        );

        // THEN
        Assertions.assertThat(result.get(Constants.VOLUMETRY_RAW_TOTAL))
                .containsOnlyKeys("QUEST1", "QUEST2");
        // QUEST1 : 5 (old) + 3 (new) = 8
        Assertions.assertThat(result.get(Constants.VOLUMETRY_RAW_TOTAL))
                .containsEntry("QUEST1", 8L);
        // QUEST2 : 0 (old) + 3 (new) = 3
        Assertions.assertThat(result.get(Constants.VOLUMETRY_RAW_TOTAL))
                .containsEntry("QUEST2", 3L);
        Assertions.assertThat(result.get(Constants.VOLUMETRY_RAW_TOTAL).get("QUEST2")).isEqualTo(3L);
    }

    @Test
    @SneakyThrows
    @DisplayName("cleanOldFiles should not delete a file with invalid date time")
    void cleanOldFiles_shouldNotDelete_whenInvalidDateInFilename() {
        // GIVEN
        Path invalidDateFile = logFilePath.resolve("not_a_date" + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
        Files.createFile(invalidDateFile);

        // WHEN
        volumetryLogService.cleanOldFiles();

        // THEN
        Assertions.assertThat(Files.exists(invalidDateFile)).isTrue();
    }

    @Test
    @SneakyThrows
    @DisplayName("cleanOldFiles should work when empty directory")
    void cleanOldFiles_shouldNotFail_whenDirectoryIsEmpty() {
        // WHEN + THEN
        Assertions.assertThatCode(() -> volumetryLogService.cleanOldFiles())
                .doesNotThrowAnyException();
    }

    @Test
    @SneakyThrows
    @DisplayName("cleanOldFiles should delete all old files")
    void cleanOldFiles_shouldDeleteAllOldFiles_bothTypes() {
        // GIVEN - deux anciens fichiers (standard et RAW), un récent
        LocalDateTime oldDateTime = LocalDateTime.now().minusYears(5);
        String oldDateStr = oldDateTime.format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT));

        Files.createFile(logFilePath.resolve(oldDateStr + Constants.VOLUMETRY_FILE_SUFFIX + ".csv"));
        Files.createFile(logFilePath.resolve(oldDateStr + Constants.VOLUMETRY_RAW_FILE_SUFFIX + ".csv"));

        LocalDateTime recentDateTime = LocalDateTime.now();
        String recentDateStr = recentDateTime.format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT));
        Files.createFile(logFilePath.resolve(recentDateStr + Constants.VOLUMETRY_FILE_SUFFIX + ".csv"));

        // WHEN
        volumetryLogService.cleanOldFiles();

        // THEN
        try (Stream<Path> pathStream = Files.list(logFilePath)) {
            List<Path> remaining = pathStream.toList();
            Assertions.assertThat(remaining).hasSize(1);
            Assertions.assertThat(remaining.getFirst().getFileName().toString()).contains(recentDateStr);
        }
    }

    @AfterEach
    @SneakyThrows
    void tearDown() {
        FileSystemUtils.deleteRecursively(logFilePath);
    }
}