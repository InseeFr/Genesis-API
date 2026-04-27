package fr.insee.genesis.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.modelefiliere.ModeDto;
import fr.insee.modelefiliere.RawResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RawResponseInputRepository tests")
class RawResponseInputRepositoryTest {

    private static final String COLLECTION_NAME = "rawResponses";
    private static final String INTERROGATION_ID = "interrogation-123";
    private static final String COLLECTION_INSTRUMENT_ID = "instrument-456";

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RawResponseInputRepository repository;

    @Nested
    @DisplayName("saveAsRawJson() tests")
    class SaveAsRawJsonTests {

        @Test
        @DisplayName("Should serialize the DTO to JSON and save to the correct collection")
        void saveAsRawJson_shouldSerializeAndSave() throws JsonProcessingException {
            // GIVEN
            RawResponseDto dto = buildDto();
            when(objectMapper.writeValueAsString(dto)).thenReturn("{\"interrogationId\":\"interrogation-123\"}");

            // WHEN
            repository.saveAsRawJson(dto);

            // THEN
            verify(objectMapper).writeValueAsString(dto);
            verify(mongoTemplate).save(any(Map.class), eq(COLLECTION_NAME));
        }

        @Test
        @DisplayName("Should save a document containing interrogationId")
        void saveAsRawJson_documentShouldContainInterrogationId() throws JsonProcessingException {
            // GIVEN
            RawResponseDto dto = buildDto();
            when(objectMapper.writeValueAsString(dto)).thenReturn("{}");

            // WHEN
            repository.saveAsRawJson(dto);

            // THEN
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
            verify(mongoTemplate).save(captor.capture(), eq(COLLECTION_NAME));
            assertThat(captor.getValue()).containsEntry("interrogationId", INTERROGATION_ID);
        }

        @Test
        @DisplayName("Should save a document containing collectionInstrumentId")
        void saveAsRawJson_documentShouldContainCollectionInstrumentId() throws JsonProcessingException {
            // GIVEN
            RawResponseDto dto = buildDto();
            when(objectMapper.writeValueAsString(dto)).thenReturn("{}");

            // WHEN
            repository.saveAsRawJson(dto);

            // THEN
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
            verify(mongoTemplate).save(captor.capture(), eq(COLLECTION_NAME));
            assertThat(captor.getValue()).containsEntry("collectionInstrumentId", COLLECTION_INSTRUMENT_ID);
        }

        @Test
        @DisplayName("Should save a document containing mode")
        void saveAsRawJson_documentShouldContainMode() throws JsonProcessingException {
            // GIVEN
            RawResponseDto dto = buildDto();
            when(objectMapper.writeValueAsString(dto)).thenReturn("{}");

            // WHEN
            repository.saveAsRawJson(dto);

            // THEN
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
            verify(mongoTemplate).save(captor.capture(), eq(COLLECTION_NAME));
            assertThat(captor.getValue()).containsEntry("mode", dto.getMode());
        }

        @Test
        @DisplayName("Should save a document containing a non-null recordDate")
        void saveAsRawJson_documentShouldContainRecordDate() throws JsonProcessingException {
            // GIVEN
            RawResponseDto dto = buildDto();
            when(objectMapper.writeValueAsString(dto)).thenReturn("{}");

            // WHEN
            repository.saveAsRawJson(dto);

            // THEN
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
            verify(mongoTemplate).save(captor.capture(), eq(COLLECTION_NAME));
            assertThat(captor.getValue()).containsKey("recordDate");
            assertThat(captor.getValue().get("recordDate")).isNotNull();
        }

        @Test
        @DisplayName("Should save a document containing a non-null payload")
        void saveAsRawJson_documentShouldContainPayload() throws JsonProcessingException {
            // GIVEN
            RawResponseDto dto = buildDto();
            when(objectMapper.writeValueAsString(dto))
                    .thenReturn("{\"interrogationId\":\"interrogation-123\",\"collectionInstrumentId\":\"instrument-456\"}");

            // WHEN
            repository.saveAsRawJson(dto);

            // THEN
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
            verify(mongoTemplate).save(captor.capture(), eq(COLLECTION_NAME));
            assertThat(captor.getValue()).containsKey("payload");
            assertThat(captor.getValue().get("payload")).isNotNull();
        }

        @Test
        @DisplayName("Should save to the 'rawResponses' collection exactly")
        void saveAsRawJson_shouldSaveToCorrectCollection() throws JsonProcessingException {
            // GIVEN
            RawResponseDto dto = buildDto();
            when(objectMapper.writeValueAsString(dto)).thenReturn("{}");

            // WHEN
            repository.saveAsRawJson(dto);

            // THEN
            ArgumentCaptor<String> collectionCaptor = ArgumentCaptor.forClass(String.class);
            verify(mongoTemplate).save(any(), collectionCaptor.capture());
            assertThat(collectionCaptor.getValue()).isEqualTo(COLLECTION_NAME);
        }

        @Test
        @DisplayName("Should call mongoTemplate.save() exactly once")
        void saveAsRawJson_shouldCallSaveExactlyOnce() throws JsonProcessingException {
            // GIVEN
            RawResponseDto dto = buildDto();
            when(objectMapper.writeValueAsString(dto)).thenReturn("{}");

            // WHEN
            repository.saveAsRawJson(dto);

            // THEN
            verify(mongoTemplate, times(1)).save(any(), eq(COLLECTION_NAME));
            verifyNoMoreInteractions(mongoTemplate);
        }

        @Test
        @DisplayName("Should wrap JsonProcessingException in RuntimeException")
        void saveAsRawJson_jsonProcessingException_shouldThrowRuntimeException() throws JsonProcessingException {
            // GIVEN
            RawResponseDto dto = buildDto();
            when(objectMapper.writeValueAsString(dto))
                    .thenThrow(new JsonProcessingException("serialization error") {});

            // WHEN / THEN
            assertThatThrownBy(() -> repository.saveAsRawJson(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(JsonProcessingException.class)
                    .hasRootCauseMessage("serialization error");
        }

        @Test
        @DisplayName("Should not call mongoTemplate when serialization fails")
        void saveAsRawJson_jsonProcessingException_shouldNotCallMongoTemplate() throws JsonProcessingException {
            // GIVEN
            RawResponseDto dto = buildDto();
            when(objectMapper.writeValueAsString(dto))
                    .thenThrow(new JsonProcessingException("serialization error") {});

            // WHEN
            try {
                repository.saveAsRawJson(dto);
            } catch (RuntimeException _) {
                //Ignored
            }

            // THEN
            verifyNoInteractions(mongoTemplate);
        }
    }

    //UTILS
    private RawResponseDto buildDto() {
        RawResponseDto dto = new RawResponseDto();
        dto.setInterrogationId(INTERROGATION_ID);
        dto.setCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);
        dto.setMode(ModeDto.CAWI);
        return dto;
    }
}