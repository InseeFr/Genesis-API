package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.dto.EditedResponseDto;
import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import fr.insee.genesis.controller.dto.VariableStateDto;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.service.editedresponse.EditedResponseJsonService;
import fr.insee.genesis.infrastructure.document.editedexternal.EditedExternalResponseDocument;
import fr.insee.genesis.infrastructure.document.editedprevious.EditedPreviousResponseDocument;
import fr.insee.genesis.stubs.EditedExternalResponsePersistancePortStub;
import fr.insee.genesis.stubs.EditedPreviousResponsePersistancePortStub;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
class EditedResponseControllerTest {

    private static final String QUESTIONNAIRE_ID = "TEST-EDITED";
    private static final String INTERROGATION_ID_1 = "TEST1";
    private static final String INTERROGATION_ID_2 = "TEST2";

    private static final EditedPreviousResponsePersistancePortStub previousStub =
            new EditedPreviousResponsePersistancePortStub();
    private static final EditedExternalResponsePersistancePortStub externalStub =
            new EditedExternalResponsePersistancePortStub();

    private final EditedResponseController editedResponseController = new EditedResponseController(
            new EditedResponseJsonService(
                    previousStub,
                    externalStub
            )
    );

    @BeforeEach
    void clean(){
        previousStub.getMongoStub().clear();
        externalStub.getMongoStub().clear();

        previousStub.getMongoStub().put(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME, new ArrayList<>());
        externalStub.getMongoStub().put(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME, new ArrayList<>());
    }

    //OK CASES
    @Test
    void getEditedResponses_test() {
        //GIVEN
        previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME).addAll(
                getEditedPreviousTestDocuments()
        );
        externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME).addAll(
                getEditedExternalTestDocuments()
        );

        //WHEN
        ResponseEntity<Object> response = editedResponseController.getEditedResponses(QUESTIONNAIRE_ID, INTERROGATION_ID_1);

        //THEN
        if(!response.getStatusCode().is2xxSuccessful()){
            log.error((String)response.getBody());
        }
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isInstanceOf(EditedResponseDto.class);

        EditedPreviousResponseDocument editedPreviousResponseDocument = previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME).stream().filter(
                editedPreviousResponseDocument1 ->
                        editedPreviousResponseDocument1.getQuestionnaireId().equals(QUESTIONNAIRE_ID)
                                && editedPreviousResponseDocument1.getInterrogationId().equals(INTERROGATION_ID_1)
        ).toList().getFirst();
        EditedExternalResponseDocument editedExternalResponseDocument =
                externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME).stream().filter(
                        editedExternalResponseDocument1 ->
                                editedExternalResponseDocument1.getQuestionnaireId().equals(QUESTIONNAIRE_ID)
                                        && editedExternalResponseDocument1.getInterrogationId().equals(INTERROGATION_ID_1)
                ).toList().getFirst();
        EditedResponseDto editedResponseDto = (EditedResponseDto) response.getBody();
        Assertions.assertThat(editedResponseDto.interrogationId()).isEqualTo(INTERROGATION_ID_1);
        assertDocumentEqualToDto(editedPreviousResponseDocument, editedResponseDto);
        assertDocumentEqualToDto(editedExternalResponseDocument, editedResponseDto);
    }

    @Test
    void getEditedResponses_test_no_external() {
        //GIVEN
        previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME).addAll(
                getEditedPreviousTestDocuments()
        );
        externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME).addAll(
                getEditedExternalTestDocuments()
        );

        //WHEN
        ResponseEntity<Object> response = editedResponseController.getEditedResponses(QUESTIONNAIRE_ID, INTERROGATION_ID_2);

        //THEN
        if(!response.getStatusCode().is2xxSuccessful()){
            log.error((String)response.getBody());
        }
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isInstanceOf(EditedResponseDto.class);

        EditedPreviousResponseDocument editedPreviousResponseDocument = previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME).stream().filter(
                editedPreviousResponseDocument1 ->
                        editedPreviousResponseDocument1.getQuestionnaireId().equals(QUESTIONNAIRE_ID)
                                && editedPreviousResponseDocument1.getInterrogationId().equals(INTERROGATION_ID_2)
        ).toList().getFirst();

        EditedResponseDto editedResponseDto = (EditedResponseDto) response.getBody();
        Assertions.assertThat(editedResponseDto.interrogationId()).isEqualTo(INTERROGATION_ID_2);
        assertDocumentEqualToDto(editedPreviousResponseDocument, editedResponseDto);
        Assertions.assertThat(editedResponseDto.editedExternal()).isNotNull().isEmpty();
    }

    private List<EditedPreviousResponseDocument> getEditedPreviousTestDocuments() {
        List<EditedPreviousResponseDocument> editedPreviousResponseDocumentList = new ArrayList<>();

        EditedPreviousResponseDocument editedPreviousResponseDocument = new EditedPreviousResponseDocument();
        editedPreviousResponseDocument.setQuestionnaireId(QUESTIONNAIRE_ID);
        editedPreviousResponseDocument.setInterrogationId(INTERROGATION_ID_1);
        editedPreviousResponseDocument.setVariables(new HashMap<>());
        editedPreviousResponseDocument.getVariables().put("TEXTECOURT", "");
        editedPreviousResponseDocument.getVariables().put("TEXTELONG", "test d'une donnée antérieure sur un texte long pour voir comment ça marche");
        editedPreviousResponseDocument.getVariables().put("FLOAT", 50.25d);
        editedPreviousResponseDocument.getVariables().put("INTEGER", null);
        editedPreviousResponseDocument.getVariables().put("BOOLEEN", true);
        editedPreviousResponseDocument.getVariables().put("DROPDOWN", "03");
        editedPreviousResponseDocument.getVariables().put("QCM_B1", true);
        editedPreviousResponseDocument.getVariables().put("QCM_B2", false);
        editedPreviousResponseDocument.getVariables().put("QCM_B4", true);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A11", 200);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A12", 150);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A23", 1000);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A24", null);
        editedPreviousResponseDocument.getVariables().put("TABOFATS1", List.of("AA","","BB","CC"));
        editedPreviousResponseDocument.getVariables().put("TABOFATS3", Arrays.asList(5,null,3));
        editedPreviousResponseDocumentList.add(editedPreviousResponseDocument);

        editedPreviousResponseDocument = new EditedPreviousResponseDocument();
        editedPreviousResponseDocument.setQuestionnaireId(QUESTIONNAIRE_ID);
        editedPreviousResponseDocument.setInterrogationId(INTERROGATION_ID_2);
        editedPreviousResponseDocument.setVariables(new HashMap<>());
        editedPreviousResponseDocument.getVariables().put("TEXTECOURT", "test previous");
        editedPreviousResponseDocument.getVariables().put("TEXTELONG", "");
        editedPreviousResponseDocument.getVariables().put("FLOAT", 12.2d);
        editedPreviousResponseDocument.getVariables().put("BOOLEEN", false);
        editedPreviousResponseDocument.getVariables().put("DROPDOWN", "");
        editedPreviousResponseDocument.getVariables().put("QCM_B1", false);
        editedPreviousResponseDocument.getVariables().put("QCM_B2", false);
        editedPreviousResponseDocument.getVariables().put("QCM_B5", true);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A11", 1);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A12", 2);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A23", 3);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A24", 4);
        editedPreviousResponseDocument.getVariables().put("TABOFATS1", List.of("BB","BB"));
        editedPreviousResponseDocument.getVariables().put("TABOFATS3", List.of(10,4,0));
        editedPreviousResponseDocumentList.add(editedPreviousResponseDocument);

        return editedPreviousResponseDocumentList;
    }

    private List<EditedExternalResponseDocument> getEditedExternalTestDocuments() {
        List<EditedExternalResponseDocument> editedExternalResponseDocumentList = new ArrayList<>();

        EditedExternalResponseDocument editedExternalResponseDocument = new EditedExternalResponseDocument();
        editedExternalResponseDocument.setQuestionnaireId(QUESTIONNAIRE_ID);
        editedExternalResponseDocument.setInterrogationId(INTERROGATION_ID_1);
        editedExternalResponseDocument.setVariables(new HashMap<>());
        editedExternalResponseDocument.getVariables().put("TVA", 302.34d);
        editedExternalResponseDocument.getVariables().put("CA", 22.45d);
        editedExternalResponseDocument.getVariables().put("COM_AUTRE", "blablablabla");
        editedExternalResponseDocument.getVariables().put("SECTEUR", "110110110");
        editedExternalResponseDocument.getVariables().put("CATEGORIE", "");
        editedExternalResponseDocument.getVariables().put("INTERRO_N_1", true);
        editedExternalResponseDocument.getVariables().put("INTERRO_N_2", false);
        editedExternalResponseDocument.getVariables().put("NAF25", "9560Y");
        editedExternalResponseDocument.getVariables().put("POIDS", null);
        editedExternalResponseDocument.getVariables().put("MILLESIME", "2024");
        editedExternalResponseDocument.getVariables().put("NSUBST", true);
        editedExternalResponseDocument.getVariables().put("TAB_EXTNUM", Arrays.asList(50,23,10,null));
        editedExternalResponseDocument.getVariables().put("TAB_EXTCAR", Arrays.asList("A", "", "B"));
        editedExternalResponseDocumentList.add(editedExternalResponseDocument);
        return editedExternalResponseDocumentList;
    }


    private void assertDocumentEqualToDto(EditedPreviousResponseDocument editedPreviousResponseDocument,
                                          EditedResponseDto editedResponseDto) {
        //For each variable of document
        for (Map.Entry<String, Object> documentVariable : editedPreviousResponseDocument.getVariables().entrySet()) {
            //Get edited previous dtos of that variable (1 per iteration)
            List<VariableQualityToolDto> variableQualityToolDtosOfEntry =
                    editedResponseDto.editedPrevious().stream().filter(
                            variableQualityToolDto -> variableQualityToolDto.getVariableName().equals(documentVariable.getKey())
                    ).toList();
            assertEntryEqualToDto(documentVariable, variableQualityToolDtosOfEntry);
        }
    }

    private void assertDocumentEqualToDto(EditedExternalResponseDocument editedExternalResponseDocument,
                                          EditedResponseDto editedResponseDto) {
        //For each variable of document
        for (Map.Entry<String, Object> documentVariable : editedExternalResponseDocument.getVariables().entrySet()) {
            //Get edited previous dtos of that variable (1 per iteration)
            List<VariableQualityToolDto> variableQualityToolDtosOfEntry =
                    editedResponseDto.editedExternal().stream().filter(
                            variableQualityToolDto -> variableQualityToolDto.getVariableName().equals(documentVariable.getKey())
                    ).toList();
            assertEntryEqualToDto(documentVariable, variableQualityToolDtosOfEntry);
        }
    }

    @SuppressWarnings("unchecked")
    private void assertEntryEqualToDto(Map.Entry<String, Object> documentVariable,
                                       List<VariableQualityToolDto> variableQualityToolDtosOfEntry) {
        Assertions.assertThat(variableQualityToolDtosOfEntry).isNotEmpty();

        //If that variable is not a list
        if (!(documentVariable.getValue() instanceof List<?>)) {
            Assertions.assertThat(variableQualityToolDtosOfEntry).hasSize(1);
            List<VariableStateDto> variableStateDtos =
                    variableQualityToolDtosOfEntry.getFirst().getVariableStateDtoList();

            Assertions.assertThat(variableStateDtos).hasSize(1); // Only 1 state
            Assertions.assertThat(variableStateDtos.getFirst().getState()).isEqualTo(DataState.COLLECTED);
            Assertions.assertThat(variableStateDtos.getFirst().getValue()).isEqualTo(documentVariable.getValue());
            return;
        }
        int i = 1;
        for (Object documentVariableElement : (List<Object>) documentVariable.getValue()) {
            int finalI = i;
            List<VariableQualityToolDto> variableQualityToolDtosOfIteration =
                    variableQualityToolDtosOfEntry.stream().filter(
                            variableQualityToolDto -> variableQualityToolDto.getIteration().equals(finalI)
                    ).toList();
            Assertions.assertThat(variableQualityToolDtosOfIteration).hasSize(1);

            List<VariableStateDto> variableStateDtos =
                    variableQualityToolDtosOfIteration.getFirst().getVariableStateDtoList();
            Assertions.assertThat(variableStateDtos).hasSize(1);
            Assertions.assertThat(variableStateDtos.getFirst().getState()).isEqualTo(DataState.COLLECTED);
            Assertions.assertThat(variableStateDtos.getFirst().getValue()).isEqualTo(documentVariableElement);
            i++;
        }
    }

    @Test
    void getEditedResponses_test_not_found(){
        //GIVEN
        //Empty stubs from clean()

        //WHEN
        ResponseEntity<Object> response = editedResponseController.getEditedResponses(QUESTIONNAIRE_ID,
                INTERROGATION_ID_1);

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isInstanceOf(EditedResponseDto.class);
        EditedResponseDto editedResponseDto = (EditedResponseDto) response.getBody();
        Assertions.assertThat(editedResponseDto.interrogationId()).isEqualTo(INTERROGATION_ID_1);
        Assertions.assertThat(editedResponseDto.editedPrevious()).isNotNull().isEmpty();
        Assertions.assertThat(editedResponseDto.editedExternal()).isNotNull().isEmpty();
    }
}
