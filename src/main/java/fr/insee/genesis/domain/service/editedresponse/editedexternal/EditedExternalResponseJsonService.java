package fr.insee.genesis.domain.service.editedresponse.editedexternal;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import fr.insee.genesis.domain.model.editedresponse.EditedExternalResponseModel;
import fr.insee.genesis.domain.ports.api.EditedExternalResponseApiPort;
import fr.insee.genesis.domain.ports.spi.EditedExternalResponsePersistancePort;
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
public class EditedExternalResponseJsonService implements EditedExternalResponseApiPort {
    private final EditedExternalResponsePersistancePort editedExternalResponsePersistancePort;

    private static final int BLOCK_SIZE = 1000;

    @Autowired
    public EditedExternalResponseJsonService(EditedExternalResponsePersistancePort editedExternalResponsePersistancePort) {
        this.editedExternalResponsePersistancePort = editedExternalResponsePersistancePort;
    }

    @Override
    public void readEditedExternalFile(InputStream inputStream,
                                       String questionnaireId) throws GenesisException {
        JsonFactory jsonFactory = new JsonFactory();
        moveCollectionToBackup(questionnaireId);
        try(JsonParser jsonParser = jsonFactory.createParser(inputStream)){
            List<EditedExternalResponseModel> toSave = new ArrayList<>();
            goToEditedExternalToken(jsonParser);
            long savedCount = 0;
            Set<String> savedInterrogationIds = new HashSet<>();
            jsonParser.nextToken(); //skip field name
            jsonParser.nextToken(); //skip [
            while (jsonParser.currentToken() != JsonToken.END_ARRAY) {
                EditedExternalResponseModel editedExternalResponseModel = readNextEditedExternal(
                        jsonParser,
                        questionnaireId
                );

                checkModel(editedExternalResponseModel, jsonParser, savedInterrogationIds);

                toSave.add(editedExternalResponseModel);
                savedInterrogationIds.add(editedExternalResponseModel.getInterrogationId());

                if(toSave.size() >= BLOCK_SIZE){
                    savedCount = saveBlock(toSave, savedCount);
                }
                jsonParser.nextToken(); //skip }
            }
            editedExternalResponsePersistancePort.saveAll(toSave);
            savedCount += toSave.size();
            log.info("Reached end of edited external file, saved %d interrogations".formatted(savedCount));
            editedExternalResponsePersistancePort.deleteBackup(questionnaireId);
        }catch (JsonParseException jpe){
            editedExternalResponsePersistancePort.restoreBackup(questionnaireId);
            throw new GenesisException(400, "JSON Parsing exception : %s".formatted(jpe.toString()));
        }catch (IOException ioe){
            editedExternalResponsePersistancePort.restoreBackup(questionnaireId);
            throw new GenesisException(500, ioe.toString());
        }
    }

    private static void goToEditedExternalToken(JsonParser jsonParser) throws IOException, GenesisException {
        boolean isTokenFound = false;
        while (!isTokenFound){
            jsonParser.nextToken();
            if(jsonParser.currentToken() == null){
                throw new GenesisException(400, "editedExternal object not found in JSON");
            }
            if(jsonParser.currentToken().equals(JsonToken.FIELD_NAME) && jsonParser.currentName() != null){
                if (jsonParser.currentName().equals("editedExternal")) {
                    isTokenFound = true;
                }
            }
        }
    }

    private void moveCollectionToBackup(String questionnaireId) {
        editedExternalResponsePersistancePort.backup(questionnaireId);
        editedExternalResponsePersistancePort.delete(questionnaireId);
    }

    private long saveBlock(List<EditedExternalResponseModel> toSave, long savedCount) {
        editedExternalResponsePersistancePort.saveAll(toSave);
        savedCount += toSave.size();
        toSave.clear();
        return savedCount;
    }

    private static void checkModel(EditedExternalResponseModel editedExternalResponseModel, JsonParser jsonParser, Set<String> savedInterrogationIds) throws GenesisException {
        if(editedExternalResponseModel.getInterrogationId() == null){
            throw new GenesisException(400,
                    "Missing interrogationId on the object that ends on line %d"
                            .formatted(jsonParser.currentLocation().getLineNr())
            );
        }
        if(savedInterrogationIds.contains(editedExternalResponseModel.getInterrogationId())){
            throw new GenesisException(400,
                    "Double interrogationId : %s".formatted(editedExternalResponseModel.getInterrogationId()));
        }
    }

    private EditedExternalResponseModel readNextEditedExternal(JsonParser jsonParser,
                                                               String questionnaireId
                                                               ) throws IOException {
        if(jsonParser.currentToken() != JsonToken.START_OBJECT){
            throw new JsonParseException("Expected { on line %d, got token %s".formatted(jsonParser.currentLocation().getLineNr(), jsonParser.currentToken()));
        }
        EditedExternalResponseModel editedExternalResponseModel = EditedExternalResponseModel.builder()
                .questionnaireId(questionnaireId)
                .variables(new HashMap<>())
                .build();
        jsonParser.nextToken(); // read {
        while (!jsonParser.currentToken().equals(JsonToken.END_OBJECT)){
            if(jsonParser.currentToken().equals(JsonToken.FIELD_NAME) && jsonParser.currentName().equals("interrogationId")){
                jsonParser.nextToken();
                editedExternalResponseModel.setInterrogationId(jsonParser.getText());
                jsonParser.nextToken();
                continue;
            }
            jsonParser.nextToken();
            editedExternalResponseModel.getVariables().put(
                    jsonParser.currentName(),
                    JsonUtils.readValue(jsonParser)
            );
            jsonParser.nextToken();
        }
        return editedExternalResponseModel;
    }
}
