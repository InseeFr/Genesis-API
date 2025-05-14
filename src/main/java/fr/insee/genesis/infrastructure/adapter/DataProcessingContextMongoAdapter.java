package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.repository.DataProcessingContextMongoDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Qualifier("dataProcessingContextMongoAdapter")
public class DataProcessingContextMongoAdapter implements DataProcessingContextPersistancePort {
    private final DataProcessingContextMongoDBRepository dataProcessingContextMongoDBRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public DataProcessingContextMongoAdapter(DataProcessingContextMongoDBRepository dataProcessingContextMongoDBRepository, MongoTemplate mongoTemplate) {
        this.dataProcessingContextMongoDBRepository = dataProcessingContextMongoDBRepository;
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public List<DataProcessingContextDocument> findAll(String partitionId) {
        return dataProcessingContextMongoDBRepository.findAllByPartitionId();
    }

    @Override
    public void save(DataProcessingContextDocument dataProcessingContextDocument) {
        dataProcessingContextMongoDBRepository.save(dataProcessingContextDocument);
    }
}
