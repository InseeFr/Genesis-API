package fr.insee.genesis.domain.service.contextualvariable.external;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;
import fr.insee.genesis.domain.ports.api.ContextualExternalVariableApiPort;
import fr.insee.genesis.domain.ports.spi.ContextualExternalVariablePersistancePort;
import fr.insee.genesis.domain.utils.JsonUtils;
import fr.insee.genesis.exceptions.GenesisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class ContextualExternalVariableJsonService implements ContextualExternalVariableApiPort {
    private final ContextualExternalVariablePersistancePort contextualExternalVariablePersistancePort;

    private static final int BLOCK_SIZE = 1000;

    @Autowired
    public ContextualExternalVariableJsonService(ContextualExternalVariablePersistancePort contextualExternalVariablePersistancePort) {
        this.contextualExternalVariablePersistancePort = contextualExternalVariablePersistancePort;
    }

    @Override
    public boolean readContextualExternalFile(String questionnaireId, String filePath) throws GenesisException {
        try(FileInputStream inputStream = new FileInputStream(filePath)){
            JsonFactory jsonFactory = new JsonFactory();
            moveCollectionToBackup(questionnaireId);
            try(JsonParser jsonParser = jsonFactory.createParser(inputStream)){
                List<ContextualExternalVariableModel> toSave = new ArrayList<>();
                goToContextualExternalToken(jsonParser);
                long savedCount = 0;
                Set<String> savedInterrogationIds = new HashSet<>();
                if(jsonParser.nextToken() == null){ //skip field name, stop if end of file
                    log.warn("Reached end of file, found no contextualExternal part.");
                    return false;
                }
                jsonParser.nextToken(); //skip [
                while (jsonParser.currentToken() != JsonToken.END_ARRAY) {
                    ContextualExternalVariableModel contextualExternalVariableModel = readNextContextualExternal(
                            jsonParser,
                            questionnaireId
                    );

                    checkModel(contextualExternalVariableModel, jsonParser, savedInterrogationIds);

                    toSave.add(contextualExternalVariableModel);
                    savedInterrogationIds.add(contextualExternalVariableModel.getInterrogationId());

                    if(toSave.size() >= BLOCK_SIZE){
                        savedCount = saveBlock(toSave, savedCount);
                    }
                    jsonParser.nextToken();
                }
                contextualExternalVariablePersistancePort.saveAll(toSave);
                savedCount += toSave.size();
                log.info("Reached end of contextual external file, saved %d interrogations".formatted(savedCount));
                contextualExternalVariablePersistancePort.deleteBackup(questionnaireId);
                return true;
            }
        }catch (JsonParseException jpe){
            contextualExternalVariablePersistancePort.restoreBackup(questionnaireId);
            throw new GenesisException(400, "JSON Parsing exception : %s".formatted(jpe.toString()));
        }catch (IOException ioe){
            contextualExternalVariablePersistancePort.restoreBackup(questionnaireId);
            throw new GenesisException(500, ioe.toString());
        }
    }

    @Override
    public ContextualExternalVariableModel findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId) {
        return contextualExternalVariablePersistancePort.findByQuestionnaireIdAndInterrogationId(questionnaireId, interrogationId);
    }

    private static void goToContextualExternalToken(JsonParser jsonParser) throws IOException{
        boolean isTokenFound = false;
        while (!isTokenFound){
            jsonParser.nextToken();
            if(jsonParser.currentToken() == null){
                return;
            }
            if(jsonParser.currentToken().equals(JsonToken.FIELD_NAME)
                    && jsonParser.currentName() != null
                    && jsonParser.currentName().equals("editedExternal")) {
                    isTokenFound = true;
            }
        }
    }

    private void moveCollectionToBackup(String questionnaireId) {
        contextualExternalVariablePersistancePort.backup(questionnaireId);
        contextualExternalVariablePersistancePort.delete(questionnaireId);
    }

    private long saveBlock(List<ContextualExternalVariableModel> toSave, long savedCount) {
        contextualExternalVariablePersistancePort.saveAll(toSave);
        savedCount += toSave.size();
        toSave.clear();
        return savedCount;
    }

    private static void checkModel(ContextualExternalVariableModel contextualExternalVariableModel, JsonParser jsonParser, Set<String> savedInterrogationIds) throws GenesisException {
        if(contextualExternalVariableModel.getInterrogationId() == null){
            throw new GenesisException(400,
                    "Missing interrogationId on the object that ends on line %d"
                            .formatted(jsonParser.currentLocation().getLineNr())
            );
        }
        if(savedInterrogationIds.contains(contextualExternalVariableModel.getInterrogationId())){
            throw new GenesisException(400,
                    "Double interrogationId : %s".formatted(contextualExternalVariableModel.getInterrogationId()));
        }
    }

    private ContextualExternalVariableModel readNextContextualExternal(JsonParser jsonParser,
                                                               String questionnaireId
                                                               ) throws IOException {
        if(jsonParser.currentToken() != JsonToken.START_OBJECT){
            throw new JsonParseException("Expected { on line %d, got token %s".formatted(jsonParser.currentLocation().getLineNr(), jsonParser.currentToken()));
        }
        ContextualExternalVariableModel contextualExternalVariableModel = ContextualExternalVariableModel.builder()
                .questionnaireId(questionnaireId)
                .variables(new HashMap<>())
                .build();
        jsonParser.nextToken();
        while (!jsonParser.currentToken().equals(JsonToken.END_OBJECT)){
            if(jsonParser.currentToken().equals(JsonToken.FIELD_NAME) && jsonParser.currentName().equals("interrogationId")){
                jsonParser.nextToken();
                contextualExternalVariableModel.setInterrogationId(jsonParser.getText());
                jsonParser.nextToken();
                continue;
            }
            jsonParser.nextToken();
            contextualExternalVariableModel.getVariables().put(
                    jsonParser.currentName(),
                    JsonUtils.readValue(jsonParser)
            );
            jsonParser.nextToken();
        }
        return contextualExternalVariableModel;
    }
}
