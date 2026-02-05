package fr.insee.genesis.domain.service.volumetry;

import fr.insee.genesis.Constants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
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
import java.util.HashMap;
import java.util.HashSet;
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
        Files.writeString(logFilePath, "campaign;volumetry\n");

        //Write lines
        Set<String> collectionInstrumentIds = surveyUnitApiPort.findDistinctQuestionnairesAndCollectionInstrumentIds();
        for (String collectionInstrumentId : collectionInstrumentIds) {
            long countResult = surveyUnitApiPort.countResponsesByCollectionInstrumentId(collectionInstrumentId);
            countResult += surveyUnitApiPort.countResponsesByQuestionnaireId(collectionInstrumentId);


            String line = collectionInstrumentId + ";" + countResult + "\n";

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

        Path logFilePath = Path.of(config.getLogFolder()).resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                                + Constants.VOLUMETRY_RAW_FILE_SUFFIX + ".csv");
        Files.createDirectories(logFilePath.getParent());
        //Overwrite log file with header if exists
        if (Files.exists(logFilePath)){
            Files.delete(logFilePath);
        }

        //Write header
        Files.writeString(logFilePath, "questionnaireId;%s;%s;%s\n"
                .formatted(
                        Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME,
                        Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME,
                        Constants.VOLUMETRY_RAW_TOTAL
                )
        );

        //Write lines
        Set<String> oldRawDataQuestionnaires = lunaticJsonRawDataApiPort.findDistinctQuestionnaireIds();
        Set<String> rawDataQuestionnaires = new HashSet<>(rawResponseApiPort.getDistinctCollectionInstrumentIds());
        rawDataQuestionnaires.addAll(oldRawDataQuestionnaires);
        for (String questionnaireId : rawDataQuestionnaires) {
            long oldRawDataCountResult = lunaticJsonRawDataApiPort.countRawResponsesByQuestionnaireId(questionnaireId);
            long rawDataCountResult = rawResponseApiPort.countByCollectionInstrumentId(questionnaireId);
            long total = oldRawDataCountResult + rawDataCountResult;

            String delimiter = ";";
            String line = questionnaireId + delimiter
                    + oldRawDataCountResult + delimiter
                    + rawDataCountResult + delimiter
                    + total
                    +"\n";

            Files.writeString(logFilePath, line, StandardOpenOption.APPEND);
            rawDataVolumetricsMap.get(Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME)
                    .put(questionnaireId, oldRawDataCountResult);
            rawDataVolumetricsMap.get(Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME)
                    .put(questionnaireId, rawDataCountResult);
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
