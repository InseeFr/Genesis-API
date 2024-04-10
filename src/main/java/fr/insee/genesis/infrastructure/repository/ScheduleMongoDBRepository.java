package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleMongoDBRepository extends MongoRepository<StoredSurveySchedule, String>{
    List<StoredSurveySchedule> findAll();

    @Query(value = "{ 'surveyName' : ?0 }")
    List<StoredSurveySchedule> findBySurveyName(String surveyName);
}
