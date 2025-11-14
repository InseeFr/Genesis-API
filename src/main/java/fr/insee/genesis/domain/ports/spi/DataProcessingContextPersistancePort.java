package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;

import java.util.List;

public interface DataProcessingContextPersistancePort {
    DataProcessingContextDocument findByPartitionId(String partitionId);

    List<DataProcessingContextModel> findByPartitionIds(List<String> partitionIds);

    List<DataProcessingContextModel> findByCollectionInstrumentIds(List<String> collectionInstrumentIds);

    void save(DataProcessingContextDocument dataProcessingContextDocument);

    void saveAll(List<DataProcessingContextDocument> dataProcessingContextDocuments);

    void deleteBypartitionId(String partitionId);

    List<DataProcessingContextDocument> findAll();

    long count();

    List<KraftwerkExecutionSchedule> removeExpiredSchedules(DataProcessingContextModel dataProcessingContextModel);

    List<DataProcessingContextDocument> findAllByReview(boolean withReview);
}
