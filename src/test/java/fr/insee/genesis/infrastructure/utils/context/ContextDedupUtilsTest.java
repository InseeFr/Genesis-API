package fr.insee.genesis.infrastructure.utils.context;

import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class ContextDedupUtilsTest {

    private final String partitionId = "TEST";

    @Test
    void emptyListTest() {
        //Given
        List<DataProcessingContextDocument> dataProcessingContextDocuments = new ArrayList<>();

        //When
        DataProcessingContextDocument dataProcessingContextDocument = ContextDedupUtils.deduplicateContexts(partitionId,
                dataProcessingContextDocuments);

        //Then
        Assertions.assertThat(dataProcessingContextDocument).isNull();
    }

    @Test
    void oneElementListTest() {
        //Given
        List<DataProcessingContextDocument> dataProcessingContextDocuments = new ArrayList<>();
        DataProcessingContextDocument existingDocument = new DataProcessingContextDocument(
                partitionId,
                new ArrayList<>(),
                false
        );
        dataProcessingContextDocuments.add(existingDocument);
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );

        //When
        DataProcessingContextDocument dataProcessingContextDocument = ContextDedupUtils.deduplicateContexts(partitionId,
                dataProcessingContextDocuments);

        //Then
        Assertions.assertThat(dataProcessingContextDocument).isNotNull();
        Assertions.assertThat(dataProcessingContextDocument.getPartitionId()).isEqualTo(partitionId);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
    }

    @Test
    void multipleElementsListTest_both() {
        //Given
        List<DataProcessingContextDocument> dataProcessingContextDocuments = new ArrayList<>();
        DataProcessingContextDocument existingDocument = new DataProcessingContextDocument(
                partitionId,
                new ArrayList<>(),
                false
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        dataProcessingContextDocuments.add(existingDocument);

        existingDocument = new DataProcessingContextDocument(
                partitionId,
                new ArrayList<>(),
                true
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 6 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.now(),
                        LocalDateTime.MAX,
                        null
                )
        );
        dataProcessingContextDocuments.add(existingDocument);


        //When
        DataProcessingContextDocument dataProcessingContextDocument = ContextDedupUtils.deduplicateContexts(partitionId,
                dataProcessingContextDocuments);

        //Then
        Assertions.assertThat(dataProcessingContextDocument).isNotNull();
        Assertions.assertThat(dataProcessingContextDocument.getPartitionId()).isEqualTo(partitionId);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
        Assertions.assertThat(dataProcessingContextDocument.isWithReview()).isFalse();
    }

    @Test
    void multipleElementsListTest_true() {
        //Given
        List<DataProcessingContextDocument> dataProcessingContextDocuments = new ArrayList<>();
        DataProcessingContextDocument existingDocument = new DataProcessingContextDocument(
                partitionId,
                new ArrayList<>(),
                true
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        dataProcessingContextDocuments.add(existingDocument);

        existingDocument = new DataProcessingContextDocument(
                partitionId,
                new ArrayList<>(),
                true
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 6 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.now(),
                        LocalDateTime.MAX,
                        null
                )
        );
        dataProcessingContextDocuments.add(existingDocument);


        //When
        DataProcessingContextDocument dataProcessingContextDocument = ContextDedupUtils.deduplicateContexts(partitionId,
                dataProcessingContextDocuments);

        //Then
        Assertions.assertThat(dataProcessingContextDocument).isNotNull();
        Assertions.assertThat(dataProcessingContextDocument.getPartitionId()).isEqualTo(partitionId);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
        Assertions.assertThat(dataProcessingContextDocument.isWithReview()).isTrue();
    }

    @Test
    void duplicateScheduleListTest() {
        //Given
        List<DataProcessingContextDocument> dataProcessingContextDocuments = new ArrayList<>();
        DataProcessingContextDocument existingDocument = new DataProcessingContextDocument(
                partitionId,
                new ArrayList<>(),
                false
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 6 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.now(),
                        LocalDateTime.MAX,
                        null
                )
        );
        dataProcessingContextDocuments.add(existingDocument);

        existingDocument = new DataProcessingContextDocument(
                partitionId,
                new ArrayList<>(),
                false
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        dataProcessingContextDocuments.add(existingDocument);

        //When
        DataProcessingContextDocument dataProcessingContextDocument = ContextDedupUtils.deduplicateContexts(partitionId,
                dataProcessingContextDocuments);

        //Then
        Assertions.assertThat(dataProcessingContextDocument).isNotNull();
        Assertions.assertThat(dataProcessingContextDocument.getPartitionId()).isEqualTo(partitionId);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
    }

    @Test
    void duplicateAllContextsTest() {
        //Given
        List<DataProcessingContextDocument> dataProcessingContextDocuments = new ArrayList<>();
        DataProcessingContextDocument existingDocument = new DataProcessingContextDocument(
                partitionId,
                new ArrayList<>(),
                false
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 6 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.now(),
                        LocalDateTime.MAX,
                        null
                )
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 12 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.now(),
                        LocalDateTime.MAX,
                        null
                )
        );
        dataProcessingContextDocuments.add(existingDocument);

        existingDocument = new DataProcessingContextDocument(
                partitionId,
                new ArrayList<>(),
                false
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        dataProcessingContextDocuments.add(existingDocument);

        existingDocument = new DataProcessingContextDocument(
                partitionId + "_2",
                new ArrayList<>(),
                false
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 6 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        dataProcessingContextDocuments.add(existingDocument);
        existingDocument = new DataProcessingContextDocument(
                partitionId + "_3",
                new ArrayList<>(),
                false
        );
        existingDocument.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 6 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        dataProcessingContextDocuments.add(existingDocument);
        existingDocument = new DataProcessingContextDocument(
                partitionId + "_4",
                new ArrayList<>(),
                false
        );
        dataProcessingContextDocuments.add(existingDocument);


        //When
        List<DataProcessingContextDocument> deduplicateContexts = ContextDedupUtils.deduplicateContexts(dataProcessingContextDocuments);

        //Then
        Assertions.assertThat(deduplicateContexts).isNotNull().hasSize(4);
    }

}
