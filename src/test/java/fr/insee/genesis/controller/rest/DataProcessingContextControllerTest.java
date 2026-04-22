package fr.insee.genesis.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.insee.genesis.controller.dto.KraftwerkExecutionScheduleInput;
import fr.insee.genesis.controller.dto.ScheduleRequestDto;
import fr.insee.genesis.controller.dto.rawdata.ScheduleResponseDto;
import fr.insee.genesis.controller.utils.ExportType;
import fr.insee.genesis.domain.model.context.schedule.DestinationType;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataProcessingContextController.class)
class DataProcessingContextControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DataProcessingContextApiPort dataProcessingContextApiPort;

    @MockitoBean
    private FileUtils fileUtils;

    private ObjectMapper objectMapper;

    private static final String COLLECTION_INSTRUMENT_ID = "INSTRUMENT_001";
    private static final String SCHEDULE_UUID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("PUT /contexts/{collectionInstrumentId}/review")
    class SaveContextWithCollectionInstrumentId {

        @Test
        @WithMockUser(roles = "USER_PLATINE")
        @DisplayName("Should return 200 OK when context saved")
        void givenValidInstrument_whenSaveContext_thenReturns200() throws Exception {
            // GIVEN
            doNothing().when(dataProcessingContextApiPort)
                    .saveContextByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID, true);

            // WHEN
            var result = mockMvc.perform(put("/contexts/{id}/review", COLLECTION_INSTRUMENT_ID)
                    .param("withReview", "true")
                    .with(csrf()));

            // THEN
            result.andExpect(status().isOk());
            verify(dataProcessingContextApiPort)
                    .saveContextByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID, true);
        }

        @Test
        @WithMockUser(roles = "USER_BACK_OFFICE")
        @DisplayName("WithReview forced to false if null")
        void givenNullWithReview_whenSaveContext_thenWithReviewFalse() throws Exception {
            // GIVEN
            doNothing().when(dataProcessingContextApiPort)
                    .saveContextByCollectionInstrumentId(anyString(), eq(false));

            // WHEN
            var result = mockMvc.perform(put("/contexts/{id}/review", COLLECTION_INSTRUMENT_ID)
                    .with(csrf()));

            // THEN
            result.andExpect(status().isOk());
            verify(dataProcessingContextApiPort)
                    .saveContextByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID, false);
        }

        @Test
        @WithMockUser(roles = "USER_PLATINE")
        @DisplayName("Return genesis exception message and status")
        void givenGenesisException_whenSaveContext_thenReturnsExceptionStatus() throws Exception {
            // GIVEN
            doThrow(new GenesisException(404, "Context not found"))
                    .when(dataProcessingContextApiPort)
                    .saveContextByCollectionInstrumentId(anyString(), any(Boolean.class));

            // WHEN
            var result = mockMvc.perform(put("/contexts/{id}/review", COLLECTION_INSTRUMENT_ID)
                    .with(csrf()));

            // THEN
            result.andExpect(status().isNotFound())
                    .andExpect(content().string("Context not found"));
        }
    }

    @Nested
    @DisplayName("GET /contexts/{collectionInstrumentId}/review")
    class GetReviewIndicatorByCollectionInstrumentId {

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        @WithMockUser(roles = "USER_BACK_OFFICE")
        @DisplayName("Should return 200 with review indicator")
        void givenValidInstrument_whenGetReviewIndicator_thenReturns200WithTrue(boolean withReview) throws Exception {
            // GIVEN
            when(dataProcessingContextApiPort.getReviewByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID))
                    .thenReturn(withReview);

            // WHEN
            var result = mockMvc.perform(get("/contexts/{id}/review", COLLECTION_INSTRUMENT_ID));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(content().string(Boolean.toString(withReview)));
        }

        @Test
        @WithMockUser(roles = "USER_PLATINE")
        @DisplayName("Return genesis exception message and status")
        void givenGenesisException_whenGetReviewIndicator_thenReturnsExceptionStatus() throws Exception {
            // GIVEN
            when(dataProcessingContextApiPort.getReviewByCollectionInstrumentId(anyString()))
                    .thenThrow(new GenesisException(404, "Instrument not found"));

            // WHEN
            var result = mockMvc.perform(get("/contexts/{id}/review", COLLECTION_INSTRUMENT_ID));

            // THEN
            result.andExpect(status().isNotFound())
                    .andExpect(content().string("Instrument not found"));
        }
    }

    @Nested
    @DisplayName("POST /contexts/schedules/v2")
    class CreateScheduleV2 {

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Should return 200 with schedule UUID")
        void givenScheduleWithoutEncryption_whenCreateSchedule_thenReturns200WithUuid() throws Exception {
            // GIVEN
            ScheduleRequestDto request = buildScheduleRequestDto(false);
            when(dataProcessingContextApiPort.createKraftwerkExecutionSchedule(any()))
                    .thenReturn(SCHEDULE_UUID);

            // WHEN
            var result = mockMvc.perform(post("/contexts/schedules/v2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(content().string(SCHEDULE_UUID));
            verify(dataProcessingContextApiPort).createKraftwerkExecutionSchedule(any());
        }

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("TrustParameters should be built if useAsymmetricEncryption true")
        void givenScheduleWithAsymmetricEncryption_whenCreateSchedule_thenTrustParametersBuilt() throws Exception {
            // GIVEN
            ScheduleRequestDto request = buildScheduleRequestDto(true);
            String outputFolder = "/output/path";
            when(fileUtils.getKraftwerkOutFolder(COLLECTION_INSTRUMENT_ID)).thenReturn(outputFolder);
            when(dataProcessingContextApiPort.createKraftwerkExecutionSchedule(any()))
                    .thenReturn(SCHEDULE_UUID);

            // WHEN
            var result = mockMvc.perform(post("/contexts/schedules/v2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()));

            // THEN
            result.andExpect(status().isOk());

            ArgumentCaptor<KraftwerkExecutionScheduleInput> argumentCaptor =
                    ArgumentCaptor.forClass(KraftwerkExecutionScheduleInput.class);
            verify(dataProcessingContextApiPort, times(1))
                    .createKraftwerkExecutionSchedule(argumentCaptor.capture());
            KraftwerkExecutionScheduleInput kraftwerkExecutionScheduleInput = argumentCaptor.getValue();
            Assertions.assertThat(kraftwerkExecutionScheduleInput).isNotNull();
            Assertions.assertThat(kraftwerkExecutionScheduleInput.getTrustParameters()).isNotNull();
            Assertions.assertThat(kraftwerkExecutionScheduleInput.getTrustParameters().getInputPath())
                    .isEqualTo(outputFolder);
        }

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Should return genesis exception status and message")
        void givenGenesisException_whenCreateSchedule_thenReturnsExceptionStatus() throws Exception {
            // GIVEN
            ScheduleRequestDto request = buildScheduleRequestDto(false);
            when(dataProcessingContextApiPort.createKraftwerkExecutionSchedule(any()))
                    .thenThrow(new GenesisException(400, "Invalid schedule"));

            // WHEN
            var result = mockMvc.perform(post("/contexts/schedules/v2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()));

            // THEN
            result.andExpect(status().isBadRequest())
                    .andExpect(content().string("Invalid schedule"));
        }
    }

    @Nested
    @DisplayName("PUT /contexts/{collectionInstrumentId}/schedules/v2/{scheduleUuid}")
    class UpdateScheduleV2 {

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Should return 200 and call update schedule in service")
        void givenValidSchedule_whenUpdateSchedule_thenReturns200() throws Exception {
            // GIVEN
            ScheduleRequestDto request = buildScheduleRequestDto(false);
            doNothing().when(dataProcessingContextApiPort).updateKraftwerkExecutionSchedule(any());

            // WHEN
            var result = mockMvc.perform(put("/contexts/{id}/schedules/v2/{uuid}",
                    COLLECTION_INSTRUMENT_ID, SCHEDULE_UUID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()));

            // THEN
            result.andExpect(status().isOk());
            verify(dataProcessingContextApiPort).updateKraftwerkExecutionSchedule(any());
        }

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("getKraftwerkOutFolder should be called if use encryption true")
        void givenAsymmetricEncryption_whenUpdateSchedule_thenOutFolderCalledWithInstrumentId() throws Exception {
            // GIVEN
            ScheduleRequestDto request = buildScheduleRequestDto(true);
            when(fileUtils.getKraftwerkOutFolder(COLLECTION_INSTRUMENT_ID)).thenReturn("/output/path");
            doNothing().when(dataProcessingContextApiPort).updateKraftwerkExecutionSchedule(any());

            // WHEN
            mockMvc.perform(put("/contexts/{id}/schedules/v2/{uuid}",
                    COLLECTION_INSTRUMENT_ID, SCHEDULE_UUID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()));

            // THEN
            verify(fileUtils).getKraftwerkOutFolder(COLLECTION_INSTRUMENT_ID);
        }

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Should return genesis exception status and message")
        void givenGenesisException_whenUpdateSchedule_thenReturnsExceptionStatus() throws Exception {
            // GIVEN
            ScheduleRequestDto request = buildScheduleRequestDto(false);
            doThrow(new GenesisException(404, "Schedule not found"))
                    .when(dataProcessingContextApiPort).updateKraftwerkExecutionSchedule(any());

            // WHEN
            var result = mockMvc.perform(put("/contexts/{id}/schedules/v2/{uuid}",
                    COLLECTION_INSTRUMENT_ID, SCHEDULE_UUID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()));

            // THEN
            result.andExpect(status().isNotFound())
                    .andExpect(content().string("Schedule not found"));
        }
    }

    @Nested
    @DisplayName("GET /contexts/schedules/v2")
    class GetAllSchedulesV2 {

        @Test
        @WithMockUser(roles = "SCHEDULER")
        @DisplayName("Should return 200 with list of all schedules")
        void givenExistingSchedules_whenGetAllSchedules_thenReturns200WithList() throws Exception {
            // GIVEN
            List<ScheduleResponseDto> schedules = List.of(
                    buildScheduleResponseDto("INSTRUMENT_A"),
                    buildScheduleResponseDto("INSTRUMENT_B")
            );
            when(dataProcessingContextApiPort.getAllSchedulesV2()).thenReturn(schedules);

            // WHEN
            var result = mockMvc.perform(get("/contexts/schedules/v2"));

            // THEN
            var response = result.andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            assertThat(response).contains("INSTRUMENT_A", "INSTRUMENT_B");
        }

        @Test
        @WithMockUser(roles = "READER")
        @DisplayName("Should return empty list if no schedule")
        void givenNoSchedules_whenGetAllSchedules_thenReturns200WithEmptyList() throws Exception {
            // GIVEN
            when(dataProcessingContextApiPort.getAllSchedulesV2()).thenReturn(List.of());

            // WHEN
            var result = mockMvc.perform(get("/contexts/schedules/v2"));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }
    }

    @Nested
    @DisplayName("GET /contexts/{collectionInstrumentId}/schedules/v2")
    class GetSchedulesV2ByCollectionInstrumentId {

        @Test
        @WithMockUser(roles = "SCHEDULER")
        @DisplayName("Should return 200 with list of schedules for a specific collection instrument id")
        void givenInstrumentWithSchedules_whenGetSchedules_thenReturns200WithList() throws Exception {
            // GIVEN
            List<ScheduleResponseDto> schedules = List.of(buildScheduleResponseDto(COLLECTION_INSTRUMENT_ID));
            when(dataProcessingContextApiPort.getSchedulesV2ByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID))
                    .thenReturn(schedules);

            // WHEN
            var result = mockMvc.perform(get("/contexts/{id}/schedules/v2", COLLECTION_INSTRUMENT_ID));

            // THEN
            result.andExpect(status().isOk());
            verify(dataProcessingContextApiPort)
                    .getSchedulesV2ByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);
        }

        @Test
        @WithMockUser(roles = "READER")
        @DisplayName("Should return empty list if no schedule")
        void givenInstrumentWithoutSchedules_whenGetSchedules_thenReturns200WithEmptyList() throws Exception {
            // GIVEN
            when(dataProcessingContextApiPort.getSchedulesV2ByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID))
                    .thenReturn(List.of());

            // WHEN
            var result = mockMvc.perform(get("/contexts/{id}/schedules/v2", COLLECTION_INSTRUMENT_ID));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }
    }

    @Nested
    @DisplayName("DELETE /context/{collectionInstrumentId}/schedules")
    class DeleteSchedulesByCollectionInstrumentId {

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Should call delete schedule in service and return 200")
        void givenValidInstrument_whenDeleteSchedules_thenReturns200() throws Exception {
            // GIVEN
            doNothing().when(dataProcessingContextApiPort)
                    .deleteSchedulesByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

            // WHEN
            var result = mockMvc.perform(delete("/context/{id}/schedules", COLLECTION_INSTRUMENT_ID)
                    .with(csrf()));

            // THEN
            result.andExpect(status().isOk());
            verify(dataProcessingContextApiPort)
                    .deleteSchedulesByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);
        }

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Should return GenesisException code and message")
        void givenGenesisException_whenDeleteSchedules_thenReturnsExceptionStatus() throws Exception {
            // GIVEN
            doThrow(new GenesisException(500, "Deletion error"))
                    .when(dataProcessingContextApiPort)
                    .deleteSchedulesByCollectionInstrumentId(anyString());

            // WHEN
            var result = mockMvc.perform(delete("/context/{id}/schedules", COLLECTION_INSTRUMENT_ID)
                    .with(csrf()));

            // THEN
            result.andExpect(status().isInternalServerError())
                    .andExpect(content().string("Deletion error"));
        }
    }

    @Nested
    @DisplayName("DELETE /contexts/{collectionInstrumentId}/schedules/v2")
    class DeleteSchedulesV2ByCollectionInstrumentId {

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Should delete all schedules for a specific collectionInstrumentId and return 200")
        void givenValidInstrument_whenDeleteAllV2Schedules_thenReturns200() throws Exception {
            // GIVEN
            doNothing().when(dataProcessingContextApiPort)
                    .deleteSchedulesV2ByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

            // WHEN
            var result = mockMvc.perform(delete("/contexts/{id}/schedules/v2", COLLECTION_INSTRUMENT_ID)
                    .with(csrf()));

            // THEN
            result.andExpect(status().isOk());
            verify(dataProcessingContextApiPort)
                    .deleteSchedulesV2ByCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);
        }

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Should return GenesisException code and message")
        void givenGenesisException_whenDeleteAllV2Schedules_thenReturnsExceptionStatus() throws Exception {
            // GIVEN
            doThrow(new GenesisException(404, "No schedule found"))
                    .when(dataProcessingContextApiPort)
                    .deleteSchedulesV2ByCollectionInstrumentId(anyString());

            // WHEN
            var result = mockMvc.perform(delete("/contexts/{id}/schedules/v2", COLLECTION_INSTRUMENT_ID)
                    .with(csrf()));

            // THEN
            result.andExpect(status().isNotFound())
                    .andExpect(content().string("No schedule found"));
        }
    }

    @Nested
    @DisplayName("DELETE /contexts/{collectionInstrumentId}/schedules/v2/{scheduleUuid}")
    class DeleteScheduleV2 {

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Should delete all schedules for a specific schedule UUID and return 200")
        void givenValidInstrumentAndUuid_whenDeleteScheduleV2_thenReturns200() throws Exception {
            // GIVEN
            doNothing().when(dataProcessingContextApiPort)
                    .deleteScheduleV2(COLLECTION_INSTRUMENT_ID, SCHEDULE_UUID);

            // WHEN
            var result = mockMvc.perform(delete("/contexts/{id}/schedules/v2/{uuid}",
                    COLLECTION_INSTRUMENT_ID, SCHEDULE_UUID)
                    .with(csrf()));

            // THEN
            result.andExpect(status().isOk());
            verify(dataProcessingContextApiPort)
                    .deleteScheduleV2(COLLECTION_INSTRUMENT_ID, SCHEDULE_UUID);
        }

        @Test
        @WithMockUser(roles = "USER_KRAFTWERK")
        @DisplayName("Should return GenesisException code and message")
        void givenGenesisException_whenDeleteScheduleV2_thenReturnsExceptionStatus() throws Exception {
            // GIVEN
            doThrow(new GenesisException(404, "Schedule UUID not found"))
                    .when(dataProcessingContextApiPort)
                    .deleteScheduleV2(anyString(), anyString());

            // WHEN
            var result = mockMvc.perform(delete("/contexts/{id}/schedules/v2/{uuid}",
                    COLLECTION_INSTRUMENT_ID, SCHEDULE_UUID)
                    .with(csrf()));

            // THEN
            result.andExpect(status().isNotFound())
                    .andExpect(content().string("Schedule UUID not found"));
        }
    }

    @Nested
    @DisplayName("DELETE /context/schedules/expired-schedules")
    class DeleteExpiredSchedules {

        @Test
        @WithMockUser(roles = "SCHEDULER")
        @DisplayName("Should call delete expired schedules in service and return 200")
        void givenExpiredSchedules_whenDeleteExpiredSchedules_thenReturns200() throws Exception {
            // GIVEN
            when(fileUtils.getLogFolder()).thenReturn("/log/path");
            doNothing().when(dataProcessingContextApiPort).deleteExpiredSchedules("/log/path");

            // WHEN
            var result = mockMvc.perform(delete("/context/schedules/expired-schedules")
                    .with(csrf()));

            // THEN
            result.andExpect(status().isOk());
            verify(fileUtils).getLogFolder();
            verify(dataProcessingContextApiPort).deleteExpiredSchedules("/log/path");
        }

        @Test
        @WithMockUser(roles = "SCHEDULER")
        @DisplayName("Should return GenesisException code and message")
        void givenGenesisException_whenDeleteExpiredSchedules_thenReturnsExceptionStatus() throws Exception {
            // GIVEN
            when(fileUtils.getLogFolder()).thenReturn("/log/path");
            doThrow(new GenesisException(500, "Purge failed"))
                    .when(dataProcessingContextApiPort).deleteExpiredSchedules(anyString());

            // WHEN
            var result = mockMvc.perform(delete("/context/schedules/expired-schedules")
                    .with(csrf()));

            // THEN
            result.andExpect(status().isInternalServerError())
                    .andExpect(content().string("Purge failed"));
        }
    }

    // UTILS
    private ScheduleRequestDto buildScheduleRequestDto(boolean useAsymmetricEncryption) {
        return ScheduleRequestDto.builder()
                .collectionInstrumentId(COLLECTION_INSTRUMENT_ID)
                .exportType(ExportType.CSV_PARQUET)
                .frequency("0 6 * * *")
                .scheduleBeginDate(LocalDateTime.now())
                .scheduleEndDate(LocalDateTime.now().plusMonths(3))
                .mode(Mode.WEB)
                .destinationType(DestinationType.APPLISHARE)
                .addStates(false)
                .destinationFolder("/destination")
                .useAsymmetricEncryption(useAsymmetricEncryption)
                .useSymmetricEncryption(false)
                .encryptionVaultPath("/vault/path")
                .useSignature(false)
                .batchSize(100)
                .build();
    }

    private ScheduleResponseDto buildScheduleResponseDto(String collectionInstrumentId) {
        return ScheduleResponseDto.builder()
                .collectionInstrumentId(collectionInstrumentId)
                .scheduleUuid(UUID.randomUUID().toString())
                .build();
    }
}