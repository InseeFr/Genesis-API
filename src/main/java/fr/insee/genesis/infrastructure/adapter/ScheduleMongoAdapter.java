package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.schedule.ScheduleModel;
import fr.insee.genesis.domain.ports.spi.SchedulePersistencePort;
import fr.insee.genesis.infrastructure.mappers.ScheduleDocumentMapper;
import fr.insee.genesis.infrastructure.repository.ScheduleMongoDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleMongoAdapter implements SchedulePersistencePort {
    private final ScheduleMongoDBRepository scheduleMongoDBRepository;

    @Autowired
    public ScheduleMongoAdapter(ScheduleMongoDBRepository scheduleMongoDBRepository) {
        this.scheduleMongoDBRepository = scheduleMongoDBRepository;
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
}
