package fr.insee.genesis.infrastructure.mapper;

import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.infrastructure.mappers.ExternalVariableMapperImpl;
import fr.insee.genesis.infrastructure.model.ExternalVariable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ExternalVariableMapperImplTest {
    static ExternalVariableMapperImpl externalVariableMapperStatic;
    static ExternalVariable externalVariableStatic;
    static VariableDto variableDtoStatic;


    //Given
    @BeforeAll
    static void init(){
        externalVariableMapperStatic = new ExternalVariableMapperImpl();

        externalVariableStatic = new ExternalVariable();
        externalVariableStatic.setIdVar("TESTIDVAR");
        externalVariableStatic.setValues(List.of(new String[]{"V1", "V2"}));

        variableDtoStatic = VariableDto.builder()
                .idVar("TESTIDVAR")
                .values(List.of(new String[]{"V1", "V2"}))
                .build();
    }

    //When + Then
    @Test
    @DisplayName("Should return null if null parameter")
    void shouldReturnNull(){
        Assertions.assertThat(externalVariableMapperStatic.entityToDto(null)).isNull();
        Assertions.assertThat(externalVariableMapperStatic.dtoToEntity(null)).isNull();
        Assertions.assertThat(externalVariableMapperStatic.listEntityToListDto(null)).isNull();
        Assertions.assertThat(externalVariableMapperStatic.listDtoToListEntity(null)).isNull();
    }

    @Test
    @DisplayName("Should convert external variable to Variable DTO")
    void shouldReturnVariableDtoFromExternalVariable(){
        VariableDto variableDto = externalVariableMapperStatic.entityToDto(externalVariableStatic);
        Assertions.assertThat(variableDto.getIdVar()).isEqualTo("TESTIDVAR");
        Assertions.assertThat(variableDto.getValues()).containsAll(List.of(new String[]{"V1", "V2"}));
    }

    @Test
    @DisplayName("Should convert external variable to Variable DTO")
    void shouldReturnExternalVariableFromVariableDto(){
        ExternalVariable externalVariable = externalVariableMapperStatic.dtoToEntity(variableDtoStatic);
        Assertions.assertThat(externalVariable.getIdVar()).isEqualTo("TESTIDVAR");
        Assertions.assertThat(externalVariable.getValues()).containsAll(List.of(new String[]{"V1", "V2"}));
    }

    @Test
    @DisplayName("Should convert list of external variable to list Variable DTO")
    void shouldReturnVariableDtoListFromExternalVariableList(){
        List<ExternalVariable> externalVariableList = new ArrayList<>();
        externalVariableList.add(externalVariableStatic);

        List<VariableDto> variableDtoList = externalVariableMapperStatic.listEntityToListDto(externalVariableList);

        Assertions.assertThat(variableDtoList).isNotNull().isNotEmpty();
        Assertions.assertThat(variableDtoList.get(0).getIdVar()).isEqualTo("TESTIDVAR");
        Assertions.assertThat(variableDtoList.get(0).getValues()).containsAll(List.of(new String[]{"V1", "V2"}));
    }

    @Test
    @DisplayName("Should convert list of external variable to list Variable DTO")
    void shouldReturnExternalVariableListFromVariableDtoList(){
        List<VariableDto> variableDtoList = new ArrayList<>();
        variableDtoList.add(variableDtoStatic);

        List<ExternalVariable> externalVariableList = externalVariableMapperStatic.listDtoToListEntity(variableDtoList);

        Assertions.assertThat(externalVariableList).isNotNull().isNotEmpty();
        Assertions.assertThat(externalVariableList.get(0).getIdVar()).isEqualTo("TESTIDVAR");
        Assertions.assertThat(externalVariableList.get(0).getValues()).containsAll(List.of(new String[]{"V1", "V2"}));
    }
}
