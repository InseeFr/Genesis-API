package fr.insee.genesis.domain.service.context;

import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.context.schedule.TrustParameters;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.mappers.DataProcessingContextMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class DataProcessingContextService implements DataProcessingContextApiPort {
    public static final String NOT_FOUND_MESSAGE = "Context not found";
    private final DataProcessingContextPersistancePort dataProcessingContextPersistancePort;
    private final SurveyUnitPersistencePort surveyUnitPersistencePort;

    @Autowired
    public DataProcessingContextService(DataProcessingContextPersistancePort dataProcessingContextPersistancePort,
                                        SurveyUnitPersistencePort surveyUnitPersistencePort) {
        this.dataProcessingContextPersistancePort = dataProcessingContextPersistancePort;
        this.surveyUnitPersistencePort = surveyUnitPersistencePort;
    }

    @Override
    public void saveContext(String partitionId, Boolean withReview) throws GenesisException {

        DataProcessingContextModel dataProcessingContextModel =
                DataProcessingContextMapper.INSTANCE.documentToModel(dataProcessingContextPersistancePort.findByPartitionId(partitionId));
        if(dataProcessingContextModel == null){
            //Create if not exist
            dataProcessingContextModel = DataProcessingContextModel.builder()
                    .partitionId(partitionId)
                    .kraftwerkExecutionScheduleList(new ArrayList<>())
                    .build();
        }
        dataProcessingContextModel.setWithReview(withReview);

        dataProcessingContextPersistancePort.save(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));
    }

    @Override
    public void saveKraftwerkExecutionSchedule(String partitionId,
                                               ServiceToCall serviceToCall,
                                               String frequency,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               TrustParameters trustParameters) throws GenesisException {
        DataProcessingContextModel dataProcessingContextModel =
                DataProcessingContextMapper.INSTANCE.documentToModel(dataProcessingContextPersistancePort.findByPartitionId(partitionId));
        if(dataProcessingContextModel == null){
            //Create if not exist
            dataProcessingContextModel = DataProcessingContextModel.builder()
                    .partitionId(partitionId)
                    .withReview(false)
                    .kraftwerkExecutionScheduleList(new ArrayList<>())
                    .build();
        }

        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(frequency,
                        serviceToCall,
                        startDate,
                        endDate,
                        trustParameters
                )
        );
        dataProcessingContextPersistancePort.save(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));
    }

    @Override
    public void updateLastExecutionDate(String partitionId, LocalDateTime newDate) throws GenesisException {
         DataProcessingContextModel dataProcessingContextModel =
                DataProcessingContextMapper.INSTANCE.documentToModel(
                        dataProcessingContextPersistancePort.findByPartitionId(partitionId)
                );
        if (dataProcessingContextModel == null) {
            throw new GenesisException(404, NOT_FOUND_MESSAGE);
        }
        dataProcessingContextModel.setLastExecution(newDate);
        dataProcessingContextPersistancePort.save(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));
    }

    @Override
    public void deleteSchedules(String partitionId) throws GenesisException {
        DataProcessingContextModel dataProcessingContextModel =
                DataProcessingContextMapper.INSTANCE.documentToModel(
                        dataProcessingContextPersistancePort.findByPartitionId(partitionId)
                );
        if (dataProcessingContextModel == null) {
            throw new GenesisException(404, NOT_FOUND_MESSAGE);
        }
        dataProcessingContextModel.setKraftwerkExecutionScheduleList(new ArrayList<>());
        dataProcessingContextPersistancePort.save(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));
    }

    @Override
    public List<ScheduleDto> getAllSchedules() {
        List<ScheduleDto> scheduleDtos = new ArrayList<>();

        List<DataProcessingContextModel> dataProcessingContextModels =
                DataProcessingContextMapper.INSTANCE.listDocumentToListModel(dataProcessingContextPersistancePort.findAll());

        dataProcessingContextModels.forEach(
                model -> scheduleDtos.add(model.toScheduleDto())
        );

        return scheduleDtos;
    }

    @Override
    public List<KraftwerkExecutionSchedule> deleteExpiredSchedules(String partitionId) throws GenesisException {
        DataProcessingContextModel dataProcessingContextModel =
                DataProcessingContextMapper.INSTANCE.documentToModel(
                        dataProcessingContextPersistancePort.findByPartitionId(partitionId)
                );
        if (dataProcessingContextModel == null) {
            throw new GenesisException(404, NOT_FOUND_MESSAGE);
        }
        return new ArrayList<>(
                dataProcessingContextPersistancePort.removeExpiredSchedules(dataProcessingContextModel)
        );
    }

    @Override
    public long countSchedules() {
        return dataProcessingContextPersistancePort.count();
    }

    @Override
    public DataProcessingContextModel getContext(String interrogationId) throws GenesisException {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findByInterrogationId(interrogationId);
        if(surveyUnitModels.isEmpty()){
            throw new GenesisException(404,"No interrogation in database with id %s".formatted(interrogationId));
        }
        Set<String> partitionIds = new HashSet<>();
        surveyUnitModels.forEach(
                surveyUnitModel -> partitionIds.add(surveyUnitModel.getCampaignId())
        );
        if(partitionIds.isEmpty()){
            return null;
        }
        if(partitionIds.size() > 1){
            throw new GenesisException(500,"Multiple partitions for interrogation %s %n%s".formatted(
                    interrogationId,
                    Arrays.toString(partitionIds.toArray())
            ));
        }

        return DataProcessingContextMapper.INSTANCE.documentToModel(
                dataProcessingContextPersistancePort.findByPartitionId(partitionIds.stream().toList().getFirst())
        );
    }

    @Override
    public DataProcessingContextModel getContextByPartitionId(String partitionId){
        return DataProcessingContextMapper.INSTANCE.documentToModel(
                dataProcessingContextPersistancePort.findByPartitionId(partitionId)
        );
    }

    @Override
    public List<String> getPartitionIds(boolean withReview){
        List<String> partitionIds = new ArrayList<>();
        for(DataProcessingContextModel dataProcessingContextModel
        : DataProcessingContextMapper.INSTANCE.listDocumentToListModel(
                dataProcessingContextPersistancePort.findAllByReview(withReview)
        )){
            partitionIds.add(dataProcessingContextModel.getPartitionId());
        }
        return partitionIds;
    }

    @Override
    public boolean getReviewByPartitionId(String partitionId) throws GenesisException {
        DataProcessingContextDocument dataProcessingContextDocument =
                dataProcessingContextPersistancePort.findByPartitionId(partitionId);
        if(dataProcessingContextDocument == null){
            throw new GenesisException(404, "Data processing context not found");
        }
        return DataProcessingContextMapper.INSTANCE.documentToModel(
                dataProcessingContextPersistancePort.findByPartitionId(partitionId)
        ).isWithReview();
    }
}
