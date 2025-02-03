package fr.insee.genesis.infrastructure.mapper;

import fr.insee.genesis.domain.model.rundeck.RundeckExecution;
import fr.insee.genesis.infrastructure.document.rundeck.RundeckExecutionDocument;
import fr.insee.genesis.infrastructure.mappers.RundeckExecutionDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.RundeckExecutionDocumentMapperImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RundeckExecutionDocumentMapperImplTest {

    //Given
    static RundeckExecutionDocumentMapper rundeckExecutionDocumentMapperImplStatic;
    static RundeckExecutionDocument rundeckExecutionDocument;
    static RundeckExecution rundeckExecution;

    @BeforeAll
    static void init(){
        rundeckExecutionDocumentMapperImplStatic = new RundeckExecutionDocumentMapperImpl();

        rundeckExecutionDocument = new RundeckExecutionDocument();
        rundeckExecutionDocument.setIdExecution(1236589);

        rundeckExecution = new RundeckExecution();
        rundeckExecution.setIdExecution(1236589);

    }

    @Test
    @DisplayName("Should return null if null parameter")
    void shouldReturnNull(){
        Assertions.assertThat(rundeckExecutionDocumentMapperImplStatic.documentToModel(null)).isNull();
        Assertions.assertThat(rundeckExecutionDocumentMapperImplStatic.modelToDocument(null)).isNull();
    }

    @Test
    @DisplayName("Should convert document to DTO")
    void shouldReturnDocumentDtoFromDocument(){
        RundeckExecution rundeckModel = rundeckExecutionDocumentMapperImplStatic.documentToModel(rundeckExecutionDocument);

        Assertions.assertThat(rundeckModel.getIdExecution()).isEqualTo(1236589);
    }

    @Test
    @DisplayName("Should convert DTO to document")
    void shouldReturnDocumentFromDocumentDto(){
        RundeckExecutionDocument rundeckDocument = rundeckExecutionDocumentMapperImplStatic.modelToDocument(rundeckExecution);

        Assertions.assertThat(rundeckDocument.getIdExecution()).isEqualTo(1236589);

    }


}
