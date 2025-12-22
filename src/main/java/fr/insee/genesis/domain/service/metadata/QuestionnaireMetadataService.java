package fr.insee.genesis.domain.service.metadata;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.reader.ReaderUtils;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.metadata.QuestionnaireMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.QuestionnaireMetadataApiPort;
import fr.insee.genesis.domain.ports.spi.QuestionnaireMetadataPersistencePort;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class QuestionnaireMetadataService implements QuestionnaireMetadataApiPort {
    private static final String DDI_FILE_PATTERN = "ddi[\\w,\\s-]+\\.xml";
    private static final String LUNATIC_FILE_PATTERN = "lunatic[\\w,\\s-]+\\.json";

    QuestionnaireMetadataPersistencePort questionnaireMetadataPersistencePort;


    @Override
    public MetadataModel find(String collectionInstrumentId, Mode mode) throws GenesisException {
        List<QuestionnaireMetadataModel> questionnaireMetadataModels =
                questionnaireMetadataPersistencePort.find(collectionInstrumentId, mode);
        if(questionnaireMetadataModels.isEmpty()){
            throw new GenesisException(404, "Collection instrument metadata not found");
        }
        return questionnaireMetadataModels.getFirst().metadataModel();
    }

    @Override
    public MetadataModel loadAndSaveIfNotExists(String campaignName, String collectionInstrumentId, Mode mode, FileUtils fileUtils,
                                                List<GenesisError> errors) throws GenesisException {
        List<QuestionnaireMetadataModel> questionnaireMetadataModels =
                questionnaireMetadataPersistencePort.find(collectionInstrumentId.toUpperCase(), mode);
        if(questionnaireMetadataModels.isEmpty() || questionnaireMetadataModels.getFirst().metadataModel() == null){
            MetadataModel metadataModel = readMetadatas(collectionInstrumentId, mode.getModeName(), fileUtils, errors);
            saveMetadata(collectionInstrumentId.toUpperCase(), mode, metadataModel);
            return metadataModel;
        }
        return questionnaireMetadataModels.getFirst().metadataModel();
    }

    private void saveMetadata(String collectionInstrumentId, Mode mode, MetadataModel metadataModel) {
        questionnaireMetadataPersistencePort.save(
                new QuestionnaireMetadataModel(
                        collectionInstrumentId,
                        mode,
                        metadataModel
                )
        );
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
    private MetadataModel readMetadatas(String campaignName, String modeName, FileUtils fileUtils,
                                  List<GenesisError> errors) throws GenesisException{

        Path ddiFilePath;
        Path lunaticFilePath;
        MetadataModel metadataModel = null;
        try {
            ddiFilePath = fileUtils.findFile(String.format("%s/%s", fileUtils.getSpecFolder(campaignName), modeName), DDI_FILE_PATTERN);
            lunaticFilePath = fileUtils.findFile(String.format("%s/%s", fileUtils.getSpecFolder(campaignName), modeName), LUNATIC_FILE_PATTERN);
            metadataModel = parseMetadata(lunaticFilePath, ddiFilePath);
        } catch (RuntimeException e) {
            //DDI file not found and already log - Go to next step
        } catch (IOException e) {
            log.warn("No DDI File found for {}, {} mode. Will try to use Lunatic...", campaignName, modeName);
        }
        if(metadataModel == null ){
            log.warn("DDI not found or error occurred. Trying Lunatic metadata...for {}, {} mode", campaignName, modeName);
            try {
                lunaticFilePath = fileUtils.findFile(String.format("%s/%s", fileUtils.getSpecFolder(campaignName), modeName), LUNATIC_FILE_PATTERN);
                return parseMetadata(lunaticFilePath, null);
            } catch (Exception ex) {
                log.error("Error reading Lunatic metadata file", ex);
                errors.add(new GenesisError(ex.toString()));
                return null;
            }
        }

        if(!errors.isEmpty()){
            throw new GenesisException(404, errors.getLast().getMessage());
        }
        return metadataModel;
    }

    /**
     * Parse metadata file
     *
     * @param lunaticFilePath path to the DDI metadata file
     * @param ddiFilePath path to the DDI metadata file, will parse only lunatic if null
     * @return VariablesMap or null if an error occurs
     */
    private MetadataModel parseMetadata(Path lunaticFilePath, Path ddiFilePath) {
        try {
            log.info("Try to read {} file: {}", ddiFilePath != null ? "DDI" : "Lunatic", ddiFilePath);

            if (ddiFilePath != null) {
                InputStream metadataInputStream = new FileInputStream(ddiFilePath.toFile());
                InputStream lunaticInputStream = new FileInputStream(lunaticFilePath.toFile());
                MetadataModel metadataModel = ReaderUtils.getMetadataFromDDIAndLunatic(
                        ddiFilePath.toFile().toURI().toURL().toString(),
                        metadataInputStream,
                        lunaticInputStream
                );
                // Temporary solution
                // the logic of adding variables from lunatic to the ones present in the DDI needs to be implemented in BPM
                // (only in Kraftwerk for the moment)
                for (String enoVar : Constants.getEnoVariables()){
                    metadataModel.getVariables().putVariable(new Variable(enoVar, metadataModel.getRootGroup(), VariableType.STRING));
                }
                return metadataModel;
            }
            return LunaticReader.getMetadataFromLunatic(new FileInputStream(lunaticFilePath.toFile()));
        } catch (MetadataParserException | IOException e) {
            log.error("Error reading metadata file", e);
            return null;
        }
    }

    @Override
    public void remove(String collectionInstrumentId, Mode mode) {
        questionnaireMetadataPersistencePort.remove(collectionInstrumentId, mode);
    }

    @Override
    public void save(String collectionInstrumentId, Mode mode, MetadataModel metadataModel) {
        questionnaireMetadataPersistencePort.save(new QuestionnaireMetadataModel(
                collectionInstrumentId,
                mode,
                metadataModel
        ));
    }
}
