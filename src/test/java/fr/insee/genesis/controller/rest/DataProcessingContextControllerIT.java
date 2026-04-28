package fr.insee.genesis.controller.rest;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.IntegrationTestAbstract;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionScheduleV2;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DataProcessingContextControllerIT extends IntegrationTestAbstract {

    @Nested
    @DisplayName("Data processing context save tests")
    class DataProcessingContextSavingTests{
        //HAPPY PATHS
        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        @DisplayName("Save new data processing context")
        @WithMockUser(roles = "SCHEDULER")
        @SneakyThrows
        void save_context_test(boolean withReview){
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";

            //WHEN
            mockMvc.perform(put("/contexts/%s/review".formatted(collectionInstrumentId))
                            .with(csrf())
                            .param("withReview", String.valueOf(withReview))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            //THEN
            ArgumentCaptor<DataProcessingContextDocument> dataProcessingContextDocumentCaptor =
                    ArgumentCaptor.forClass(DataProcessingContextDocument.class);
            verify(dataProcessingContextMongoDBRepository,times(1)).save(
                    dataProcessingContextDocumentCaptor.capture()
            );

            DataProcessingContextDocument dataProcessingContextDocument = dataProcessingContextDocumentCaptor.getValue();
            Assertions.assertThat(dataProcessingContextDocument).isNotNull();
            Assertions.assertThat(dataProcessingContextDocument.getCollectionInstrumentId())
                    .isEqualTo(collectionInstrumentId);
            Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList())
                    .isNotNull().isEmpty();
            Assertions.assertThat(dataProcessingContextDocument.isWithReview())
                    .isEqualTo(withReview);
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        @DisplayName("Overwrite data processing context")
        @WithMockUser(roles = "SCHEDULER")
        @SneakyThrows
        void overwrite_context_test(boolean withReview){
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            String frequency = "0 0 0 0 0 0";
            ServiceToCall serviceToCall = ServiceToCall.GENESIS;
            KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                    null,
                    frequency,
                    serviceToCall,
                    start,
                    end,
                    null
            );
            List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList = new ArrayList<>();
            kraftwerkExecutionScheduleList.add(kraftwerkExecutionSchedule);
            DataProcessingContextDocument dataProcessingContextDocument = new DataProcessingContextDocument();
            dataProcessingContextDocument.setCollectionInstrumentId(collectionInstrumentId);
            //Reversed withReview
            dataProcessingContextDocument.setWithReview(!withReview);
            dataProcessingContextDocument.setKraftwerkExecutionScheduleList(kraftwerkExecutionScheduleList);
            when(dataProcessingContextMongoDBRepository.findByCollectionInstrumentIdList(anyList()))
                    .thenReturn(List.of(dataProcessingContextDocument));

            //WHEN
            mockMvc.perform(put("/contexts/%s/review".formatted(collectionInstrumentId))
                            .with(csrf())
                            .param("withReview", String.valueOf(withReview))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            //THEN
            ArgumentCaptor<DataProcessingContextDocument> dataProcessingContextDocumentCaptor =
                    ArgumentCaptor.forClass(DataProcessingContextDocument.class);
            verify(dataProcessingContextMongoDBRepository,times(1)).save(
                    dataProcessingContextDocumentCaptor.capture()
            );

            //Saved document content
            DataProcessingContextDocument savedDataProcessingContextDocument = dataProcessingContextDocumentCaptor.getValue();
            Assertions.assertThat(savedDataProcessingContextDocument).isNotNull();
            Assertions.assertThat(savedDataProcessingContextDocument.getCollectionInstrumentId())
                    .isEqualTo(collectionInstrumentId);
            Assertions.assertThat(savedDataProcessingContextDocument.isWithReview())
                    .isEqualTo(withReview);

            //We need to keep the schedule
            Assertions.assertThat(savedDataProcessingContextDocument.getKraftwerkExecutionScheduleList())
                    .isNotNull().hasSize(1);
            KraftwerkExecutionSchedule savedSchedule =
                    savedDataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst();
            Assertions.assertThat(savedSchedule).isNotNull();
            Assertions.assertThat(savedSchedule.getFrequency()).isEqualTo(frequency);
            Assertions.assertThat(savedSchedule.getServiceToCall()).isEqualTo(serviceToCall);
            Assertions.assertThat(savedSchedule.getScheduleBeginDate()).isEqualTo(start);
            Assertions.assertThat(savedSchedule.getScheduleEndDate()).isEqualTo(end);
            Assertions.assertThat(savedSchedule.getTrustParameters()).isNull();
        }

        @Test
        @DisplayName("Save new data processing context with null withReview, false expected")
        @WithMockUser(roles = "SCHEDULER")
        @SneakyThrows
        void save_context_no_withReview_test(){
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";

            //WHEN
            mockMvc.perform(put("/contexts/%s/review".formatted(collectionInstrumentId))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            //THEN
            ArgumentCaptor<DataProcessingContextDocument> dataProcessingContextDocumentCaptor =
                    ArgumentCaptor.forClass(DataProcessingContextDocument.class);
            verify(dataProcessingContextMongoDBRepository,times(1)).save(
                    dataProcessingContextDocumentCaptor.capture()
            );

            DataProcessingContextDocument dataProcessingContextDocument = dataProcessingContextDocumentCaptor.getValue();
            Assertions.assertThat(dataProcessingContextDocument).isNotNull();
            Assertions.assertThat(dataProcessingContextDocument.getCollectionInstrumentId())
                    .isEqualTo(collectionInstrumentId);
            Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList())
                    .isNotNull().isEmpty();
            Assertions.assertThat(dataProcessingContextDocument.isWithReview())
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("Get schedules tests")
    class GetSchedulesTests{
        //HAPPY PATHS
        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Get schedules V1 test")
        @SneakyThrows
        void get_schedulesV1_test(){
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            ObjectId objectId = new ObjectId();
            String frequency1 = "0 0 0 0 0 0";
            String frequency2 = "0 0 0 0 0 1";

            DataProcessingContextDocument dataProcessingContextDocument = new DataProcessingContextDocument();
            dataProcessingContextDocument.setId(objectId);
            dataProcessingContextDocument.setPartitionId(collectionInstrumentId);
            dataProcessingContextDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
            dataProcessingContextDocument.getKraftwerkExecutionScheduleList().add(new KraftwerkExecutionSchedule(
                    collectionInstrumentId,
                    frequency1,
                    null,
                    null,
                    null,
                    null
            ));
            dataProcessingContextDocument.getKraftwerkExecutionScheduleList().add(new KraftwerkExecutionSchedule(
                    collectionInstrumentId,
                    frequency2,
                    null,
                    null,
                    null,
                    null
            ));
            when(dataProcessingContextMongoDBRepository.findAll()).thenReturn(List.of(dataProcessingContextDocument));

            //WHEN
            mockMvc.perform(get("/contexts/schedules/v1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    //THEN
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(collectionInstrumentId)))
                    .andExpect(content().string(containsString(frequency1)))
                    .andExpect(content().string(containsString(frequency2)));

            verify(dataProcessingContextMongoDBRepository, times(1))
                    .findAll();
        }

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Get schedules V2 test")
        @SneakyThrows
        void get_schedulesV2_test(){
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            ObjectId objectId = new ObjectId();
            String frequency1 = "0 0 0 0 0 0";
            String frequency2 = "0 0 0 0 0 1";

            DataProcessingContextDocument dataProcessingContextDocument = new DataProcessingContextDocument();
            dataProcessingContextDocument.setId(objectId);
            dataProcessingContextDocument.setPartitionId(collectionInstrumentId);
            dataProcessingContextDocument.setKraftwerkExecutionScheduleV2List(new ArrayList<>());
            dataProcessingContextDocument.getKraftwerkExecutionScheduleV2List().add(new KraftwerkExecutionScheduleV2(
                    UUID.randomUUID().toString(),
                    frequency1,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    null,
                    false,
                    null,
                    null
            ));
            dataProcessingContextDocument.getKraftwerkExecutionScheduleV2List().add(new KraftwerkExecutionScheduleV2(
                    collectionInstrumentId,
                    frequency2,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    null,
                    false,
                    null,
                    null
            ));
            when(dataProcessingContextMongoDBRepository.findAll()).thenReturn(List.of(dataProcessingContextDocument));

            //WHEN
            mockMvc.perform(get("/contexts/schedules/v2")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    //THEN
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(collectionInstrumentId)))
                    .andExpect(content().string(containsString(frequency1)))
                    .andExpect(content().string(containsString(frequency2)));

            verify(dataProcessingContextMongoDBRepository, times(1))
                    .findAll();
        }

    }

    @Nested
    @DisplayName("Schedule deleting tests")
    class DeleteSchedulesTests{
        //HAPPY PATHS
        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Delete schedule test")
        @SneakyThrows
        void delete_schedule_test(){
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            ObjectId objectId = new ObjectId();

            DataProcessingContextDocument dataProcessingContextDocument = new DataProcessingContextDocument();
            dataProcessingContextDocument.setId(objectId);
            dataProcessingContextDocument.setCollectionInstrumentId(collectionInstrumentId);
            dataProcessingContextDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
            dataProcessingContextDocument.getKraftwerkExecutionScheduleList().add(new KraftwerkExecutionSchedule(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ));
            when(dataProcessingContextMongoDBRepository.findByCollectionInstrumentIdList(anyList()))
                    .thenReturn(List.of(dataProcessingContextDocument));

            //WHEN
            //TODO s in context
            mockMvc.perform(delete("/context/%s/schedules".formatted(collectionInstrumentId))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            //THEN
            ArgumentCaptor<DataProcessingContextDocument> dataProcessingContextDocumentArgumentCaptor =
                    ArgumentCaptor.forClass(DataProcessingContextDocument.class);
            verify(dataProcessingContextMongoDBRepository, times(1))
                    .save(dataProcessingContextDocumentArgumentCaptor.capture());
            DataProcessingContextDocument savedDocument = dataProcessingContextDocumentArgumentCaptor.getValue();
            Assertions.assertThat(savedDocument).isNotNull();
            Assertions.assertThat(savedDocument.getId()).isEqualTo(objectId);
            Assertions.assertThat(savedDocument.getKraftwerkExecutionScheduleList()).isEmpty();
        }

        @Test
        //TODO use a file infra interface (mocked) here
        @Disabled("Writes directly on the pod/runner with files")
        @WithMockUser(roles = "SCHEDULER")
        @DisplayName("Delete expired schedules test")
        @SneakyThrows
        void delete_expired_schedules_test(){
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";
            ObjectId objectId = new ObjectId();

            DataProcessingContextDocument dataProcessingContextDocument = new DataProcessingContextDocument();
            dataProcessingContextDocument.setId(objectId);
            dataProcessingContextDocument.setCollectionInstrumentId(collectionInstrumentId);
            dataProcessingContextDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
            //Expired schedule
            String expiredFrequency = "0 0 0 0 0 0";
            LocalDateTime expiredDate = LocalDateTime.now().minusDays(3);
            dataProcessingContextDocument.getKraftwerkExecutionScheduleList().add(new KraftwerkExecutionSchedule(
                    null,
                    expiredFrequency,
                    null,
                    LocalDateTime.now().minusDays(7),
                    expiredDate,
                    null
            ));
            when(dataProcessingContextMongoDBRepository.findAll())
                    .thenReturn(List.of(dataProcessingContextDocument));

            //WHEN
            //TODO s in context
            mockMvc.perform(delete("/context/schedules/expired-schedules")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            //THEN
            ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
            ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);

            verify(mongoTemplate).updateMulti(
                    queryCaptor.capture(),
                    updateCaptor.capture(),
                    eq(Constants.MONGODB_SCHEDULE_COLLECTION_NAME)
            );

            Assertions.assertThat(queryCaptor.getValue().getQueryObject()).containsEntry(
                   "collectionInstrumentId", collectionInstrumentId
            );

            Document updateObject = updateCaptor.getValue().getUpdateObject();
            Document pullClause = (Document) updateObject.get("$pull");
            Assertions.assertThat(pullClause).isNotNull();

            Query pullQuery = (Query) pullClause.get("kraftwerkExecutionScheduleList");

            Document pullQueryObject = pullQuery.getQueryObject();
            Assertions.assertThat(pullQueryObject).isNotNull().containsEntry("scheduleEndDate", expiredDate);
        }

        //BAD PATHS
        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Delete schedule should return 404 if context doesn't exist")
        @SneakyThrows
        void delete_schedule_not_found_test(){
            //GIVEN
            String collectionInstrumentId = "collectionInstrumentId";

            //WHEN + THEN
            mockMvc.perform(delete("/contexts/%s/schedules".formatted(collectionInstrumentId))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

    }
}
