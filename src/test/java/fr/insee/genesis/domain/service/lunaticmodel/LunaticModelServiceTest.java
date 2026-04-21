package fr.insee.genesis.domain.service.lunaticmodel;

import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.domain.ports.spi.LunaticModelPersistancePort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.lunaticmodel.LunaticModelDocument;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LunaticModelServiceTest {

    @Mock
    LunaticModelPersistancePort lunaticModelPersistancePort;

    @InjectMocks
    LunaticModelService lunaticModelService;

    @Captor
    ArgumentCaptor<LunaticModelModel> lunaticModelModelArgumentCaptor;

    @Test
    void save_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        Map<String, Object> lunaticModel = new HashMap<>();

        //WHEN
        lunaticModelService.save(collectionInstrumentId, lunaticModel);

        //THEN
        verify(lunaticModelPersistancePort, times(1))
                .save(lunaticModelModelArgumentCaptor.capture());
        Assertions.assertThat(lunaticModelModelArgumentCaptor.getValue()).isNotNull();
        Assertions.assertThat(lunaticModelModelArgumentCaptor.getValue().collectionInstrumentId())
                .isEqualTo(collectionInstrumentId);
        Assertions.assertThat(lunaticModelModelArgumentCaptor.getValue().lunaticModel())
                .isEqualTo(lunaticModel);
        Assertions.assertThat(lunaticModelModelArgumentCaptor.getValue().recordDate())
                .isNotNull();
    }

    @Test
    @SneakyThrows
    void get_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        LocalDateTime recordDate = LocalDateTime.now();

        LunaticModelDocument expected = LunaticModelDocument.builder()
                .collectionInstrumentId(collectionInstrumentId)
                .recordDate(recordDate)
                .lunaticModel(new HashMap<>())
                .build();
        String lunaticModelKey = "testKey";
        int lunaticModelValue = 1;
        expected.lunaticModel().put(lunaticModelKey, lunaticModelValue);
        doReturn(List.of(expected)).when(lunaticModelPersistancePort).find(any());

        //WHEN
        LunaticModelModel actual = lunaticModelService.get(collectionInstrumentId);

        //THEN
        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.collectionInstrumentId()).isEqualTo(collectionInstrumentId);
        Assertions.assertThat(actual.recordDate()).isEqualTo(recordDate);
        Assertions.assertThat(actual.lunaticModel()).isNotNull().containsEntry(
                lunaticModelKey, lunaticModelValue
        );
    }

    @Test
    void get_not_found_test() {
        //GIVEN
        doReturn(new ArrayList<>()).when(lunaticModelPersistancePort).find(any());

        //WHEN + THEN
        try{
            lunaticModelService.get("test");
            Assertions.fail();
        }catch (GenesisException ge){
            Assertions.assertThat(ge.getStatus()).isEqualTo(404);
        }
    }
}