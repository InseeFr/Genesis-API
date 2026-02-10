package fr.insee.genesis.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.domain.ports.api.LunaticModelApiPort;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LunaticModelControllerTest {

    @Mock
    private LunaticModelApiPort lunaticModelApiPort;
    @InjectMocks
    private LunaticModelController lunaticModelController;

    @Test
    void saveRawResponsesFromJsonBody() {
        //GIVEN
        String questionnaireId = "test";
        Map<String, Object> dataJson = new HashMap<>();

        //WHEN
        lunaticModelController.saveRawResponsesFromJsonBody(questionnaireId, dataJson);

        //THEN
        verify(lunaticModelApiPort, times(1)).save(
                questionnaireId.toUpperCase(),
                dataJson
        );
    }

    @Test
    @SneakyThrows
    void getLunaticModelFromQuestionnaireId() {
        //GIVEN
        String questionnaireId = "test";
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        Map<String, Object> lunaticModel = new HashMap<>();
        String expected = objectMapper.writeValueAsString(lunaticModel);
        doReturn(LunaticModelModel.builder()
                .collectionInstrumentId(questionnaireId)
                .lunaticModel(lunaticModel)
                .recordDate(LocalDateTime.now()).build()
        ).when(lunaticModelApiPort).get(any());

        //WHEN
        ResponseEntity<String> response = lunaticModelController.getLunaticModelFromQuestionnaireId(questionnaireId);

        //THEN
        verify(lunaticModelApiPort, times(1)).get(
                questionnaireId
        );
        Assertions.assertThat(response.getBody()).isEqualTo(expected);
    }
}