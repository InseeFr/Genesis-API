package fr.insee.genesis.controller.rest;

import com.mongodb.client.result.UpdateResult;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.IntegrationTestAbstract;
import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.infrastructure.adapter.LunaticModelMongoAdapter;
import fr.insee.genesis.infrastructure.document.lunaticmodel.LunaticModelDocument;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.ExecutableUpdateOperation;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LunaticModelControllerIT extends IntegrationTestAbstract {

    @Mock
    private ExecutableUpdateOperation.ExecutableUpdate<LunaticModelDocument> executableUpdate;

    @Mock
    private ExecutableUpdateOperation.UpdateWithUpdate<LunaticModelDocument> updateWithUpdate;

    @Mock
    private ExecutableUpdateOperation.TerminatingUpdate<LunaticModelDocument> terminatingUpdate;

    @MockitoSpyBean
    private LunaticModelMongoAdapter lunaticModelMongoAdapterSpy;


    @BeforeEach
    void setUp() {
        when(mongoTemplate.update(LunaticModelDocument.class)).thenReturn(executableUpdate);
        when(executableUpdate.matching(any(CriteriaDefinition.class))).thenReturn(updateWithUpdate);
        when(updateWithUpdate.apply(any(Update.class))).thenReturn(terminatingUpdate);
        when(terminatingUpdate.upsert()).thenReturn(mock(UpdateResult.class));
    }

    @Nested
    @DisplayName("Save Lunatic model tests")
    class SaveLunaticModelTests{
        //HAPPY PATH
        @ParameterizedTest
        @ValueSource(strings ={
                "specs/LUNATIC-TEST/lunaticlog2021x21_web.json",
                "specs/RAWDATATESTCAMPAIGN/WEB/lunaticFAM2025X01.json"
        })
        @WithMockUser(roles = "USER_BACK_OFFICE")
        @DisplayName("Lunatic model saving test")
        @SneakyThrows
        void save_lunaticModel_test(String jsonFilePathString){
            //GIVEN
            String questionnaireId = "QUEST01";
            Path jsonFilePath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,
                    jsonFilePathString);

            String jsonBody = Files.readString(jsonFilePath);

            //WHEN
            mockMvc.perform(put("/lunatic-model/save")
                            .with(csrf())
                            .param("questionnaireId", questionnaireId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody))
                    .andExpect(status().isOk());

            // THEN
            // Capture input lunatic model to compare with mongoTemplate parameter
            ArgumentCaptor<LunaticModelModel> lunaticModelModelArgumentCaptor =
                    ArgumentCaptor.forClass(LunaticModelModel.class);
            verify(lunaticModelMongoAdapterSpy, times(1))
                    .save(lunaticModelModelArgumentCaptor.capture());
            LunaticModelModel capturedLunaticModel = lunaticModelModelArgumentCaptor.getValue();
            Assertions.assertThat(capturedLunaticModel).isNotNull();


            // Criteria check
            ArgumentCaptor<CriteriaDefinition> criteriaCaptor = ArgumentCaptor.forClass(CriteriaDefinition.class);
            verify(executableUpdate, times(1)).matching(criteriaCaptor.capture());

            Document criteriaDoc = criteriaCaptor.getValue().getCriteriaObject();
            assertThat(criteriaDoc).containsKey("$or");

            List<Document> orClauses = criteriaDoc.getList("$or", Document.class);
            assertThat(orClauses).anyMatch(doc -> questionnaireId.equals(doc.getString("collectionInstrumentId")));

            //Update check
            verify(mongoTemplate, times(1)).update(LunaticModelDocument.class);
            ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
            verify(updateWithUpdate, times(1)).apply(updateCaptor.capture());
            Document updateDoc = updateCaptor.getValue().getUpdateObject();
            assertThat((Document) updateDoc.get("$set"))
                    .containsEntry("lunaticModel", capturedLunaticModel.lunaticModel());
            assertThat((Document) updateDoc.get("$setOnInsert"))
                    .containsEntry("collectionInstrumentId", questionnaireId);

            //Upsert check
            verify(terminatingUpdate, times(1)).upsert();
        }

        //SAD PATH
        @Test
        @WithMockUser(roles = "USER_BACK_OFFICE")
        @DisplayName("Lunatic model save invalid syntax")
        @SneakyThrows
        void save_lunaticModel_syntax_error_test(){
            //GIVEN
            String questionnaireId = "QUEST01";
            Path jsonFilePath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,
                    "specs/LUNATIC-TEST-ERROR/lunaticsyntaxerror.json");

            String jsonBody = Files.readString(jsonFilePath);

            //WHEN + THEN
            mockMvc.perform(put("/lunatic-model/save")
                            .with(csrf())
                            .param("questionnaireId", questionnaireId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody))
                    .andExpect(status().isBadRequest());
            verifyNoInteractions(lunaticModelMongoAdapterSpy);
        }
    }

    @Nested
    @DisplayName("Get Lunatic model tests")
    class GetLunaticModelTests{
        //HAPPY PATH
        @Test
        @DisplayName("Get Lunatic model test")
        @WithMockUser(roles = "READER")
        @SneakyThrows
        void get_lunaticModel_test(){
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";

            String key = "key";
            String value = "value";
            Map<String, Object> lunaticModelMap = new HashMap<>();
            lunaticModelMap.put(key, value);

            LunaticModelDocument lunaticModelDocument = LunaticModelDocument.builder()
                    .collectionInstrumentId(collectionInstrumentId)
                    .lunaticModel(lunaticModelMap)
                    .recordDate(LocalDateTime.now())
                    .build();
            when(lunaticModelMongoDBRepository.findByCollectionInstrumentId(collectionInstrumentId))
                    .thenReturn(List.of(lunaticModelDocument));

            //WHEN + THEN
            mockMvc.perform(get("/lunatic-model/get")
                            .with(csrf())
                            .param("questionnaireId", collectionInstrumentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + key).value(value));
        }

        //BAD PATHS
        @Test
        @DisplayName("Get non existent Lunatic model")
        @WithMockUser(roles = "READER")
        @SneakyThrows
        void get_lunaticModel_not_found_test(){
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";

            //WHEN + THEN
            mockMvc.perform(get("/lunatic-model/get")
                            .with(csrf())
                            .param("questionnaireId", collectionInstrumentId))
                    .andExpect(status().isNotFound());
        }
    }
}