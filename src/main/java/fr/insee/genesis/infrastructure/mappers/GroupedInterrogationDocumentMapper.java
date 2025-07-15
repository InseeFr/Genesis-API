package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.surveyunit.GroupedInterrogation;
import fr.insee.genesis.infrastructure.document.surveyunit.GroupedInterrogationDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface GroupedInterrogationDocumentMapper {


    GroupedInterrogationDocumentMapper INSTANCE = Mappers.getMapper(GroupedInterrogationDocumentMapper.class);

    GroupedInterrogation documentToModel(GroupedInterrogationDocument interrogationIdDoc);

    GroupedInterrogationDocument modelToDocument(GroupedInterrogation interrogationId);

    List<GroupedInterrogation> listDocumentToListModel(List<GroupedInterrogationDocument> interrogationIdDocList);

    List<GroupedInterrogationDocument> listModelToListDocument(List<GroupedInterrogation> interrogationIdList);
}
