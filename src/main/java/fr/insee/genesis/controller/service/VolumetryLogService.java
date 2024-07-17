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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
public class VolumetryLogService {
    private final Config config;

    @Autowired
    public VolumetryLogService(Config config) {
        this.config = config;
    }

    public void writeVolumetries(SurveyUnitUpdateApiPort surveyUnitUpdateApiPort) throws IOException {
        Set<String> campaigns = surveyUnitUpdateApiPort.findDistinctIdCampaigns();
        for (String campaignId : campaigns) {
            long countResult = surveyUnitUpdateApiPort.countResponsesByIdCampaign(campaignId);

            Path logFilePath = Path.of(config.getLogFolder()).resolve(Constants.VOLUMETRY_FOLDER_NAME)
                    .resolve(campaignId + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
            Files.createDirectories(logFilePath.getParent());

            //Create log file with header if not exists
            if (!Files.exists(logFilePath)) {
                Files.writeString(logFilePath, "date;volumetry\n");
            }
            //TODO maybe remove last line if the date begins with current date to update it

            String line = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ";" +
                    countResult +
                    "\n";

            Files.writeString(logFilePath, line, StandardOpenOption.APPEND);
        }
    }
}
