package fr.insee.genesis.domain.service.contextualvariable.previous;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.domain.ports.api.ContextualPreviousVariableApiPort;
import fr.insee.genesis.domain.ports.spi.ContextualPreviousVariablePersistancePort;
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
public class ContextualPreviousVariableJsonService implements ContextualPreviousVariableApiPort {
    private final ContextualPreviousVariablePersistancePort contextualPreviousVariablePersistancePort;

    private static final int BLOCK_SIZE = 1000;

    @Autowired
    public ContextualPreviousVariableJsonService(ContextualPreviousVariablePersistancePort contextualPreviousVariablePersistancePort) {
        this.contextualPreviousVariablePersistancePort = contextualPreviousVariablePersistancePort;
    }

    @Override
    public boolean readContextualPreviousFile(String collectionInstrumentId,
                                              String sourceState,
                                              String filePath) throws GenesisException {
        try(FileInputStream inputStream = new FileInputStream(filePath)){
            checkSourceStateLength(sourceState);
            moveCollectionToBackup(collectionInstrumentId);

            JsonFactory jsonFactory = new JsonFactory();
            try (JsonParser jsonParser = jsonFactory.createParser(inputStream)) {
                List<ContextualPreviousVariableModel> toSave = new ArrayList<>();
                goToEditedPreviousToken(jsonParser);
                long savedCount = 0;
                Set<String> savedInterrogationIds = new HashSet<>();
                if (jsonParser.nextToken() == null) { //skip field name, stop if end of file
                    log.warn("Reached end of file, found no EditedPrevious part.");
                    return false;
                }
                jsonParser.nextToken(); //skip [
                while (jsonParser.currentToken() != JsonToken.END_ARRAY) {
                    ContextualPreviousVariableModel contextualPreviousVariableModel = readNextContextualPrevious(
                            jsonParser,
                            collectionInstrumentId,
                            sourceState
                    );

                    checkModel(contextualPreviousVariableModel, jsonParser, savedInterrogationIds);

                    toSave.add(contextualPreviousVariableModel);
                    savedInterrogationIds.add(contextualPreviousVariableModel.getInterrogationId());

                    if (toSave.size() >= BLOCK_SIZE) {
                        savedCount = saveBlock(toSave, savedCount);
                    }
                    jsonParser.nextToken();
                }
                savedCount = saveBlock(toSave, savedCount);
                log.info("Reached end of contextual previous file, saved %d interrogations".formatted(savedCount));
                contextualPreviousVariablePersistancePort.deleteBackup(collectionInstrumentId);
                return true;
            }
        }catch (JsonParseException jpe){
            contextualPreviousVariablePersistancePort.restoreBackup(collectionInstrumentId);
            throw new GenesisException(400, "JSON Parsing exception : %s".formatted(jpe.toString()));
        }catch (IOException ioe){
            contextualPreviousVariablePersistancePort.restoreBackup(collectionInstrumentId);
            throw new GenesisException(500, ioe.toString());
        }
    }

    @Override
    public ContextualPreviousVariableModel findByCollectionInstrumentIdAndInterrogationId(String collectionInstrumentId, String interrogationId) {
        return contextualPreviousVariablePersistancePort.findByCollectionInstrumentIdAndInterrogationId(
                collectionInstrumentId,
                interrogationId
        );
    }

    private long saveBlock(List<ContextualPreviousVariableModel> toSave, long savedCount) {
        contextualPreviousVariablePersistancePort.saveAll(toSave);
        savedCount += toSave.size();
        toSave.clear();
        return savedCount;
    }

    private void moveCollectionToBackup(String collectionInstrumentId) {
        contextualPreviousVariablePersistancePort.backup(collectionInstrumentId);
        contextualPreviousVariablePersistancePort.delete(collectionInstrumentId);
    }

    private static void checkSourceStateLength(String sourceState) throws GenesisException {
        if(sourceState != null && sourceState.length() > 15){
            throw new GenesisException(400, "Source state is too long (>15 characters)");
        }
    }

    private static void goToEditedPreviousToken(JsonParser jsonParser) throws IOException{
        boolean isTokenFound = false;
        while (!isTokenFound){
            jsonParser.nextToken();
            if(jsonParser.currentToken() == null){
                return;
            }
            if(jsonParser.currentToken().equals(JsonToken.FIELD_NAME)
                    && jsonParser.currentName() != null
                    && jsonParser.currentName().equals("editedPrevious")) {
                    isTokenFound = true;
            }
        }
    }

    private ContextualPreviousVariableModel readNextContextualPrevious(JsonParser jsonParser,
                                                               String collectionInstrumentId,
                                                               String sourceState
                                                               ) throws IOException {
        if(jsonParser.currentToken() != JsonToken.START_OBJECT){
            throw new JsonParseException("Expected { on line %d, got token %s".formatted(jsonParser.currentLocation().getLineNr(), jsonParser.currentToken()));
        }
        ContextualPreviousVariableModel contextualPreviousVariableModel = ContextualPreviousVariableModel.builder()
                .collectionInstrumentId(collectionInstrumentId)
                .sourceState(sourceState)
                .variables(new HashMap<>())
                .build();
        jsonParser.nextToken();
        while (!jsonParser.currentToken().equals(JsonToken.END_OBJECT)){
            if(jsonParser.currentToken().equals(JsonToken.FIELD_NAME) && jsonParser.currentName().equals("interrogationId")){
                jsonParser.nextToken();
                contextualPreviousVariableModel.setInterrogationId(jsonParser.getText());
                jsonParser.nextToken();
                continue;
            }
            jsonParser.nextToken();
            contextualPreviousVariableModel.getVariables().put(
                    jsonParser.currentName(),
                    JsonUtils.readValue(jsonParser)
            );
            jsonParser.nextToken();
        }
        return contextualPreviousVariableModel;
    }

    private Object readValue(JsonParser jsonParser) throws IOException{
        switch (jsonParser.currentToken()){
            case VALUE_STRING -> {
                return jsonParser.getText();
            }
            case VALUE_NUMBER_INT -> {
                return jsonParser.getIntValue();
            }
            case VALUE_NUMBER_FLOAT -> {
                return jsonParser.getDoubleValue();
            }
            case VALUE_TRUE, VALUE_FALSE -> {
                return jsonParser.getBooleanValue();
            }
            case VALUE_NULL -> {
                return null;
            }
            case START_ARRAY -> {
                return readArray(jsonParser);
            }
            case null, default -> throw new JsonParseException("Unexpected token %s on line %d".formatted(
                    jsonParser.currentToken(), jsonParser.currentLocation().getLineNr())
            );
        }
    }

    private List<Object> readArray(JsonParser jsonParser) throws IOException {
        List<Object> list = new ArrayList<>();
        jsonParser.nextToken(); //Read [
        while(!jsonParser.currentToken().equals(JsonToken.END_ARRAY)){
            list.add(readValue(jsonParser));
            jsonParser.nextToken();
        }
        return list;
    }

    private static void checkModel(ContextualPreviousVariableModel contextualPreviousVariableModel, JsonParser jsonParser, Set<String> savedInterrogationIds) throws GenesisException {
        if(contextualPreviousVariableModel.getInterrogationId() == null){
            throw new GenesisException(400,
                    "Missing interrogationId on the object that ends on line %d"
                            .formatted(jsonParser.currentLocation().getLineNr())
            );
        }
        if(savedInterrogationIds.contains(contextualPreviousVariableModel.getInterrogationId())){
            throw new GenesisException(400,
                    "Double interrogationId : %s".formatted(contextualPreviousVariableModel.getInterrogationId()));
        }
    }
}
