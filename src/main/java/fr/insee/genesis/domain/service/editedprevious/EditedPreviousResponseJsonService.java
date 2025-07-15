package fr.insee.genesis.domain.service.editedprevious;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import fr.insee.genesis.domain.model.editedprevious.EditedPreviousResponseModel;
import fr.insee.genesis.domain.ports.api.EditedPreviousResponseApiPort;
import fr.insee.genesis.domain.ports.spi.EditedPreviousResponsePersistancePort;
import fr.insee.genesis.domain.utils.JsonUtils;
import fr.insee.genesis.exceptions.GenesisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class EditedPreviousResponseJsonService implements EditedPreviousResponseApiPort {
    private final EditedPreviousResponsePersistancePort editedPreviousResponsePersistancePort;

    private static final int BLOCK_SIZE = 1000;

    @Autowired
    public EditedPreviousResponseJsonService(EditedPreviousResponsePersistancePort editedPreviousResponsePersistancePort) {
        this.editedPreviousResponsePersistancePort = editedPreviousResponsePersistancePort;
    }

    @Override
    public void readEditedPreviousFile(InputStream inputStream,
                                       String questionnaireId,
                                       String sourceState) throws GenesisException {
        checkSourceStateLength(sourceState);
        moveCollectionToBackup(questionnaireId);

        JsonFactory jsonFactory = new JsonFactory();
        try(JsonParser jsonParser = jsonFactory.createParser(inputStream)){
            List<EditedPreviousResponseModel> toSave = new ArrayList<>();
            boolean isTokenFound = false;
            goToEditedPreviousToken(isTokenFound, jsonParser);
            long savedCount = 0;
            Set<String> savedInterrogationIds = new HashSet<>();
            jsonParser.nextToken(); //skip field name
            jsonParser.nextToken(); //skip [
            while (jsonParser.currentToken() != JsonToken.END_ARRAY) {
                EditedPreviousResponseModel editedPreviousResponseModel = readNextEditedPrevious(
                        jsonParser,
                        questionnaireId,
                        sourceState
                );

                checkModel(editedPreviousResponseModel, jsonParser, savedInterrogationIds);

                toSave.add(editedPreviousResponseModel);
                savedInterrogationIds.add(editedPreviousResponseModel.getInterrogationId());

                if(toSave.size() >= BLOCK_SIZE){
                    savedCount = saveBlock(toSave, savedCount);
                }
                jsonParser.nextToken(); //skip }
            }
            savedCount = saveBlock(toSave, savedCount);
            log.info("Reached end of edited previous file, saved %d interrogations".formatted(savedCount));
            editedPreviousResponsePersistancePort.deleteBackup(questionnaireId);
        }catch (JsonParseException jpe){
            editedPreviousResponsePersistancePort.restoreBackup(questionnaireId);
            throw new GenesisException(400, "JSON Parsing exception : %s".formatted(jpe.toString()));
        }catch (IOException ioe){
            editedPreviousResponsePersistancePort.restoreBackup(questionnaireId);
            throw new GenesisException(500, ioe.toString());
        }
    }

    private long saveBlock(List<EditedPreviousResponseModel> toSave, long savedCount) {
        editedPreviousResponsePersistancePort.saveAll(toSave);
        savedCount += toSave.size();
        toSave.clear();
        return savedCount;
    }

    private void moveCollectionToBackup(String questionnaireId) {
        editedPreviousResponsePersistancePort.backup(questionnaireId);
        editedPreviousResponsePersistancePort.delete(questionnaireId);
    }

    private static void checkSourceStateLength(String sourceState) throws GenesisException {
        if(sourceState != null && sourceState.length() > 15){
            throw new GenesisException(400, "Source state is too long (>15 characters)");
        }
    }

    private static void goToEditedPreviousToken(boolean isTokenFound, JsonParser jsonParser) throws IOException, GenesisException {
        while (!isTokenFound){
            jsonParser.nextToken();
            if(jsonParser.currentToken() == null){
                throw new GenesisException(400, "editedPrevious object not found in JSON");
            }
            if(jsonParser.currentToken().equals(JsonToken.FIELD_NAME) && jsonParser.currentName() != null){
                if (jsonParser.currentName().equals("editedPrevious")) {
                    isTokenFound = true;
                }
            }
        }
    }

    private EditedPreviousResponseModel readNextEditedPrevious(JsonParser jsonParser,
                                                               String questionnaireId,
                                                               String sourceState
                                                               ) throws IOException {
        if(jsonParser.currentToken() != JsonToken.START_OBJECT){
            throw new JsonParseException("Expected { on line %d, got token %s".formatted(jsonParser.currentLocation().getLineNr(), jsonParser.currentToken()));
        }
        EditedPreviousResponseModel editedPreviousResponseModel = EditedPreviousResponseModel.builder()
                .questionnaireId(questionnaireId)
                .sourceState(sourceState)
                .variables(new HashMap<>())
                .build();
        jsonParser.nextToken(); // read {
        while (!jsonParser.currentToken().equals(JsonToken.END_OBJECT)){
            if(jsonParser.currentToken().equals(JsonToken.FIELD_NAME) && jsonParser.currentName().equals("interrogationId")){
                jsonParser.nextToken();
                editedPreviousResponseModel.setInterrogationId(jsonParser.getText());
                jsonParser.nextToken();
                continue;
            }
            jsonParser.nextToken();
            editedPreviousResponseModel.getVariables().put(
                    jsonParser.currentName(),
                    JsonUtils.readValue(jsonParser)
            );
            jsonParser.nextToken();
        }
        return editedPreviousResponseModel;
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

    private static void checkModel(EditedPreviousResponseModel editedPreviousResponseModel, JsonParser jsonParser, Set<String> savedInterrogationIds) throws GenesisException {
        if(editedPreviousResponseModel.getInterrogationId() == null){
            throw new GenesisException(400,
                    "Missing interrogationId on the object that ends on line %d"
                            .formatted(jsonParser.currentLocation().getLineNr())
            );
        }
        if(savedInterrogationIds.contains(editedPreviousResponseModel.getInterrogationId())){
            throw new GenesisException(400,
                    "Double interrogationId : %s".formatted(editedPreviousResponseModel.getInterrogationId()));
        }
    }
}
