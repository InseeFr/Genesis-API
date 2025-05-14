package fr.insee.genesis.domain.service.context;

import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.schedule.TrustParameters;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.mappers.DataProcessingContextMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataProcessingContextService implements DataProcessingContextApiPort {
    private final DataProcessingContextPersistancePort dataProcessingContextPersistancePort;

    @Autowired
    public DataProcessingContextService(DataProcessingContextPersistancePort dataProcessingContextPersistancePort) {
        this.dataProcessingContextPersistancePort = dataProcessingContextPersistancePort;
    }

    @Override
    public void saveContext(String partitionId, Boolean withReview) throws GenesisException {
        List<DataProcessingContextModel> existingModels =
                DataProcessingContextMapper.INSTANCE.listDocumentToListModel(
                        dataProcessingContextPersistancePort.findAll(partitionId)
                );

        DataProcessingContextModel dataProcessingContextModel;
        if(existingModels.isEmpty()){
            dataProcessingContextModel = DataProcessingContextModel.builder()
                    .partitionId(partitionId)
                    .kraftwerkExecutionScheduleList(new ArrayList<>())
                    .build();
        }else{
            dataProcessingContextModel = existingModels.getFirst();
        }
        dataProcessingContextModel.setWithReview(withReview);

        dataProcessingContextPersistancePort.save(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));
    }

    @Override
    public void saveKraftwerkExecutionSchedule(String partitionId,
                                               String frequency,
                                               ServiceToCall serviceToCall,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               TrustParameters trustParameters) throws GenesisException {
        List<DataProcessingContextModel> existingModels =
                DataProcessingContextMapper.INSTANCE.listDocumentToListModel(
                        dataProcessingContextPersistancePort.findAll(partitionId)
                );

        DataProcessingContextModel dataProcessingContextModel;
        if(existingModels.isEmpty()){
            dataProcessingContextModel = DataProcessingContextModel.builder()
                    .partitionId(partitionId)
                    .withReview(false)
                    .kraftwerkExecutionScheduleList(new ArrayList<>())
                    .build();
        }else{
            dataProcessingContextModel = existingModels.getFirst();
        }
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(frequency,
                        serviceToCall,
                        startDate,
                        endDate,
                        null
                )
        );

        dataProcessingContextPersistancePort.save(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));
    }
}
