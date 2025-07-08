package fr.insee.genesis.domain.service.editedprevious;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import fr.insee.genesis.domain.model.editedprevious.EditedPreviousResponseModel;
import fr.insee.genesis.domain.ports.api.EditedPreviousResponseApiPort;
import fr.insee.genesis.domain.ports.spi.EditedPreviousResponsePersistancePort;
import fr.insee.genesis.exceptions.GenesisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        if(sourceState.length() > 15){
            throw new GenesisException(400, "Source state is too long (>15 characters)");
        }

        JsonFactory jsonFactory = new JsonFactory();
        editedPreviousResponsePersistancePort.backup(questionnaireId);
        editedPreviousResponsePersistancePort.delete(questionnaireId);
        try(JsonParser jsonParser = jsonFactory.createParser(inputStream)){
            List<EditedPreviousResponseModel> toSave = new ArrayList<>();
            while (!jsonParser.currentName().equals("editedPrevious")
                    && jsonParser.currentToken() != JsonToken.START_ARRAY){
                jsonParser.nextToken();
            }
            long savedCount = 0;
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                toSave.add(readNextEditedPrevious(jsonParser,questionnaireId,sourceState));
                if(toSave.size() >= BLOCK_SIZE){
                    editedPreviousResponsePersistancePort.saveAll(questionnaireId, toSave);
                    savedCount += toSave.size();
                    toSave.clear();
                }
            }
            log.info("Reached end of edited previous file, saved %d interrogations".formatted(savedCount));
            editedPreviousResponsePersistancePort.saveAll(questionnaireId, toSave);
            editedPreviousResponsePersistancePort.deleteBackup(questionnaireId);
        }catch (JsonParseException jpe){
            editedPreviousResponsePersistancePort.restoreBackup(questionnaireId);
            throw new GenesisException(400, "JSON Parsing exception : %s".formatted(jpe.toString()));
        }catch (IOException ioe){
            editedPreviousResponsePersistancePort.restoreBackup(questionnaireId);
            throw new GenesisException(500, ioe.toString());
        }
    }

    private EditedPreviousResponseModel readNextEditedPrevious(JsonParser jsonParser,
                                                               String questionnaireId,
                                                               String sourceState
                                                               ) throws IOException, JsonParseException {
        if(jsonParser.nextToken() != JsonToken.START_OBJECT){
            throw new JsonParseException("Expected { on line %d".formatted(jsonParser.currentLocation().getLineNr()));
        }
        EditedPreviousResponseModel editedPreviousResponseModel = EditedPreviousResponseModel.builder()
                .questionnaireId(questionnaireId)
                .sourceState(sourceState)
                .variables(new HashMap<>())
                .build();
        jsonParser.nextToken(); // read {
        while (!jsonParser.nextToken().equals(JsonToken.END_OBJECT)){
            if(jsonParser.currentName().equals("interrogationId")){
                editedPreviousResponseModel.setInterrogationId(jsonParser.getText());
                continue;
            }
            editedPreviousResponseModel.getVariables().put(
                    jsonParser.currentName(),
                    readValue(jsonParser)
            );
        }
        return editedPreviousResponseModel;
    }

    private Object readValue(JsonParser jsonParser) throws IOException, JsonParseException{
        switch (jsonParser.currentToken()){
            case VALUE_STRING -> {
                return jsonParser.getText();
            }
            case VALUE_NUMBER_INT -> {
                return jsonParser.getIntValue();
            }
            case VALUE_NUMBER_FLOAT -> {
                return jsonParser.getFloatValue();
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
        while(jsonParser.nextToken() != JsonToken.END_ARRAY){
            list.add(readValue(jsonParser));
        }
        return list;
    }
}
