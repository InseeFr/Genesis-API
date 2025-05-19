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
        List<DataProcessingContextModel> existingModels =
                DataProcessingContextMapper.INSTANCE.listDocumentToListModel(
                        dataProcessingContextPersistancePort.findByPartitionId(partitionId)
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
                                               ServiceToCall serviceToCall,
                                               String frequency,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               TrustParameters trustParameters) throws GenesisException {
        List<DataProcessingContextModel> existingModels =
                DataProcessingContextMapper.INSTANCE.listDocumentToListModel(
                        dataProcessingContextPersistancePort.findByPartitionId(partitionId)
                );

        DataProcessingContextModel dataProcessingContextModel =
                new ContextUnicityService().deduplicateSchedules(partitionId, existingModels);
        if(existingModels.isEmpty()){
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
    public void updateLastExecutionName(String partitionId, LocalDateTime newDate) throws GenesisException {
        List<DataProcessingContextDocument> dataProcessingContextDocuments =
                dataProcessingContextPersistancePort.findByPartitionId(partitionId);
        if (dataProcessingContextDocuments.isEmpty()) {
            throw new GenesisException(404, "Context not found");
        }
        for(DataProcessingContextDocument dataProcessingContextDocument : dataProcessingContextDocuments){
            dataProcessingContextDocument.setLastExecution(newDate);
        }
        dataProcessingContextPersistancePort.deleteBypartitionId(partitionId);
        dataProcessingContextPersistancePort.saveAll(dataProcessingContextDocuments);
    }

    @Override
    public void deleteSchedules(String partitionId) throws GenesisException {
        List<DataProcessingContextModel> dataProcessingContextModels =
                DataProcessingContextMapper.INSTANCE.listDocumentToListModel(dataProcessingContextPersistancePort.findByPartitionId(partitionId));
        if(dataProcessingContextModels.isEmpty()){
            throw new GenesisException(404, "No context found for %s !".formatted(partitionId));
        }
        for(DataProcessingContextModel dataProcessingContextModel : dataProcessingContextModels){
            dataProcessingContextModel.setKraftwerkExecutionScheduleList(new ArrayList<>());
        }
        dataProcessingContextPersistancePort.deleteBypartitionId(partitionId);
        dataProcessingContextPersistancePort.saveAll(DataProcessingContextMapper.INSTANCE.listModelToListDocument(dataProcessingContextModels));
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
        List<DataProcessingContextModel> dataProcessingContextModels =
                DataProcessingContextMapper.INSTANCE.listDocumentToListModel(
                        dataProcessingContextPersistancePort.findByPartitionId(partitionId));
        if (dataProcessingContextModels.isEmpty()) {
            throw new GenesisException(404, NOT_FOUND_MESSAGE);
        }
        List<KraftwerkExecutionSchedule> deletedKraftwerkExecutionSchedules = new ArrayList<>();
        for (DataProcessingContextModel dataProcessingContextModel : dataProcessingContextModels) {
            deletedKraftwerkExecutionSchedules.addAll(dataProcessingContextPersistancePort.removeExpiredSchedules(dataProcessingContextModel));
        }
        return deletedKraftwerkExecutionSchedules;
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
            throw new GenesisException(500,"Multiple partitions for interrogation %s\n%s".formatted(
                    interrogationId,
                    Arrays.toString(partitionIds.toArray())
            ));
        }

        List<DataProcessingContextModel> dataProcessingContextModels = DataProcessingContextMapper.INSTANCE.listDocumentToListModel(
                dataProcessingContextPersistancePort.findByPartitionId(partitionIds.stream().toList().getFirst())
        );
        if(dataProcessingContextModels.isEmpty()){
            return null;
        }

        //withReview is false if at least one context for same partition is false
        boolean withReview = true;
        for(DataProcessingContextModel dataProcessingContextModel : dataProcessingContextModels){
            withReview = dataProcessingContextModel.isWithReview();
        }

        DataProcessingContextModel dataProcessingContextModelToReturn = dataProcessingContextModels.getFirst();
        dataProcessingContextModelToReturn.setWithReview(withReview);

        return dataProcessingContextModelToReturn;
    }

    @Override
    public DataProcessingContextModel getContextByPartitionId(String partitionId){
        List<DataProcessingContextModel> dataProcessingContextModels = DataProcessingContextMapper.INSTANCE.listDocumentToListModel(
                dataProcessingContextPersistancePort.findByPartitionId(partitionId)
        );

        if(dataProcessingContextModels.isEmpty()){
            return null;
        }

        return dataProcessingContextModels.getFirst();
    }
}
