package fr.insee.genesis.domain.service.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.insee.genesis.Constants;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public void saveContextByCollectionInstrumentId(String collectionInstrumentId, Boolean withReview) throws GenesisException {
        DataProcessingContextModel dataProcessingContextModel = dataProcessingContextPersistancePort.findByCollectionInstrumentId(collectionInstrumentId);
        if(dataProcessingContextModel == null){
            //Create if not exist
            dataProcessingContextModel = DataProcessingContextModel.builder()
                    .collectionInstrumentId(collectionInstrumentId)
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
    public void saveKraftwerkExecutionScheduleByCollectionInstrumentId(String collectionInstrumentId, ServiceToCall serviceToCall, String frequency, LocalDateTime startDate, LocalDateTime endDate, TrustParameters trustParameters) throws GenesisException {
        DataProcessingContextModel dataProcessingContextModel = dataProcessingContextPersistancePort.findByCollectionInstrumentId(collectionInstrumentId);
        if(dataProcessingContextModel == null){
            //Create if not exist
            dataProcessingContextModel = DataProcessingContextModel.builder()
                    .collectionInstrumentId(collectionInstrumentId)
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

    @Deprecated(forRemoval = true)
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
    public void updateLastExecutionDateByCollectionInstrumentId(String collectionInstrumentId, LocalDateTime newDate) throws GenesisException {
        DataProcessingContextModel dataProcessingContextModel = dataProcessingContextPersistancePort.findByCollectionInstrumentId(collectionInstrumentId);
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
    public void deleteSchedulesByCollectionInstrumentId(String collectionInstrumentId) throws GenesisException {
        DataProcessingContextModel dataProcessingContextModel =
                        dataProcessingContextPersistancePort.findByCollectionInstrumentId(collectionInstrumentId);
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
    public void deleteExpiredSchedules(String logFolder) throws GenesisException {
        List<DataProcessingContextModel> dataProcessingContextModels =
                DataProcessingContextMapper.INSTANCE.listDocumentToListModel(dataProcessingContextPersistancePort.findAll());
        for(DataProcessingContextModel context : dataProcessingContextModels){
            try {
                List<KraftwerkExecutionSchedule> deletedKraftwerkExecutionSchedules = dataProcessingContextPersistancePort.removeExpiredSchedules(context);
                //Save in JSON log
                if(!deletedKraftwerkExecutionSchedules.isEmpty()) {
                    String scheduleName = context.getCollectionInstrumentId()==null ?
                            context.getPartitionId() : context.getCollectionInstrumentId();
                    Path jsonLogPath = Path.of(logFolder, Constants.SCHEDULE_ARCHIVE_FOLDER_NAME,
                            scheduleName + ".json");
                    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
                    objectMapper.registerModule(new JavaTimeModule());
                    String jsonToWrite = objectMapper.writeValueAsString(deletedKraftwerkExecutionSchedules);
                    if(Files.exists(jsonLogPath)){
                        //Remove last ] and append survey
                        StringBuilder content = new StringBuilder(Files.readString(jsonLogPath));
                        content.setCharAt(content.length()-1, ',');
                        content.append(jsonToWrite, 1, jsonToWrite.length()-1);
                        content.append(']');
                        Files.write(jsonLogPath, content.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                    }else {
                        Files.createDirectories(jsonLogPath.getParent());
                        Files.write(jsonLogPath, jsonToWrite.getBytes());
                    }
                }
            } catch (IOException e) {
                String name = context.getCollectionInstrumentId()!=null?context.getCollectionInstrumentId() :context.getPartitionId();
                throw new GenesisException(500,String.format("An error occured trying to delete expired schedules for %s",name));
            }
        }
    }

    @Override
    public long countSchedules() {
        return dataProcessingContextPersistancePort.count();
    }

    @Override
    public DataProcessingContextModel getContext(String interrogationId) throws GenesisException {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findByInterrogationId(interrogationId);
        if(surveyUnitModels.isEmpty()){
            throw new GenesisException(404, "No interrogation in database with id %s".formatted(interrogationId));
        }
        Set<String> collectionInstrumentIds = new HashSet<>();

        for (SurveyUnitModel su : surveyUnitModels){
            if (su.getCollectionInstrumentId() != null){
                collectionInstrumentIds.add(su.getCollectionInstrumentId());
            }
        }

        if(collectionInstrumentIds.size() > 1){
            throw new GenesisException(500,"Multiple collection instruments for interrogation %s".formatted(interrogationId));
        }

        if(collectionInstrumentIds.isEmpty()){
            throw new GenesisException(404,"No collection instrument found for interrogation %s".formatted(interrogationId));
        }

        return dataProcessingContextPersistancePort.findByCollectionInstrumentId(collectionInstrumentIds.stream().toList().getFirst());
    }

    @Override
    public DataProcessingContextModel getContextByCollectionInstrumentId(String collectionInstrumentId){
        return DataProcessingContextMapper.INSTANCE.documentToModel(
                dataProcessingContextPersistancePort.findByPartitionId(collectionInstrumentId)
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

    @Deprecated(forRemoval = true)
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

    @Override
    public boolean getReviewByCollectionInstrumentId(String collectionInstrumentId) throws GenesisException {
        DataProcessingContextModel dataProcessingContextModel =
                dataProcessingContextPersistancePort.findByCollectionInstrumentId(collectionInstrumentId);
        if(dataProcessingContextModel == null){
            throw new GenesisException(404, "Data processing context not found");
        }
        return dataProcessingContextModel.isWithReview();
    }
}
