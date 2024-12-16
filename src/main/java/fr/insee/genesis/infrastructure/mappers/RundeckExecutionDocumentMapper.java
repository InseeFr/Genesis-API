package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.rundeck.RundeckExecution;
import fr.insee.genesis.infrastructure.document.rundeck.RundeckExecutionDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RundeckExecutionDocumentMapper {

    RundeckExecutionDocumentMapper INSTANCE = Mappers.getMapper(RundeckExecutionDocumentMapper.class);

    RundeckExecution documentToModel(RundeckExecutionDocument rundeckExecutionDocument);

    RundeckExecutionDocument modelToDocument(RundeckExecution rundeckExecution);
}
