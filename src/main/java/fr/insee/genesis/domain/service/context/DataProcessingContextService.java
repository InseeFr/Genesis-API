package fr.insee.genesis.domain.service.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.dto.KraftwerkExecutionScheduleInput;
import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.controller.dto.rawdata.ScheduleResponseDto;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionScheduleV2;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.mappers.DataProcessingContextMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
    public String createKraftwerkExecutionSchedule(KraftwerkExecutionScheduleInput scheduleInput) {

        DataProcessingContextModel dataProcessingContextModel =
                dataProcessingContextPersistancePort.findByCollectionInstrumentId(
                        scheduleInput.getCollectionInstrumentId()
                );

        if (dataProcessingContextModel == null) {
            dataProcessingContextModel = DataProcessingContextModel.builder()
                    .collectionInstrumentId(scheduleInput.getCollectionInstrumentId())
                    .withReview(false)
                    .kraftwerkExecutionScheduleV2List(new ArrayList<>())
                    .build();
        }

        if (dataProcessingContextModel.getKraftwerkExecutionScheduleV2List() == null) {
            dataProcessingContextModel.setKraftwerkExecutionScheduleV2List(new ArrayList<>());
        }

        String scheduleUuid = UUID.randomUUID().toString();

        Optional<KraftwerkExecutionScheduleV2> scheduleAlreadyExists = dataProcessingContextModel.getKraftwerkExecutionScheduleV2List()
                .stream()
                .filter(schedule ->
                    schedule.getMode()==scheduleInput.getMode() && schedule.getExportType() == scheduleInput.getExportType()
                )
                .findFirst();

        if (scheduleAlreadyExists.isPresent()){
            throw new DuplicateKeyException(String.format("Schedule already exists for collectionInstrumentId %s with mode %s and exportType %s. Use update endpoint with scheduleUuid %s",
                    scheduleInput.getCollectionInstrumentId(),
                    scheduleInput.getMode(),
                    scheduleInput.getExportType(),
                    scheduleAlreadyExists.get().getScheduleUuid()));
        }

        KraftwerkExecutionScheduleV2 newSchedule = new KraftwerkExecutionScheduleV2(
                scheduleUuid,
                scheduleInput.getFrequency(),
                scheduleInput.getExportType(),
                scheduleInput.getStartDate(),
                scheduleInput.getEndDate(),
                scheduleInput.getMode(),
                scheduleInput.getDestinationType(),
                scheduleInput.isAddStates(),
                scheduleInput.getDestinationFolder(),
                scheduleInput.isUseSymmetricEncryption(),
                scheduleInput.getTrustParameters(),
                scheduleInput.getBatchSize()
        );

        dataProcessingContextModel.getKraftwerkExecutionScheduleV2List().add(newSchedule);

        dataProcessingContextPersistancePort.save(
                DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel)
        );

        return scheduleUuid;
    }

    @Override
    public void updateKraftwerkExecutionSchedule(KraftwerkExecutionScheduleInput scheduleInput) throws GenesisException {

        DataProcessingContextModel dataProcessingContextModel =
                dataProcessingContextPersistancePort.findByCollectionInstrumentId(
                        scheduleInput.getCollectionInstrumentId()
                );

        if (dataProcessingContextModel == null) {
            throw new GenesisException(404, "Collection instrument not found");
        }

        if (dataProcessingContextModel.getKraftwerkExecutionScheduleV2List() == null
                || dataProcessingContextModel.getKraftwerkExecutionScheduleV2List().isEmpty()) {
            throw new GenesisException(404, "No V2 schedule found for this collection instrument");
        }

        KraftwerkExecutionScheduleV2 scheduleToUpdate = dataProcessingContextModel.getKraftwerkExecutionScheduleV2List()
                .stream()
                .filter(schedule -> scheduleInput.getScheduleUuid().equals(schedule.getScheduleUuid()))
                .findFirst()
                .orElseThrow(() -> new GenesisException(404, "V2 schedule not found"));

        Optional<KraftwerkExecutionScheduleV2> tripletAlreadyExists = dataProcessingContextModel.getKraftwerkExecutionScheduleV2List()
                .stream()
                .filter(schedule ->
                        schedule.getMode()==scheduleInput.getMode() && schedule.getExportType() == scheduleInput.getExportType()
                )
                .filter(schedule -> !schedule.getScheduleUuid().equals(scheduleInput.getScheduleUuid()))
                .findFirst();

        if (tripletAlreadyExists.isPresent()){
            throw new DuplicateKeyException(String.format("Schedule already exists for collectionInstrumentId %s with mode %s and exportType %s. Modify scheduleUuid %s instead",
                    scheduleInput.getCollectionInstrumentId(),
                    scheduleInput.getMode(),
                    scheduleInput.getExportType(),
                    tripletAlreadyExists.get().getScheduleUuid()));
        }

        scheduleToUpdate.setFrequency(scheduleInput.getFrequency());
        scheduleToUpdate.setExportType(scheduleInput.getExportType());
        scheduleToUpdate.setScheduleBeginDate(scheduleInput.getStartDate());
        scheduleToUpdate.setScheduleEndDate(scheduleInput.getEndDate());
        scheduleToUpdate.setMode(scheduleInput.getMode());
        scheduleToUpdate.setDestinationType(scheduleInput.getDestinationType());
        scheduleToUpdate.setAddStates(scheduleInput.isAddStates());
        scheduleToUpdate.setDestinationFolder(scheduleInput.getDestinationFolder());
        scheduleToUpdate.setUseSymmetricEncryption(scheduleInput.isUseSymmetricEncryption());
        scheduleToUpdate.setTrustParameters(scheduleInput.getTrustParameters());
        scheduleToUpdate.setBatchSize(scheduleInput.getBatchSize());

        dataProcessingContextPersistancePort.save(
                DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel)
        );
    }

    @Override
    public void deleteScheduleV2(String collectionInstrumentId, String scheduleUuid) throws GenesisException {
        DataProcessingContextModel dataProcessingContextModel =
                dataProcessingContextPersistancePort.findByCollectionInstrumentId(collectionInstrumentId);
        if (dataProcessingContextModel == null) {
            throw new GenesisException(404, NOT_FOUND_MESSAGE);
        }

        if (dataProcessingContextModel.getKraftwerkExecutionScheduleV2List() == null
                || dataProcessingContextModel.getKraftwerkExecutionScheduleV2List().isEmpty()) {
            throw new GenesisException(404, "No V2 schedule found for this collection instrument");
        }

        boolean removed = dataProcessingContextModel.getKraftwerkExecutionScheduleV2List()
                .removeIf(schedule -> scheduleUuid.equals(schedule.getScheduleUuid()));

        if (!removed) {
            throw new GenesisException(404, "V2 schedule not found");
        }

        dataProcessingContextPersistancePort.save(
                DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel)
        );
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
    public void deleteSchedulesV2ByCollectionInstrumentId(String collectionInstrumentId) throws GenesisException {
        DataProcessingContextModel dataProcessingContextModel =
                dataProcessingContextPersistancePort.findByCollectionInstrumentId(collectionInstrumentId);

        if (dataProcessingContextModel == null) {
            throw new GenesisException(404, NOT_FOUND_MESSAGE);
        }

        dataProcessingContextModel.setKraftwerkExecutionScheduleV2List(new ArrayList<>());

        dataProcessingContextPersistancePort.save(
                DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel)
        );
    }

    @Override
    public List<ScheduleResponseDto> getSchedulesV2ByCollectionInstrumentId(String collectionInstrumentId) {
        List<DataProcessingContextModel> dataProcessingContextModels =
                dataProcessingContextPersistancePort.findByCollectionInstrumentIds(List.of(collectionInstrumentId));

        return dataProcessingContextModels.stream()
                .flatMap(model -> model.toScheduleResponseDtos().stream())
                .toList();
    }

    @Override
    public List<ScheduleResponseDto> getAllSchedulesV2() {
        List<DataProcessingContextModel> dataProcessingContextModels =
                DataProcessingContextMapper.INSTANCE.listDocumentToListModel(
                        dataProcessingContextPersistancePort.findAll()
                );

        return dataProcessingContextModels.stream()
                .flatMap(model -> model.toScheduleResponseDtos().stream())
                .toList();
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
                    String scheduleName = context.getCollectionInstrumentId();
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
                String name = context.getCollectionInstrumentId();
                throw new GenesisException(500,String.format("An error occured trying to delete expired schedules for %s",name));
            }
        }
    }

    @Override
    public long countContexts() {
        return dataProcessingContextPersistancePort.count();
    }

    //TODO get context by collectionInstrumentId
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
        //TODO if multiple contexts, priorize withReview false
    }

    @Override
    public DataProcessingContextModel getContextByCollectionInstrumentId(String collectionInstrumentId){
        return dataProcessingContextPersistancePort.findByCollectionInstrumentId(collectionInstrumentId);
    }

    @Override
    public List<String> getCollectionInstrumentIds(boolean withReview) {
        List<String> collectionInstrumentIds = new ArrayList<>();
        for(DataProcessingContextModel dataProcessingContextModel
                : DataProcessingContextMapper.INSTANCE.listDocumentToListModel(
                dataProcessingContextPersistancePort.findAllByReview(withReview)
        )){
            if(dataProcessingContextModel.getCollectionInstrumentId() != null) {
                collectionInstrumentIds.add(dataProcessingContextModel.getCollectionInstrumentId());
            }
        }
        return collectionInstrumentIds;
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
