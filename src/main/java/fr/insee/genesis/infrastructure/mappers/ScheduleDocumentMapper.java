package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.schedule.ScheduleModel;
import fr.insee.genesis.infrastructure.model.document.schedule.ScheduleDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ScheduleDocumentMapper {
    ScheduleDocumentMapper INSTANCE = Mappers.getMapper(ScheduleDocumentMapper.class);

    ScheduleModel documentToModel(ScheduleDocument scheduleDocument);

    ScheduleDocument modelToDocument(ScheduleModel scheduleModel);

    List<ScheduleModel> listDocumentToListModel(List<ScheduleDocument> scheduleDocumentList);

    List<ScheduleDocument> listModelToListDocument(List<ScheduleModel> scheduleModelList);
}
