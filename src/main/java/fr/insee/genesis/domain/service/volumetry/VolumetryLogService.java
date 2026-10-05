package fr.insee.genesis.domain.service.volumetry;

import fr.insee.genesis.Constants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.ports.api.FileSystemPort;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class VolumetryLogService {
    private final Config config;
    private final FileSystemPort fileSystemPort;

    public Map<String, Long> writeVolumetries(SurveyUnitApiPort surveyUnitApiPort) throws IOException {
        Map<String, Long> responseVolumetricsByQuestionnaireMap = new HashMap<>();

        Path logFilePath = Path.of(config.getLogFolder()).resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                                + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
        fileSystemPort.createDirectories(logFilePath.getParent().toString());
        //Overwrite log file with header if exists
        fileSystemPort.deleteIfExists(logFilePath.toString());
        fileSystemPort.writeString(logFilePath.toString(), "campaign;volumetry;distinctInterrogationIds\n");

        //Write lines
        Set<String> collectionInstrumentIds =
                surveyUnitApiPort.findDistinctQuestionnairesAndCollectionInstrumentIds();

        List<String> sortedIds = new ArrayList<>(collectionInstrumentIds);
        Collections.sort(sortedIds);

        for (String collectionInstrumentId : sortedIds) {
            long countResult = surveyUnitApiPort.countResponsesByCollectionInstrumentId(collectionInstrumentId);
            countResult += surveyUnitApiPort.countResponsesByQuestionnaireId(collectionInstrumentId);

            long distinctInterrogationIds =
                    surveyUnitApiPort.countDistinctInterrogationIdsByQuestionnaireAndCollectionInstrumentId(collectionInstrumentId);

            String line = collectionInstrumentId + ";" + countResult + ";" + distinctInterrogationIds + "\n";
            fileSystemPort.writeString(logFilePath.toString(), line, StandardOpenOption.APPEND);
            responseVolumetricsByQuestionnaireMap.put(collectionInstrumentId, countResult);
        }

        return responseVolumetricsByQuestionnaireMap;
    }

    public Map<String, Map<String, Long>> writeRawDataVolumetries(
            LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort,
            RawResponseApiPort rawResponseApiPort
    ) throws IOException {

        Map<String, Map<String, Long>> rawDataVolumetricsMap = new HashMap<>();
        rawDataVolumetricsMap.put(Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME, new HashMap<>());
        rawDataVolumetricsMap.put(Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME, new HashMap<>());
        rawDataVolumetricsMap.put(Constants.VOLUMETRY_RAW_TOTAL, new HashMap<>());

        Path logFilePath = Path.of(config.getLogFolder())
                .resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(
                        LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                                + Constants.VOLUMETRY_RAW_FILE_SUFFIX + ".csv"
                );

        fileSystemPort.createDirectories(logFilePath.getParent().toString());

        // Overwrite file if exists
        fileSystemPort.deleteIfExists(logFilePath.toString());

        fileSystemPort.writeString(
                logFilePath.toString(),
                "questionnaireId;%s;%s;%s;distinctInterrogationIds%n"
                        .formatted(
                                Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME,
                                Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME,
                                Constants.VOLUMETRY_RAW_TOTAL
                        )
        );

        // Merge questionnaire ids from both sources
        Set<String> lunaticQuestionnaires = lunaticJsonRawDataApiPort.findDistinctQuestionnaireIds();
        Set<String> rawQuestionnaires = new HashSet<>(rawResponseApiPort.getDistinctCollectionInstrumentIds());
        rawQuestionnaires.addAll(lunaticQuestionnaires);
        rawQuestionnaires.removeIf(Objects::isNull);

        List<String> sortedQuestionnaires = new ArrayList<>(rawQuestionnaires);
        Collections.sort(sortedQuestionnaires);

        for (String questionnaireId : sortedQuestionnaires) {

            long lunaticCount =
                    lunaticJsonRawDataApiPort.countRawResponsesByQuestionnaireId(questionnaireId);

            long rawCount =
                    rawResponseApiPort.countByCollectionInstrumentId(questionnaireId);

            long total = lunaticCount + rawCount;

            long lunaticDistinct =
                    lunaticJsonRawDataApiPort.countDistinctInterrogationIdsByQuestionnaireId(questionnaireId);

            long rawDistinct =
                    rawResponseApiPort.countDistinctInterrogationIdsByCollectionInstrumentId(questionnaireId);

            long distinctTotal = lunaticDistinct + rawDistinct;

            String line = questionnaireId + ";"
                    + lunaticCount + ";"
                    + rawCount + ";"
                    + total + ";"
                    + distinctTotal
                    + "\n";

            fileSystemPort.writeString(logFilePath.toString(), line, StandardOpenOption.APPEND);

            rawDataVolumetricsMap.get(Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME)
                    .put(questionnaireId, lunaticCount);

            rawDataVolumetricsMap.get(Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME)
                    .put(questionnaireId, rawCount);

            rawDataVolumetricsMap.get(Constants.VOLUMETRY_RAW_TOTAL)
                    .put(questionnaireId, total);
        }

        return rawDataVolumetricsMap;
    }

    public void cleanOldFiles() throws IOException {
        try (Stream<String> stream =
                     fileSystemPort.walk(Path.of(config.getLogFolder()).resolve(Constants.VOLUMETRY_FOLDER_NAME).toString())){
            for (String logFilePath : stream.filter(path -> path.endsWith(".csv")).toList()){
                //If older than x months
                //Extract date
                String datePart = logFilePath
                        .split(Constants.VOLUMETRY_FILE_SUFFIX + "\\.csv")[0] // Delete common suffix
                        .replace("_RAW", ""); // Delete "_RAW" if present
                try{
                    if (LocalDateTime.parse(datePart, DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                            .isBefore(LocalDateTime.now().minusDays(Constants.VOLUMETRY_FILE_EXPIRATION_DAYS))
                    ) {
                        fileSystemPort.deleteIfExists(logFilePath);
                        log.info("Deleted {}", logFilePath);
                    }
                }catch (DateTimeParseException dtpe){
                    log.warn(dtpe.toString());
                }

            }
        }
    }
}
