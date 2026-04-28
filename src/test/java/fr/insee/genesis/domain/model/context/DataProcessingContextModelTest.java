package fr.insee.genesis.domain.model.context;

import fr.insee.genesis.controller.dto.rawdata.ScheduleResponseDto;
import fr.insee.genesis.controller.utils.ExportType;
import fr.insee.genesis.domain.model.context.schedule.DestinationType;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionScheduleV2;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.context.schedule.TrustParameters;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class DataProcessingContextModelTest {

    @Nested
    @DisplayName("toScheduleV1ResponseDtos tests")
    class ToScheduleV1ResponseDtosTests{
        @Test
        void toScheduleV1ResponseDtos_no_encryption_test() {
            //GIVEN
            String collectionInstrumentId = "test";
            String frequency = "0 0 0 0 0 0";
            ServiceToCall serviceToCall = ServiceToCall.GENESIS;
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            LocalDateTime lastExecutionDate = LocalDateTime.now().minusDays(1);
            List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList = List.of(
                    new KraftwerkExecutionSchedule(
                            collectionInstrumentId,
                            frequency,
                            serviceToCall,
                            start,
                            end,
                            null
                    )
            );
            DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                    .collectionInstrumentId(collectionInstrumentId)
                    .lastExecution(lastExecutionDate)
                    .kraftwerkExecutionScheduleList(kraftwerkExecutionScheduleList)
                    .build();

            //WHEN
            List<ScheduleResponseDto> scheduleResponseDtoList = dataProcessingContextModel.toScheduleV1ResponseDtos();

            //THEN
            Assertions.assertThat(scheduleResponseDtoList).hasSize(1);
            Assertions.assertThat(scheduleResponseDtoList.getFirst()).isNotNull();
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getCollectionInstrumentId())
                    .isEqualTo(collectionInstrumentId);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getLastExecution())
                    .isEqualTo(lastExecutionDate);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getFrequency())
                    .isEqualTo(frequency);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getScheduleBeginDate())
                    .isEqualTo(start);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getScheduleEndDate())
                    .isEqualTo(end);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getExportType())
                    .isEqualTo(ExportType.CSV_PARQUET);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isUseSymmetricEncryption())
                    .isFalse();
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isUseAsymmetricEncryption())
                    .isFalse();
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getEncryptionVaultPath())
                    .isNull();
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isUseSignature())
                    .isFalse();
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        void toScheduleV1ResponseDtos_with_encryption_test(boolean useSignature) {
            //GIVEN
            String collectionInstrumentId = "test";
            String frequency = "0 0 0 0 0 0";
            ServiceToCall serviceToCall = ServiceToCall.GENESIS;
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            LocalDateTime lastExecutionDate = LocalDateTime.now().minusDays(1);
            String vaultPath = "vaultPath";
            TrustParameters trustParameters = new TrustParameters(
                    "input",
                    "output",
                    vaultPath,
                    useSignature
            );
            List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList = List.of(
                    new KraftwerkExecutionSchedule(
                            collectionInstrumentId,
                            frequency,
                            serviceToCall,
                            start,
                            end,
                            trustParameters
                    )
            );
            DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                    .collectionInstrumentId(collectionInstrumentId)
                    .lastExecution(lastExecutionDate)
                    .kraftwerkExecutionScheduleList(kraftwerkExecutionScheduleList)
                    .build();

            //WHEN
            List<ScheduleResponseDto> scheduleResponseDtoList = dataProcessingContextModel.toScheduleV1ResponseDtos();

            //THEN
            Assertions.assertThat(scheduleResponseDtoList).hasSize(1);
            Assertions.assertThat(scheduleResponseDtoList.getFirst()).isNotNull();
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getCollectionInstrumentId())
                    .isEqualTo(collectionInstrumentId);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getLastExecution())
                    .isEqualTo(lastExecutionDate);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getFrequency())
                    .isEqualTo(frequency);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getScheduleBeginDate())
                    .isEqualTo(start);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getScheduleEndDate())
                    .isEqualTo(end);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getExportType())
                    .isEqualTo(ExportType.CSV_PARQUET);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isUseSymmetricEncryption())
                    .isFalse();
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isUseAsymmetricEncryption())
                    .isTrue();
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getEncryptionVaultPath())
                    .isEqualTo(vaultPath);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isUseSignature())
                    .isEqualTo(useSignature);
        }

        @Test
        void toScheduleV1ResponseDtos_empty_list_test() {
            //GIVEN
            DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                    .kraftwerkExecutionScheduleList(new ArrayList<>())
                    .build();

            //WHEN + THEN
            Assertions.assertThat(dataProcessingContextModel.toScheduleV1ResponseDtos()).isEmpty();
        }
        @Test
        void toScheduleV1ResponseDtos_null_list_test() {
            //GIVEN
            DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                    .build();

            //WHEN + THEN
            Assertions.assertThat(dataProcessingContextModel.toScheduleV1ResponseDtos()).isEmpty();
        }

        @Test
        void toScheduleV1ResponseDtos_null_schedule_ignored_test() {
            //GIVEN
            String collectionInstrumentId = "test";
            String frequency = "0 0 0 0 0 0";
            ServiceToCall serviceToCall = ServiceToCall.GENESIS;
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            LocalDateTime lastExecutionDate = LocalDateTime.now().minusDays(1);

            List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList = new ArrayList<>();
            kraftwerkExecutionScheduleList.add(new KraftwerkExecutionSchedule(
                    collectionInstrumentId,
                    frequency,
                    serviceToCall,
                    start,
                    end,
                    null
            ));
            kraftwerkExecutionScheduleList.add(null);

            DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                    .collectionInstrumentId(collectionInstrumentId)
                    .lastExecution(lastExecutionDate)
                    .kraftwerkExecutionScheduleList(kraftwerkExecutionScheduleList)
                    .build();

            //WHEN
            List<ScheduleResponseDto> scheduleResponseDtoList = dataProcessingContextModel.toScheduleV1ResponseDtos();

            //THEN
            Assertions.assertThat(scheduleResponseDtoList).hasSize(1);
        }

        @Test
        void toScheduleV1ResponseDtos_no_partitionId_schedule_ignored_test() {
            //GIVEN
            String collectionInstrumentId = "test";
            String frequency = "0 0 0 0 0 0";
            ServiceToCall serviceToCall = ServiceToCall.GENESIS;
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            LocalDateTime lastExecutionDate = LocalDateTime.now().minusDays(1);

            List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList = new ArrayList<>();
            kraftwerkExecutionScheduleList.add(new KraftwerkExecutionSchedule(
                    collectionInstrumentId,
                    frequency,
                    serviceToCall,
                    start,
                    end,
                    null
            ));
            kraftwerkExecutionScheduleList.add(new KraftwerkExecutionSchedule(
                    null, //Null partitionId
                    frequency,
                    serviceToCall,
                    start,
                    end,
                    null
            ));

            DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                    .collectionInstrumentId(collectionInstrumentId)
                    .lastExecution(lastExecutionDate)
                    .kraftwerkExecutionScheduleList(kraftwerkExecutionScheduleList)
                    .build();

            //WHEN
            List<ScheduleResponseDto> scheduleResponseDtoList = dataProcessingContextModel.toScheduleV1ResponseDtos();

            //THEN
            Assertions.assertThat(scheduleResponseDtoList).hasSize(1);
        }
    }


    @Nested
    @DisplayName("toScheduleV2ResponseDtos tests")
    class ToScheduleV2ResponseDtosTests {
        @Test
        void toScheduleV2ResponseDtos_no_encryption_test() {
            //GIVEN
            String scheduleUuid = UUID.randomUUID().toString();
            String collectionInstrumentId = "test";
            String frequency = "0 0 0 0 0 0";
            ExportType exportType = ExportType.JSON;
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            Mode mode = Mode.WEB;
            DestinationType destinationType = DestinationType.APPLISHARE;
            boolean addStates = true;
            String destinationFolder = "test";
            boolean useSymmetricEncryption = false;
            int batchSize = 100;
            LocalDateTime lastExecutionDate = LocalDateTime.now().minusDays(1);
            List<KraftwerkExecutionScheduleV2> kraftwerkExecutionScheduleV2List = List.of(
                    new KraftwerkExecutionScheduleV2(
                            scheduleUuid,
                            frequency,
                            exportType,
                            start,
                            end,
                            mode,
                            destinationType,
                            addStates,
                            destinationFolder,
                            useSymmetricEncryption,
                            null,
                            batchSize
                    )
            );
            DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                    .collectionInstrumentId(collectionInstrumentId)
                    .lastExecution(lastExecutionDate)
                    .kraftwerkExecutionScheduleV2List(kraftwerkExecutionScheduleV2List)
                    .build();

            //WHEN
            List<ScheduleResponseDto> scheduleResponseDtoList = dataProcessingContextModel.toScheduleV2ResponseDtos();

            //THEN
            Assertions.assertThat(scheduleResponseDtoList).hasSize(1);
            Assertions.assertThat(scheduleResponseDtoList.getFirst()).isNotNull();
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getScheduleUuid())
                    .isEqualTo(scheduleUuid);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getCollectionInstrumentId())
                    .isEqualTo(collectionInstrumentId);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getLastExecution())
                    .isEqualTo(lastExecutionDate);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getFrequency())
                    .isEqualTo(frequency);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getExportType())
                    .isEqualTo(exportType);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getScheduleBeginDate())
                    .isEqualTo(start);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getScheduleEndDate())
                    .isEqualTo(end);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getMode())
                    .isEqualTo(mode);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isUseSymmetricEncryption())
                    .isEqualTo(useSymmetricEncryption);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isUseAsymmetricEncryption())
                    .isFalse();
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getEncryptionVaultPath())
                    .isEmpty();
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isUseSignature())
                    .isFalse();
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isAddStates())
                    .isEqualTo(addStates);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getDestinationType())
                    .isEqualTo(destinationType);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getDestinationFolder())
                    .isEqualTo(destinationFolder);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getBatchSize())
                    .isEqualTo(batchSize);
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        void toScheduleV2ResponseDtos_with_encryption_test(boolean useSignature) {
            //GIVEN
            String scheduleUuid = UUID.randomUUID().toString();
            String collectionInstrumentId = "test";
            String frequency = "0 0 0 0 0 0";
            ExportType exportType = ExportType.JSON;
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            Mode mode = Mode.WEB;
            DestinationType destinationType = DestinationType.APPLISHARE;
            boolean addStates = true;
            String destinationFolder = "test";
            boolean useSymmetricEncryption = false;
            int batchSize = 100;
            LocalDateTime lastExecutionDate = LocalDateTime.now().minusDays(1);
            String vaultPath = "vaultPath";
            TrustParameters trustParameters = new TrustParameters(
                    "input",
                    "output",
                    vaultPath,
                    useSignature
            );
            List<KraftwerkExecutionScheduleV2> kraftwerkExecutionScheduleV2List = List.of(
                    new KraftwerkExecutionScheduleV2(
                            scheduleUuid,
                            frequency,
                            exportType,
                            start,
                            end,
                            mode,
                            destinationType,
                            addStates,
                            destinationFolder,
                            useSymmetricEncryption,
                            trustParameters,
                            batchSize
                    )
            );
            DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                    .collectionInstrumentId(collectionInstrumentId)
                    .lastExecution(lastExecutionDate)
                    .kraftwerkExecutionScheduleV2List(kraftwerkExecutionScheduleV2List)
                    .build();

            //WHEN
            List<ScheduleResponseDto> scheduleResponseDtoList = dataProcessingContextModel.toScheduleV2ResponseDtos();

            //THEN
            Assertions.assertThat(scheduleResponseDtoList).hasSize(1);
            Assertions.assertThat(scheduleResponseDtoList.getFirst()).isNotNull();
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getScheduleUuid())
                    .isEqualTo(scheduleUuid);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getCollectionInstrumentId())
                    .isEqualTo(collectionInstrumentId);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getLastExecution())
                    .isEqualTo(lastExecutionDate);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getFrequency())
                    .isEqualTo(frequency);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getExportType())
                    .isEqualTo(exportType);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getScheduleBeginDate())
                    .isEqualTo(start);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getScheduleEndDate())
                    .isEqualTo(end);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getMode())
                    .isEqualTo(mode);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isUseSymmetricEncryption())
                    .isEqualTo(useSymmetricEncryption);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isUseAsymmetricEncryption())
                    .isTrue();
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getEncryptionVaultPath())
                    .isEqualTo(vaultPath);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isUseSignature())
                    .isEqualTo(useSignature);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().isAddStates())
                    .isEqualTo(addStates);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getDestinationType())
                    .isEqualTo(destinationType);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getDestinationFolder())
                    .isEqualTo(destinationFolder);
            Assertions.assertThat(scheduleResponseDtoList.getFirst().getBatchSize())
                    .isEqualTo(batchSize);
        }

        @Test
        void toScheduleV2ResponseDtos_empty_list_test() {
            //GIVEN
            DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                    .kraftwerkExecutionScheduleV2List(new ArrayList<>())
                    .build();

            //WHEN + THEN
            Assertions.assertThat(dataProcessingContextModel.toScheduleV2ResponseDtos()).isEmpty();
        }
        @Test
        void toScheduleV2ResponseDtos_null_list_test() {
            //GIVEN
            DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                    .build();

            //WHEN + THEN
            Assertions.assertThat(dataProcessingContextModel.toScheduleV2ResponseDtos()).isEmpty();
        }

        @Test
        void toScheduleV2ResponseDtos_null_schedule_ignored_test() {
            //GIVEN
            String scheduleUuid = UUID.randomUUID().toString();
            String collectionInstrumentId = "test";
            String frequency = "0 0 0 0 0 0";
            ExportType exportType = ExportType.JSON;
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            Mode mode = Mode.WEB;
            DestinationType destinationType = DestinationType.APPLISHARE;
            boolean addStates = true;
            String destinationFolder = "test";
            boolean useSymmetricEncryption = false;
            int batchSize = 100;
            LocalDateTime lastExecutionDate = LocalDateTime.now().minusDays(1);
            List<KraftwerkExecutionScheduleV2> kraftwerkExecutionScheduleV2List = new ArrayList<>();
            kraftwerkExecutionScheduleV2List.add(new KraftwerkExecutionScheduleV2(
                    scheduleUuid,
                    frequency,
                    exportType,
                    start,
                    end,
                    mode,
                    destinationType,
                    addStates,
                    destinationFolder,
                    useSymmetricEncryption,
                    null,
                    batchSize
            ));
            kraftwerkExecutionScheduleV2List.add(null);

            DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                    .collectionInstrumentId(collectionInstrumentId)
                    .lastExecution(lastExecutionDate)
                    .kraftwerkExecutionScheduleV2List(kraftwerkExecutionScheduleV2List)
                    .build();

            //WHEN
            List<ScheduleResponseDto> scheduleResponseDtoList = dataProcessingContextModel.toScheduleV2ResponseDtos();

            //THEN
            Assertions.assertThat(scheduleResponseDtoList).hasSize(1);
        }

        @Test
        void toScheduleV2ResponseDtos_no_scheduleUuid_schedule_ignored_test() {
            //GIVEN
            String scheduleUuid = UUID.randomUUID().toString();
            String collectionInstrumentId = "test";
            String frequency = "0 0 0 0 0 0";
            ExportType exportType = ExportType.JSON;
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            Mode mode = Mode.WEB;
            DestinationType destinationType = DestinationType.APPLISHARE;
            boolean addStates = true;
            String destinationFolder = "test";
            boolean useSymmetricEncryption = false;
            int batchSize = 100;
            LocalDateTime lastExecutionDate = LocalDateTime.now().minusDays(1);
            List<KraftwerkExecutionScheduleV2> kraftwerkExecutionScheduleV2List = new ArrayList<>();
            kraftwerkExecutionScheduleV2List.add(new KraftwerkExecutionScheduleV2(
                    scheduleUuid,
                    frequency,
                    exportType,
                    start,
                    end,
                    mode,
                    destinationType,
                    addStates,
                    destinationFolder,
                    useSymmetricEncryption,
                    null,
                    batchSize
            ));
            kraftwerkExecutionScheduleV2List.add(new KraftwerkExecutionScheduleV2(
                    null, //null UUID
                    frequency,
                    exportType,
                    start,
                    end,
                    mode,
                    destinationType,
                    addStates,
                    destinationFolder,
                    useSymmetricEncryption,
                    null,
                    batchSize
            ));

            DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                    .collectionInstrumentId(collectionInstrumentId)
                    .lastExecution(lastExecutionDate)
                    .kraftwerkExecutionScheduleV2List(kraftwerkExecutionScheduleV2List)
                    .build();

            //WHEN
            List<ScheduleResponseDto> scheduleResponseDtoList = dataProcessingContextModel.toScheduleV2ResponseDtos();

            //THEN
            Assertions.assertThat(scheduleResponseDtoList).hasSize(1);
        }
    }
}