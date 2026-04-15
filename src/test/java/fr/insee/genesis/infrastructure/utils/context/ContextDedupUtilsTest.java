package fr.insee.genesis.infrastructure.utils.context;

import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class ContextDedupUtilsTest {

    private final String collectionInstrumentId = "TEST";

    @Test
    void emptyListTest() {
        //Given
        List<DataProcessingContextDocument> dataProcessingContextDocuments = new ArrayList<>();

        //When
        DataProcessingContextDocument dataProcessingContextDocument = ContextDedupUtils.deduplicateContexts(collectionInstrumentId,
                dataProcessingContextDocuments);

        //Then
        Assertions.assertThat(dataProcessingContextDocument).isNull();
    }

    @Test
    void oneElementListTest() {
        //Given
        List<DataProcessingContextDocument> dataProcessingContextDocuments = new ArrayList<>();
        DataProcessingContextDocument existingDocument = new DataProcessingContextDocument();
        existingDocument.setCollectionInstrumentId(collectionInstrumentId);
        existingDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        existingDocument.setWithReview(false);

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
        DataProcessingContextDocument dataProcessingContextDocument = ContextDedupUtils.deduplicateContexts(collectionInstrumentId,
                dataProcessingContextDocuments);

        //Then
        Assertions.assertThat(dataProcessingContextDocument).isNotNull();
        Assertions.assertThat(dataProcessingContextDocument.getCollectionInstrumentId()).isEqualTo(collectionInstrumentId);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
    }

    @Test
    @Disabled
    void multipleElementsListTest_both() {
        //Given
        List<DataProcessingContextDocument> dataProcessingContextDocuments = new ArrayList<>();
        DataProcessingContextDocument existingDocument = new DataProcessingContextDocument();
        existingDocument.setCollectionInstrumentId(collectionInstrumentId);
        existingDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        existingDocument.setWithReview(false);

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

        existingDocument = new DataProcessingContextDocument();
        existingDocument.setCollectionInstrumentId(collectionInstrumentId);
        existingDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        existingDocument.setWithReview(true);

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
        DataProcessingContextDocument dataProcessingContextDocument = ContextDedupUtils.deduplicateContexts(collectionInstrumentId,
                dataProcessingContextDocuments);

        //Then
        Assertions.assertThat(dataProcessingContextDocument).isNotNull();
        Assertions.assertThat(dataProcessingContextDocument.getCollectionInstrumentId()).isEqualTo(collectionInstrumentId);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
        Assertions.assertThat(dataProcessingContextDocument.isWithReview()).isFalse();
    }

    @Test
    @Disabled
    void multipleElementsListTest_true() {
        //Given
        List<DataProcessingContextDocument> dataProcessingContextDocuments = new ArrayList<>();
        DataProcessingContextDocument existingDocument = new DataProcessingContextDocument();
        existingDocument.setCollectionInstrumentId(collectionInstrumentId);
        existingDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        existingDocument.setWithReview(true);

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

        existingDocument = new DataProcessingContextDocument();
        existingDocument.setCollectionInstrumentId(collectionInstrumentId);
        existingDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        existingDocument.setWithReview(true);

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
        DataProcessingContextDocument dataProcessingContextDocument = ContextDedupUtils.deduplicateContexts(collectionInstrumentId,
                dataProcessingContextDocuments);

        //Then
        Assertions.assertThat(dataProcessingContextDocument).isNotNull();
        Assertions.assertThat(dataProcessingContextDocument.getCollectionInstrumentId()).isEqualTo(collectionInstrumentId);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
        Assertions.assertThat(dataProcessingContextDocument.isWithReview()).isTrue();
    }

    @Test
    @Disabled
    void duplicateScheduleListTest() {
        //Given
        List<DataProcessingContextDocument> dataProcessingContextDocuments = new ArrayList<>();
        DataProcessingContextDocument existingDocument = new DataProcessingContextDocument();
        existingDocument.setCollectionInstrumentId(collectionInstrumentId);
        existingDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        existingDocument.setWithReview(false);

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

        existingDocument = new DataProcessingContextDocument();
        existingDocument.setCollectionInstrumentId(collectionInstrumentId);
        existingDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        existingDocument.setWithReview(false);

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
        DataProcessingContextDocument dataProcessingContextDocument = ContextDedupUtils.deduplicateContexts(collectionInstrumentId,
                dataProcessingContextDocuments);

        //Then
        Assertions.assertThat(dataProcessingContextDocument).isNotNull();
        Assertions.assertThat(dataProcessingContextDocument.getCollectionInstrumentId()).isEqualTo(collectionInstrumentId);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
    }

    @Test
    @Disabled
    void duplicateAllContextsTest() {
        //Given
        List<DataProcessingContextDocument> dataProcessingContextDocuments = new ArrayList<>();
        DataProcessingContextDocument existingDocument = new DataProcessingContextDocument();
        existingDocument.setCollectionInstrumentId(collectionInstrumentId);
        existingDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        existingDocument.setWithReview(false);

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

        existingDocument = new DataProcessingContextDocument();
        existingDocument.setCollectionInstrumentId(collectionInstrumentId);
        existingDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        existingDocument.setWithReview(false);

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

        existingDocument = new DataProcessingContextDocument();
        existingDocument.setCollectionInstrumentId(collectionInstrumentId + "_2");
        existingDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        existingDocument.setWithReview(false);

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
        existingDocument = new DataProcessingContextDocument();
        existingDocument.setCollectionInstrumentId(collectionInstrumentId + "_3");
        existingDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        existingDocument.setWithReview(false);

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
        existingDocument = new DataProcessingContextDocument();
        existingDocument.setCollectionInstrumentId(collectionInstrumentId + "_4");
        existingDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        existingDocument.setWithReview(false);

        dataProcessingContextDocuments.add(existingDocument);


        //When
        List<DataProcessingContextDocument> deduplicateContexts = ContextDedupUtils.deduplicateContexts(dataProcessingContextDocuments);

        //Then
        Assertions.assertThat(deduplicateContexts).isNotNull().hasSize(4);
    }

}
