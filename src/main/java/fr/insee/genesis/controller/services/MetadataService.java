package fr.insee.genesis.controller.services;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.bpm.metadata.reader.ddi.DDIReader;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.utils.GroupUtils;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
@Slf4j
@Service
public class MetadataService {

    private static final String DDI_FILE_PATTERN = "ddi[\\w,\\s-]+\\.xml";
    private static final String LUNATIC_FILE_PATTERN = "lunatic[\\w,\\s-]+\\.json";

    /**
     * Parse metadata file, either DDI or Lunatic, depending on the withDDI flag.
     *
     * @param metadataFilePath path to the metadata file
     * @param withDDI          true for DDI parsing, false for Lunatic parsing
     * @return VariablesMap or null if an error occurs
     */
    public VariablesMap parseMetadata(String metadataFilePath, boolean withDDI) {
        try {
            log.info("Try to read {} file: {}", withDDI ? "DDI" : "Lunatic", metadataFilePath);
            if (withDDI) {
                MetadataModel metadataModel = DDIReader.getMetadataFromDDI(
                        Path.of(metadataFilePath).toFile().toURI().toURL().toString(),
                        new FileInputStream(metadataFilePath));
                // Temporary solution
                // the logic of adding variables from lunatic to the ones present in the DDI needs to be implemented in BPM
                // (only in Kraftwerk for the moment)
                for (String enoVar : Constants.getEnoVariables()){
                    metadataModel.getVariables().putVariable(new Variable(enoVar, metadataModel.getRootGroup(), VariableType.STRING));
                }
                return metadataModel.getVariables();
            } else {
                return LunaticReader.getMetadataFromLunatic(
                        new FileInputStream(metadataFilePath)).getVariables();
            }
        } catch (MetadataParserException | IOException e) {
            log.error("Error reading metadata file", e);
            return null;
        }
    }

    /**
     * By folder defined by campaignName
     * Attempt to parse DDI metadata first; if it fails, fall back to Lunatic parsing.
     *
     * @param campaignName name of the campaign
     * @param modeName     mode associated with the data
     * @param fileUtils    utility for file operations
     * @param errors       list to populate with errors
     * @return VariablesMap or null if parsing fails
     */
    public VariablesMap readMetadatas(String campaignName, String modeName, FileUtils fileUtils, List<GenesisError> errors) {

        Path ddiFilePath;
        VariablesMap variablesMap = null;
        try {
            ddiFilePath = fileUtils.findFile(String.format("%s/%s", fileUtils.getSpecFolder(campaignName), modeName), DDI_FILE_PATTERN);
            variablesMap = parseMetadata(ddiFilePath.toString(), true);

        } catch (RuntimeException e) {
            //DDI file not found and already log - Go to next step
        } catch (IOException e) {
            log.warn("No DDI File found for {}, {} mode. Will try to use Lunatic...", campaignName, modeName);
        }
        if(variablesMap == null ){
            log.warn("DDI not found or error occurred. Trying Lunatic metadata...for {}, {} mode", campaignName, modeName);
            try {
                Path lunaticFilePath = fileUtils.findFile(String.format("%s/%s", fileUtils.getSpecFolder(campaignName), modeName), LUNATIC_FILE_PATTERN);
                return parseMetadata(lunaticFilePath.toString(), false);
            } catch (Exception ex) {
                log.error("Error reading Lunatic metadata file", ex);
                errors.add(new GenesisError(ex.toString()));
                return null;
            }
        }
/*        // Adding Eno variables if necessary
        // For review : not sure if this the best way to do it
        for (String enoVar : Arrays.stream(Constants.getEnoVariables()).toList()){
            variablesMap.putVariable(new Variable(enoVar, ));
        }*/
        return variablesMap;
    }
}