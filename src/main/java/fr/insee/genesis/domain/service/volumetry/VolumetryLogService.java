package fr.insee.genesis.domain.service.volumetry;

import fr.insee.genesis.Constants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    public void writeVolumetries(SurveyUnitApiPort surveyUnitApiPort) throws IOException {
        Path logFilePath = Path.of(config.getLogFolder()).resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(
                        LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                                + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
        Files.createDirectories(logFilePath.getParent());
        //Overwrite log file with header if exists
        if (Files.exists(logFilePath)){
            Files.delete(logFilePath);
        }
        Files.writeString(logFilePath, "campaign;volumetry\n");

        //Write lines
        Set<String> campaigns = surveyUnitApiPort.findDistinctIdCampaigns();
        for (String campaignId : campaigns) {
            long countResult = surveyUnitApiPort.countResponsesByIdCampaign(campaignId);

            String line = campaignId + ";" + countResult + "\n";

            Files.writeString(logFilePath, line, StandardOpenOption.APPEND);
        }

    }

    public void cleanOldFiles() throws IOException {
        try (Stream<Path> pathStream = Files.walk(Path.of(config.getLogFolder()).resolve(Constants.VOLUMETRY_FOLDER_NAME))){
            for (Path logFilePath : pathStream.filter(path -> path.getFileName().toString().endsWith(".csv")).toList()){
                //If older than x months
                if (LocalDate.parse(
                        logFilePath.getFileName().toString().replace(Constants.VOLUMETRY_FILE_SUFFIX + ".csv", ""),
                        DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT)
                ).isBefore(LocalDate.now().minusDays(Constants.VOLUMETRY_FILE_EXPIRATION_DAYS))
                ) {
                    Files.deleteIfExists(logFilePath);
                    log.info("Deleted {}", logFilePath);
                }
            }
        }
    }
}
