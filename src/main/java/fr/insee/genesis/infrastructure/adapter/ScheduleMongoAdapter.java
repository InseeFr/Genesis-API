package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.schedule.ScheduleModel;
import fr.insee.genesis.domain.ports.spi.SchedulePersistencePort;
import fr.insee.genesis.infrastructure.mappers.ScheduleDocumentMapper;
import fr.insee.genesis.infrastructure.repository.ScheduleMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Qualifier("scheduleMongoAdapter")
@Slf4j
public class ScheduleMongoAdapter implements SchedulePersistencePort {
    private final ScheduleMongoDBRepository scheduleMongoDBRepository;
    private final MongoTemplate mongoTemplate;


    @Autowired
    public ScheduleMongoAdapter(ScheduleMongoDBRepository scheduleMongoDBRepository, MongoTemplate mongoTemplate) {
        this.scheduleMongoDBRepository = scheduleMongoDBRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<ScheduleModel> getAll() {
        return ScheduleDocumentMapper.INSTANCE.listDocumentToListModel(scheduleMongoDBRepository.findAll());
    }

    @Override
    public void saveAll(List<ScheduleModel> scheduleModels) {
        scheduleMongoDBRepository.saveAll(ScheduleDocumentMapper.INSTANCE.listModelToListDocument(scheduleModels));
    }

    @Override
    public List<ScheduleModel> findBySurveyName(String surveyName) {
        return ScheduleDocumentMapper.INSTANCE.listDocumentToListModel(scheduleMongoDBRepository.findBySurveyName(surveyName));
    }

    @Override
    public void deleteBySurveyName(String surveyName) {
        scheduleMongoDBRepository.deleteBySurveyName(surveyName);
    }

    @Override
    public long countSchedules() {
        return scheduleMongoDBRepository.count();
    }

    @Override
    public List<KraftwerkExecutionSchedule> removeExpiredSchedules(ScheduleModel scheduleModel) {
        List<KraftwerkExecutionSchedule> deletedKraftwerkExecutionSchedules = new ArrayList<>();
        for (KraftwerkExecutionSchedule kraftwerkExecutionScheduleToRemove :
                scheduleModel.getKraftwerkExecutionScheduleList().stream().filter(
                        kraftwerkExecutionSchedule -> kraftwerkExecutionSchedule.getScheduleEndDate().isBefore(LocalDateTime.now())
                ).toList()) {
            deletedKraftwerkExecutionSchedules.add(kraftwerkExecutionScheduleToRemove);
            Query query =
                    Query.query(Criteria.where("scheduleEndDate").is(kraftwerkExecutionScheduleToRemove.getScheduleEndDate()));
            mongoTemplate.updateMulti(Query.query(Criteria.where("surveyName").is(scheduleModel.getSurveyName())), new Update().pull(
                            "kraftwerkExecutionScheduleList", query),
                    Constants.MONGODB_SCHEDULE_COLLECTION_NAME);
            log.info("Removed kraftwerk execution schedule on {} because it is expired since {}", scheduleModel.getSurveyName(),
                    kraftwerkExecutionScheduleToRemove.getScheduleEndDate());
        }
        return deletedKraftwerkExecutionSchedules;
    }
}
