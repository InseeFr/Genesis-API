package fr.insee.genesis.controller.service;

import fr.insee.genesis.Constants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
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
public class VolumetryLogService {
    private final Config config;

    @Autowired
    public VolumetryLogService(Config config) {
        this.config = config;
    }

    public void writeVolumetries(SurveyUnitUpdateApiPort surveyUnitUpdateApiPort) throws IOException {
        Path logFilePath = Path.of(config.getLogFolder()).resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(
                        LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                                + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
        Files.createDirectories(logFilePath.getParent());
        //Create log file with header if not exists
        if (!Files.exists(logFilePath)) {
            Files.writeString(logFilePath, "campaign;volumetry\n");
        }

        //Write lines
        Set<String> campaigns = surveyUnitUpdateApiPort.findDistinctIdCampaigns();
        for (String campaignId : campaigns) {
            long countResult = surveyUnitUpdateApiPort.countResponsesByIdCampaign(campaignId);

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
                }
            }
        }
    }
}
