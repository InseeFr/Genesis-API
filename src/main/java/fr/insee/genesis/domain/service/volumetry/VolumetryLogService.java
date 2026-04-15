package fr.insee.genesis.domain.service.volumetry;

import fr.insee.genesis.Constants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
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
import java.util.Set;
import java.util.stream.Stream;

@Service
@Slf4j
public class VolumetryLogService {
    private final Config config;

    @Autowired
    public VolumetryLogService(Config config) {
        this.config = config;
    }

    public Map<String, Long> writeVolumetries(SurveyUnitApiPort surveyUnitApiPort) throws IOException {
        Map<String, Long> responseVolumetricsByQuestionnaireMap = new HashMap<>();

        Path logFilePath = Path.of(config.getLogFolder()).resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                                + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
        Files.createDirectories(logFilePath.getParent());
        //Overwrite log file with header if exists
        if (Files.exists(logFilePath)){
            Files.delete(logFilePath);
        }
        Files.writeString(logFilePath, "campaign;volumetry;distinctInterrogationIds\n");

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
            Files.writeString(logFilePath, line, StandardOpenOption.APPEND);
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

        Files.createDirectories(logFilePath.getParent());

        // Overwrite file if exists
        if (Files.exists(logFilePath)) {
            Files.delete(logFilePath);
        }

        Files.writeString(
                logFilePath,
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

            Files.writeString(logFilePath, line, StandardOpenOption.APPEND);

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
        try (Stream<Path> pathStream = Files.walk(Path.of(config.getLogFolder()).resolve(Constants.VOLUMETRY_FOLDER_NAME))){
            for (Path logFilePath : pathStream.filter(path -> path.getFileName().toString().endsWith(".csv")).toList()){
                //If older than x months
                //Extract date
                String datePart = logFilePath.getFileName().toString()
                        .split(Constants.VOLUMETRY_FILE_SUFFIX + "\\.csv")[0] // Delete common suffix
                        .replace("_RAW", ""); // Delete "_RAW" if present
                try{
                    if (LocalDateTime.parse(datePart, DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                            .isBefore(LocalDateTime.now().minusDays(Constants.VOLUMETRY_FILE_EXPIRATION_DAYS))
                    ) {
                        Files.deleteIfExists(logFilePath);
                        log.info("Deleted {}", logFilePath);
                    }
                }catch (DateTimeParseException dtpe){
                    log.warn(dtpe.toString());
                }

            }
        }
    }
}
