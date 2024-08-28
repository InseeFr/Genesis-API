package fr.insee.genesis.infrastructure.mapper;

import fr.insee.genesis.domain.model.surveyunit.Variable;
import fr.insee.genesis.infrastructure.mappers.ExternalVariableMapperImpl;
import fr.insee.genesis.infrastructure.model.document.surveyunit.ExternalVariable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ExternalVariableMapperImplTest {
    static ExternalVariableMapperImpl externalVariableMapperStatic;
    static ExternalVariable externalVariableStatic;
    static Variable variableStatic;


    //Given
    @BeforeAll
    static void init(){
        externalVariableMapperStatic = new ExternalVariableMapperImpl();

        externalVariableStatic = new ExternalVariable();
        externalVariableStatic.setIdVar("TESTIDVAR");
        externalVariableStatic.setValues(List.of(new String[]{"V1", "V2"}));

        variableStatic = Variable.builder()
                .idVar("TESTIDVAR")
                .values(List.of(new String[]{"V1", "V2"}))
                .build();
    }

    //When + Then
    @Test
    @DisplayName("Should return null if null parameter")
    void shouldReturnNull(){
        Assertions.assertThat(externalVariableMapperStatic.entityToModel(null)).isNull();
        Assertions.assertThat(externalVariableMapperStatic.modelToEntity(null)).isNull();
        Assertions.assertThat(externalVariableMapperStatic.listEntityToListModel(null)).isNull();
        Assertions.assertThat(externalVariableMapperStatic.listModelToListEntity(null)).isNull();
    }

    @Test
    @DisplayName("Should convert external variable to Variable DTO")
    void shouldReturnVariableDtoFromExternalVariable(){
        Variable variable = externalVariableMapperStatic.entityToModel(externalVariableStatic);
        Assertions.assertThat(variable.getIdVar()).isEqualTo("TESTIDVAR");
        Assertions.assertThat(variable.getValues()).containsAll(List.of(new String[]{"V1", "V2"}));
    }

    @Test
    @DisplayName("Should convert external variable to Variable DTO")
    void shouldReturnExternalVariableFromVariableDto(){
        ExternalVariable externalVariable = externalVariableMapperStatic.modelToEntity(variableStatic);
        Assertions.assertThat(externalVariable.getIdVar()).isEqualTo("TESTIDVAR");
        Assertions.assertThat(externalVariable.getValues()).containsAll(List.of(new String[]{"V1", "V2"}));
    }

    @Test
    @DisplayName("Should convert list of external variable to list Variable DTO")
    void shouldReturnVariableDtoListFromExternalVariableList(){
        List<ExternalVariable> externalVariableList = new ArrayList<>();
        externalVariableList.add(externalVariableStatic);

        List<Variable> variableList = externalVariableMapperStatic.listEntityToListModel(externalVariableList);

        Assertions.assertThat(variableList).isNotNull().isNotEmpty();
        Assertions.assertThat(variableList.getFirst().getIdVar()).isEqualTo("TESTIDVAR");
        Assertions.assertThat(variableList.getFirst().getValues()).containsAll(List.of(new String[]{"V1", "V2"}));
    }

    @Test
    @DisplayName("Should convert list of external variable to list Variable DTO")
    void shouldReturnExternalVariableListFromVariableDtoList(){
        List<Variable> variableList = new ArrayList<>();
        variableList.add(variableStatic);

        List<ExternalVariable> externalVariableList = externalVariableMapperStatic.listModelToListEntity(variableList);

        Assertions.assertThat(externalVariableList).isNotNull().isNotEmpty();
        Assertions.assertThat(externalVariableList.getFirst().getIdVar()).isEqualTo("TESTIDVAR");
        Assertions.assertThat(externalVariableList.getFirst().getValues()).containsAll(List.of(new String[]{"V1", "V2"}));
    }
}
